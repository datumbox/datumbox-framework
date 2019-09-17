/**
 * Copyright (C) 2013-2019 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
package com.datumbox.framework.core.statistics.decisiontheory;

import com.datumbox.framework.common.dataobjects.AssociativeArray;
import com.datumbox.framework.common.dataobjects.DataTable2D;
import com.datumbox.framework.common.dataobjects.TypeInference;
import com.datumbox.framework.core.common.utilities.MapMethods;
import com.datumbox.framework.core.statistics.descriptivestatistics.Descriptives;

import java.util.AbstractMap;
import java.util.Map;

/**
 * Implementations of various Decision Criteria based on a payoff matrix.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class DecisionCriteria {
    
    /**
     * Returns the best option and the payoff under maxMin strategy
     * 
     * @param payoffMatrix
     * @return 
     */
    public static Map.Entry<Object, Object> maxMin(DataTable2D payoffMatrix) {
        if(payoffMatrix.isValid()==false) {
            throw new IllegalArgumentException("The payoff matrix does not have a rectangular format.");
        }
        AssociativeArray minPayoffs = new AssociativeArray();
        
        for(Map.Entry<Object, AssociativeArray> entry : payoffMatrix.entrySet()) {
            //Object event = entry.getKey();
            AssociativeArray optionList = entry.getValue();
            for(Map.Entry<Object, Object> entry2 : optionList.entrySet()) {
                Object option = entry2.getKey();
                Double payoff = TypeInference.toDouble(entry2.getValue());
                
                Double currentMinPayoffOption = minPayoffs.getDouble(option);
                if(currentMinPayoffOption==null || payoff<currentMinPayoffOption) {
                    minPayoffs.put(option, payoff);
                }
            }
        }
        
        Map.Entry<Object, Object> entry = MapMethods.selectMaxKeyValue(minPayoffs);
        return entry;
    }
    
    /**
     * Returns the best option and the payoff under maxMax strategy
     * 
     * @param payoffMatrix
     * @return 
     */
    public static Map.Entry<Object, Object> maxMax(DataTable2D payoffMatrix) {
        if(payoffMatrix.isValid()==false) {
            throw new IllegalArgumentException("The payoff matrix does not have a rectangular format.");
        }
        
        Double maxMaxPayoff = Double.NEGATIVE_INFINITY;
        Object maxMaxPayoffOption = null;
        
        for(Map.Entry<Object, AssociativeArray> entry : payoffMatrix.entrySet()) {
            //Object event = entry.getKey();
            AssociativeArray optionList = entry.getValue();
            for(Map.Entry<Object, Object> entry2 : optionList.entrySet()) {
                Double payoff = TypeInference.toDouble(entry2.getValue());
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
     */
    public static Map.Entry<Object, Object> savage(DataTable2D payoffMatrix) {
        if(payoffMatrix.isValid()==false) {
            throw new IllegalArgumentException("The payoff matrix does not have a rectangular format.");
        }
        
        //Deep clone the payoffMatrix to avoid modifying its original values
        DataTable2D regretMatrix = new DataTable2D();
        
        for(Map.Entry<Object, AssociativeArray> entry : payoffMatrix.entrySet()) {
            Object event = entry.getKey();
            AssociativeArray optionList = entry.getValue();
            double maxI = Descriptives.max(optionList.toFlatDataCollection());
            
            for(Map.Entry<Object, Object> entry2 : optionList.entrySet()) {
                Object option = entry2.getKey();
                Double payoff = TypeInference.toDouble(entry2.getValue());
                
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
     */
    public static Map.Entry<Object, Object> laplace(DataTable2D payoffMatrix) {
        if(payoffMatrix.isValid()==false) {
            throw new IllegalArgumentException("The payoff matrix does not have a rectangular format.");
        }
        
        //http://orms.pef.czu.cz/text/game-theory/DecisionTheory.html
        AssociativeArray optionAverages = new AssociativeArray();
        int numberOfEvents = payoffMatrix.size();
        
        for(Map.Entry<Object, AssociativeArray> entry : payoffMatrix.entrySet()) {
            //Object event = entry.getKey();
            AssociativeArray optionList = entry.getValue();
            for(Map.Entry<Object, Object> entry2 : optionList.entrySet()) {
                Object option = entry2.getKey();
                Double payoff = TypeInference.toDouble(entry2.getValue());
                
                Double value = optionAverages.getDouble(option);
                if(value==null) {
                    value=0.0;
                }
                optionAverages.put(option, value + payoff/numberOfEvents);
            }
        }
        
        Map.Entry<Object, Object> entry = MapMethods.selectMaxKeyValue(optionAverages);
        return entry;
    }
    
    /**
     * Returns the best option and the payoff under hurwiczAlpha strategy
     * 
     * @param payoffMatrix
     * @param alpha
     * @return 
     */
    public static Map.Entry<Object, Object> hurwiczAlpha(DataTable2D payoffMatrix, double alpha) {
        if(payoffMatrix.isValid()==false) {
            throw new IllegalArgumentException("The payoff matrix does not have a rectangular format.");
        }
        
        AssociativeArray minPayoffs = new AssociativeArray();
        AssociativeArray maxPayoffs = new AssociativeArray();
        
        for(Map.Entry<Object, AssociativeArray> entry : payoffMatrix.entrySet()) {
            //Object event = entry.getKey();
            AssociativeArray optionList = entry.getValue();
            for(Map.Entry<Object, Object> entry2 : optionList.entrySet()) {
                Object option = entry2.getKey();
                Double payoff = TypeInference.toDouble(entry2.getValue());
                
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
            combinedPayoffs.put(option, TypeInference.toDouble(entry.getValue())*alpha + minPayoffs.getDouble(option)*(1.0-alpha));
        }
        
        Map.Entry<Object, Object> entry = MapMethods.selectMaxKeyValue(combinedPayoffs);
        return entry;
    }
    
    /**
     * Returns the best option and the payoff under maximumLikelihood strategy
     * 
     * @param payoffMatrix
     * @param eventProbabilities
     * @return 
     */
    public static Map.Entry<Object, Object> maximumLikelihood(DataTable2D payoffMatrix, AssociativeArray eventProbabilities) {
        if(payoffMatrix.isValid()==false) {
            throw new IllegalArgumentException("The payoff matrix does not have a rectangular format.");
        }
        
        Map.Entry<Object, Object> eventEntry = MapMethods.selectMaxKeyValue(eventProbabilities);
        
        Object mostProbableEvent = eventEntry.getKey();
        //Double mostProbableEventProbability = Dataset.toDouble(eventEntry.getValue());
        
        return MapMethods.selectMaxKeyValue(payoffMatrix.get(mostProbableEvent));
    }
    
    /**
     * Returns the best option and the payoff under bayes strategy
     * 
     * @param payoffMatrix
     * @param eventProbabilities
     * @return 
     */
    public static Map.Entry<Object, Object> bayes(DataTable2D payoffMatrix, AssociativeArray eventProbabilities) {
        if(payoffMatrix.isValid()==false) {
            throw new IllegalArgumentException("The payoff matrix does not have a rectangular format.");
        }
        
        AssociativeArray expectedPayoffs = new AssociativeArray();
        
        for(Map.Entry<Object, AssociativeArray> entry : payoffMatrix.entrySet()) {
            Object event = entry.getKey();
            AssociativeArray optionList = entry.getValue();
            for(Map.Entry<Object, Object> entry2 : optionList.entrySet()) {
                Object option = entry2.getKey();
                Double payoff = TypeInference.toDouble(entry2.getValue());
                
                Double value = expectedPayoffs.getDouble(option);
                if(value==null) {
                    value=0.0;
                }
                expectedPayoffs.put(option, value + payoff*eventProbabilities.getDouble(event));
            }
        }
        
        Map.Entry<Object, Object> entry = MapMethods.selectMaxKeyValue(expectedPayoffs);
        return entry;
    }
}
