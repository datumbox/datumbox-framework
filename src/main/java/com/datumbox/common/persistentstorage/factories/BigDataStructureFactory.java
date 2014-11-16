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
package com.datumbox.common.persistentstorage.factories;

import com.datumbox.common.persistentstorage.interfaces.BigDataStructureContainerHolder;
import com.datumbox.configuration.StorageConfiguration;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * DB Drivers which can be used to permanently store/handle the parameters of the
 * models should implement this interface. This interface defines the methods which
 * are used to initialize db-backed collections and objects used for storing the
 * data. It is also responsible for connecting, clearing and managing the dbs.
 * 
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public interface BigDataStructureFactory {
    
    
    public static BigDataStructureFactory newInstance(String dbName) {
        //get from General StorageConfiguration the class that handles permanent storage
        try {
            Class<? extends BigDataStructureFactory> selectedBDSFClass = StorageConfiguration.getSelectedBDSFClass();
            return (BigDataStructureFactory) selectedBDSFClass.getConstructor(String.class).newInstance(dbName);
        } 
        catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public <H extends BigDataStructureContainerHolder> void save(H holderObject);
    
    public <H extends BigDataStructureContainerHolder> H load(Class<H> klass);
    
    public boolean existsDatabase();
    
    public void dropDatabase();
    
    public void clearDatabase();
    
    public void cleanUp();
        
    public <T extends Map> void dropMap(String collectionName, T map);
    
    public <K,V> Map<K,V> getMap(String collectionName);
    
}
