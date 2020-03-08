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
package com.datumbox.framework.core.common.dataobjects;

import com.datumbox.framework.common.dataobjects.AssociativeArray;

import java.io.Serializable;
import java.util.Objects;

/**
 * The Record is the object that stores all the entries of the Dataset and it contains
 * information about the x, y, predicted class and probabilities of the
 * predictions. The record is immutable.
 * 
 * In general Machine Learning methods can work with a variety of different types 
 * (Numbers, Boolean, Categorical and Ordinal). The types of the provided data
 * are not known at compile time but rather on runtime and they heavily depend
 * on the case or method. As a result all the data of x and y in the Record object, 
 * are stored internally as Objects. Moreover note that the column names of X are 
 * also defined as Object. This is because depending on the case, the columns can 
 * be Strings, Integers or even Tuples in the case of categorical features 
 * (tuples are implemented as immutable {@literal List<Object>}). For the response variable
 * y it applies the same; depending on the method it can be a Number, Boolean,
 * Categorical or Ordinal. As a result it is also stored as Object.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class Record implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 
     * The X vector of the Record 
     */
    private final AssociativeArray x;
    
    /** 
     * The Y response variable of the Record 
     */
    private final Object y;
    
    /** 
     * The Y predicted response variable of the Record 
     */
    private final Object yPredicted;
    
    /** 
     * The probabilities of yPredicted values (applicable only in classifiers and some clustering algorithms) 
     */
    private final AssociativeArray yPredictedProbabilities;
    
    
    // Constructors
    
    /**
     * Constructor which takes as arguments the x and y data. If the real response 
     * variable y is unknown you should pass a null value.
     * 
     * @param x
     * @param y 
     */
    public Record(AssociativeArray x, Object y) {
        this(x, y, null, null);
    }
    
    /**
     * Constructor which takes as arguments the x, y, predicted y and predicted
     * probaibilites.
     * 
     * @param x
     * @param y
     * @param yPredicted
     * @param yPredictedProbabilities 
     */
    public Record(AssociativeArray x, Object y, Object yPredicted, AssociativeArray yPredictedProbabilities) {
        this.x = AssociativeArray.copy2Unmodifiable(x);
        this.y = y;
        this.yPredicted = yPredicted;
        if (yPredictedProbabilities != null) {
            this.yPredictedProbabilities = AssociativeArray.copy2Unmodifiable(yPredictedProbabilities);
        }
        else {
            this.yPredictedProbabilities = null;
        }
    }
    
    /**
     * It returns an AssociativeArray with all the xData of the Record. The 
     * data are stored in an unmodifiable map to ensure they can't be changed.
     * 
     * @return 
     */
    public AssociativeArray getX() {
        return x;
    }
    
    /**
     * It returns the real response variable of the Record. If unknown it will have
     * a null value.
     * 
     * @return 
     */
    public Object getY() {
        return y;
    }
    
    /**
     * It returns the predicted response variable of the Record. The predicted
     * variable has a non-null value when a prediction is made by the
     * machine learning model.
     * 
     * @return 
     */
    public Object getYPredicted() {
        return yPredicted;
    }
    
    /**
     * If prediction probabilities are available for the particular algorithm, 
     * then this method will return these probabilities. This is supported by
     * all the classification and some clustering algorithms.
     * 
     * @return 
     */
    public AssociativeArray getYPredictedProbabilities() {
        return yPredictedProbabilities;
    }
    
    /**
     * The hash code of the record. Depends only on x and y.
     * 
     * @return 
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.x);
        hash = 23 * hash + Objects.hashCode(this.y);
        return hash;
    }
    
    /**
     * Checks if the provided object is equal to the current instance. It checks
     * only x and y.
     * 
     * @param obj
     * @return 
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        else if (getClass() != obj.getClass()) {
            return false;
        }
        
        final Record other = (Record) obj;
        
        if (!Objects.equals(this.y, other.y)) {
            return false;
        }
        else if (!Objects.equals(this.x, other.x)) {
            return false;
        }
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("x => ");
        sb.append(String.valueOf(x));
        sb.append(" | ");
        sb.append("y => ");
        sb.append(String.valueOf(y));
        sb.append(" | ");
        sb.append("yPredicted => ");
        sb.append(String.valueOf(yPredicted));
        sb.append(" | ");
        sb.append("yPredictedProbabilities => ");
        sb.append(String.valueOf(yPredictedProbabilities));
        return sb.toString();
    }
    
}
