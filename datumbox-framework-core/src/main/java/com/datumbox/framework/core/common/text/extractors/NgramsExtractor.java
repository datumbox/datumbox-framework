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
package com.datumbox.framework.core.common.text.extractors;

import java.util.*;

/**
 * The NgramsExtractor class can be used to tokenize a string, extract its keyword
 * combinations and estimate their occurrence scores in the original string. This
 * extractor is ideal for the feature extraction phase of Text ClassificationMetrics.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class NgramsExtractor extends AbstractTextExtractor<NgramsExtractor.Parameters, String, Double> {
    
    /**
     * AbstractParameters of the NgramsExtractor.
     */
    public static class Parameters extends AbstractTextExtractor.AbstractParameters {  
        private static final long serialVersionUID = 1L;
        
        private int maxCombinations=3;
        
        private int minWordLength=1;
        private int minWordOccurrence=1;
        
        private int examinationWindowLength=3; 
        private int maxDistanceBetweenKwds=0;
        
        /**
         * Getter for the number of Maximum keyword combinations that we want to
         * extract.
         * 
         * @return 
         */
        public int getMaxCombinations() {
            return maxCombinations;
        }
        
        /**
         * Setter for the number of Maximum keyword combinations that we want to
         * extract. 
         * 
         * @param maxCombinations 
         */
        public void setMaxCombinations(int maxCombinations) {
            this.maxCombinations = maxCombinations;
        }

        /**
         * Getter for the minimum word length threshold. Words with a length less
         * than this value will not be extracted.
         * 
         * @return 
         */
        public int getMinWordLength() {
            return minWordLength;
        }

        /**
         * Setter for the minimum word length threshold. Words with a length less
         * than this value will not be extracted. 
         * 
         * @param minWordLength 
         */
        public void setMinWordLength(int minWordLength) {
            this.minWordLength = minWordLength;
        }
        
        /**
         * Getter for the minimum word occurrence threshold. Words with less
         * occurrences than this value will be ignored.
         * 
         * @return 
         */
        public int getMinWordOccurrence() {
            return minWordOccurrence;
        }
        
        /**
         * Setter for the minimum word occurrence threshold. Words with less
         * occurrences than this value will be ignored. 
         * 
         * @param minWordOccurrence 
         */
        public void setMinWordOccurrence(int minWordOccurrence) {
            this.minWordOccurrence = minWordOccurrence;
        }

        /**
         * Getter for the length of the examination window in which we search
         * for keyword combinations.
         * 
         * @return 
         */
        public int getExaminationWindowLength() {
            return examinationWindowLength;
        }
        
        /**
         * Setter for the length of the examination window in which we search
         * for keyword combinations. 
         * 
         * @param examinationWindowLength 
         */
        public void setExaminationWindowLength(int examinationWindowLength) {
            this.examinationWindowLength = examinationWindowLength;
        }
        
        /**
         * Getter for the maximum distance between keywords. If the distance 
         * between keywords is greater than this value, the keyword combination 
         * is ignored.
         * 
         * @return 
         */
        public int getMaxDistanceBetweenKwds() {
            return maxDistanceBetweenKwds;
        }
        
        /**
         * Setter for the maximum distance between keywords. If the distance 
         * between keywords is greater than this value, the keyword combination 
         * is ignored. 
         * 
         * @param maxDistanceBetweenKwds 
         */
        public void setMaxDistanceBetweenKwds(int maxDistanceBetweenKwds) {
            this.maxDistanceBetweenKwds = maxDistanceBetweenKwds;
        }
        
    }
        
    /**
     * Public constructor that accepts as arguments the AbstractParameters object.
     * 
     * @param parameters 
     */
    public NgramsExtractor(Parameters parameters) {
        super(parameters);
    }
    
    /**
     * This method gets as input a string and returns as output a map with the
     * extracted keywords along with the number of their scores in the text. Their
     * scores are a combination of occurrences and proximity metrics.
     * 
     * @param text
     * @return 
     */
    @Override
    public Map<String, Double> extract(final String text) {
        Map<Integer, String> ID2word = new HashMap<>(); //ID=>Kwd
        Map<Integer, Double> ID2occurrences = new HashMap<>(); //ID=>counts/scores
        Map<Integer, Integer> position2ID = new LinkedHashMap<>(); //word position=>ID maintain the order of insertation
        
        int numberOfWordsInDoc = buildInternalArrays(text, ID2word, ID2occurrences, position2ID);
        
        int maxCombinations = parameters.getMaxCombinations();
        Map<String, Double> keywordsMap = new HashMap<>();
        //move the "window" across the document by 1 word at each time
        for(Map.Entry<Integer, Integer> entry : position2ID.entrySet()) {
            Integer wordID = entry.getValue();
            if(!useThisWord(wordID, ID2word, ID2occurrences)) {
                continue;
            }
            
            Integer position = entry.getKey();
            //Build all the combinations of the current word with other words within the window and estimate the scores
            Map<LinkedList<Integer>, Double> positionCombinationsWithScores = getPositionCombinationsWithinWindow(position, maxCombinations, ID2word, ID2occurrences, position2ID, numberOfWordsInDoc);
            
            //Convert the positions into words and aggregate over their scores.
            for(Map.Entry<LinkedList<Integer>, Double> entry2 : positionCombinationsWithScores.entrySet()) {
                LinkedList<Integer> positionCombination = entry2.getKey();
                
                StringBuilder sb = new StringBuilder(positionCombination.size()*6);
                for(Integer pos : positionCombination) {
                    sb.append(ID2word.get(position2ID.get(pos))).append(" ");
                }
                
                if(sb.length()>0) {
                    String key = sb.toString().trim();
                    double score = entry2.getValue();
                    
                    keywordsMap.put(key, keywordsMap.getOrDefault(key, 0.0)+score);
                }
                
            }
        }
        
        //remove any word that has score less than the min occurrence
        double minScore = parameters.getMinWordOccurrence();
        Iterator<Map.Entry<String, Double>> it = keywordsMap.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<String, Double> entry = it.next();
            if(entry.getValue()<minScore) {
                it.remove();
            }
        }
        
        return keywordsMap;
    }

    private Map<LinkedList<Integer>, Double> getPositionCombinationsWithinWindow(Integer windowStart, int maxCombinations, 
            Map<Integer, String> ID2word, Map<Integer, Double> ID2occurrences, Map<Integer, Integer> position2ID, 
            int numberOfWordsInDoc) {
        int maxDistanceBetweenKwds=parameters.getMaxDistanceBetweenKwds(); 
        
        //make sure the window is atleast as the number of combinations but smaller than the total size of the document
        int windowLength;
        if(maxDistanceBetweenKwds == 0) {
            windowLength = maxCombinations;
        }
        else {
            windowLength = Math.max(parameters.getExaminationWindowLength(), maxCombinations);
        }
        int windowEnd=Math.min(windowStart+windowLength, numberOfWordsInDoc);

        //stores POSITION1,...,POSITIONn-1,POSITIONn=>scores of combinations
        Map<LinkedList<Integer>, Double> positionCombinationsWithScores = new HashMap<>();
        
        LinkedList<Integer> seedList = new LinkedList<>();
        seedList.add(windowStart);
        positionCombinationsWithScores.put(seedList, 1.0); //score of 1 since we have once occurence
        
        //take the next word within the window
        for(int i=windowStart+1;i<windowEnd;++i) {
            //take a new word and ensure that we want to use it
            Integer ID = position2ID.get(i);
            if(ID==null || useThisWord(ID, ID2word, ID2occurrences)==false) {
                continue;
            }
            
            Map<LinkedList<Integer>, Double> newPositionCombinations = new HashMap<>();//here is where we store the new combinations that we produce
            
            //it stores the number of words betwen the current position and the original word. It does not include the start/end words only everything between them.
            int wordsBetweenStart=i-(windowStart+1); 
            
            //take any combination already created and try to combine it with the new word
            for(LinkedList<Integer> previousPositionCombination : positionCombinationsWithScores.keySet()) {
                int previousNumWords = previousPositionCombination.size();
                if(previousNumWords>=maxCombinations) {
                    continue; //respect the max combination size
                }
                int wordsBetweenLastCombo = i-(previousPositionCombination.getLast()+1); //this is the number of words between the last word of the previous combo and the new word that we have
                if(wordsBetweenLastCombo>maxDistanceBetweenKwds) {
                    continue; //this ensures that the words that are added in the combination will never be more than this threhold appart.
                }
                int currentNumWords = previousNumWords+1; //the number of words the next combination will include
                
                int extraWords = wordsBetweenStart - (currentNumWords-2); //we do -2 because again we did not include the start and end words
                
                double score = 1.0/(1.0+extraWords);
                
                LinkedList<Integer> currentPositionCombination = new LinkedList<>(previousPositionCombination);
                currentPositionCombination.add(i); //add it at the end
                newPositionCombinations.put(currentPositionCombination, score);
                
            }
            
            //add the new combinations in the list
            positionCombinationsWithScores.putAll(newPositionCombinations);
        }
        
        
        
        return positionCombinationsWithScores;
    }
    
    private boolean useThisWord(Integer wordID, Map<Integer, String> ID2word, Map<Integer, Double> ID2occurrences) {
        String word = ID2word.get(wordID);
        if(word==null) {
            return false;
        }
        else if(word.length() < parameters.getMinWordLength()) {
            return false;
        }
        else if(ID2occurrences.get(wordID)<parameters.getMinWordOccurrence()) {
            return false;
        }
        return true;
    }
    
    private int buildInternalArrays(final String text, Map<Integer, String> ID2word, Map<Integer, Double> ID2occurrences, Map<Integer, Integer> position2ID) {
        
        Map<String, Integer> word2ID = new HashMap<>();
        
        List<String> keywordList = generateTokenizer().tokenize(text);
        
        int lastId=-1;
        int numberOfWordsInDoc = keywordList.size();
        for(int position=0;position<numberOfWordsInDoc;++position) {
            String word = keywordList.get(position);
            
            Integer id = word2ID.get(word);
            if(id==null) {
                id=++lastId;
                word2ID.put(word, id);
                ID2word.put(id, word);
                ID2occurrences.put(id, 0.0);
            }
            
            ID2occurrences.put(id, ID2occurrences.get(id)+1);
            position2ID.put(position, id);
        }
        
        //keywordList = null;

        //word2ID = null;
        return numberOfWordsInDoc;
    }
    
}
