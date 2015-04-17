/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
package com.datumbox.common.utilities;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Class that simulates PHP array functions
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class PHPfunctions {
    
    private static final Pattern LTRIM = Pattern.compile("^\\s+");
    private static final Pattern RTRIM = Pattern.compile("\\s+$");
    
    /**
     * Trims spaces on the left.
     * 
     * @param s
     * @return 
     */
    public static String ltrim(String s) {
        return LTRIM.matcher(s).replaceAll("");
    }
    
    /**
     * Trims spaces on the right.
     * 
     * @param s
     * @return 
     */
    public static String rtrim(String s) {
        return RTRIM.matcher(s).replaceAll("");
    }    
    
    /**
     * Count the number of substring occurrences.
     * 
     * @param string
     * @param substring
     * @return 
     */
    public static int substr_count(final String string, final String substring) {
        if(substring.length()==1) {
            return substr_count(string, substring.charAt(0));
        }
        
        int count = 0;
        int idx = 0;

        while ((idx = string.indexOf(substring, idx)) != -1) {
           ++idx;
           ++count;
        }

        return count;
    }
    
    public static int substr_count(final String string, final char character) {
        int count = 0;
        
        for(char c : string.toCharArray()) {
            if(c==character) {
                ++count;
            }
        }

        return count;
    }

    public static String preg_replace(String regex, String replacement, String subject) {
        Pattern p = Pattern.compile(regex);
        return preg_replace(p, replacement, subject);
    }

    public static String preg_replace(Pattern pattern, String replacement, String subject) {
        Matcher m = pattern.matcher(subject);
        StringBuffer sb = new StringBuffer(subject.length());
        while(m.find()){ 
            m.appendReplacement(sb, replacement);
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public static int preg_match(String regex, String subject) {
        Pattern p = Pattern.compile(regex);
        return preg_match(p, subject);
    }
    
    public static int preg_match(Pattern pattern, String subject) {
        int matches=0;
        Matcher m = pattern.matcher(subject);
        while(m.find()){ 
            ++matches;
        }
        return matches;
    }
    
    public static double round(double d, int i) {
        double multiplier = Math.pow(10, i);
        return Math.round(d*multiplier)/multiplier;
    }
    
    public static double log(double d, double base) {
        if(base==1.0 || base<=0.0) {
            throw new RuntimeException("Invalid base for logarithm");
        }
        return Math.log(d)/Math.log(base);
    }
    
    /**
     * Returns a random positive integer
     * 
     * @return 
     */
    public static int mt_rand() {
        return PHPfunctions.mt_rand(0,Integer.MAX_VALUE);
    }
    
    /**
     * Returns a random integer between min and max
     * 
     * @param min
     * @param max
     * @return 
     */
    public static int mt_rand(int min, int max) {
        return min + (int)(RandomGenerator.getThreadLocalRandom().nextDouble() * ((max - min) + 1));
    }
    
    
    /**
     * Returns a random double between min and max
     * 
     * @param min
     * @param max
     * @return 
     */
    public static double mt_rand(double min, double max) {
        return min + (RandomGenerator.getThreadLocalRandom().nextDouble() * (max - min));
    }
    
    /**
     * Flip key and values of a map.
     * 
     * @param <K>
     * @param <V>
     * @param map
     * @return 
     */
    public static <K,V> Map<V,K> array_flip(Map<K,V> map) {
        Map<V,K> flipped = new HashMap<>();
        for(Map.Entry<K,V> entry : map.entrySet()) {
            flipped.put(entry.getValue(), entry.getKey());
        }
        return flipped;
    }
    
    /**
     * Shuffles the array values
     * 
     * @param <T>
     * @param array 
     */
    public static <T> void shuffle(T[] array) {
        //Implementing Fisher-Yates shuffle
        T tmp;
        Random rnd = RandomGenerator.getThreadLocalRandom();
        for (int i = array.length - 1; i > 0; --i) {
            int index = rnd.nextInt(i + 1);
            
            tmp = array[index];
            array[index] = array[i];
            array[i] = tmp;
        }
    }
    
    public static <T> String print_r(T object) {
        return ToStringBuilder.reflectionToString(object);
    }
    
    /**
     * Sorts array and returns the original index order
     * 
     * @param <T>
     * @param array
     * @return 
     */
    public static <T extends Comparable<T>> Integer[] asort(T[] array) {
        //sort the indexes first
        ArrayIndexComparator<T> comparator = new ArrayIndexComparator<>(array);
        Integer[] indexes = comparator.createIndexArray();
        Arrays.sort(indexes, comparator);
        
        //sort the array based on the indexes
        //sortArrayBasedOnIndex(array, indexes);
        Arrays.sort(array);
        
        return indexes;
    }
    
    /**
     * Sorts array in descending order and returns the original index order
     * 
     * @param <T>
     * @param array
     * @return 
     */
    public static <T extends Comparable<T>> Integer[] arsort(T[] array) {
        //sort the indexes first
        ArrayIndexReverseComparator<T> comparator = new ArrayIndexReverseComparator<>(array);
        Integer[] indexes = comparator.createIndexArray();
        Arrays.sort(indexes, comparator);
        
        //sort the array based on the indexes
        //sortArrayBasedOnIndex(array, indexes);
        Arrays.sort(array,Collections.reverseOrder());
        
        return indexes;
    }
    /*
    private static <T extends Comparable<T>> void sortArrayBasedOnIndex(T[] array, Integer[] indexes) {
        T[] arrayBackup =  array.clone();
        for(int i=0;i<indexes.length;++i) {
            array[i] = arrayBackup[indexes[i]];
        }
    }
    */
}
