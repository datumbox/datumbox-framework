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
package com.datumbox.framework.core.common.utilities;

import com.datumbox.framework.common.utilities.RandomGenerator;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class contains a number of convenience methods which have an API similar
 * to PHP functions and their are implemented in Java.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class PHPMethods {
    
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
    
    /**
     * Count the number of times a character appears in the string.
     * 
     * @param string
     * @param character
     * @return 
     */
    public static int substr_count(final String string, final char character) {
        int count = 0;
        int n = string.length();
        for(int i=0;i<n;i++) {
            if(string.charAt(i)==character) {
                ++count;
            }
        }

        return count;
    }
    
    /**
     * Matches a string with a regex and replaces the matched components with 
     * a provided string.
     * 
     * @param regex
     * @param replacement
     * @param subject
     * @return 
     */
    public static String preg_replace(String regex, String replacement, String subject) {
        Pattern p = Pattern.compile(regex);
        return preg_replace(p, replacement, subject);
    }
    
    /**
     * Matches a string with a pattern and replaces the matched components with 
     * a provided string.
     * 
     * @param pattern
     * @param replacement
     * @param subject
     * @return 
     */
    public static String preg_replace(Pattern pattern, String replacement, String subject) {
        Matcher m = pattern.matcher(subject);
        StringBuffer sb = new StringBuffer(subject.length());
        while(m.find()){ 
            m.appendReplacement(sb, replacement);
        }
        m.appendTail(sb);
        return sb.toString();
    }
    
    /**
     * Matches a string with a regex.
     * 
     * @param regex
     * @param subject
     * @return 
     */
    public static int preg_match(String regex, String subject) {
        Pattern p = Pattern.compile(regex);
        return preg_match(p, subject);
    }
    
    /**
     * Matches a string with a pattern.
     * 
     * @param pattern
     * @param subject
     * @return 
     */
    public static int preg_match(Pattern pattern, String subject) {
        int matches=0;
        Matcher m = pattern.matcher(subject);
        while(m.find()){ 
            ++matches;
        }
        return matches;
    }
    
    /**
     * Rounds a number to a specified precision.
     * 
     * @param d
     * @param i
     * @return 
     */
    public static double round(double d, int i) {
        double multiplier = Math.pow(10, i);
        return Math.round(d*multiplier)/multiplier;
    }
    
    /**
     * Returns the logarithm of a number at an arbitrary base.
     * 
     * @param d
     * @param base
     * @return 
     */
    public static double log(double d, double base) {
        if(base==1.0 || base<=0.0) {
            throw new IllegalArgumentException("Invalid base for logarithm.");
        }
        return Math.log(d)/Math.log(base);
    }
    
    /**
     * Returns a random positive integer
     * 
     * @return 
     */
    public static int mt_rand() {
        return PHPMethods.mt_rand(0,Integer.MAX_VALUE);
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
     * It flips the key and values of a map.
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
     * Shuffles the values of any array in place.
     *
     * @param <T>
     * @param array
     */
    public static <T> void shuffle(T[] array) {
        shuffle(array, RandomGenerator.getThreadLocalRandom());
    }
    
    /**
     * Shuffles the values of any array in place using the provided random generator.
     * 
     * @param <T>
     * @param array
     * @param rnd
     */
    public static <T> void shuffle(T[] array, Random rnd) {
        //Implementing Fisher-Yates shuffle
        T tmp;
        for (int i = array.length - 1; i > 0; --i) {
            int index = rnd.nextInt(i + 1);
            
            tmp = array[index];
            array[index] = array[i];
            array[i] = tmp;
        }
    }
    
    /**
     * Sorts an array in ascending order and returns an array with indexes of 
     * the original order.
     * 
     * @param <T>
     * @param array
     * @return 
     */
    public static <T extends Comparable<T>> Integer[] asort(T[] array) {
        return _asort(array, false);
    }
    
    /**
     * Sorts an array in descending order and returns an array with indexes of 
     * the original order.
     * 
     * @param <T>
     * @param array
     * @return 
     */
    public static <T extends Comparable<T>> Integer[] arsort(T[] array) {
        return _asort(array, true);
    }
    
    private static <T extends Comparable<T>> Integer[] _asort(T[] array, boolean reverse) {
        //create an array with the indexes
        Integer[] indexes = new Integer[array.length];
        for (int i = 0; i < array.length; ++i) {
            indexes[i] = i;
        }
        
        //sort the indexes first
        Comparator<Integer> c = (Integer index1, Integer index2) -> array[index1].compareTo(array[index2]);
        c = reverse?Collections.reverseOrder(c):c;
        Arrays.sort(indexes, c);
        
        //rearrenage the array based on the order of indexes
        arrangeByIndex(array, indexes);
        
        return indexes;
    }
    
    /**
     * Rearranges the array based on the order of the provided indexes.
     * 
     * @param <T>
     * @param array
     * @param indexes 
     */
    public static <T> void arrangeByIndex(T[] array, Integer[] indexes) {
        if(array.length != indexes.length) {
            throw new IllegalArgumentException("The length of the two arrays must match.");
        }
        
        //sort the array based on the indexes
        for(int i=0;i<array.length;i++) {
            int index = indexes[i];
            
            //swap
            T tmp = array[i];
            array[i] = array[index];
            array[index] = tmp;
        }
    }
    
    /**
     * Copies the elements of double array.
     * 
     * @param a
     * @return 
     */
    public static double[] array_clone(double[] a) {
        if(a == null) {
            return a;
        }
        return Arrays.copyOf(a, a.length);
    }
    
    /**
     * Copies the elements of double 2D array.
     * 
     * @param a
     * @return 
     */
    public static double[][] array_clone(double[][] a) {
        if(a == null) {
            return a;
        }
        double[][] copy = new double[a.length][];
        for(int i=0;i<a.length;i++) {
            copy[i] = Arrays.copyOf(a[i], a[i].length);
        }
        return copy;
    }
    
}
