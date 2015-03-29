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
package com.datumbox.common.persistentstorage.interfaces;

import com.datumbox.framework.machinelearning.common.dataobjects.KnowledgeBase;
import java.util.Map;

/**
 * DB connectors that permanently store the parameters of the models should 
 * implement this interface. This interface defines the methods which are 
 * required to initialize db-backed collections. It is also responsible for 
 * connecting, clearing and managing the databases.
 * 
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public interface DatabaseConnector {
    
    /**
     * This method is responsible for storing the data of each algorithm in the
     * database.
     * 
     * @param <KB>
     * @param knowledgeBaseObject 
     */
    public <KB extends KnowledgeBase> void save(KB knowledgeBaseObject);
    
    /**
     * Loads the data of an algorithm from the database.
     * 
     * @param <KB>
     * @param klass
     * @return 
     */
    public <KB extends KnowledgeBase> KB load(Class<KB> klass);
    
    /**
     * Checks if a particular database exists.
     * 
     * @return 
     */
    public boolean existsDatabase();
    
    /**
     * Drops the particular database.
     */
    public void dropDatabase();
    
    /**
     * Creates or loads a database-backed Map. The BigMap maps are used to store
     * a huge number of records.
     * 
     * @param <K>
     * @param <V>
     * @param name
     * @return 
     */
    public <K,V> Map<K,V> getBigMap(String name);
    
    /**
     * Drops a particular database-backed Map.
     * 
     * @param <T>
     * @param name
     * @param map 
     */
    public <T extends Map> void dropBigMap(String name, T map);
    
}
