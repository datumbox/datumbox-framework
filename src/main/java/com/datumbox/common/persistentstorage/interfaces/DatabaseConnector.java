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
package com.datumbox.common.persistentstorage.interfaces;

import com.datumbox.framework.machinelearning.common.dataobjects.KnowledgeBase;
import java.util.Map;

/**
 * DB connectors that permanently store the parameters of the models should 
 * implement this interface. This interface defines the methods which are 
 * required to initialize db-backed collections. It is also responsible for 
 * connecting, clearing and managing the databases.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
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
     * @param isTemporary
     * @return 
     */
    public <K,V> Map<K,V> getBigMap(String name, boolean isTemporary);
    
    /**
     * Drops a particular database-backed Map.
     * 
     * @param <T>
     * @param name
     * @param map 
     */
    public <T extends Map> void dropBigMap(String name, T map);
    
}
