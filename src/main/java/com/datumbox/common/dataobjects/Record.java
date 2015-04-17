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
package com.datumbox.common.dataobjects;

import java.io.Serializable;
import java.util.Collections;
import java.util.Objects;

/**
 * The Record is the object that stores all the entries of the Dataset and it contains
 * information about the x, y, predicted class and predicted probabilities of the
 * observation. The record is immutable.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public final class Record implements Serializable {
    
    /* The X vector of the Record */
    private final AssociativeArray x;
    
    /* The Y response variable of the Record */
    private final Object y;
    
    /* The Y predicted response variable of the Record */
    private final Object yPredicted;
    
    /* The probabilities of yPredicted values (applicable only in classifiers and some clustering algorithms) */
    private final AssociativeArray yPredictedProbabilities;
    
    
    // Constructors
    public Record(AssociativeArray x, Object y) {
        this(x, y, null, null);
    }
    
    public Record(AssociativeArray x, Object y, Object yPredicted, AssociativeArray yPredictedProbabilities) {
        this.x = new AssociativeArray(x);
        this.y = y;
        this.yPredicted = yPredicted;
        if (yPredictedProbabilities != null) {
            this.yPredictedProbabilities = new AssociativeArray(yPredictedProbabilities);
        }
        else {
            this.yPredictedProbabilities = null;
        }
    }

    public AssociativeArray getX() {
        if(x == null) {
            return null;
        }
        return new AssociativeArray(Collections.unmodifiableMap(x.internalData));
    }

    public Object getY() {
        return y;
    }

    public Object getYPredicted() {
        return yPredicted;
    }

    public AssociativeArray getYPredictedProbabilities() {
        if(yPredictedProbabilities == null) {
            return null;
        }
        return new AssociativeArray(Collections.unmodifiableMap(yPredictedProbabilities.internalData));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.x);
        hash = 23 * hash + Objects.hashCode(this.y);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Record other = (Record) obj;
        if (!Objects.equals(this.y, other.y)) {
            return false;
        }
        if (!Objects.equals(this.x, other.x)) {
            return false;
        }
        return true;
    }
    
    
}
