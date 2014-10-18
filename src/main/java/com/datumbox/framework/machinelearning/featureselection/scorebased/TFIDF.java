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
package com.datumbox.framework.machinelearning.featureselection.scorebased;

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.factories.BigDataStructureFactory;
import com.datumbox.common.persistentstorage.interfaces.BigDataStructureMarker;
import com.datumbox.configuration.MemoryConfiguration;
import com.datumbox.configuration.StorageConfiguration;
import com.datumbox.framework.machinelearning.common.bases.featureselection.ScoreBasedFeatureSelection;
import java.util.Iterator;
import java.util.Map;
import org.mongodb.morphia.annotations.Transient;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class TFIDF extends ScoreBasedFeatureSelection<TFIDF.ModelParameters, TFIDF.TrainingParameters> {
    /*
        References: 
            http://en.wikipedia.org/wiki/Tf%E2%80%93idf
            https://gist.github.com/AloneRoad/1605037
            http://www.tfidf.com/
    */
    public static final String SHORT_METHOD_NAME = "TFIDF";

    public static class TrainingParameters extends ScoreBasedFeatureSelection.TrainingParameters {
        private boolean binarized = false;
        private Integer maxFeatures=null;

        public boolean isBinarized() {
            return binarized;
        }

        public void setBinarized(boolean binarized) {
            this.binarized = binarized;
        }

        public Integer getMaxFeatures() {
            return maxFeatures;
        }

        public void setMaxFeatures(Integer maxFeatures) {
            this.maxFeatures = maxFeatures;
        }
        
    }
    
    public static class ModelParameters extends ScoreBasedFeatureSelection.ModelParameters {
        private int N;
        
        @BigDataStructureMarker
        @Transient
        private Map<Object, Double> maxTFIDFfeatureScores; //map which stores the max tfidf of the features

        
        @Override
        public void bigDataStructureInitializer(BigDataStructureFactory bdsf, MemoryConfiguration memoryConfiguration) {
            super.bigDataStructureInitializer(bdsf, memoryConfiguration);
            
            BigDataStructureFactory.MapType mapType = memoryConfiguration.getMapType();
            int LRUsize = memoryConfiguration.getLRUsize();
            
            maxTFIDFfeatureScores = bdsf.getMap("maxTFIDFfeatureScores", mapType, LRUsize);
        }
        
        public int getN() {
            return N;
        }

        public void setN(int N) {
            this.N = N;
        }

        public Map<Object, Double> getMaxTFIDFfeatureScores() {
            return maxTFIDFfeatureScores;
        }

        public void setMaxTFIDFfeatureScores(Map<Object, Double> maxTFIDFfeatureScores) {
            this.maxTFIDFfeatureScores = maxTFIDFfeatureScores;
        }

    }
    
    
    public TFIDF(String dbName) {
        super(dbName, TFIDF.ModelParameters.class, TFIDF.TrainingParameters.class);
    }
    
    @Override
    public String shortMethodName() {
        return SHORT_METHOD_NAME;
    }
    
    @Override
    protected void estimateModelParameters(Dataset trainingData) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        String tmpPrefix=StorageConfiguration.getTmpPrefix();
        
        boolean binarized = trainingParameters.isBinarized();
        
        
        int n = trainingData.size();
        modelParameters.setN(n);
        
        BigDataStructureFactory bdsf = knowledgeBase.getBdsf();
        Map<Object, Double> idfMap = bdsf.getMap(tmpPrefix+"idf", knowledgeBase.getMemoryConfiguration().getMapType(), knowledgeBase.getMemoryConfiguration().getLRUsize());

        //initially estimate the counts of the terms in the dataset and store this temporarily
        //in idf map. this help us avoid using twice much memory comparing to
        //using two different maps
        for(Record r : trainingData) {
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object keyword = entry.getKey();
                Double counts = Dataset.toDouble(entry.getValue());
                
                if(counts==null || counts == 0.0) {
                    continue;
                }
                
                Double previousIDFvalue = idfMap.get(keyword);
                if(previousIDFvalue==null) {
                    previousIDFvalue = 0.0;
                }
                
                idfMap.put(keyword, ++previousIDFvalue);
            }
        }
        
        //convert counts to idf scores
        for(Map.Entry<Object, Double> entry : idfMap.entrySet()) {
            Object keyword = entry.getKey();
            Double countsInDocument = entry.getValue();
            
            idfMap.put(keyword, Math.log10(n/countsInDocument));
        }
        
        
        Map<Object, Double> maxTFIDFfeatureScores = modelParameters.getMaxTFIDFfeatureScores();
        //calculate the maximum tfidf scores
        for(Record r : trainingData) {
            /*
            //calculate the length of the document
            double documentLength = 0.0;
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Double counts = Dataset.toDouble(entry.getValue());
                
                if(counts==null || counts == 0.0) {
                    continue;
                }
                if(binarized) {
                    counts = 1.0;
                }
                
                documentLength += counts;
            }
            */
            
            //calculate the tfidf scores
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object keyword = entry.getKey();
                Double counts = Dataset.toDouble(entry.getValue());
                
                if(counts==null || counts == 0.0) {
                    continue;
                }
                
                if(binarized) {
                    counts = 1.0;
                }
                
                //double tf = counts/documentLength;
                double tf = counts;
                double idf = idfMap.get(keyword);
                
                double tfidf = tf*idf;
                
                if(tfidf==0.0) {
                    continue; //ignore 0 scored features
                }
                
                //store the maximum value of the tfidf
                Double maxTfidf = maxTFIDFfeatureScores.get(keyword);
                if(maxTfidf==null || maxTfidf<tfidf) {
                    maxTFIDFfeatureScores.put(keyword, tfidf);
                }
            }
        }
        
        //Drop the temporary Collection
        bdsf.dropTable(tmpPrefix+"idf", idfMap);
        
        Integer maxFeatures = trainingParameters.getMaxFeatures();
        if(maxFeatures!=null && maxFeatures<maxTFIDFfeatureScores.size()) {
            ScoreBasedFeatureSelection.selectHighScoreFeatures(maxTFIDFfeatureScores, maxFeatures);
        }
    }

    @Override
    protected void filterFeatures(Dataset newData) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        Map<Object, Double> maxTFIDFfeatureScores = modelParameters.getMaxTFIDFfeatureScores();
        
        for(Record r : newData) {
            Iterator<Map.Entry<Object, Object>> it = r.getX().entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry<Object, Object> entry = it.next();
                Object feature = entry.getKey();
                
                Double value = Dataset.toDouble(entry.getValue());
                
                if(!maxTFIDFfeatureScores.containsKey(feature)) { //unselected feature
                    //remove it both from the columns and from the record
                    newData.getColumns().remove(feature);
                    it.remove();
                }
                else if(value==null || value==0.0) { //inactive feature
                    //remove it only from this record
                    it.remove();
                }
            }
        }
    }
    
}
