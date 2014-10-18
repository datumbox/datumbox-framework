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
package com.datumbox.framework.utilities.text.extractors;

import com.datumbox.common.utilities.PHPfunctions;
import com.datumbox.framework.utilities.text.tokenizers.Tokenizer;
import com.datumbox.framework.utilities.text.tokenizers.WhitespaceTokenizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.math.NumberUtils;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class NgramsExtractor extends TextExtractor<NgramsExtractor.Parameters, String, Double> {
    
    public static class Parameters extends TextExtractor.Parameters {           
        private int maxCombinations=3;
        
        private int minWordLength=1;
        private int minWordOccurrence=1;
        
        private int examinationWindowLength=15; //15 words later
        private int maxDistanceBetweenKwds=4; //up to 4 words distance between two words of the same keyword
        
        private int keepFloatPointsUntilCombination=3; //After 3 word combinations ignore the float points to reduce memory and improve speed
        private double keepFloatPointsAbove=0.1; //ignore all floating point scores less than 0.1 to reduce memory and improve speed
        
        private Class<? extends Tokenizer> tokenizer = WhitespaceTokenizer.class;

        public int getMaxCombinations() {
            return maxCombinations;
        }

        public void setMaxCombinations(int maxCombinations) {
            this.maxCombinations = maxCombinations;
        }

        public int getMinWordLength() {
            return minWordLength;
        }

        public void setMinWordLength(int minWordLength) {
            this.minWordLength = minWordLength;
        }

        public int getMinWordOccurrence() {
            return minWordOccurrence;
        }

        public void setMinWordOccurrence(int minWordOccurrence) {
            this.minWordOccurrence = minWordOccurrence;
        }

        public int getExaminationWindowLength() {
            return examinationWindowLength;
        }

        public void setExaminationWindowLength(int examinationWindowLength) {
            this.examinationWindowLength = examinationWindowLength;
        }

        public int getMaxDistanceBetweenKwds() {
            return maxDistanceBetweenKwds;
        }

        public void setMaxDistanceBetweenKwds(int maxDistanceBetweenKwds) {
            this.maxDistanceBetweenKwds = maxDistanceBetweenKwds;
        }

        public int getKeepFloatPointsUntilCombination() {
            return keepFloatPointsUntilCombination;
        }

        public void setKeepFloatPointsUntilCombination(int keepFloatPointsUntilCombination) {
            this.keepFloatPointsUntilCombination = keepFloatPointsUntilCombination;
        }

        public double getKeepFloatPointsAbove() {
            return keepFloatPointsAbove;
        }

        public void setKeepFloatPointsAbove(double keepFloatPointsAbove) {
            this.keepFloatPointsAbove = keepFloatPointsAbove;
        }

        public Class<? extends Tokenizer> getTokenizer() {
            return tokenizer;
        }

        public void setTokenizer(Class<? extends Tokenizer> tokenizer) {
            this.tokenizer = tokenizer;
        }
    }
    
    protected static final String SEPARATOR = "_";
        
    
    private Map<Integer, String> ID2word; //ID=>Kwd
    private Map<Integer, Double> ID2occurrences; //ID=>counts/scores
    private Map<Integer, Integer> position2ID; //word position=>ID

    private Integer numberOfWordsInDoc;
        
    @Override
    public Map<String, Double> extract(final String text) {
        //initialize/reset the protected variables
        ID2word = new HashMap<>();
        ID2occurrences = new HashMap<>();
        position2ID = new LinkedHashMap<>(); //maintain the order of insertation
        numberOfWordsInDoc = 0;
        
        
        buildInternalArrays(text);
        
        Map<String, Double> keywordProximityScores = new HashMap<>();
        
        //move the "window" across the document by 1 word at each time
        for(Map.Entry<Integer, Integer> entry : position2ID.entrySet()) {
            Integer wordID = entry.getValue();
            if(!useThisWord(wordID)) {
                continue;
            }
            
            Integer position = entry.getKey();
            
            Map<String, Integer> wordCombinations = getCombinationsWithinWindow(position, parameters.getMaxCombinations());
            
            //translate positions to proximity metrics
            for(Map.Entry<String, Integer> entry2 : wordCombinations.entrySet()) {
                String IDcombinationReverse = entry2.getKey();
                Integer wordsBetween = entry2.getValue();
                
                int numberOfWords = PHPfunctions.substr_count(IDcombinationReverse, SEPARATOR)-1;//starts enumeration from 0. We need to subtract one because at the end each string has an extra SEPARATOR
                
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
            
            
            wordCombinations = null;
        }
        
        //initialize keyword map
        Map<String, Double> keywordsMap = new HashMap<>();
        for(Map.Entry<String, Double> entry : keywordProximityScores.entrySet()) {
            Double proximityScore = entry.getValue();
            if(proximityScore>=parameters.getMinWordOccurrence()) {
                String IDcombinationReverse = entry.getKey();
                
                String[] listOfWordIDsReverse = IDcombinationReverse.split(SEPARATOR);
                
                StringBuilder sb = new StringBuilder();
                for(int i=listOfWordIDsReverse.length-1;i>=0;--i) {
                    Integer ID = Integer.valueOf(listOfWordIDsReverse[i]);
                    sb.append(ID2word.get(ID)).append(" ");
                }
                
                if(sb.length()>0) {
                    keywordsMap.put(sb.toString().trim(), proximityScore);
                }
                
                sb=null;
            }
        }
        keywordProximityScores=null;
        
        return keywordsMap;
    }
 
    public double numberOfOccurrences(String keyword, final String text) {
        if(parameters==null) {
            parameters = new Parameters();
        }
        
        double points=0.0;
        

        Tokenizer tokenizer = null;
        try {
            tokenizer = parameters.getTokenizer().newInstance();
        } 
        catch (InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
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

    protected Map<String, Integer> getCombinationsWithinWindow(Integer windowStart, int maxCombinations) {
        int windowLength=Math.min(windowStart+parameters.getExaminationWindowLength(), numberOfWordsInDoc);
        
        //stores IDn_...ID2_ID1_=>words Between last word and windowStart
        Map<String, Integer> wordCombinations = new HashMap<>(); 
        wordCombinations.put(position2ID.get(windowStart).toString()+SEPARATOR, 0);
        
        boolean isfirstWordNumber = NumberUtils.isNumber(ID2word.get(position2ID.get(windowStart)));
        
        int maxDistanceBetweenKwds=parameters.getMaxDistanceBetweenKwds()+2; //the method below substracts the current minus the last occurence. As a result it counts also the first and last word and NOT the words between them. That is why we add 2 on the distance.
        
        
        for(int i=windowStart+1;i<windowLength;++i) {
            Integer ID = position2ID.get(i);
            if(ID==null || useThisWord(ID)==false || (isfirstWordNumber && NumberUtils.isNumber(ID2word.get(ID)))) {
                continue;
            }

            String tokenizedID = SEPARATOR + ID + SEPARATOR;

            Map<String, Integer> newWordCombinations = new HashMap<>();
            
            for(Map.Entry<String, Integer> entry : wordCombinations.entrySet()) {
                String IDcombinationReverse = entry.getKey();
                
                int numberOfWords=PHPfunctions.substr_count(IDcombinationReverse, SEPARATOR);
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
            
            
            newWordCombinations=null;
        }
        
        
        
        return wordCombinations;
    }
    
    protected boolean useThisWord(Integer wordID) {
        String word = ID2word.get(wordID);
        if(word==null) {
            return false;
        }
        if(parameters.getMinWordLength()>1 && word.length() <parameters.getMinWordLength() && !NumberUtils.isNumber(word)) {
            return false;
        }
        if(parameters.getMinWordOccurrence()>1 && ID2occurrences.get(wordID)<parameters.getMinWordOccurrence()) {
            return false;
        }
        return true;
    }
    
    protected void buildInternalArrays(final String text) {
        
        Map<String, Integer> word2ID = new HashMap<>();
        
        Tokenizer tokenizer = null;
        try {
            tokenizer = parameters.getTokenizer().newInstance();
        } 
        catch (InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
        List<String> keywordList = tokenizer.tokenize(text);
        
        int lastId=-1;
        numberOfWordsInDoc = keywordList.size();
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
        
        keywordList = null;

        word2ID = null;
    }
   
}
