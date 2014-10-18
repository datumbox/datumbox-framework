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
package com.datumbox.configuration;

import com.datumbox.common.persistentstorage.factories.InMemoryStructureFactory;
import com.datumbox.common.persistentstorage.factories.BigDataStructureFactory;
import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class MemoryConfiguration implements Cloneable {
    
    private BigDataStructureFactory.MapType mapType;
    private BigDataStructureFactory.CollectionType collectionType;
    private BigDataStructureFactory.SetType setType;
    private BigDataStructureFactory.QueueType queueType;

    private int LRUsize;
            
    
    
    public MemoryConfiguration() {
        
        
        if(StorageConfiguration.IN_MEMORY_TRAINING) {
            mapType = InMemoryStructureFactory.getDefaultMapType();
            collectionType = InMemoryStructureFactory.getDefaultCollectionType();
            setType = InMemoryStructureFactory.getDefaultSetType();
            queueType = InMemoryStructureFactory.getDefaultQueueType();
            LRUsize = InMemoryStructureFactory.getDefaultLRUsize();
        }
        else {
            try {
                //get from General StorageConfiguration the class that handles permanent storage and call the default*Type methods
                Class<? extends BigDataStructureFactory> selectedBDSFClass = StorageConfiguration.getSelectedBDSFClass();

                mapType = (BigDataStructureFactory.MapType) selectedBDSFClass.getMethod("getDefaultMapType").invoke(null);
                collectionType = (BigDataStructureFactory.CollectionType) selectedBDSFClass.getMethod("getDefaultCollectionType").invoke(null);
                setType = (BigDataStructureFactory.SetType) selectedBDSFClass.getMethod("getDefaultSetType").invoke(null);
                queueType = (BigDataStructureFactory.QueueType) selectedBDSFClass.getMethod("getDefaultQueueType").invoke(null);   
                LRUsize = (int) selectedBDSFClass.getMethod("getDefaultLRUsize").invoke(null);   
            } 
            catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                throw new RuntimeException(ex);
            } 
        }
        

        
        
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        MemoryConfiguration copy = (MemoryConfiguration)super.clone();

        copy.mapType = mapType;
        copy.collectionType = collectionType;
        copy.setType = setType;
        copy.queueType = queueType;
        copy.LRUsize = LRUsize;
        
        return copy;
    }
    
    public BigDataStructureFactory.MapType getMapType() {
        return mapType;
    }

    public void setMapType(BigDataStructureFactory.MapType mapType) {
        this.mapType = mapType;
    }

    public BigDataStructureFactory.CollectionType getCollectionType() {
        return collectionType;
    }

    public void setCollectionType(BigDataStructureFactory.CollectionType collectionType) {
        this.collectionType = collectionType;
    }

    public BigDataStructureFactory.SetType getSetType() {
        return setType;
    }

    public void setSetType(BigDataStructureFactory.SetType setType) {
        this.setType = setType;
    }

    public BigDataStructureFactory.QueueType getQueueType() {
        return queueType;
    }

    public void setQueueType(BigDataStructureFactory.QueueType queueType) {
        this.queueType = queueType;
    }
    
    public int getLRUsize() {
        return LRUsize;
    }

    public void setLRUsize(int LRUsize) {
        this.LRUsize = LRUsize;
    }
    
    
}
