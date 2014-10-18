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
package com.datumbox.framework.statistics.descriptivestatistics;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.AssociativeArray2D;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.FlatDataCollection;
import com.datumbox.common.utilities.DeepCopy;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 *
 * @author bbriniotis
 */
public class CensoredDescriptives {
    /**
     * The internalDataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the internalDataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
    public static final String CENSORED_NUMBER_POSTFIX="+";
    
    /**
     * Calculates the survivalFunction by processing the flatDataCollection with the
 censored internalData. The flatDataCollection contains numbers in string format. The
     * Censored entries contain a + symbol at the end of the number.
     * 
     * @param flatDataCollection
     * @return
     * @throws IllegalArgumentException 
     */
    public static AssociativeArray2D survivalFunction(FlatDataCollection flatDataCollection) throws IllegalArgumentException {
        AssociativeArray2D survivalFunction = new AssociativeArray2D(new LinkedHashMap<>()); //LinkedHashMap is important to maintain the order of the first key of the AssociativeArray2D
        
        Queue<Double> censoredData = new PriorityQueue<>();
        Queue<Double> uncensoredData = new PriorityQueue<>();
        
        int n = flatDataCollection.size();
        for(Object value : flatDataCollection) {
            String str = value.toString();
            if(str.endsWith(CENSORED_NUMBER_POSTFIX)) {
                //censored internalData encoded as 4.3+ or -4.3+
                censoredData.add(Double.valueOf(str.substring(0,str.length()-CENSORED_NUMBER_POSTFIX.length()))); //remove the trailing char and convert it to double
            }
            else {
                //uncensored internalData
                uncensoredData.add(Dataset.toDouble(value)); //convert it to double
            }
        }
        if(n==0) {
            throw new IllegalArgumentException();
        }
        
        Double currentCensored = null;
        Double currentUncensored = null;
        int i = 1;
        double previousUncensoredValue = 1.0;
        double varianceDenominator = 0.0;
        
        do {
            if(currentCensored==null) {
                currentCensored=censoredData.poll();
            }
            if(currentUncensored==null) {
                currentUncensored=uncensoredData.poll();
            }
            
            boolean isCensored = false;
            String key;
            if(currentUncensored == null) {
                key=currentCensored.toString().concat(CENSORED_NUMBER_POSTFIX);
                currentCensored = null;
                isCensored = true;
            }
            else if(currentCensored == null) {
                key=currentUncensored.toString();
                currentUncensored = null;
            }
            else if(currentCensored<currentUncensored) { //NOT EQUAL! Uncensored internalData of the same value are always larger
                key=currentCensored.toString().concat(CENSORED_NUMBER_POSTFIX);
                currentCensored = null;
                isCensored = true;
            }
            else {
                key=currentUncensored.toString();
                currentUncensored = null;
            }
            
            Integer previousMi = (Integer)survivalFunction.get2d(key, "mi");
            if(previousMi==null) {
                previousMi=0;
            }
            
            
            //in case of ties the last value will overwrite the previous. Thus we make automatically the tie correction
            survivalFunction.put2d(key, "i", i);
            survivalFunction.put2d(key, "mi", previousMi+1);
            
            if(isCensored==false) {
                survivalFunction.put2d(key, "r", i);

                double Sti=(n-i)/(n-i+1.0)*previousUncensoredValue;
                if(n-i>0) {
                    varianceDenominator+=1.0/((n-i)*(n-i+1.0));
                }

                survivalFunction.put2d(key, "Sti", Sti);
                survivalFunction.put2d(key, "varianceSti", Sti*Sti*varianceDenominator);

                previousUncensoredValue=Sti;
            }
            
            
            ++i;
        } 
        while(currentCensored!=null || currentUncensored!=null || 
              !censoredData.isEmpty() || !uncensoredData.isEmpty());
        
        censoredData=null;
        uncensoredData=null;
        
        return survivalFunction;
    }
    
    /**
     * Calculates median
     * 
     * @param survivalFunction
     * @return
     * @throws IllegalArgumentException 
     */
    public static double median(AssociativeArray2D survivalFunction) throws IllegalArgumentException {
        Double ApointTi = null;
        Double BpointTi = null;
        
        int n = survivalFunction.size();
        if(n==0) {
            throw new IllegalArgumentException();
        } 
        
        for(Map.Entry<Object, AssociativeArray> entry : survivalFunction.entrySet()) {
            Object ti = entry.getKey();
            AssociativeArray row = entry.getValue();
            
            Double Sti = row.getDouble("Sti");
            
            if(Sti==null) {
                continue; //skip censored internalData
            }
            
            Double point = Double.valueOf(ti.toString());
            if(Sti==0.5) {
                return point; //we found extactly the point
            }
            else if(Sti>0.5) {
                ApointTi=point; //keep the point just before the 0.5 probability
            }
            else {
                BpointTi=point; //keep the first point after the 0.5 probability and exit loop
                break;
            }
        }
        
        if(n==1) {
            return (ApointTi!=null)?ApointTi:BpointTi;
        }
        
        double ApointTiValue = Dataset.toDouble(survivalFunction.get2d(ApointTi.toString(), "Sti"));
        double BpointTiValue = Dataset.toDouble(survivalFunction.get2d(BpointTi.toString(), "Sti"));
        double median=BpointTi-(BpointTiValue-0.5)*(BpointTi-ApointTi)/(BpointTiValue-ApointTiValue);

        return median;
    }
    
    /**
     * Calculates simple mean
     * 
     * @param survivalFunction
     * @return
     * @throws IllegalArgumentException 
     */
    public static double mean(AssociativeArray2D survivalFunction) throws IllegalArgumentException {
        if(survivalFunction.isEmpty()) {
            throw new IllegalArgumentException();
        } 
        
        return Ar(survivalFunction, 0);
    }
    
    /**
     * Ar function used to estimate mean and variance.
     * 
     * @param survivalFunction
     * @param r
     * @return
     * @throws IllegalArgumentException 
     */
    private static double Ar(AssociativeArray2D survivalFunction, int r) throws IllegalArgumentException {
        if(survivalFunction.isEmpty()) {
            throw new IllegalArgumentException();
        }
        
        AssociativeArray2D survivalFunctionCopy = survivalFunction;
        
        //check if last one is censored and close it
        Map.Entry<Object, AssociativeArray> lastRowEntry = null;
        for(Map.Entry<Object, AssociativeArray> currentRowEntry : survivalFunction.entrySet()) {
            lastRowEntry=currentRowEntry;
        }
        
        AssociativeArray lastRow = lastRowEntry.getValue();
        if(lastRow.get("Sti")==null) { //if the last record is censored we must close the line
            survivalFunctionCopy = DeepCopy.<AssociativeArray2D>cloneObject(survivalFunction); //copy internalData to avoid modifying the internalData
            
            Object lastRowKey = lastRowEntry.getKey();
            AssociativeArray lastRowValue = survivalFunctionCopy.remove(lastRowKey);
            
            String str = lastRowKey.toString();
            Double newLastRowKey = Double.valueOf(str.substring(0,str.length()-CENSORED_NUMBER_POSTFIX.length())); //remove censored postfix
            newLastRowKey = Math.floor(newLastRowKey)+1;
            
            survivalFunctionCopy.put2d(newLastRowKey, "i", lastRowValue.get("i"));
            survivalFunctionCopy.put2d(newLastRowKey, "r", lastRowValue.get("i"));
            survivalFunctionCopy.put2d(newLastRowKey, "Sti", 0.0);
            survivalFunctionCopy.put2d(newLastRowKey, "varianceSti", 0.0);
        }
        

        double Ar=0.0;
        double StiPrevious=1;
        double tiPrevious=0;
        for(Map.Entry<Object, AssociativeArray> entry : survivalFunctionCopy.entrySet()) {
            Object ti = entry.getKey();
            AssociativeArray row = entry.getValue();
            

            Double Sti = row.getDouble("Sti");
            
            if(Sti==null) {
                continue; //skip censored internalData
            }
            
            double tiCurrent = Double.valueOf(ti.toString());
            if(row.getDouble("r")>r) {
                Ar+=StiPrevious*(tiCurrent-tiPrevious);
            }
            StiPrevious=Sti;
            tiPrevious=tiCurrent;
        }

        return Ar;
    }
    
    /**
     * Calculates the Variance of Mean.
     * 
     * @param survivalFunction
     * @return 
     */
    public static double meanVariance(AssociativeArray2D survivalFunction) {
        double meanVariance=0;

        int m=0;
        int n=0;
        for(Map.Entry<Object, AssociativeArray> entry : survivalFunction.entrySet()) {
            //Object ti = entry.getKey();
            AssociativeArray row = entry.getValue();
     
            Number mi = (Number)row.get("mi");
            
            n+=mi.intValue();
            if(row.get("Sti")==null) { //if censored internalData
                m+=mi.intValue();
            }
        }
        for(Map.Entry<Object, AssociativeArray> entry : survivalFunction.entrySet()) {
            //Object ti = entry.getKey();
            AssociativeArray row = entry.getValue();
     
            if(row.get("Sti")==null) { 
                continue; //skip censored internalData
            }            
            
            Number mi = (Number)row.get("mi");
            Number r = (Number)row.get("r");
     
            double Ar = Ar(survivalFunction,r.intValue());
            if(n-r.intValue()>0) {
                meanVariance+=mi.intValue()*(Ar*Ar)/((n-r.intValue())*(n-r.intValue()+1.0));
            }
        }

        meanVariance*=m/(m-1.0);

        return meanVariance;
    }
    
    /**
     * Calculates Standard Deviation of Mean (Standard Error)
     * 
     * @param survivalFunction
     * @return 
     */
    public static double meanStd(AssociativeArray2D survivalFunction) {
        return Math.sqrt(meanVariance(survivalFunction));
    }
}
