/* 
 * Copyright (C) 2014 Vasilis Vryniotis <bbriniotis at datumbox.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.datumbox.applications.nlp;

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.FlatDataCollection;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.objecttypes.Parameterizable;
import com.datumbox.common.persistentstorage.factories.InMemoryStructureFactory;
import com.datumbox.common.utilities.MapFunctions;
import com.datumbox.common.utilities.PHPfunctions;
import com.datumbox.common.utilities.RandomValue;
import com.datumbox.configuration.MemoryConfiguration;
import com.datumbox.framework.machinelearning.clustering.Kmeans;
import com.datumbox.framework.statistics.descriptivestatistics.Descriptives;
import com.datumbox.framework.utilities.text.cleaners.HTMLCleaner;
import com.datumbox.framework.utilities.text.cleaners.StringCleaner;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class CETR {
    /**
     * References: 
     *  http://www.cs.uiuc.edu/~hanj/pdf/www10_tweninger.pdf 
     *  http://web.engr.illinois.edu/~weninge1/cetr/
     */
    private final static Pattern NUMBER_OF_TAGS_PATTERN = Pattern.compile("<[^>]+?>", Pattern.DOTALL);

    public static class Parameters implements Parameterizable {
        private int numberOfClusters = 2;
        private int alphaWindowSizeFor2DModel = 3; //0 turns off the 2d Model. Suggested value from paper: 3
        private int smoothingAverageRadius = 2; //used by smoothing average method if selected

        public int getNumberOfClusters() {
            return numberOfClusters;
        }

        public void setNumberOfClusters(int numberOfClusters) {
            this.numberOfClusters = numberOfClusters;
        }

        public int getAlphaWindowSizeFor2DModel() {
            return alphaWindowSizeFor2DModel;
        }

        public void setAlphaWindowSizeFor2DModel(int alphaWindowSizeFor2DModel) {
            this.alphaWindowSizeFor2DModel = alphaWindowSizeFor2DModel;
        }

        public int getSmoothingAverageRadius() {
            return smoothingAverageRadius;
        }

        public void setSmoothingAverageRadius(int smoothingAverageRadius) {
            this.smoothingAverageRadius = smoothingAverageRadius;
        }
    }
    
    public String extract(String text, CETR.Parameters parameters) {
        text = clearText(text); //preprocess the Document by removing irrelevant HTML tags and empty lines
        List<String> rows = extractRows(text); //break the document to its lines
        
        List<Integer> selectedRowIds = selectRows(rows, parameters);
        
        StringBuilder sb = new StringBuilder();
        for(Integer rowId : selectedRowIds) {
            String row = rows.get(rowId);
            
            //extract the clear text from the selected row
            row = StringCleaner.removeExtraSpaces(HTMLCleaner.extractText(row));
            if(row.isEmpty()) {
                continue;
            }
            sb.append(row).append(" ");
        }
        
        return sb.toString().trim();
    }
    
    
    private List<Integer> selectRows(List<String> rows, Parameters parameters) {
        List<Double> TTRlist = calculateTTRlist(rows, parameters);
        gaussianSmoothing(TTRlist); //perform smoothing
        
        boolean use2Dmodel = (parameters.getAlphaWindowSizeFor2DModel()>0);
        
        Dataset dataset = new Dataset();
        if(use2Dmodel) {
            List<Double> G = computeDerivatives(TTRlist, parameters.getAlphaWindowSizeFor2DModel());
            gaussianSmoothing(G);
            
            //build dataset for Cluster Analysis by including the information from G
            int n = TTRlist.size();
            for(int i=0;i<n;++i) {
                dataset.add(Record.newDataVector(new Double[] {TTRlist.get(i), G.get(i)}, null));
            }
            
            G = null;
        }
        else {
            //build dataset for Cluster Analysis by using only TTRlist info
            int n = TTRlist.size();
            for(int i=0;i<n;++i) {
                dataset.add(Record.newDataVector(new Double[] {TTRlist.get(i)}, null));
            }
        }
        
        
        //perform clustering
        performClustering(dataset, parameters.getNumberOfClusters());
        
        Map<Object, Double> avgTTRscorePerCluster = new HashMap<>();
        Map<Object, Integer> clusterCounts = new HashMap<>();
        for(Record r : dataset) {
            Integer clusterId = (Integer)r.getYPredicted();
            Double ttr = r.getX().getDouble(0); //the first value is always set the TTR as you can see above
            
            Double previousValue = avgTTRscorePerCluster.get(clusterId);
            Integer counter = clusterCounts.get(clusterId);
            if(previousValue==null) {
                previousValue=0.0;
                counter = 0;
            }
            
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
        Map.Entry<Object, Double> entry = MapFunctions.selectMinKeyValue(avgTTRscorePerCluster);
        
        //this cluster is considered the non-content cluster
        Integer nonContentClusterId = (Integer)entry.getKey();
        
        List<Integer> selectedRows = new ArrayList<>();
        for(Record r : dataset) {
            Integer clusterId = (Integer)r.getYPredicted();
            //if the point is not classified as non-content add it in the selected list
            if(!Objects.equals(clusterId, nonContentClusterId)) { 
                selectedRows.add(r.getId());
            }
        }
        
        return selectedRows;
    }

    private void performClustering(Dataset dataset, int numberOfClusters) {
        String dbName = new BigInteger(130, RandomValue.randomGenerator).toString(32);
        Kmeans instance = new Kmeans(dbName);
        
        Kmeans.TrainingParameters param = instance.getEmptyTrainingParametersObject();
        param.setK(numberOfClusters);
        param.setMaxIterations(200);
        param.setInitMethod(Kmeans.TrainingParameters.Initialization.SET_FIRST_K); //using the first k
        param.setDistanceMethod(Kmeans.TrainingParameters.Distance.EUCLIDIAN);
        param.setWeighted(false);
        param.setCategoricalGamaMultiplier(1.0);
        //param.setSubsetFurthestFirstcValue(2.0);
        
        MemoryConfiguration memoryConfiguration = new MemoryConfiguration(); //EXPAND: find way to ensure that this will performed only in memory despite the global memory configuration
        memoryConfiguration.setMapType(InMemoryStructureFactory.MapType.HASH_MAP); //enforce inmemory training
        
        instance.setTemporary(true); //enforce temporary/not saved training
        instance.initializeTrainingConfiguration(memoryConfiguration, param);
        instance.train(dataset, dataset);
        
        //Map<Integer, BaseMLclusterer.Cluster> clusters = instance.getClusters();
        
        instance.erase(true); //erase immediately the result
    }
    
    private List<Double> calculateTTRlist(List<String> rows, Parameters parameters) {
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
        
        int sygma = (int)Math.min(Math.ceil(std), (n-1)/2);
        
        
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
        return StringCleaner.removeExtraSpaces(HTMLCleaner.extractText(text)).length();
    }
    
    private List<String> extractRows(String text) {
        return Arrays.asList(text.split("\n"));
    }
    
    private String clearText(String text) {
        text = HTMLCleaner.removeNonTextTagsAndAttributes(text); //remove all the irrelevant HTML Tags that are not related to the text (such as forms, scripts etc)
        if(PHPfunctions.substr_count(text, "\n")<=1) { //if the document is in a single line (no spaces), then break it in order for this algorithm to work
            text = text.replace(">", ">\n");
        }
        
        text = text.replaceAll("[\\n\\r]+", "\n").replaceAll("(?m)^[ \t]*\r?\n", "").trim(); //remove the empty lines
        
        return text;
    }
}
