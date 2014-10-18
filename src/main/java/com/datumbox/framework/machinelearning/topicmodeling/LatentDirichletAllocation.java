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
package com.datumbox.framework.machinelearning.topicmodeling;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.AssociativeArray2D;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.factories.BigDataStructureFactory;
import com.datumbox.common.persistentstorage.interfaces.BigDataStructureMarker;
import com.datumbox.common.utilities.MapFunctions;
import com.datumbox.common.utilities.PHPfunctions;
import com.datumbox.configuration.GeneralConfiguration;
import com.datumbox.configuration.MemoryConfiguration;
import com.datumbox.configuration.StorageConfiguration;
import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLtopicmodeler;
import com.datumbox.framework.machinelearning.common.validation.LatentDirichletAllocationValidation;
import com.datumbox.framework.statistics.descriptivestatistics.Descriptives;
import com.datumbox.framework.statistics.sampling.SRS;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.mongodb.morphia.annotations.Transient;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class LatentDirichletAllocation extends BaseMLtopicmodeler<LatentDirichletAllocation.ModelParameters, LatentDirichletAllocation.TrainingParameters, LatentDirichletAllocation.ValidationMetrics> {
    /*
    References:
        - http://videolectures.net/mlss09uk_blei_tm/
        - http://www.ncbi.nlm.nih.gov/pmc/articles/PMC387300/
        - https://github.com/angeloskath/php-nlp-tools/blob/master/src/NlpTools/Models/Lda.php
        - https://gist.github.com/mblondel/542786
        - http://stats.stackexchange.com/questions/9315/topic-prediction-using-latent-dirichlet-allocation
        - http://home.uchicago.edu/~lkorsos/GibbsNGramLDA.pdf
        - http://machinelearning.wustl.edu/mlpapers/paper_files/BleiNJ03.pdf
        - http://www.cl.cam.ac.uk/teaching/1213/L101/clark_lectures/lect7.pdf
        - http://www.ics.uci.edu/~newman/pubs/fastlda.pdf
        - http://www.tnkcs.inf.elte.hu/vedes/Biro_Istvan_Tezisek_en.pdf (Limit Gibbs Sampler & unseen inference)
        - http://airweb.cse.lehigh.edu/2008/submissions/biro_2008_latent_dirichlet_allocation_spam.pdf (unseen inference)
        - http://www.cs.cmu.edu/~akyrola/10702project/kyrola10702FINAL.pdf 
        - http://stats.stackexchange.com/questions/18167/how-to-calculate-perplexity-of-a-holdout-with-latent-dirichlet-allocation
        - http://www.slideserve.com/adamdaniel/an-introduction-to-latent-dirichlet-allocation-lda
    */
    /**
     * The internalDataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the internalDataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
    public static final String SHORT_METHOD_NAME = "LDirA";
    
    public static class ModelParameters extends BaseMLtopicmodeler.ModelParameters {
        private int totalIterations;
        
        //number of observations used for training
        private Integer n =0;
        private Integer d =0; //the vocabulary size
        
        /**
         * The topic assignments of a particular word in a particular document.
         * It is a key-value of <Integer, Object> => Integer 
         * The key is a combination of Record.id and Word Position number (also 
         * an Integer but stored as object because the Record stores columns as Objects).
         * The value is the Id of the topic to which the word is assigned.
         */
        @BigDataStructureMarker
        @Transient
        private Map<List<Object>, Integer> topicAssignmentOfDocumentWord; //the Z in the graphical model

        /**
         * Counts the number of occurrences of topics in a particular document
         * (in other words the number of times that a word from the particular
         * document has been assigned to a particular topic).
         * It is a key value of <Integer, Integer> => Integer
         * The key is a combination of Record.id and Topic id.
         * The value is the number of counts of the pair.
         */
        @BigDataStructureMarker
        @Transient
        private Map<List<Integer>, Integer> documentTopicCounts; //the nj(d) in the papers

        /**
         * Counts the number of times a particular word is assigned to a particular
         * topic.
         * It is a key value of <Integer, Object> => Integer
         * The key is a combination of Topic id and Record Value which should normally
         * be a String (the word) but is stored in the associative array of the
         * record as an Object.
         * The value is the number of counts of the pair.
         */
        @BigDataStructureMarker
        @Transient
        private Map<List<Object>, Integer> topicWordCounts; //the nj(w) in the papers
        
        /**
         * Counts the number of words in a document. Even though this information
         * is available to us from Record.size(), we store this info to be able
         * to compute the probability of θ without having access on the original
         * Record.
         * It is a key value of Integer => Integer
         * The key the Record.Id.
         * The Value is the number of counts.
         */
        @BigDataStructureMarker
        @Transient
        private Map<Integer, Integer> documentWordCounts; //the n.(d) in the papers
        
        /**
         * Counts the number of words assigned to the particular topic.
         * It is a key value of Integer => Integer
         * The key the Topic Id.
         * The Value is the number of counts.
         */
        @BigDataStructureMarker
        @Transient
        private Map<Integer, Integer> topicCounts; //the nj(.) in the papers
        
        
        @Override
        public void bigDataStructureInitializer(BigDataStructureFactory bdsf, MemoryConfiguration memoryConfiguration) {
            super.bigDataStructureInitializer(bdsf, memoryConfiguration);
            
            BigDataStructureFactory.MapType mapType = memoryConfiguration.getMapType();
            int LRUsize = memoryConfiguration.getLRUsize();
            
            topicAssignmentOfDocumentWord = bdsf.getMap("topicAssignmentOfDocumentWord", mapType, LRUsize);
            documentTopicCounts = bdsf.getMap("documentTopicCounts", mapType, LRUsize);
            topicWordCounts = bdsf.getMap("topicWordCounts", mapType, LRUsize);
            documentWordCounts = bdsf.getMap("documentWordCounts", mapType, LRUsize);
            topicCounts = bdsf.getMap("topicCounts", mapType, LRUsize);
        }

        public int getTotalIterations() {
            return totalIterations;
        }

        public void setTotalIterations(int totalIterations) {
            this.totalIterations = totalIterations;
        }
        
        public Integer getN() {
            return n;
        }

        public void setN(Integer n) {
            this.n = n;
        }

        public Integer getD() {
            return d;
        }

        public void setD(Integer d) {
            this.d = d;
        }

        public Map<List<Object>, Integer> getTopicAssignmentOfDocumentWord() {
            return topicAssignmentOfDocumentWord;
        }

        public void setTopicAssignmentOfDocumentWord(Map<List<Object>, Integer> topicAssignmentOfDocumentWord) {
            this.topicAssignmentOfDocumentWord = topicAssignmentOfDocumentWord;
        }

        public Map<List<Integer>, Integer> getDocumentTopicCounts() {
            return documentTopicCounts;
        }

        public void setDocumentTopicCounts(Map<List<Integer>, Integer> documentTopicCounts) {
            this.documentTopicCounts = documentTopicCounts;
        }

        public Map<List<Object>, Integer> getTopicWordCounts() {
            return topicWordCounts;
        }

        public void setTopicWordCounts(Map<List<Object>, Integer> topicWordCounts) {
            this.topicWordCounts = topicWordCounts;
        }

        public Map<Integer, Integer> getDocumentWordCounts() {
            return documentWordCounts;
        }

        public void setDocumentWordCounts(Map<Integer, Integer> documentWordCounts) {
            this.documentWordCounts = documentWordCounts;
        }

        public Map<Integer, Integer> getTopicCounts() {
            return topicCounts;
        }

        public void setTopicCounts(Map<Integer, Integer> topicCounts) {
            this.topicCounts = topicCounts;
        }
        
        
    } 
    
    public static class TrainingParameters extends BaseMLtopicmodeler.TrainingParameters {    
        private int k = 2; //number of topics
        private int maxIterations = 50; //both for training and testing
        
        //a good value for alpha and beta is to set them equal to 1.0/k
        private double alpha = 1.0; //the hyperparameter of dirichlet prior for document topic distribution
        private double beta = 1.0; //the hyperparameter of dirichlet prior for word topic distribution

        public int getK() {
            return k;
        }

        public void setK(int k) {
            this.k = k;
        }

        public int getMaxIterations() {
            return maxIterations;
        }

        public void setMaxIterations(int maxIterations) {
            this.maxIterations = maxIterations;
        }

        public double getAlpha() {
            return alpha;
        }

        public void setAlpha(double alpha) {
            this.alpha = alpha;
        }

        public double getBeta() {
            return beta;
        }

        public void setBeta(double beta) {
            this.beta = beta;
        }
        
    } 

    public static class ValidationMetrics extends BaseMLtopicmodeler.ValidationMetrics {
        double perplexity = 0.0;

        public double getPerplexity() {
            return perplexity;
        }

        public void setPerplexity(double perplexity) {
            this.perplexity = perplexity;
        }

    }
    
    public LatentDirichletAllocation(String dbName) {
        super(dbName, LatentDirichletAllocation.ModelParameters.class, LatentDirichletAllocation.TrainingParameters.class, LatentDirichletAllocation.ValidationMetrics.class, new LatentDirichletAllocationValidation()); 
    }

    @Override
    public String shortMethodName() {
        return SHORT_METHOD_NAME;
    }
    

    @Override
    protected void estimateModelParameters(Dataset trainingData) {
        int n = trainingData.size();
        int d = trainingData.getColumnSize();
        
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        
        modelParameters.setN(n);
        modelParameters.setD(d);
        
        //get model parameters
        int k = trainingParameters.getK(); //number of topics
        Map<List<Object>, Integer> topicAssignmentOfDocumentWord = modelParameters.getTopicAssignmentOfDocumentWord();
        Map<List<Integer>, Integer> documentTopicCounts = modelParameters.getDocumentTopicCounts();
        Map<List<Object>, Integer> topicWordCounts = modelParameters.getTopicWordCounts();
        Map<Integer, Integer> documentWordCounts = modelParameters.getDocumentWordCounts();
        Map<Integer, Integer> topicCounts = modelParameters.getTopicCounts();
        
        //initialize topic assignments of each word randomly and update the counters
        for(Record r : trainingData) {
            Integer documentId = r.getId();
            
            documentWordCounts.put(documentId, r.getX().size());
            
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object wordPosition = entry.getKey();
                Object word = entry.getValue();
                
                //sample a topic
                Integer topic = PHPfunctions.mt_rand(0,k-1);
                
                increase(topicCounts, topic);
                topicAssignmentOfDocumentWord.put(Arrays.asList(documentId, wordPosition), topic);
                increase(documentTopicCounts, Arrays.asList(documentId, topic));
                increase(topicWordCounts, Arrays.asList(topic, word));
            }
        }
        
        
        double alpha = trainingParameters.getAlpha();
        double beta = trainingParameters.getBeta();
        
        int maxIterations = trainingParameters.getMaxIterations();
        
        int iteration=0;
        while(iteration<maxIterations) {
            
            if(GeneralConfiguration.DEBUG) {
                System.out.println("Iteration "+iteration);
            }
            
            int changedCounter = 0;
            //collapsed gibbs sampler
            for(Record r : trainingData) {
                Integer documentId = r.getId();
                
                AssociativeArray topicAssignments = new AssociativeArray();
                for(int j=0;j<k;++j) {
                    topicAssignments.put(j, 0.0);
                }
                
                int totalWords = r.getX().size();
                
                for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                    Object wordPosition = entry.getKey();
                    Object word = entry.getValue();
            
                    
                    //remove the word from the dataset
                    Integer topic = topicAssignmentOfDocumentWord.get(Arrays.asList(documentId, wordPosition));
                    //decrease(documentWordCounts, documentId); //slow
                    decrease(topicCounts, topic);
                    decrease(documentTopicCounts, Arrays.asList(documentId, topic));
                    decrease(topicWordCounts, Arrays.asList(topic, word));
                    
                    //int numberOfDocumentWords = r.getX().size()-1; //fast - decreased by 1
                    
                    //compute the posteriors of the topics and sample from it
                    AssociativeArray topicProbabilities = new AssociativeArray();
                    for(int j=0;j<k;++j) {
                        double enumerator = 0.0;
                        Integer njw = topicWordCounts.get(Arrays.asList(j,word));
                        if(njw !=null) {
                            enumerator = njw + beta;
                        }
                        else {
                            enumerator = beta;
                        }
                        
                        Integer njd = documentTopicCounts.get(Arrays.asList(documentId, j));
                        if(njd != null) {
                            enumerator *= (njd + alpha);
                        }
                        else {
                            enumerator *= alpha;
                        }
                        
                        double denominator = topicCounts.get((Integer)j)+beta*d;
                        //denominator *= numberOfDocumentWords+alpha*k; //this is not necessary because it is the same for all categories, so it can be omited
                        
                        topicProbabilities.put(j, enumerator/denominator);
                    }

                    //normalize probabilities
                    Descriptives.normalize(topicProbabilities);
                    
                    //sample from these probabilieis
                    Integer newTopic = (Integer)SRS.weightedProbabilitySampling(topicProbabilities, 1, true).iterator().next();
                    topic = newTopic; //new topic assigment
                    
                    //add back the word in the dataset
                    topicAssignmentOfDocumentWord.put(Arrays.asList(documentId, wordPosition), topic);
                    //increase(documentWordCounts, documentId); //slow
                    increase(topicCounts, topic);
                    increase(documentTopicCounts, Arrays.asList(documentId, topic));
                    increase(topicWordCounts, Arrays.asList(topic, word));
                    
                    topicAssignments.put(topic, Dataset.toDouble(topicAssignments.get(topic))+1.0/totalWords);
                }
                
                Object mainTopic=MapFunctions.selectMaxKeyValue(topicAssignments).getKey();
                
                if(!mainTopic.equals(r.getYPredicted())) {
                    ++changedCounter;
                }
                r.setYPredicted(mainTopic);
                r.setYPredictedProbabilities(topicAssignments);
            }
            ++iteration;
            
            if(GeneralConfiguration.DEBUG) {
                System.out.println("Reassigned Records "+ changedCounter);
            }
            
            if(changedCounter==0) {
                break;
            }
        }
        
        modelParameters.setTotalIterations(iteration);
        
    }

    @Override
    protected void predictDataset(Dataset newData) {
        predictAndValidate(newData);
    }
    
    @Override
    protected ValidationMetrics validateModel(Dataset validationData) {
        return predictAndValidate(validationData);
    }
    
    public AssociativeArray2D getWordProbabilitiesPerTopic() {
        AssociativeArray2D ptw = new AssociativeArray2D();
        
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        
        //initialize a probability list for every topic
        int k = trainingParameters.getK();
        for(Integer topicId=0;topicId<k;++topicId) {
            ptw.put(topicId, new AssociativeArray());
        }
        
        int d = modelParameters.getD();
        double beta = trainingParameters.getBeta();
        
        Map<List<Object>, Integer> topicWordCounts = modelParameters.getTopicWordCounts();
        Map<Integer, Integer> topicCounts = modelParameters.getTopicCounts();
        for(Map.Entry<List<Object>, Integer> entry : topicWordCounts.entrySet()) {
            List<Object> tpk = entry.getKey();
            Integer topicId = (Integer)tpk.get(0);
            Object word = tpk.get(1);
            Integer njw = entry.getValue();
            
            Integer nj = topicCounts.get(topicId);
            
            double probability = (njw+beta)/(nj+beta*d);
            
            ptw.get(topicId).put(word, probability);
        }
        
        for(Integer topicId=0;topicId<k;++topicId) {
            ptw.put(topicId, MapFunctions.sortAssociativeArrayByValueDescending(ptw.get(topicId)));
        }
        
        return ptw;
    }
    
    /**
     * Utility method that increases the map value by 1.
     * 
     * @param <K>
     * @param map
     * @param key 
     */
    private <K> void increase(Map<K, Integer> map, K key) {
        Integer previousValue = map.get(key);
        if(previousValue==null) {
            previousValue=0;
        }
        map.put(key, previousValue+1);
    }

    /**
     * Utility method that decreases the map value by 1.
     * @param <K>
     * @param map
     * @param key 
     */
    private <K> void decrease(Map<K, Integer> map, K key) {
        Integer previousValue = map.get(key);
        if(previousValue==null) {
            previousValue=0;
        }
        map.put(key, previousValue-1);
    }
    
    private ValidationMetrics predictAndValidate(Dataset newData) {
        //This method uses similar approach to the training but the most important
        //difference is that we do not wish to modify the original training params.
        //as a result we need to modify the code to use additional temporary
        //counts for the testing data and merge them with the parameters from the
        //training data in order to make a decision
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        
        
        //create new validation metrics object
        ValidationMetrics validationMetrics = knowledgeBase.getEmptyValidationMetricsObject();
        
        String tmpPrefix=StorageConfiguration.getTmpPrefix();
        
        //get model parameters
        int n = modelParameters.getN();
        int d = modelParameters.getD();
        int k = trainingParameters.getK(); //number of topics
        
        
        Map<List<Object>, Integer> topicWordCounts = modelParameters.getTopicWordCounts();
        Map<Integer, Integer> topicCounts = modelParameters.getTopicCounts();
        
        
        BigDataStructureFactory.MapType mapType = knowledgeBase.getMemoryConfiguration().getMapType();
        int LRUsize = knowledgeBase.getMemoryConfiguration().getLRUsize();
        BigDataStructureFactory bdsf = knowledgeBase.getBdsf();
        
        //we create temporary maps for the prediction sets to avoid modifing the maps that we already learned
        Map<List<Object>, Integer> tmp_topicAssignmentOfDocumentWord = bdsf.getMap(tmpPrefix+"topicAssignmentOfDocumentWord", mapType, LRUsize);
        Map<List<Integer>, Integer> tmp_documentTopicCounts = bdsf.getMap(tmpPrefix+"documentTopicCounts", mapType, LRUsize);
        Map<List<Object>, Integer> tmp_topicWordCounts = bdsf.getMap(tmpPrefix+"topicWordCounts", mapType, LRUsize);
        Map<Integer, Integer> tmp_topicCounts = bdsf.getMap(tmpPrefix+"topicCounts", mapType, LRUsize);
        
        //initialize topic assignments of each word randomly and update the counters
        for(Record r : newData) {
            Integer documentId = r.getId();
            
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object wordPosition = entry.getKey();
                Object word = entry.getValue();
                
                //sample a topic
                Integer topic = PHPfunctions.mt_rand(0,k-1);
                
                increase(tmp_topicCounts, topic);
                tmp_topicAssignmentOfDocumentWord.put(Arrays.asList(documentId, wordPosition), topic);
                increase(tmp_documentTopicCounts, Arrays.asList(documentId, topic));
                increase(tmp_topicWordCounts, Arrays.asList(topic, word));
            }
        }
        
        
        double alpha = trainingParameters.getAlpha();
        double beta = trainingParameters.getBeta();
        
        int maxIterations = trainingParameters.getMaxIterations();
        
        double perplexity = Double.MAX_VALUE;
        for(int iteration=0;iteration<maxIterations;++iteration) {
            
            if(GeneralConfiguration.DEBUG) {
                System.out.println("Iteration "+iteration);
            }
            
            
            //collapsed gibbs sampler
            int changedCounter = 0;
            perplexity = 0.0;
            double totalDatasetWords = 0.0;
            for(Record r : newData) {
                Integer documentId = r.getId();
                
                
                AssociativeArray topicAssignments = new AssociativeArray();
                for(int j=0;j<k;++j) {
                    topicAssignments.put(j, 0.0);
                }
                
                int totalDocumentWords = r.getX().size();
                totalDatasetWords+=totalDocumentWords;
                for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                    Object wordPosition = entry.getKey();
                    Object word = entry.getValue();
            
                    
                    //remove the word from the dataset
                    Integer topic = tmp_topicAssignmentOfDocumentWord.get(Arrays.asList(documentId, wordPosition));
                    decrease(tmp_topicCounts, topic);
                    decrease(tmp_documentTopicCounts, Arrays.asList(documentId, topic));
                    decrease(tmp_topicWordCounts, Arrays.asList(topic, word));
                        
                    int numberOfDocumentWords = r.getX().size()-1;
                    
                    //compute the posteriors of the topics and sample from it
                    AssociativeArray topicProbabilities = new AssociativeArray();
                    for(int j=0;j<k;++j) {
                        double enumerator = 0.0;
                        
                        //get the counts from the current testing data
                        List<Object> topicWordKey = Arrays.asList(j,word);
                        Integer njw = tmp_topicWordCounts.get(topicWordKey);
                        if(njw !=null) {
                            enumerator = njw + beta;
                        }
                        else {
                            enumerator = beta;
                        }
                        
                        //get also the counts from the training data
                        Integer njw_original = topicWordCounts.get(topicWordKey);
                        if(njw_original!=null) {
                            enumerator+=njw_original;
                        }
                        
                        Integer njd = tmp_documentTopicCounts.get(Arrays.asList(documentId, j));
                        if(njd != null) {
                            enumerator *= (njd + alpha);
                        }
                        else {
                            enumerator *= alpha;
                        }
                        
                        //add the counts from testing data
                        double denominator = tmp_topicCounts.get((Integer)j)+beta*d -1;
                        //and the ones from training data
                        denominator+=topicCounts.get((Integer)j);
                        denominator *= numberOfDocumentWords+alpha*k;
                        
                        topicProbabilities.put(j, enumerator/denominator);
                    }
                    
                    perplexity += Math.log(Descriptives.sum(topicProbabilities.toFlatDataCollection()));
                    
                    //normalize probabilities
                    Descriptives.normalize(topicProbabilities);
                    
                    //sample from these probabilieis
                    Integer newTopic = (Integer)SRS.weightedProbabilitySampling(topicProbabilities, 1, true).iterator().next();
                    topic = newTopic; //new topic assignment
                    
                    
                    //add back the word in the dataset
                    tmp_topicAssignmentOfDocumentWord.put(Arrays.asList(documentId, wordPosition), topic);
                    increase(tmp_topicCounts, topic);
                    increase(tmp_documentTopicCounts, Arrays.asList(documentId, topic));
                    increase(tmp_topicWordCounts, Arrays.asList(topic, word));
                    
                    topicAssignments.put(topic, Dataset.toDouble(topicAssignments.get(topic))+1.0/totalDocumentWords);
                }
                
                Object mainTopic=MapFunctions.selectMaxKeyValue(topicAssignments).getKey();
                
                if(!mainTopic.equals(r.getYPredicted())) {
                    ++changedCounter;
                }                
                r.setYPredicted(mainTopic);
                r.setYPredictedProbabilities(topicAssignments);
            }

            perplexity=Math.exp(-perplexity/totalDatasetWords);
            
            if(GeneralConfiguration.DEBUG) {
                System.out.println("Reassigned Records "+ changedCounter +" - Perplexity: "+perplexity);
            }
            
            if(changedCounter==0) {
                break;
            }            
        }
        
        //Drop the temporary Collection
        bdsf.dropTable(tmpPrefix+"topicAssignmentOfDocumentWord", tmp_topicAssignmentOfDocumentWord);
        bdsf.dropTable(tmpPrefix+"documentTopicCounts", tmp_documentTopicCounts);
        bdsf.dropTable(tmpPrefix+"topicWordCounts", tmp_topicWordCounts);
        bdsf.dropTable(tmpPrefix+"topicCounts", tmp_topicCounts);
        
        
        validationMetrics.setPerplexity(perplexity);
        
        return validationMetrics;
    }
}
