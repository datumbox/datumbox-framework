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
package com.datumbox.common.dataobjects;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 *
 * @author bbriniotis
 */
public final class Record implements Serializable {
    /* The numeric id of the Record. Used for identification perposes. */ 
    private Integer id;
    
    /* The X vector of the Record */
    private AssociativeArray x;
    
    /* The Y response variable of the Record */
    private Object y;
    
    /* The Y predicted response variable of the Record */
    private Object yPredicted;
    
    /* The probabilities of yPredicted values (applicable only in classifiers and some clustering algorithms) */
    private AssociativeArray yPredictedProbabilities;
    
    public Record quickCopy() {
        //shallow copy of Record. It is used in order to avoid modifying the ids of the records when assigned to different datasets
        Record r = new Record();
        //r.id = id;
        r.x = x; //shallow copies
        r.y = y; 
        r.yPredicted = yPredicted; 
        r.yPredictedProbabilities = yPredictedProbabilities; //shallow copies
        
        return r;
    }
    
    public Record() {
        x = new AssociativeArray(new LinkedHashMap<>());
    }
    
    public static <T> Record newDataVector(T[] xArray, Object y) {
        Record r = new Record();
        r.y=y;
        for(int i=0;i<xArray.length;++i) {
            r.x.internalData.put(i, xArray[i]);
        }
        return r;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.id);
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
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }
    
    public Integer getId() {
        return id;
    }
    
    protected void setId(Integer id) {
        this.id = id;
    }

    public AssociativeArray getX() {
        return x;
    }

    public void setX(AssociativeArray x) {
        this.x = x;
    }

    public Object getY() {
        return y;
    }

    public void setY(Object y) {
        this.y = y;
    }

    public Object getYPredicted() {
        return yPredicted;
    }

    public void setYPredicted(Object yPredicted) {
        this.yPredicted = yPredicted;
    }

    public AssociativeArray getYPredictedProbabilities() {
        return yPredictedProbabilities;
    }

    public void setYPredictedProbabilities(AssociativeArray yPredictedProbabilities) {
        this.yPredictedProbabilities = yPredictedProbabilities;
    }
}
