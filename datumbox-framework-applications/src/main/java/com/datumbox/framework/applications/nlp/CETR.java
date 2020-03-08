/**
 * Copyright (C) 2013-2020 Vasilis Vryniotis <bbriniotis@datumbox.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datumbox.framework.applications.nlp;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.dataobjects.AssociativeArray;
import com.datumbox.framework.core.common.dataobjects.Dataframe;
import com.datumbox.framework.common.dataobjects.FlatDataCollection;
import com.datumbox.framework.core.common.dataobjects.Record;
import com.datumbox.framework.core.common.interfaces.Parameterizable;
import com.datumbox.framework.core.common.utilities.MapMethods;
import com.datumbox.framework.core.common.utilities.PHPMethods;
import com.datumbox.framework.core.common.text.StringCleaner;
import com.datumbox.framework.core.machinelearning.MLBuilder;
import com.datumbox.framework.core.machinelearning.clustering.Kmeans;
import com.datumbox.framework.core.statistics.descriptivestatistics.Descriptives;
import com.datumbox.framework.core.common.text.parsers.HTMLParser;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The CETR class (Content Extraction with Tag Ratios) enables you to extract the
 * content text from HTML pages. It uses the tag ratios of each line and performs
 * clustering to separate the real text of the page from menus, footers and headers.
 * 
 * References: 
 *  http://www.cs.uiuc.edu/~hanj/pdf/www10_tweninger.pdf 
 *  http://web.engr.illinois.edu/~weninge1/cetr/
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class CETR {
    private static final Pattern NUMBER_OF_TAGS_PATTERN = Pattern.compile("<[^>]+?>", Pattern.DOTALL);
    
    /**
     * The object with the Parameters of the Algorithm.
     */
    public static class Parameters implements Parameterizable {
        private static final long serialVersionUID = 1L;
        
        private int numberOfClusters = 2;
        private int alphaWindowSizeFor2DModel = 3; //0 turns off the 2d Model. Suggested value from paper: 3
        private int smoothingAverageRadius = 2; //used by smoothing average method if selected
        
        /**
         * Getter for the number of clusters that will be used by the clustering
         * algorithm
         * 
         * @return 
         */
        public int getNumberOfClusters() {
            return numberOfClusters;
        }
        
        /**
         * Setter for the number of clusters that will be used by the clustering
         * algorithm
         * 
         * @param numberOfClusters 
         */
        public void setNumberOfClusters(int numberOfClusters) {
            this.numberOfClusters = numberOfClusters;
        }
        
        /**
         * Getter for the alpha parameter which is the window size used in the
         * moving average of the 2D model.
         * 
         * @return 
         */
        public int getAlphaWindowSizeFor2DModel() {
            return alphaWindowSizeFor2DModel;
        }
        
        /**
         * Setter for the alpha parameter which is the window size used in the
         * moving average of the 2D model.
         * 
         * @param alphaWindowSizeFor2DModel 
         */
        public void setAlphaWindowSizeFor2DModel(int alphaWindowSizeFor2DModel) {
            this.alphaWindowSizeFor2DModel = alphaWindowSizeFor2DModel;
        }
        
        /**
         * Getter for the smoothing average radius.
         * 
         * @return 
         */
        public int getSmoothingAverageRadius() {
            return smoothingAverageRadius;
        }
        
        /**
         * Setter for the smoothing average radius.
         * 
         * @param smoothingAverageRadius 
         */
        public void setSmoothingAverageRadius(int smoothingAverageRadius) {
            this.smoothingAverageRadius = smoothingAverageRadius;
        }
    }

    private final Configuration configuration;
    
    /**
     * Constructor for the CETR class.
     *
     * @param configuration
     */
    public CETR(Configuration configuration) {
        this.configuration = configuration;
    }
    
    /**
     * Extracts the main content for an HTML page.
     * 
     * @param html
     * @param parameters
     * @return 
     */
    public String extract(String html, CETR.Parameters parameters) {
        html = clearText(html); //preprocess the Document by removing irrelevant HTML tags and empty lines and break the document to its lines
        List<String> rows = extractRows(html); //
        
        List<Integer> selectedRowIds = selectRows(rows, parameters);
        
        StringBuilder sb = new StringBuilder(html.length());
        for(Integer rowId : selectedRowIds) {
            String row = rows.get(rowId);
            
            //extract the clear text from the selected row
            row = StringCleaner.removeExtraSpaces(HTMLParser.extractText(row));
            if(row.isEmpty()) {
                continue;
            }
            sb.append(row).append(" ");
        }
        
        return sb.toString().trim();
    }
    
    
    private List<Integer> selectRows(List<String> rows, Parameters parameters) {
        List<Double> TTRlist = calculateTTRlist(rows);
        gaussianSmoothing(TTRlist); //perform smoothing
        
        boolean use2Dmodel = (parameters.getAlphaWindowSizeFor2DModel()>0);
        
        Dataframe dataset = new Dataframe(configuration);
        if(use2Dmodel) {
            List<Double> G = computeDerivatives(TTRlist, parameters.getAlphaWindowSizeFor2DModel());
            gaussianSmoothing(G);
            
            //build dataset for Cluster Analysis by including the information from G
            int n = TTRlist.size();
            for(int i=0;i<n;++i) {
                AssociativeArray xData = new AssociativeArray();
                xData.put(0, TTRlist.get(i));
                xData.put(1, G.get(i));
                dataset.add(new Record(xData, null));
            }
            
            //G = null;
        }
        else {
            //build dataset for Cluster Analysis by using only TTRlist info
            int n = TTRlist.size();
            for(int i=0;i<n;++i) {
                AssociativeArray xData = new AssociativeArray();
                xData.put(0, TTRlist.get(i));
                dataset.add(new Record(xData, null));
            }
        }
        
        
        //perform clustering
        performClustering(dataset, parameters.getNumberOfClusters());
        
        Map<Object, Double> avgTTRscorePerCluster = new HashMap<>();
        Map<Object, Integer> clusterCounts = new HashMap<>();
        for(Record r : dataset) {
            Integer clusterId = (Integer)r.getYPredicted();
            Double ttr = r.getX().getDouble(0); //the first value is always set the TTR as you can see above
            
            Double previousValue = avgTTRscorePerCluster.getOrDefault(clusterId, 0.0);
            Integer counter = clusterCounts.getOrDefault(clusterId, 0);

            avgTTRscorePerCluster.put(clusterId, previousValue+ttr);
            clusterCounts.put(clusterId, counter+1);
        }
        
        //estimate the average TTR for each cluster
        for(Map.Entry<Object, Double> entry : avgTTRscorePerCluster.entrySet()) {
            Integer clusterId = (Integer)entry.getKey();
            double avgTTR = entry.getValue()/clusterCounts.get(clusterId);
            avgTTRscorePerCluster.put(clusterId, avgTTR);
        }
        
        //fetch the cluster with the smallest average.
        Map.Entry<Object, Double> entry = MapMethods.selectMinKeyValue(avgTTRscorePerCluster);
        
        //this cluster is considered the non-content cluster
        Integer nonContentClusterId = (Integer)entry.getKey();
        
        List<Integer> selectedRows = new ArrayList<>();
        for(Map.Entry<Integer, Record> e : dataset.entries()) {
            Integer rId = e.getKey();
            Record r = e.getValue();
            Integer clusterId = (Integer)r.getYPredicted();
            //if the point is not classified as non-content add it in the selected list
            if(!Objects.equals(clusterId, nonContentClusterId)) { 
                selectedRows.add(rId);
            }
        }
        
        dataset.close();
        
        return selectedRows;
    }

    private void performClustering(Dataframe dataset, int numberOfClusters) {
        Kmeans.TrainingParameters param = new Kmeans.TrainingParameters();
        param.setK(numberOfClusters);
        param.setMaxIterations(200);
        param.setInitializationMethod(Kmeans.TrainingParameters.Initialization.SET_FIRST_K); //using the first k
        param.setDistanceMethod(Kmeans.TrainingParameters.Distance.EUCLIDIAN);
        param.setWeighted(false);
        param.setCategoricalGamaMultiplier(1.0);
        //param.setSubsetFurthestFirstcValue(2.0);

        Kmeans instance = MLBuilder.create(param, configuration);
        
        instance.fit(dataset);
        instance.predict(dataset);
        instance.close();
    }
    
    private List<Double> calculateTTRlist(List<String> rows) {
        List<Double> TTRlist = new ArrayList<>();
        
        for(String row : rows) {
            int x = countContentChars(row); //calculate the total number of text in the row
            int y = countNumberOfTags(row); //count the number of tags in row
            
            if(y==0) {
                y=1;
            }
            
            TTRlist.add(x/(double)y); //divide to estimate the Text Tag Ratio score for this row
        }
        
        return TTRlist;
    }
    
    /*
    private List<Double> movingAverageSmoothing(List<Double> list, int smoothingAverageRadius) {
        int n = list.size();
        List<Double> smoothedList = new ArrayList<>(n);
        
        for(int idRow =0;idRow<n;++idRow) {
            //apply a window based on 2*radius+1
            int from = Math.max(idRow-smoothingAverageRadius, 0);
            int to = Math.min(idRow+smoothingAverageRadius, n);
            
            double sum = 0.0;
            for(int i = from; i<to ; ++i) {
                sum+=list.get(i);
            }
            
            smoothedList.add(sum/(to-from)); //simple moving average
        }
        
        return smoothedList;
    }
    */
    
    private List<Double> gaussianSmoothing(List<Double> list) {
        
        int n = list.size();
        
        double std = Descriptives.std(new FlatDataCollection((List)list), false);
        double variance = std*std;
        
        int sygma = (int)Math.min(Math.ceil(std), (n-1.0)/2.0);
        
        
        List<Double> gaussianKernel = new ArrayList<>(2*sygma+1);
        double normalizer = 0.0;
        for(int i =0;i<=2*sygma;++i) {
            double value = 0.0;
            for(int j=-sygma;j<=sygma;++j) {
                value += Math.exp((-j*j)/(2.0*variance));
            }
            
            gaussianKernel.add(value);
            
            normalizer += value;//sum of all kernel values
        }
        
        //normalize the kernel
        for(int i =0;i<=2*sygma;++i) {
            gaussianKernel.set(i, gaussianKernel.get(i)/normalizer);
        }
        
        
        List<Double> smoothedList = new ArrayList<>(n); //calculate Gaussian Kernel
        for(int i=0;i<n;++i) {
            double smoothedValue = 0.0;
            for(int j=-sygma;j<=sygma;++j) {
                int index = i-j;
                if(index>=0 && index<n) {
                    smoothedValue+=gaussianKernel.get(j+sygma)*list.get(index);//apply the kernel to the original values
                }
            }
            smoothedList.add(smoothedValue);
        }
        
        
        return smoothedList;
    }
    
    private List<Double> computeDerivatives(List<Double> list, int alphaWindowSizeFor2DModel) {
        //calculating the absolute smoothed derivatives of the smoothed TR histogram
        
        int n = list.size();
        List<Double> G = new ArrayList<>(n);
        
        for(int i=0;i<n;++i) {
            double sum = 0.0;
            
            int counter = 0;
            for(int j=0;j<alphaWindowSizeFor2DModel;++j) {
                int index = i+j;
                if(index>=0 && index<n) {
                    sum+=list.get(index);
                    ++counter;
                }
            }
            
            if(counter==0) {
                counter=1;
            }
            
            double avgInWindow = sum/counter;
            
            G.add(Math.abs(avgInWindow - list.get(i)));//absolute derivative. We subtract from the mean of window the original value and we take the absolute value
        }
        
        return G;
    }

    
    private int countNumberOfTags(String text) {
        Matcher m = NUMBER_OF_TAGS_PATTERN.matcher(text);
        
        int count = 0;
        while(m.find()) {
            ++count;
        }
        
        return count;
    }
    
    private int countContentChars(String text) {
        return StringCleaner.removeExtraSpaces(HTMLParser.extractText(text)).length();
    }
    
    private List<String> extractRows(String text) {
        return Arrays.asList(text.split("\n"));
    }
    
    private String clearText(String text) {
        text = HTMLParser.removeNonTextTagsAndAttributes(text); //remove all the irrelevant HTML Tags that are not related to the text (such as forms, scripts etc)
        if(PHPMethods.substr_count(text, '\n')<=1) { //if the document is in a single line (no spaces), then break it in order for this algorithm to work
            text = text.replace(">", ">\n");
        }
        
        text = text.replaceAll("[\\n\\r]+", "\n").replaceAll("(?m)^[ \t]*\r?\n", "").trim(); //remove the empty lines
        
        return text;
    }
}
