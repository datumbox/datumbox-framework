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
package com.datumbox.framework.utilities.text.extractors;

import com.datumbox.common.utilities.PHPMethods;
import com.datumbox.framework.utilities.text.tokenizers.AbstractTokenizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * The NgramsExtractor class can be used to tokenize a string, extract its keyword
 * combinations and estimate their occurrence scores in the original string. This
 * extractor is ideal for the feature extraction phase of Text Classification.
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
        
        private int examinationWindowLength=15; //15 words later
        private int maxDistanceBetweenKwds=4; //up to 4 words distance between two words of the same keyword
        
        private int keepFloatPointsUntilCombination=3; //After 3 word combinations ignore the float points to reduce memory and improve speed
        private double keepFloatPointsAbove=0.1; //ignore all floating point scores less than 0.1 to reduce memory and improve speed
        
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
         * extract. The default number of keyword combinations extracted by this
         * class is 3.
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
         * than this value will not be extracted. The default value for this
         * threshold is 1.
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
         * occurrences than this value will be ignored. The default value for
         * this threshold is 1.
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
         * for keyword combinations. The default value for this length is 15.
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
         * is ignored. The default value for this threshold is 4.
         * 
         * @param maxDistanceBetweenKwds 
         */
        public void setMaxDistanceBetweenKwds(int maxDistanceBetweenKwds) {
            this.maxDistanceBetweenKwds = maxDistanceBetweenKwds;
        }
        
        /**
         * Getter for the keepFloatPointsUntilCombination variable. 
         * 
         * @return 
         */
        public int getKeepFloatPointsUntilCombination() {
            return keepFloatPointsUntilCombination;
        }

        /**
         * Setter for the keepFloatPointsUntilCombination variable.
         * 
         * @param keepFloatPointsUntilCombination 
         */
        public void setKeepFloatPointsUntilCombination(int keepFloatPointsUntilCombination) {
            this.keepFloatPointsUntilCombination = keepFloatPointsUntilCombination;
        }

        /**
         * Getter for the keepFloatPointsAbove variable.
         * 
         * @return 
         */
        public double getKeepFloatPointsAbove() {
            return keepFloatPointsAbove;
        }
        
        /**
         * Setter for the keepFloatPointsAbove variable.
         * 
         * @param keepFloatPointsAbove 
         */
        public void setKeepFloatPointsAbove(double keepFloatPointsAbove) {
            this.keepFloatPointsAbove = keepFloatPointsAbove;
        }
        
    }
    
    private static final char SEPARATOR = '_';
        
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
        
        Map<String, Double> keywordProximityScores = new HashMap<>();
        
        //move the "window" across the document by 1 word at each time
        for(Map.Entry<Integer, Integer> entry : position2ID.entrySet()) {
            Integer wordID = entry.getValue();
            if(!useThisWord(wordID, ID2word, ID2occurrences)) {
                continue;
            }
            
            Integer position = entry.getKey();
            
            Map<String, Integer> wordCombinations = getCombinationsWithinWindow(position, parameters.getMaxCombinations(), ID2word, ID2occurrences, position2ID, numberOfWordsInDoc);
            
            //translate positions to proximity metrics
            for(Map.Entry<String, Integer> entry2 : wordCombinations.entrySet()) {
                String IDcombinationReverse = entry2.getKey();
                Integer wordsBetween = entry2.getValue();
                
                int numberOfWords = PHPMethods.substr_count(IDcombinationReverse, SEPARATOR)-1;//starts enumeration from 0. We need to subtract one because at the end each string has an extra SEPARATOR
                
                int extraWords = wordsBetween - numberOfWords;
                
                Double proximityScore = keywordProximityScores.get(IDcombinationReverse);
                if(proximityScore==null) {
                    proximityScore=0.0;
                }
                
                if(extraWords<=0) {
                    ++proximityScore;
                }
                else {
                    proximityScore+=0.5*extraWords;
                }
                
                keywordProximityScores.put(IDcombinationReverse, proximityScore);
            }
            
            
            //wordCombinations = null;
        }
        
        //initialize keyword map
        Map<String, Double> keywordsMap = new HashMap<>();
        for(Map.Entry<String, Double> entry : keywordProximityScores.entrySet()) {
            Double proximityScore = entry.getValue();
            if(proximityScore>=parameters.getMinWordOccurrence()) {
                String IDcombinationReverse = entry.getKey();
                
                String[] listOfWordIDsReverse = StringUtils.split(IDcombinationReverse, SEPARATOR);
                
                StringBuilder sb = new StringBuilder(listOfWordIDsReverse.length*6);
                for(int i=listOfWordIDsReverse.length-1;i>=0;--i) {
                    Integer ID = Integer.valueOf(listOfWordIDsReverse[i]);
                    sb.append(ID2word.get(ID)).append(" ");
                }
                
                if(sb.length()>0) {
                    keywordsMap.put(sb.toString().trim(), proximityScore);
                }
                
                //sb=null;
            }
        }
        //keywordProximityScores=null;
        
        return keywordsMap;
    }
    
    /**
     * Counts the number of occurrences of a keyword combination in the text.
     * 
     * @param keyword
     * @param text
     * @return 
     */
    public double numberOfOccurrences(String keyword, final String text) {        
        double points=0.0;
        

        AbstractTokenizer tokenizer = generateTokenizer();
        
        List<String> tmpKwd = tokenizer.tokenize(keyword);
        
        int numberOfWords=tmpKwd.size();
        if(numberOfWords==0) {
            return points;
        }

        List<String> tmpWords = tokenizer.tokenize(text);
        int n = tmpWords.size();
        
        Map<String, List<Integer>> word2Positions = new LinkedHashMap<>();
        for(int position=0;position<n;++position) {
            String word = tmpWords.get(position);
            if(tmpKwd.contains(word)==false) {
                continue;
            }
            
            if(word2Positions.containsKey(word)==false) {
                word2Positions.put(word, new ArrayList<>());
            }
            
            word2Positions.get(word).add(position);
        }
        
        

        while(!word2Positions.isEmpty()) {
            int extraWords=0;
            int wordsBetween=0;
            Integer previousKwdPosition = null;
            for(String kwd : tmpKwd) {
                List<Integer> positionList = word2Positions.get(kwd);
                if(positionList == null || positionList.isEmpty()) {
                    return points;
                }
                if(previousKwdPosition==null) {
                    Iterator<Integer> it = positionList.iterator();
                    previousKwdPosition=it.next();
                    it.remove();
                }
                else {
                    Integer closestPosition2Previous=null;
                    int minDistance= Integer.MAX_VALUE;
                    for(Integer position : positionList) {
                        int distance=position-previousKwdPosition;
                        if(distance>0 && distance<=minDistance) {
                            minDistance=distance;
                            closestPosition2Previous=position;
                        }
                    }

                    if(closestPosition2Previous==null) {
                        return points;
                    }
                    Integer currentKwdPosition=closestPosition2Previous;

                    wordsBetween+=currentKwdPosition-previousKwdPosition;

                    previousKwdPosition=currentKwdPosition;
                }

                if(word2Positions.get(kwd).isEmpty()) {
                    word2Positions.remove(kwd);
                }
            }
            
            extraWords+=wordsBetween-(numberOfWords-1);
            if(extraWords<=0) {
                ++points;
            }
            else {
                points+=0.5*extraWords;
            }
        }


        return points;
    }

    private Map<String, Integer> getCombinationsWithinWindow(Integer windowStart, int maxCombinations, 
            Map<Integer, String> ID2word, Map<Integer, Double> ID2occurrences, Map<Integer, Integer> position2ID, 
            int numberOfWordsInDoc) {
        int windowLength=Math.min(windowStart+parameters.getExaminationWindowLength(), numberOfWordsInDoc);
        
        //stores IDn_...ID2_ID1_=>words Between last word and windowStart
        Map<String, Integer> wordCombinations = new HashMap<>(); 
        wordCombinations.put(position2ID.get(windowStart).toString()+SEPARATOR, 0);
        
        boolean isfirstWordNumber = NumberUtils.isNumber(ID2word.get(position2ID.get(windowStart)));
        
        int maxDistanceBetweenKwds=parameters.getMaxDistanceBetweenKwds()+2; //the method below substracts the current minus the last occurence. As a result it counts also the first and last word and NOT the words between them. That is why we add 2 on the distance.
        
        String separator = String.valueOf(SEPARATOR);
        for(int i=windowStart+1;i<windowLength;++i) {
            Integer ID = position2ID.get(i);
            if(ID==null || useThisWord(ID, ID2word, ID2occurrences)==false || (isfirstWordNumber && NumberUtils.isNumber(ID2word.get(ID)))) {
                continue;
            }

            String tokenizedID = separator + ID + separator;

            Map<String, Integer> newWordCombinations = new HashMap<>();
            
            for(Map.Entry<String, Integer> entry : wordCombinations.entrySet()) {
                String IDcombinationReverse = entry.getKey();
                
                int numberOfWords=PHPMethods.substr_count(IDcombinationReverse, SEPARATOR);
                if(numberOfWords<maxCombinations) {
                    int wordsBetween=i-windowStart;

                    int extraWords=wordsBetween-numberOfWords;

                    if(extraWords>0 && (numberOfWords>parameters.getKeepFloatPointsUntilCombination() || 0.5*extraWords>parameters.getKeepFloatPointsAbove())) {
                        continue;
                    }
                    
                    Integer previousWordsBetween = entry.getValue();

                    if(wordsBetween-previousWordsBetween<maxDistanceBetweenKwds) {// the distance between the previous last word and the current last word.  
                        String IDofFirstWord = IDcombinationReverse.substring(0, IDcombinationReverse.indexOf(SEPARATOR));
                        if(i>(windowStart+previousWordsBetween+1) && ID2occurrences.get(Integer.valueOf(IDofFirstWord))==1) { //ignore keyword combinations that are not exactly next to the previous word and they occur only once in the text
                            continue;
                        }

                        IDcombinationReverse=SEPARATOR+IDcombinationReverse;
                        if(IDcombinationReverse.contains(tokenizedID)==false) {
                            newWordCombinations.put(ID+IDcombinationReverse, wordsBetween);
                        }
                    }
                }
            }
            
            //add the new combinations in the list if they don't exist already
            for(Map.Entry<String, Integer> entry : newWordCombinations.entrySet()) {
                String IDcombinationReverse = entry.getKey();
                
                if(!wordCombinations.containsKey(IDcombinationReverse)) {
                    wordCombinations.put(IDcombinationReverse, entry.getValue());
                }
            }
            
            
            //newWordCombinations=null;
        }
        
        
        
        return wordCombinations;
    }
    
    private boolean useThisWord(Integer wordID, Map<Integer, String> ID2word, Map<Integer, Double> ID2occurrences) {
        String word = ID2word.get(wordID);
        if(word==null) {
            return false;
        }
        else if(parameters.getMinWordLength()>1 && word.length() <parameters.getMinWordLength() && !NumberUtils.isNumber(word)) {
            return false;
        }
        else if(parameters.getMinWordOccurrence()>1 && ID2occurrences.get(wordID)<parameters.getMinWordOccurrence()) {
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
