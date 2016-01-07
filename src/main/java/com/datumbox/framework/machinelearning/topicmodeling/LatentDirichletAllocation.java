/**
 * Copyright (C) 2013-2016 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
package com.datumbox.framework.machinelearning.topicmodeling;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.AssociativeArray2D;
import com.datumbox.common.dataobjects.Dataframe;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.common.persistentstorage.interfaces.BigMap;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.utilities.MapFunctions;
import com.datumbox.common.utilities.PHPfunctions;
import com.datumbox.common.dataobjects.TypeInference;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector.MapType;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector.StorageHint;
import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLtopicmodeler;
import com.datumbox.framework.machinelearning.common.validation.LatentDirichletAllocationValidation;
import com.datumbox.framework.statistics.descriptivestatistics.Descriptives;
import com.datumbox.framework.statistics.sampling.SRS;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * Implementation of the Latent Dirichlet Allocation algorithm.
 * 
 * References:
 * http://videolectures.net/mlss09uk_blei_tm/
 * http://www.ncbi.nlm.nih.gov/pmc/articles/PMC387300/
 * https://github.com/angeloskath/php-nlp-tools/blob/master/src/NlpTools/Models/Lda.php
 * https://gist.github.com/mblondel/542786
 * http://stats.stackexchange.com/questions/9315/topic-prediction-using-latent-dirichlet-allocation
 * http://home.uchicago.edu/~lkorsos/GibbsNGramLDA.pdf
 * http://machinelearning.wustl.edu/mlpapers/paper_files/BleiNJ03.pdf
 * http://www.cl.cam.ac.uk/teaching/1213/L101/clark_lectures/lect7.pdf
 * http://www.ics.uci.edu/~newman/pubs/fastlda.pdf
 * http://www.tnkcs.inf.elte.hu/vedes/Biro_Istvan_Tezisek_en.pdf (Limit Gibbs Sampler & unseen inference)
 * http://airweb.cse.lehigh.edu/2008/submissions/biro_2008_latent_dirichlet_allocation_spam.pdf (unseen inference)
 * http://www.cs.cmu.edu/~akyrola/10702project/kyrola10702FINAL.pdf 
 * http://stats.stackexchange.com/questions/18167/how-to-calculate-perplexity-of-a-holdout-with-latent-dirichlet-allocation
 * http://www.slideserve.com/adamdaniel/an-introduction-to-latent-dirichlet-allocation-lda
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class LatentDirichletAllocation extends BaseMLtopicmodeler<LatentDirichletAllocation.ModelParameters, LatentDirichletAllocation.TrainingParameters, LatentDirichletAllocation.ValidationMetrics> {
    
    /** {@inheritDoc} */
    public static class ModelParameters extends BaseMLtopicmodeler.ModelParameters {
        private static final long serialVersionUID = 1L;
        
        private int totalIterations;
        
        @BigMap(mapType=MapType.HASHMAP, storageHint=StorageHint.IN_CACHE)
        private Map<List<Object>, Integer> topicAssignmentOfDocumentWord; //the Z in the graphical model

        @BigMap(mapType=MapType.HASHMAP, storageHint=StorageHint.IN_MEMORY)
        private Map<List<Integer>, Integer> documentTopicCounts; //the nj(d) in the papers

        @BigMap(mapType=MapType.HASHMAP, storageHint=StorageHint.IN_CACHE)
        private Map<List<Object>, Integer> topicWordCounts; //the nj(w) in the papers
        
        @BigMap(mapType=MapType.HASHMAP, storageHint=StorageHint.IN_MEMORY)
        private Map<Integer, Integer> documentWordCounts; //the n.(d) in the papers
        
        @BigMap(mapType=MapType.HASHMAP, storageHint=StorageHint.IN_MEMORY)
        private Map<Integer, Integer> topicCounts; //the nj(.) in the papers
        
        /** 
         * @see com.datumbox.framework.machinelearning.common.bases.baseobjects.BaseModelParameters#BaseModelParameters(com.datumbox.common.persistentstorage.interfaces.DatabaseConnector) 
         */
        protected ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }
        
        /**
         * Getter for the total number of iterations performed during training.
         * 
         * @return 
         */
        public int getTotalIterations() {
            return totalIterations;
        }
        
        /**
         * Setter for the total number of iterations performed during training.
         * 
         * @param totalIterations 
         */
        protected void setTotalIterations(int totalIterations) {
            this.totalIterations = totalIterations;
        }
        
        /**
         * Getter for the Topic Assignments of the words of the document. 
         * It returns the topic assignments of a particular word in a particular document.
         * It is a key-value of {@literal [<Integer>, <Object>]} => Integer 
         * The key is a combination of Record.id and Word Position number (also 
         * an Integer but stored as object because the Record stores columns as Objects).
         * The value is the Id of the topic to which the word is assigned. 
         * 
         * @return 
         */
        public Map<List<Object>, Integer> getTopicAssignmentOfDocumentWord() {
            return topicAssignmentOfDocumentWord;
        }
        
        /**
         * Setter for the Topic Assignments of the words of the document. 
         * 
         * @param topicAssignmentOfDocumentWord 
         */
        protected void setTopicAssignmentOfDocumentWord(Map<List<Object>, Integer> topicAssignmentOfDocumentWord) {
            this.topicAssignmentOfDocumentWord = topicAssignmentOfDocumentWord;
        }
        
        /**
         * Getter for the Document's Topic counts.
         * It contains counts the number of occurrences of topics in a particular document
         * (in other words the number of times that a word from the particular
         * document has been assigned to a particular topic).
         * It is a key value of {@literal [<Integer>, <Integer>]} => Integer
         * The key is a combination of Record.id and Topic id.
         * The value is the number of counts of the pair.
         * 
         * @return 
         */
        public Map<List<Integer>, Integer> getDocumentTopicCounts() {
            return documentTopicCounts;
        }
        
        /**
         * setter for the Document's Topic counts.
         * 
         * @param documentTopicCounts 
         */
        protected void setDocumentTopicCounts(Map<List<Integer>, Integer> documentTopicCounts) {
            this.documentTopicCounts = documentTopicCounts;
        }
        
        /**
         * Getter for the topic-word counts.
         * It counts the number of times a particular word is assigned to a particular
         * topic.
         * It is a key value of {@literal [<Integer>, <Object>]} => Integer
         * The key is a combination of Topic id and Record Value which should normally
         * be a String (the word) but is stored in the associative array of the
         * record as an Object.
         * The value is the number of counts of the pair.
         * 
         * @return 
         */
        public Map<List<Object>, Integer> getTopicWordCounts() {
            return topicWordCounts;
        }
        
        /**
         * Setter for the topic-word counts.
         * 
         * @param topicWordCounts 
         */
        protected void setTopicWordCounts(Map<List<Object>, Integer> topicWordCounts) {
            this.topicWordCounts = topicWordCounts;
        }

        /**
         * Getter for the number of words in each document.
         * Even though this information is available to us from Record.size(), we 
         * store this info to be able to compute the probability of θ without 
         * having access on the original Record.
         * It is a key value of Integer => Integer
         * The key is the Record.Id.
         * The Value is the number of counts.
         * 
         * @return 
         */
        public Map<Integer, Integer> getDocumentWordCounts() {
            return documentWordCounts;
        }
        
        /**
         * Setter for the number of words in each document.
         * 
         * @param documentWordCounts 
         */
        protected void setDocumentWordCounts(Map<Integer, Integer> documentWordCounts) {
            this.documentWordCounts = documentWordCounts;
        }

        /**
         * Getter for the number of words assigned to the particular topic.
         * It is a key value of Integer => Integer
         * The key the Topic Id. The Value is the number of counts.
         * 
         * @return 
         */
        public Map<Integer, Integer> getTopicCounts() {
            return topicCounts;
        }
        
        /**
         * Setter for the number of words assigned to the particular topic.
         * 
         * @param topicCounts 
         */
        protected void setTopicCounts(Map<Integer, Integer> topicCounts) {
            this.topicCounts = topicCounts;
        }
        
    }  
    
    /** {@inheritDoc} */
    public static class TrainingParameters extends BaseMLtopicmodeler.TrainingParameters {  
        private static final long serialVersionUID = 1L;
        
        private int k = 2; //number of topics
        private int maxIterations = 50; //both for training and testing
        
        //a good value for alpha and beta is to set them equal to 1.0/k
        private double alpha = 1.0; //the hyperparameter of dirichlet prior for document topic distribution
        private double beta = 1.0; //the hyperparameter of dirichlet prior for word topic distribution
        
        /**
         * Getter for the total number of topics k.
         * 
         * @return 
         */
        public int getK() {
            return k;
        }
        
        /**
         * Setter for the total number of topics k.
         * 
         * @param k 
         */
        public void setK(int k) {
            this.k = k;
        }
        
        /**
         * Getter for the total number of max iterations permitted in the training.
         * 
         * @return 
         */
        public int getMaxIterations() {
            return maxIterations;
        }
        
        /**
         * Setter for the total number of max iterations permitted in the training.
         * 
         * @param maxIterations 
         */
        public void setMaxIterations(int maxIterations) {
            this.maxIterations = maxIterations;
        }
        
        /**
         * Getter for the hyperparameter of dirichlet prior for document topic distribution.
         * 
         * @return 
         */
        public double getAlpha() {
            return alpha;
        }
        
        /**
         * Setter for the hyperparameter of dirichlet prior for document topic distribution.
         * 
         * @param alpha 
         */
        public void setAlpha(double alpha) {
            this.alpha = alpha;
        }
        
        /**
         * Getter for the hyperparameter of dirichlet prior for word topic distribution.
         * 
         * @return 
         */
        public double getBeta() {
            return beta;
        }
        
        /**
         * Setter for the hyperparameter of dirichlet prior for word topic distribution.
         * 
         * @param beta 
         */
        public void setBeta(double beta) {
            this.beta = beta;
        }
        
    } 
    
    /** {@inheritDoc} */
    public static class ValidationMetrics extends BaseMLtopicmodeler.ValidationMetrics {
        private static final long serialVersionUID = 1L;
        
        private double perplexity = 0.0;
        
        /**
         * Getter for the perplexity metric.
         * 
         * @return 
         */
        public double getPerplexity() {
            return perplexity;
        }
        
        /**
         * Setter for the perplexity metric.
         * 
         * @param perplexity 
         */
        public void setPerplexity(double perplexity) {
            this.perplexity = perplexity;
        }

    }
    
    /**
     * Public constructor of the algorithm.
     * 
     * @param dbName
     * @param dbConf 
     */
    public LatentDirichletAllocation(String dbName, DatabaseConfiguration dbConf) {
        super(dbName, dbConf, LatentDirichletAllocation.ModelParameters.class, LatentDirichletAllocation.TrainingParameters.class, LatentDirichletAllocation.ValidationMetrics.class, new LatentDirichletAllocationValidation()); 
    }
    
    /**
     * Returns the distribution of the words in each topic.
     * 
     * @return 
     */
    public AssociativeArray2D getWordProbabilitiesPerTopic() {
        AssociativeArray2D ptw = new AssociativeArray2D();
        
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        
        //initialize a probability list for every topic
        int k = trainingParameters.getK();
        for(int topicId=0;topicId<k;++topicId) {
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
        
        for(int topicId=0;topicId<k;++topicId) {
            ptw.put(topicId, MapFunctions.sortAssociativeArrayByValueDescending(ptw.get(topicId)));
        }
        
        return ptw;
    }
    
    @Override
    protected void _fit(Dataframe trainingData) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        int d = modelParameters.getD();
        
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        
        
        //get model parameters
        int k = trainingParameters.getK(); //number of topics
        Map<List<Object>, Integer> topicAssignmentOfDocumentWord = modelParameters.getTopicAssignmentOfDocumentWord();
        Map<List<Integer>, Integer> documentTopicCounts = modelParameters.getDocumentTopicCounts();
        Map<List<Object>, Integer> topicWordCounts = modelParameters.getTopicWordCounts();
        Map<Integer, Integer> documentWordCounts = modelParameters.getDocumentWordCounts();
        Map<Integer, Integer> topicCounts = modelParameters.getTopicCounts();
        
        //initialize topic assignments of each word randomly and update the counters
        for(Map.Entry<Integer, Record> e : trainingData.entries()) {
            Integer rId = e.getKey();
            Record r = e.getValue();
            Integer documentId = rId;
            
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
            
            logger.debug("Iteration {}", iteration);
            
            int changedCounter = 0;
            //collapsed gibbs sampler
            for(Map.Entry<Integer, Record> e : trainingData.entries()) {
                Integer rId = e.getKey();
                Record r = e.getValue();
                Integer documentId = rId;
                
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
                    //Descriptives.normalize(topicProbabilities);
                    
                    //sample from these probabilieis
                    Integer newTopic = (Integer)SRS.weightedSampling(topicProbabilities, 1, true).iterator().next();
                    topic = newTopic; //new topic assigment
                    
                    //add back the word in the dataset
                    topicAssignmentOfDocumentWord.put(Arrays.asList(documentId, wordPosition), topic);
                    //increase(documentWordCounts, documentId); //slow
                    increase(topicCounts, topic);
                    increase(documentTopicCounts, Arrays.asList(documentId, topic));
                    increase(topicWordCounts, Arrays.asList(topic, word));
                    
                    topicAssignments.put(topic, TypeInference.toDouble(topicAssignments.get(topic))+1.0/totalWords);
                }
                
                Object mainTopic=MapFunctions.selectMaxKeyValue(topicAssignments).getKey();
                
                if(!mainTopic.equals(r.getYPredicted())) {
                    ++changedCounter;
                }
                trainingData._unsafe_set(rId, new Record(r.getX(), r.getY(), mainTopic, topicAssignments));
            }
            ++iteration;
            
            logger.debug("Reassigned Records {}", changedCounter);
            
            if(changedCounter==0) {
                break;
            }
        }
        
        modelParameters.setTotalIterations(iteration);
        
    }

    @Override
    protected void predictDataset(Dataframe newData) {
        predictAndValidate(newData);
    }
    
    @Override
    protected ValidationMetrics validateModel(Dataframe validationData) {
        return predictAndValidate(validationData);
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
    
    private ValidationMetrics predictAndValidate(Dataframe newData) {
        //This method uses similar approach to the training but the most important
        //difference is that we do not wish to modify the original training params.
        //as a result we need to modify the code to use additional temporary
        //counts for the testing data and merge them with the parameters from the
        //training data in order to make a decision
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        
        
        //create new validation metrics object
        ValidationMetrics validationMetrics = knowledgeBase.getEmptyValidationMetricsObject();
        
        //get model parameters
        int d = modelParameters.getD();
        int k = trainingParameters.getK(); //number of topics
        
        
        Map<List<Object>, Integer> topicWordCounts = modelParameters.getTopicWordCounts();
        Map<Integer, Integer> topicCounts = modelParameters.getTopicCounts();
        
        
        DatabaseConnector dbc = knowledgeBase.getDbc();
        
        //we create temporary maps for the prediction sets to avoid modifing the maps that we already learned
        Map<List<Object>, Integer> tmp_topicAssignmentOfDocumentWord = dbc.getBigMap("tmp_topicAssignmentOfDocumentWord", MapType.HASHMAP, StorageHint.IN_CACHE, true);
        Map<List<Integer>, Integer> tmp_documentTopicCounts = dbc.getBigMap("tmp_documentTopicCounts", MapType.HASHMAP, StorageHint.IN_MEMORY, true);
        Map<List<Object>, Integer> tmp_topicWordCounts = dbc.getBigMap("tmp_topicWordCounts", MapType.HASHMAP, StorageHint.IN_CACHE, true);
        Map<Integer, Integer> tmp_topicCounts = dbc.getBigMap("tmp_topicCounts", MapType.HASHMAP, StorageHint.IN_MEMORY, true);
        
        //initialize topic assignments of each word randomly and update the counters
        for(Map.Entry<Integer, Record> e : newData.entries()) {
            Integer rId = e.getKey();
            Record r = e.getValue();
            Integer documentId = rId;
            
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
            
            logger.debug("Iteration {}", iteration);
            
            
            //collapsed gibbs sampler
            int changedCounter = 0;
            perplexity = 0.0;
            double totalDatasetWords = 0.0;
            for(Map.Entry<Integer, Record> e : newData.entries()) {
                Integer rId = e.getKey();
                Record r = e.getValue();
                Integer documentId = rId;
                
                
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
                    //Descriptives.normalize(topicProbabilities);
                    
                    //sample from these probabilieis
                    Integer newTopic = (Integer)SRS.weightedSampling(topicProbabilities, 1, true).iterator().next();
                    topic = newTopic; //new topic assignment
                    
                    
                    //add back the word in the dataset
                    tmp_topicAssignmentOfDocumentWord.put(Arrays.asList(documentId, wordPosition), topic);
                    increase(tmp_topicCounts, topic);
                    increase(tmp_documentTopicCounts, Arrays.asList(documentId, topic));
                    increase(tmp_topicWordCounts, Arrays.asList(topic, word));
                    
                    topicAssignments.put(topic, TypeInference.toDouble(topicAssignments.get(topic))+1.0/totalDocumentWords);
                }
                
                Object mainTopic=MapFunctions.selectMaxKeyValue(topicAssignments).getKey();
                
                if(!mainTopic.equals(r.getYPredicted())) {
                    ++changedCounter;
                }                
                newData._unsafe_set(rId, new Record(r.getX(), r.getY(), mainTopic, topicAssignments));
            }

            perplexity=Math.exp(-perplexity/totalDatasetWords);
            
            logger.debug("Reassigned Records {} - Perplexity: {}", changedCounter, perplexity);
            
            if(changedCounter==0) {
                break;
            }            
        }
        
        //Drop the temporary Collection
        dbc.dropBigMap("tmp_topicAssignmentOfDocumentWord", tmp_topicAssignmentOfDocumentWord);
        dbc.dropBigMap("tmp_documentTopicCounts", tmp_documentTopicCounts);
        dbc.dropBigMap("tmp_topicWordCounts", tmp_topicWordCounts);
        dbc.dropBigMap("tmp_topicCounts", tmp_topicCounts);
        
        
        validationMetrics.setPerplexity(perplexity);
        
        return validationMetrics;
    }
}
