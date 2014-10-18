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
package com.datumbox.framework.statistics.decisiontheory;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.DataTable2D;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.utilities.MapFunctions;
import com.datumbox.framework.statistics.descriptivestatistics.Descriptives;
import java.util.AbstractMap;
import java.util.Map;

/**
 *
 * @author bbriniotis
 */
public class DecisionCriteria {
    /**
     * The internalDataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the internalDataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
    /**
     * Returns the best option and the payoff under maxMin strategy
     * 
     * @param payoffMatrix
     * @return 
     * @throws IllegalArgumentException 
     */
    public static Map.Entry<Object, Object> maxMin(DataTable2D payoffMatrix) throws IllegalArgumentException {
        if(payoffMatrix.isValid()==false) {
            throw new IllegalArgumentException();
        }
        AssociativeArray minPayoffs = new AssociativeArray();
        
        for(Map.Entry<Object, AssociativeArray> entry : payoffMatrix.entrySet()) {
            //Object event = entry.getKey();
            AssociativeArray optionList = entry.getValue();
            for(Map.Entry<Object, Object> entry2 : optionList.entrySet()) {
                Object option = entry2.getKey();
                Double payoff = Dataset.toDouble(entry2.getValue());
                
                Double currentMinPayoffOption = minPayoffs.getDouble(option);
                if(currentMinPayoffOption==null || payoff<currentMinPayoffOption) {
                    minPayoffs.put(option, payoff);
                }
            }
        }
        
        Map.Entry<Object, Object> entry = MapFunctions.selectMaxKeyValue(minPayoffs);
        return entry;
    }
    
    /**
     * Returns the best option and the payoff under maxMax strategy
     * 
     * @param payoffMatrix
     * @return 
     * @throws IllegalArgumentException 
     */
    public static Map.Entry<Object, Object> maxMax(DataTable2D payoffMatrix) throws IllegalArgumentException {
        if(payoffMatrix.isValid()==false) {
            throw new IllegalArgumentException();
        }
        
        Double maxMaxPayoff = Double.NEGATIVE_INFINITY;
        Object maxMaxPayoffOption = null;
        
        for(Map.Entry<Object, AssociativeArray> entry : payoffMatrix.entrySet()) {
            //Object event = entry.getKey();
            AssociativeArray optionList = entry.getValue();
            for(Map.Entry<Object, Object> entry2 : optionList.entrySet()) {
                Double payoff = Dataset.toDouble(entry2.getValue());
                //Object option = entry2.getKey();
                
                if(payoff>maxMaxPayoff) {
                    maxMaxPayoff=payoff;
                    maxMaxPayoffOption = entry2.getKey();
                }
            }
        }
        
        return new AbstractMap.SimpleEntry<>(maxMaxPayoffOption, maxMaxPayoff);
    }
    
    /**
     * Returns the best option and the payoff under savage strategy
     * 
     * @param payoffMatrix
     * @return 
     * @throws IllegalArgumentException 
     */
    public static Map.Entry<Object, Object> savage(DataTable2D payoffMatrix) throws IllegalArgumentException {
        if(payoffMatrix.isValid()==false) {
            throw new IllegalArgumentException();
        }
        
        //Deep clone the payoffMatrix to avoid modifying its original values
        DataTable2D regretMatrix = new DataTable2D();
        
        for(Map.Entry<Object, AssociativeArray> entry : payoffMatrix.entrySet()) {
            Object event = entry.getKey();
            AssociativeArray optionList = entry.getValue();
            double maxI = Descriptives.max(optionList.toFlatDataCollection());
            
            for(Map.Entry<Object, Object> entry2 : optionList.entrySet()) {
                Object option = entry2.getKey();
                Double payoff = Dataset.toDouble(entry2.getValue());
                
                //regretMatrix.internalData.get(event).internalData.put(option, payoff-maxI);
                regretMatrix.put2d(event, option, payoff-maxI);
            }
        }
        
        return maxMin(regretMatrix);
    }
    
    /**
     * Returns the best option and the payoff under laplace strategy
     * 
     * @param payoffMatrix
     * @return 
     * @throws IllegalArgumentException 
     */
    public static Map.Entry<Object, Object> laplace(DataTable2D payoffMatrix) throws IllegalArgumentException {
        if(payoffMatrix.isValid()==false) {
            throw new IllegalArgumentException();
        }
        
        //http://orms.pef.czu.cz/text/game-theory/DecisionTheory.html
        AssociativeArray optionAverages = new AssociativeArray();
        int numberOfEvents = payoffMatrix.size();
        
        for(Map.Entry<Object, AssociativeArray> entry : payoffMatrix.entrySet()) {
            //Object event = entry.getKey();
            AssociativeArray optionList = entry.getValue();
            for(Map.Entry<Object, Object> entry2 : optionList.entrySet()) {
                Object option = entry2.getKey();
                Double payoff = Dataset.toDouble(entry2.getValue());
                
                Double value = optionAverages.getDouble(option);
                if(value==null) {
                    value=0.0;
                }
                optionAverages.put(option, value + payoff/numberOfEvents);
            }
        }
        
        Map.Entry<Object, Object> entry = MapFunctions.selectMaxKeyValue(optionAverages);
        return entry;
    }
    
    /**
     * Returns the best option and the payoff under hurwiczAlpha strategy
     * 
     * @param payoffMatrix
     * @param alpha
     * @return 
     * @throws IllegalArgumentException 
     */
    public static Map.Entry<Object, Object> hurwiczAlpha(DataTable2D payoffMatrix, double alpha) throws IllegalArgumentException {
        if(payoffMatrix.isValid()==false) {
            throw new IllegalArgumentException();
        }
        
        AssociativeArray minPayoffs = new AssociativeArray();
        AssociativeArray maxPayoffs = new AssociativeArray();
        
        for(Map.Entry<Object, AssociativeArray> entry : payoffMatrix.entrySet()) {
            //Object event = entry.getKey();
            AssociativeArray optionList = entry.getValue();
            for(Map.Entry<Object, Object> entry2 : optionList.entrySet()) {
                Object option = entry2.getKey();
                Double payoff = Dataset.toDouble(entry2.getValue());
                
                Double currentMinPayoffOption = minPayoffs.getDouble(option);
                if(currentMinPayoffOption==null || payoff<currentMinPayoffOption) {
                    minPayoffs.put(option, payoff);
                }
                
                Double currentMaxPayoffOption = maxPayoffs.getDouble(option);
                if(currentMaxPayoffOption==null || payoff>currentMaxPayoffOption) {
                    maxPayoffs.put(option, payoff);
                }
            }
        }        

        AssociativeArray combinedPayoffs = new AssociativeArray();
        for(Map.Entry<Object, Object> entry : maxPayoffs.entrySet()) {
            Object option = entry.getKey();
            combinedPayoffs.put(option, Dataset.toDouble(entry.getValue())*alpha + minPayoffs.getDouble(option)*(1.0-alpha));
        }
        
        Map.Entry<Object, Object> entry = MapFunctions.selectMaxKeyValue(combinedPayoffs);
        return entry;
    }
    
    /**
     * Returns the best option and the payoff under maximumLikelihood strategy
     * 
     * @param payoffMatrix
     * @param eventProbabilities
     * @return 
     * @throws IllegalArgumentException 
     */
    public static Map.Entry<Object, Object> maximumLikelihood(DataTable2D payoffMatrix, AssociativeArray eventProbabilities) throws IllegalArgumentException {
        if(payoffMatrix.isValid()==false) {
            throw new IllegalArgumentException();
        }
        
        Map.Entry<Object, Object> eventEntry = MapFunctions.selectMaxKeyValue(eventProbabilities);
        
        Object mostProbableEvent = eventEntry.getKey();
        //Double mostProbableEventProbability = Dataset.toDouble(eventEntry.getValue());
        
        return MapFunctions.selectMaxKeyValue(payoffMatrix.get(mostProbableEvent));
    }
    
    /**
     * Returns the best option and the payoff under bayes strategy
     * 
     * @param payoffMatrix
     * @param eventProbabilities
     * @return 
     * @throws IllegalArgumentException 
     */
    public static Map.Entry<Object, Object> bayes(DataTable2D payoffMatrix, AssociativeArray eventProbabilities) throws IllegalArgumentException {
        if(payoffMatrix.isValid()==false) {
            throw new IllegalArgumentException();
        }
        
        AssociativeArray expectedPayoffs = new AssociativeArray();
        
        for(Map.Entry<Object, AssociativeArray> entry : payoffMatrix.entrySet()) {
            Object event = entry.getKey();
            AssociativeArray optionList = entry.getValue();
            for(Map.Entry<Object, Object> entry2 : optionList.entrySet()) {
                Object option = entry2.getKey();
                Double payoff = Dataset.toDouble(entry2.getValue());
                
                Double value = expectedPayoffs.getDouble(option);
                if(value==null) {
                    value=0.0;
                }
                expectedPayoffs.put(option, value + payoff*eventProbabilities.getDouble(event));
            }
        }
        
        Map.Entry<Object, Object> entry = MapFunctions.selectMaxKeyValue(expectedPayoffs);
        return entry;
    }
}
