/**
 * Copyright (C) 2013-2017 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
package com.datumbox.framework.core.common;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.dataobjects.AssociativeArray;
import com.datumbox.framework.common.dataobjects.TypeInference;
import com.datumbox.framework.common.utilities.PHPMethods;
import com.datumbox.framework.common.utilities.RandomGenerator;
import com.datumbox.framework.core.common.dataobjects.Dataframe;
import com.datumbox.framework.core.common.dataobjects.Record;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;


/**
 * This class contains a series of different Datasets which can be used for 
 * testing the accuracy of the algorithms.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class Datasets {
    
    /**
     * Generates a new Record from an Array.
     * 
     * @param <T>
     * @param xArray
     * @param y
     * @return 
     */
    private static <T> Record newDataVector(T[] xArray, Object y) {
        AssociativeArray x = new AssociativeArray();
        for(int i=0;i<xArray.length;++i) {
            x.put(i, xArray[i]);
        }
        return new Record(x, y);
    }
    
    /**
     * Cars Numeric Dataframe.
     * 
     * @param configuration
     * @return 
     */
    public static Dataframe[] carsNumeric(Configuration configuration) {
        /*
        Example from http://www.inf.u-szeged.hu/~ormandi/ai2/06-naiveBayes-example.pdf
        FeatureList: 
            - 0: red
            - 1: yellow
            - 2: sports
            - 3: suv
            - 4: domestic
            - 5: imported
            - c1: yes
            - c2: no
        */
        Dataframe trainingData;
        try (Reader fileReader = new InputStreamReader(Datasets.class.getClassLoader().getResourceAsStream("datasets/carsNumeric.csv"), StandardCharsets.UTF_8)) {
            LinkedHashMap<String, TypeInference.DataType> headerDataTypes = new LinkedHashMap<>(); 
            headerDataTypes.put("red", TypeInference.DataType.BOOLEAN);
            headerDataTypes.put("yellow", TypeInference.DataType.BOOLEAN);
            headerDataTypes.put("sports", TypeInference.DataType.BOOLEAN);
            headerDataTypes.put("suv", TypeInference.DataType.BOOLEAN);
            headerDataTypes.put("domestic", TypeInference.DataType.BOOLEAN);
            headerDataTypes.put("imported", TypeInference.DataType.BOOLEAN);
            headerDataTypes.put("stolen", TypeInference.DataType.BOOLEAN);
            
            trainingData = Dataframe.Builder.parseCSVFile(fileReader, "stolen", headerDataTypes, ',', '"', "\r\n", null, null, configuration);
        }
        catch(IOException ex) {
            throw new UncheckedIOException(ex);
        }
        
        
        Dataframe validationData = new Dataframe(configuration);
        AssociativeArray xData = new AssociativeArray();
        xData.put("red", true);
        xData.put("yellow", false);
        xData.put("sports", false);
        xData.put("suv", true);
        xData.put("domestic", true);
        xData.put("imported", false);
        validationData.add(new Record(xData, false));
        
        return new Dataframe[] {trainingData, validationData};
    }
    
    /**
     * Cars Categorical Dataframe.
     * 
     * @param configuration
     * @return 
     */
    public static Dataframe[] carsCategorical(Configuration configuration) {
        /*
        Example from http://www.inf.u-szeged.hu/~ormandi/ai2/06-naiveBayes-example.pdf
        FeatureList: 
            - color: red/yellow
            - type: sports/suv
            - origin: domestic/imported
            - stolen: yes/no
        */
        Dataframe trainingData;
        try (Reader fileReader = new InputStreamReader(Datasets.class.getClassLoader().getResourceAsStream("datasets/carsCategorical.csv"), StandardCharsets.UTF_8)) {
            LinkedHashMap<String, TypeInference.DataType> headerDataTypes = new LinkedHashMap<>(); 
            headerDataTypes.put("color", TypeInference.DataType.CATEGORICAL);
            headerDataTypes.put("type", TypeInference.DataType.CATEGORICAL);
            headerDataTypes.put("origin", TypeInference.DataType.CATEGORICAL);
            headerDataTypes.put("stolen", TypeInference.DataType.CATEGORICAL);
            
            trainingData = Dataframe.Builder.parseCSVFile(fileReader, "stolen", headerDataTypes, ',', '"', "\r\n", null, null, configuration);
        }
        catch(IOException ex) {
            throw new UncheckedIOException(ex);
        }
        
        Dataframe validationData = new Dataframe(configuration);
        AssociativeArray xData = new AssociativeArray();
        xData.put("color", "red");
        xData.put("type", "suv");
        xData.put("stolen", "domestic");
        validationData.add(new Record(xData, "no"));
        
        return new Dataframe[] {trainingData, validationData};
    }

    /**
     * Housing numerical Dataframe.
     *
     * @param configuration
     * @return
     */
    public static Dataframe[] housingNumerical(Configuration configuration) {
        //Data from https://archive.ics.uci.edu/ml/machine-learning-databases/housing/housing.names
        Dataframe trainingData;
        try (Reader fileReader = new InputStreamReader(Datasets.class.getClassLoader().getResourceAsStream("datasets/housing.csv"), StandardCharsets.UTF_8)) {
            LinkedHashMap<String, TypeInference.DataType> headerDataTypes = new LinkedHashMap<>();
            headerDataTypes.put("CRIM", TypeInference.DataType.NUMERICAL);
            headerDataTypes.put("ZN", TypeInference.DataType.NUMERICAL);
            headerDataTypes.put("INDUS", TypeInference.DataType.NUMERICAL);
            headerDataTypes.put("CHAS", TypeInference.DataType.BOOLEAN);
            headerDataTypes.put("NOX", TypeInference.DataType.NUMERICAL);
            headerDataTypes.put("RM", TypeInference.DataType.NUMERICAL);
            headerDataTypes.put("AGE", TypeInference.DataType.NUMERICAL);
            headerDataTypes.put("DIS", TypeInference.DataType.NUMERICAL);
            headerDataTypes.put("RAD", TypeInference.DataType.ORDINAL);
            headerDataTypes.put("TAX", TypeInference.DataType.NUMERICAL);
            headerDataTypes.put("PTRATIO", TypeInference.DataType.NUMERICAL);
            headerDataTypes.put("B", TypeInference.DataType.NUMERICAL);
            headerDataTypes.put("LSTAT", TypeInference.DataType.NUMERICAL);
            headerDataTypes.put("MEDV", TypeInference.DataType.NUMERICAL);

            trainingData = Dataframe.Builder.parseCSVFile(fileReader, "MEDV", headerDataTypes, ',', '"', "\r\n", null, null, configuration);
        }
        catch(IOException ex) {
            throw new UncheckedIOException(ex);
        }

        Dataframe validationData = trainingData.copy();

        return new Dataframe[] {trainingData, validationData};
    }
    
    /**
     * Wines Ordinal Dataframe.
     * 
     * @param configuration
     * @return 
     */
    public static Dataframe[] winesOrdinal(Configuration configuration) {
        //Data from http://www.unt.edu/rss/class/Jon/R_SC/
        Dataframe trainingData;
        try (Reader fileReader = new InputStreamReader(Datasets.class.getClassLoader().getResourceAsStream("datasets/winesOrdinal.csv"), StandardCharsets.UTF_8)) {
            LinkedHashMap<String, TypeInference.DataType> headerDataTypes = new LinkedHashMap<>(); 
            headerDataTypes.put("c1", TypeInference.DataType.NUMERICAL);
            headerDataTypes.put("c2", TypeInference.DataType.NUMERICAL);
            headerDataTypes.put("c3", TypeInference.DataType.NUMERICAL);
            headerDataTypes.put("class", TypeInference.DataType.ORDINAL);
            
            trainingData = Dataframe.Builder.parseCSVFile(fileReader, "class", headerDataTypes, ',', '"', "\r\n", null, null, configuration);
        }
        catch(IOException ex) {
            throw new UncheckedIOException(ex);
        }
        
        
        Dataframe validationData = new Dataframe(configuration);
        AssociativeArray xData1 = new AssociativeArray();
        xData1.put("c1", 5.92085126899850);
        xData1.put("c2", 6.01037072456601);
        xData1.put("c3", 4.66307928268761);
        validationData.add(new Record(xData1, (short)1));
        AssociativeArray xData2 = new AssociativeArray();
        xData2.put("c1", 7.18606367787857);
        xData2.put("c2", 6.64194264491917);
        xData2.put("c3", 4.41233885708698);
        validationData.add(new Record(xData2, (short)2));
        AssociativeArray xData3 = new AssociativeArray();
        xData3.put("c1", 7.83232073356316);
        xData3.put("c2", 8.76007761528955);
        xData3.put("c3", 7.05235518409310);
        validationData.add(new Record(xData3, (short)3));
        
        return new Dataframe[] {trainingData, validationData};
    }
    
    /**
     * Gaussian Clusters Dataframe.
     * 
     * @param configuration
     * @return 
     */
    public static Dataframe[] gaussianClusters(Configuration configuration) {
        Dataframe trainingData = new Dataframe(configuration);
        int observationsPerCluster = 50;
        Random rnd = RandomGenerator.getThreadLocalRandom();
        for(int i=0;i<observationsPerCluster;++i) {
            trainingData.add(Datasets.<Object>newDataVector(new Object[] {rnd.nextGaussian(),rnd.nextGaussian()}, "c1"));
        }
        
        for(int i=0;i<observationsPerCluster;++i) {
            trainingData.add(Datasets.<Object>newDataVector(new Object[] {100+rnd.nextGaussian(),50+rnd.nextGaussian()}, "c2"));
        }
        
        for(int i=0;i<observationsPerCluster;++i) {
            trainingData.add(Datasets.<Object>newDataVector(new Object[] {50+rnd.nextGaussian(),100+rnd.nextGaussian()}, "c3"));
        }
        
        Dataframe validationData = trainingData.copy();
        
        return new Dataframe[] {trainingData, validationData};
    }
    
    /**
     * Multinomial Clusters Dataframe.
     * 
     * @param configuration
     * @return 
     */
    public static Dataframe[] multinomialClusters(Configuration configuration) {
        Dataframe trainingData = new Dataframe(configuration);
        //cluster 1
        trainingData.add(Datasets.<Object>newDataVector(new Object[] {10.0,13.0, 5.0,6.0,5.0,4.0, 0.0,0.0,0.0,0.0}, "c1"));
        trainingData.add(Datasets.<Object>newDataVector(new Object[] {11.0,11.0, 6.0,7.0,7.0,3.0, 0.0,0.0,1.0,0.0}, "c1"));
        trainingData.add(Datasets.<Object>newDataVector(new Object[] {12.0,12.0, 10.0,16.0,4.0,6.0, 0.0,0.0,0.0,2.0}, "c1"));
        trainingData.add(Datasets.<Object>newDataVector(new Object[] {10.0,13.0, 5.0,6.0,5.0,4.0, 0.0,0.0,0.0,0.0}, "c1"));
        trainingData.add(Datasets.<Object>newDataVector(new Object[] {11.0,11.0, 6.0,7.0,7.0,3.0, 0.0,0.0,1.0,0.0}, "c1"));
        trainingData.add(Datasets.<Object>newDataVector(new Object[] {12.0,12.0, 10.0,16.0,4.0,6.0, 0.0,0.0,0.0,2.0}, "c1"));
        trainingData.add(Datasets.<Object>newDataVector(new Object[] {10.0,13.0, 5.0,6.0,5.0,4.0, 0.0,0.0,0.0,0.0}, "c1"));
        trainingData.add(Datasets.<Object>newDataVector(new Object[] {11.0,11.0, 6.0,7.0,7.0,3.0, 0.0,0.0,1.0,0.0}, "c1"));
        trainingData.add(Datasets.<Object>newDataVector(new Object[] {12.0,12.0, 10.0,16.0,4.0,6.0, 0.0,0.0,0.0,2.0}, "c1"));
        trainingData.add(Datasets.<Object>newDataVector(new Object[] {10.0,13.0, 5.0,6.0,5.0,4.0, 0.0,0.0,0.0,0.0}, "c1"));
        trainingData.add(Datasets.<Object>newDataVector(new Object[] {11.0,11.0, 6.0,7.0,7.0,3.0, 0.0,0.0,1.0,0.0}, "c1"));
        trainingData.add(Datasets.<Object>newDataVector(new Object[] {12.0,12.0, 10.0,16.0,4.0,6.0, 0.0,0.0,0.0,2.0}, "c1"));
        //cluster 2
        trainingData.add(Datasets.<Object>newDataVector(new Object[] {10.0,13.0, 0.0,0.0,0.0,0.0, 5.0,6.0,5.0,4.0}, "c2"));
        trainingData.add(Datasets.<Object>newDataVector(new Object[] {11.0,11.0, 0.0,0.0,1.0,0.0, 6.0,7.0,7.0,3.0}, "c2"));
        trainingData.add(Datasets.<Object>newDataVector(new Object[] {12.0,12.0, 0.0,0.0,0.0,2.0, 10.0,16.0,4.0,6.0}, "c2"));
        trainingData.add(Datasets.<Object>newDataVector(new Object[] {10.0,13.0, 0.0,0.0,0.0,0.0, 5.0,6.0,5.0,4.0}, "c2"));
        trainingData.add(Datasets.<Object>newDataVector(new Object[] {11.0,11.0, 0.0,0.0,1.0,0.0, 6.0,7.0,7.0,3.0}, "c2"));
        trainingData.add(Datasets.<Object>newDataVector(new Object[] {12.0,12.0, 0.0,0.0,0.0,2.0, 10.0,16.0,4.0,6.0}, "c2"));
        trainingData.add(Datasets.<Object>newDataVector(new Object[] {10.0,13.0, 0.0,0.0,0.0,0.0, 5.0,6.0,5.0,4.0}, "c2"));
        trainingData.add(Datasets.<Object>newDataVector(new Object[] {11.0,11.0, 0.0,0.0,1.0,0.0, 6.0,7.0,7.0,3.0}, "c2"));
        trainingData.add(Datasets.<Object>newDataVector(new Object[] {12.0,12.0, 0.0,0.0,0.0,2.0, 10.0,16.0,4.0,6.0}, "c2"));
        trainingData.add(Datasets.<Object>newDataVector(new Object[] {10.0,13.0, 0.0,0.0,0.0,0.0, 5.0,6.0,5.0,4.0}, "c2"));
        trainingData.add(Datasets.<Object>newDataVector(new Object[] {11.0,11.0, 0.0,0.0,1.0,0.0, 6.0,7.0,7.0,3.0}, "c2"));
        trainingData.add(Datasets.<Object>newDataVector(new Object[] {12.0,12.0, 0.0,0.0,0.0,2.0, 10.0,16.0,4.0,6.0}, "c2"));
        //cluster 3
        trainingData.add(Datasets.<Object>newDataVector(new Object[] {10.0,13.0, 5.0,6.0,5.0,4.0, 5.0,6.0,5.0,4.0}, "c3"));
        trainingData.add(Datasets.<Object>newDataVector(new Object[] {11.0,11.0, 6.0,7.0,7.0,3.0, 6.0,7.0,7.0,3.0}, "c3"));
        trainingData.add(Datasets.<Object>newDataVector(new Object[] {12.0,12.0, 10.0,16.0,4.0,6.0, 10.0,16.0,4.0,6.0}, "c3"));
        trainingData.add(Datasets.<Object>newDataVector(new Object[] {10.0,13.0, 5.0,6.0,5.0,4.0, 5.0,6.0,5.0,4.0}, "c3"));
        trainingData.add(Datasets.<Object>newDataVector(new Object[] {11.0,11.0, 6.0,7.0,7.0,3.0, 6.0,7.0,7.0,3.0}, "c3"));
        trainingData.add(Datasets.<Object>newDataVector(new Object[] {12.0,12.0, 10.0,16.0,4.0,6.0, 10.0,16.0,4.0,6.0}, "c3"));
        trainingData.add(Datasets.<Object>newDataVector(new Object[] {10.0,13.0, 5.0,6.0,5.0,4.0, 5.0,6.0,5.0,4.0}, "c3"));
        trainingData.add(Datasets.<Object>newDataVector(new Object[] {11.0,11.0, 6.0,7.0,7.0,3.0, 6.0,7.0,7.0,3.0}, "c3"));
        trainingData.add(Datasets.<Object>newDataVector(new Object[] {12.0,12.0, 10.0,16.0,4.0,6.0, 10.0,16.0,4.0,6.0}, "c3"));
        trainingData.add(Datasets.<Object>newDataVector(new Object[] {10.0,13.0, 5.0,6.0,5.0,4.0, 5.0,6.0,5.0,4.0}, "c3"));
        trainingData.add(Datasets.<Object>newDataVector(new Object[] {11.0,11.0, 6.0,7.0,7.0,3.0, 6.0,7.0,7.0,3.0}, "c3"));
        trainingData.add(Datasets.<Object>newDataVector(new Object[] {12.0,12.0, 10.0,16.0,4.0,6.0, 10.0,16.0,4.0,6.0}, "c3"));
        
        
        Dataframe validationData = trainingData.copy();
        
        return new Dataframe[] {trainingData, validationData};
    }
    
    /**
     * Heart Disease Clusters Dataframe.
     * 
     * @param configuration
     * @return 
     */
    public static Dataframe[] heartDiseaseClusters(Configuration configuration) {
        //Heart Disease - C2: Age, Sex, ChestPain, RestBP, Cholesterol, BloodSugar, ECG, MaxHeartRate, Angina, OldPeak, STSlope, Vessels, Thal
        //http://www.sgi.com/tech/mlc/db/heart.names
        Dataframe trainingData;
        try (Reader fileReader = new InputStreamReader(Datasets.class.getClassLoader().getResourceAsStream("datasets/heart.csv"), StandardCharsets.UTF_8)) {
            LinkedHashMap<String, TypeInference.DataType> headerDataTypes = new LinkedHashMap<>(); 
            headerDataTypes.put("Age", TypeInference.DataType.NUMERICAL);
            headerDataTypes.put("Sex", TypeInference.DataType.CATEGORICAL);
            headerDataTypes.put("ChestPain", TypeInference.DataType.ORDINAL); 
            headerDataTypes.put("RestBP", TypeInference.DataType.NUMERICAL);
            headerDataTypes.put("Cholesterol", TypeInference.DataType.NUMERICAL);
            headerDataTypes.put("BloodSugar", TypeInference.DataType.BOOLEAN); 
            headerDataTypes.put("ECG", TypeInference.DataType.CATEGORICAL);
            headerDataTypes.put("MaxHeartRate", TypeInference.DataType.NUMERICAL);
            headerDataTypes.put("Angina", TypeInference.DataType.BOOLEAN); 
            headerDataTypes.put("OldPeak", TypeInference.DataType.NUMERICAL);
            headerDataTypes.put("STSlope", TypeInference.DataType.NUMERICAL);
            headerDataTypes.put("Vessels", TypeInference.DataType.NUMERICAL);
            headerDataTypes.put("Thal", TypeInference.DataType.CATEGORICAL);
            headerDataTypes.put("Status", TypeInference.DataType.CATEGORICAL);
            
            trainingData = Dataframe.Builder.parseCSVFile(fileReader, "Status", headerDataTypes, ',', '"', "\r\n", null, null, configuration);
        }
        catch(IOException ex) {
            throw new UncheckedIOException(ex);
        }       
        
        
        Dataframe validationData = new Dataframe(configuration);
        AssociativeArray xData1 = new AssociativeArray();
        xData1.put("Age", 51);
        xData1.put("Sex", "M");
        xData1.put("ChestPain", (short)3);
        xData1.put("RestBP", 100);
        xData1.put("Cholesterol", 222);
        xData1.put("BloodSugar", false);
        xData1.put("ECG", "0");
        xData1.put("MaxHeartRate", 143);
        xData1.put("Angina", true);
        xData1.put("OldPeak", 1.2);
        xData1.put("STSlope", 2);
        xData1.put("Vessels", 0);
        xData1.put("Thal", "3");
        validationData.add(new Record(xData1, "healthy"));
        AssociativeArray xData2 = new AssociativeArray();
        xData2.put("Age", 67);
        xData2.put("Sex", "M");
        xData2.put("ChestPain", (short)4);
        xData2.put("RestBP", 120);
        xData2.put("Cholesterol", 229);
        xData2.put("BloodSugar", false);
        xData2.put("ECG", "2");
        xData2.put("MaxHeartRate", 129);
        xData2.put("Angina", true);
        xData2.put("OldPeak", 2.6);
        xData2.put("STSlope", 2);
        xData2.put("Vessels", 2);
        xData2.put("Thal", "7");
        validationData.add(new Record(xData2, "problem"));

        return new Dataframe[] {trainingData, validationData};
    }
    
    /**
     * Ensemble Learning Responses Dataframe.
     * 
     * @param configuration
     * @return 
     */
    public static Dataframe[] ensembleLearningResponses(Configuration configuration) {
        Dataframe trainingData = new Dataframe(configuration);
        trainingData.add(Datasets.<String>newDataVector(new String[] {"pos","pos"}, "pos"));
        trainingData.add(Datasets.<String>newDataVector(new String[] {"pos","pos"}, "pos"));
        trainingData.add(Datasets.<String>newDataVector(new String[] {"pos","pos"}, "pos"));
        trainingData.add(Datasets.<String>newDataVector(new String[] {"pos","pos"}, "pos"));
        trainingData.add(Datasets.<String>newDataVector(new String[] {"pos","pos"}, "pos"));

        trainingData.add(Datasets.<String>newDataVector(new String[] {"pos","neg"}, "pos"));
        trainingData.add(Datasets.<String>newDataVector(new String[] {"pos","neg"}, "pos"));
        trainingData.add(Datasets.<String>newDataVector(new String[] {"pos","neg"}, "pos"));
        trainingData.add(Datasets.<String>newDataVector(new String[] {"neg","pos"}, "pos"));
        trainingData.add(Datasets.<String>newDataVector(new String[] {"neg","pos"}, "neg"));
        trainingData.add(Datasets.<String>newDataVector(new String[] {"neg","pos"}, "neg"));
        trainingData.add(Datasets.<String>newDataVector(new String[] {"pos","neg"}, "neg"));
        trainingData.add(Datasets.<String>newDataVector(new String[] {"pos","neg"}, "neg"));
        trainingData.add(Datasets.<String>newDataVector(new String[] {"neg","neg"}, "neg"));
        trainingData.add(Datasets.<String>newDataVector(new String[] {"neg","neg"}, "neg"));
        trainingData.add(Datasets.<String>newDataVector(new String[] {"neg","neg"}, "neg"));
        trainingData.add(Datasets.<String>newDataVector(new String[] {"neg","neg"}, "neg"));
        
        
        Dataframe validationData = new Dataframe(configuration);
        validationData.add(Datasets.<String>newDataVector(new String[] {"pos","pos"}, "pos"));
        validationData.add(Datasets.<String>newDataVector(new String[] {"pos","neg"}, "pos"));
        validationData.add(Datasets.<String>newDataVector(new String[] {"neg","pos"}, "neg"));
        validationData.add(Datasets.<String>newDataVector(new String[] {"neg","neg"}, "neg"));
        
        return new Dataframe[] {trainingData, validationData};
    }
    
    /**
     * Categorical Feature Selection Dataframe.
     * 
     * @param configuration
     * @param n
     * @return 
     */
    public static Dataframe[] featureSelectorCategorical(Configuration configuration, int n) {
        Dataframe data = new Dataframe(configuration);
        for(int i=0;i<n;++i) {
            AssociativeArray xData = new AssociativeArray();
            //important fields
            xData.put("high_paid", (PHPMethods.mt_rand(0, 4)>3)?1:0);
            xData.put("has_boat", PHPMethods.mt_rand(0, 1));
            xData.put("has_luxury_car", PHPMethods.mt_rand(0, 1));
            xData.put("has_butler", PHPMethods.mt_rand(0, 1));
            //xData.put("has_butler", (PHPMethods.mt_rand(0, 1)==1)?"yes":"no");
            xData.put("has_pool", PHPMethods.mt_rand(0, 1));
            
            //not important fields
            xData.put("has_tv", PHPMethods.mt_rand(0, 1));
            xData.put("has_dog", PHPMethods.mt_rand(0, 1));
            xData.put("has_cat", PHPMethods.mt_rand(0, 1));
            xData.put("has_fish", PHPMethods.mt_rand(0, 1));
            xData.put("random_field", (double)PHPMethods.mt_rand(0, 1000));
            
            double richScore = xData.getDouble("has_boat")
                             + xData.getDouble("has_luxury_car")
                             //+ ((xData.get("has_butler").equals("yes"))?1.0:0.0)
                             + xData.getDouble("has_butler")
                             + xData.getDouble("has_pool");
            
            Boolean isRich=false;
            if(richScore>=2 || xData.getDouble("high_paid")==1.0) {
                isRich = true;
            }
            
            data.add(new Record(xData, isRich));
        }
        
        return new Dataframe[] {data, data.copy()};
    }
    
    /**
     * PCA Dataframe.
     * 
     * @param configuration
     * @return 
     */
    public static Dataframe[] featureTransformationPCA(Configuration configuration) {
        Dataframe originalData = new Dataframe(configuration);
        originalData.add(Datasets.<Double>newDataVector(new Double[]{1.0, 2.0, 3.0}, null));
        originalData.add(Datasets.<Double>newDataVector(new Double[]{0.0, 5.0, 6.0}, null));
        originalData.add(Datasets.<Double>newDataVector(new Double[]{7.0, 8.0, 0.0}, null));
        originalData.add(Datasets.<Double>newDataVector(new Double[]{10.0, 0.0, 12.0}, null));
        originalData.add(Datasets.<Double>newDataVector(new Double[]{13.0, 14.0, 15.0}, null));
        
        
        Dataframe transformedData = new Dataframe(configuration);
        transformedData.add(Datasets.<Double>newDataVector(new Double[]{-3.4438, 0.0799, -1.4607}, null));
        transformedData.add(Datasets.<Double>newDataVector(new Double[]{-6.0641, 1.0143, -4.8165}, null));
        transformedData.add(Datasets.<Double>newDataVector(new Double[]{-7.7270, 6.7253, 2.8399}, null));
        transformedData.add(Datasets.<Double>newDataVector(new Double[]{-14.1401, -6.4677, 1.4920}, null));
        transformedData.add(Datasets.<Double>newDataVector(new Double[]{-23.8837, 3.7408, -2.3614}, null));
        
        return new Dataframe[] {originalData, transformedData};
    }
    
    /**
     * TF-IDF Dataframe.
     * 
     * @param configuration
     * @return 
     */
    public static Dataframe[] featureSelectorTFIDF(Configuration configuration) {
        Dataframe trainingData = new Dataframe(configuration);
        
        AssociativeArray xData1 = new AssociativeArray();
        xData1.put("important1", 2.0);
        xData1.put("important2", 3.0);
        xData1.put("stopword1", 10.0);
        xData1.put("stopword2", 4.0);
        xData1.put("stopword3", 8.0);
        trainingData.add(new Record(xData1, null));
        
        AssociativeArray xData2 = new AssociativeArray();
        xData2.put("important1", 2.0);
        xData2.put("important3", 5.0);
        xData2.put("stopword1", 10.0);
        xData2.put("stopword2", 2.0);
        xData2.put("stopword3", 4.0);
        trainingData.add(new Record(xData2, null));
        
        AssociativeArray xData3 = new AssociativeArray();
        xData3.put("important2", 2.0);
        xData3.put("important3", 5.0);
        xData3.put("stopword1", 10.0);
        xData3.put("stopword2", 2.0);
        xData3.put("stopword3", 4.0);
        trainingData.add(new Record(xData3, null));
        
        Dataframe validationData = trainingData.copy();
        
        return new Dataframe[] {trainingData, validationData};
    }
    
    /**
     * Recommender System Dataframe.
     * 
     * @param configuration
     * @return 
     */
    public static Dataframe[] recommenderSystemFood(Configuration configuration) {
        Dataframe trainingData = new Dataframe(configuration);
        
        AssociativeArray xData1 = new AssociativeArray();
        xData1.put("ml1", 5.0);
        xData1.put("ml2", 4.0);
        xData1.put("ml3", 4.5);
        xData1.put("vg1", 1.0);
        xData1.put("vg2", 1.5);
        xData1.put("vg3", 0.5);
        trainingData.add(new Record(xData1, "pizza"));
        
        AssociativeArray xData2 = new AssociativeArray();
        xData2.put("ml1", 3.5);
        xData2.put("ml2", 4.5);
        xData2.put("ml3", 4.0);
        xData2.put("vg1", 1.5);
        xData2.put("vg2", 1.0);
        xData2.put("vg3", 2);
        trainingData.add(new Record(xData2, "burger"));
        
        AssociativeArray xData3 = new AssociativeArray();
        xData3.put("ml1", 4.0);
        xData3.put("ml2", 3.0);
        xData3.put("ml3", 5.0);
        xData3.put("vg1", 4.0);
        xData3.put("vg2", 3.0);
        xData3.put("vg3", 2.0);
        trainingData.add(new Record(xData3, "beer"));
        
        AssociativeArray xData4 = new AssociativeArray();
        xData4.put("ml1", 4.5);
        xData4.put("ml2", 4.0);
        xData4.put("ml3", 4.5);
        xData4.put("vg1", 4.5);
        xData4.put("vg2", 2.5);
        xData4.put("vg3", 1.0);
        trainingData.add(new Record(xData4, "potato"));
        
        AssociativeArray xData5 = new AssociativeArray();
        xData5.put("ml1", 1.5);
        xData5.put("ml2", 1.0);
        xData5.put("ml3", 0.5);
        xData5.put("vg1", 4.5);
        xData5.put("vg2", 5.0);
        xData5.put("vg3", 4.0);
        trainingData.add(new Record(xData5, "salad"));
        
        AssociativeArray xData6 = new AssociativeArray();
        xData6.put("ml1", 0.5);
        xData6.put("ml2", 0.5);
        xData6.put("ml3", 1.0);
        xData6.put("vg1", 4.0);
        xData6.put("vg2", 4.5);
        xData6.put("vg3", 5.0);
        trainingData.add(new Record(xData6, "risecookie"));
        
        AssociativeArray xData7 = new AssociativeArray();
        xData7.put("ml1", 4.0);
        xData7.put("ml2", 1.5);
        xData7.put("ml3", 3.0);
        xData7.put("vg1", 1.0);
        xData7.put("vg2", 5.0);
        xData7.put("vg3", 4.5);
        trainingData.add(new Record(xData7, "sparklewatter"));
        
        AssociativeArray xData8 = new AssociativeArray();
        xData8.put("ml1", 1.0);
        xData8.put("ml2", 1.0);
        xData8.put("ml3", 0.5);
        xData8.put("vg1", 4.0);
        xData8.put("vg2", 3.5);
        xData8.put("vg3", 5.0);
        trainingData.add(new Record(xData8, "rise"));
        
        AssociativeArray xData9 = new AssociativeArray();
        xData9.put("ml1", 3.0);
        xData9.put("ml2", 2.0);
        xData9.put("ml3", 1.0);
        xData9.put("vg1", 4.5);
        xData9.put("vg2", 4.5);
        xData9.put("vg3", 5.0);
        trainingData.add(new Record(xData9, "tea"));
        
        AssociativeArray xData10 = new AssociativeArray();
        xData10.put("ml1", 3.5);
        xData10.put("ml2", 5.0);
        xData10.put("ml3", 4.0);
        xData10.put("vg1", 1.5);
        xData10.put("vg2", 2.0);
        xData10.put("vg3", 2.5);
        trainingData.add(new Record(xData10, "chocolate"));
        
        AssociativeArray xData11 = new AssociativeArray();
        xData11.put("ml1", 5.0);
        xData11.put("ml2", 5.0);
        xData11.put("ml3", 5.0);
        xData11.put("vg1", 0.5);
        xData11.put("vg2", 0.5);
        xData11.put("vg3", 0.5);
        trainingData.add(new Record(xData11, "pitta"));
        
        Dataframe validationData = new Dataframe(configuration);
        
        AssociativeArray profileData = new AssociativeArray();
        profileData.put("pizza", 4.5);
        profileData.put("beer", 5);
        profileData.put("salad", 0.5);
        validationData.add(new Record(profileData, null));
        
        return new Dataframe[] {trainingData, validationData};
    }
    
    /**
     * Regression Numeric Dataframe.
     * 
     * @param configuration
     * @return 
     */
    public static Dataframe[] regressionNumeric(Configuration configuration) {
        /*
        Synthetic Data generated with:
        
        $x1=rand(40,50);
        $x2=rand(1,60)/10;

        $y=2+0.002*$x1+30*$x2;
        $dataTable[]=array(array($x1,$x2),null);
        */
        Dataframe trainingData;
        try (Reader fileReader = new InputStreamReader(Datasets.class.getClassLoader().getResourceAsStream("datasets/regressionNumeric.csv"), StandardCharsets.UTF_8)) {
            LinkedHashMap<String, TypeInference.DataType> headerDataTypes = new LinkedHashMap<>(); 
            headerDataTypes.put("c1", TypeInference.DataType.NUMERICAL);
            headerDataTypes.put("c2", TypeInference.DataType.NUMERICAL);
            headerDataTypes.put("y", TypeInference.DataType.NUMERICAL);
            
            trainingData = Dataframe.Builder.parseCSVFile(fileReader, "y", headerDataTypes, ',', '"', "\r\n", null, null, configuration);
        }
        catch(IOException ex) {
            throw new UncheckedIOException(ex);
        }
        
        Dataframe validationData = trainingData.copy();
        
        return new Dataframe[] {trainingData, validationData};
    }
    
    /**
     * Regression Mixed Dataframe.
     * 
     * @param configuration
     * @return 
     */
    public static Dataframe[] regressionMixed(Configuration configuration) {
        /*
        Synthetic Data generated with:
        
        $x1=rand(1,3);
        $x2=rand(40,50);
        $x3=rand(1,60)/10;
        $x4=rand(0,4);

        $y=2+10*$x1+0.002*$x2+30*$x3+10*$x4;
        $dataTable[]=array(array((string)$x1,$x2,$x3,(string)$x4),null);
        */
        Dataframe trainingData;
        try (Reader fileReader = new InputStreamReader(Datasets.class.getClassLoader().getResourceAsStream("datasets/regressionMixed.csv"), StandardCharsets.UTF_8)) {
            LinkedHashMap<String, TypeInference.DataType> headerDataTypes = new LinkedHashMap<>(); 
            headerDataTypes.put("c1", TypeInference.DataType.CATEGORICAL);
            headerDataTypes.put("c2", TypeInference.DataType.NUMERICAL);
            headerDataTypes.put("c3", TypeInference.DataType.NUMERICAL);
            headerDataTypes.put("c4", TypeInference.DataType.CATEGORICAL);
            headerDataTypes.put("y", TypeInference.DataType.NUMERICAL);
            
            trainingData = Dataframe.Builder.parseCSVFile(fileReader, "y", headerDataTypes, ',', '"', "\r\n", null, null, configuration);
        }
        catch(IOException ex) {
            throw new UncheckedIOException(ex);
        }
        
        Dataframe validationData = trainingData.copy();
        
        return new Dataframe[] {trainingData, validationData};
    }

    /**
     * Returns a map with the URIs of a sentiment analysis dataset.
     *
     * @return
     */
    public static Map<Object, URI> sentimentAnalysis() {
        Map<Object, URI> dataset = new HashMap<>();
        dataset.put("negative", inputStreamToURI(Datasets.class.getClassLoader().getResourceAsStream("datasets/sentimentAnalysis.neg.txt")));
        dataset.put("positive", inputStreamToURI(Datasets.class.getClassLoader().getResourceAsStream("datasets/sentimentAnalysis.pos.txt")));
        return dataset;
    }

    /**
     * Returns the URI of an unlabelled sentiment analysis dataset.
     *
     * @return
     */
    public static URI sentimentAnalysisUnlabeled() {
        return inputStreamToURI(Datasets.class.getClassLoader().getResourceAsStream("datasets/sentimentAnalysis.unlabelled.txt"));
    }

    /**
     * Returns the HTML code of example.com.
     *
     * @return
     */
    public static String exampleHtmlCode() {
        try(BufferedReader fileReader = new BufferedReader(new InputStreamReader(Datasets.class.getClassLoader().getResourceAsStream("datasets/example.com.html"), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            for(String line;(line = fileReader.readLine()) != null;){
                sb.append(line);
                sb.append("\r\n");
            }
            return sb.toString().trim();
        }
        catch(IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * Converts an input stream to a URI.
     *
     * @param is
     * @return
     */
    private static URI inputStreamToURI(InputStream is) {
        try {
            File f = File.createTempFile("is2uri", "tmp");
            f.deleteOnExit();
            Files.copy(is, f.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return f.toURI();
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        finally {
            try {
                is.close();
            }
            catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }
}
