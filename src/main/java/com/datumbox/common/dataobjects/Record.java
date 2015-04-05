/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis at datumbox.com>
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
package com.datumbox.common.dataobjects;

import java.io.Serializable;
import java.util.Collections;
import java.util.Objects;

/**
 * The Record is the object that stores all the entries of the Dataset and it contains
 * information about the x, y, predicted class and predicted probabilities of the
 * observation. The record is immutable.
 * 
 * @author bbriniotis
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
    
    // new record methods
    public static <T> Record newDataVector(T[] xArray, Object y) {
        AssociativeArray x = new AssociativeArray();
        for(int i=0;i<xArray.length;++i) {
            x.internalData.put(i, xArray[i]);
        }
        return new Record(x, y);
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
        if (!Objects.equals(this.x, other.x)) {
            return false;
        }
        if (!Objects.equals(this.y, other.y)) {
            return false;
        }
        return true;
    }
    
    
}
