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
package com.datumbox.common.persistentstorage.interfaces;

import com.datumbox.common.objecttypes.Trainable;
import com.datumbox.common.persistentstorage.factories.BigDataStructureFactory;
import com.datumbox.configuration.MemoryConfiguration;
import java.io.Serializable;

/**
 * This interface is used to identify the parent/root objects that hold
 * one ore more BigDataStructureContainers. Only Root containers should implement
 * this interface, meaning entities that are stored as a whole document along with
 * their children in db.
 * 
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public interface BigDataStructureContainerHolder extends Serializable {
    public void save(boolean callPresave);
    public void load();
    public void reinitialize();
    public void erase(boolean completed);
    
    /**
     * The configuration takes place when the initializeTrainingConfiguration()
     * method of the wrapping object is called. Only it knows how to pass the 
     * variables properly and initialized the parameters of the object. This cannot
     * be done in a constructor.
     * @return 
     */
    public boolean isConfigured();
    public boolean isTrained();
    public boolean alreadyExists();
    public void setTrained(boolean trained);
    
    public String getDbName();
    public MemoryConfiguration getMemoryConfiguration();
    public void setMemoryConfiguration(MemoryConfiguration memoryConfiguration);
    public BigDataStructureFactory getBdsf();
    

    public Class<? extends Trainable> getOwnerClass();

    public void setOwnerClass(Class<? extends Trainable> ownerClass);
    
}
