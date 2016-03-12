/**
 * Copyright (C) 2013-2016 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
package com.datumbox.framework.common.persistentstorage;

import com.datumbox.framework.common.persistentstorage.inmemory.InMemoryConfiguration;
import com.datumbox.framework.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.framework.common.persistentstorage.mapdb.MapDBConfiguration;
import java.util.Locale;

/**
 * Enum with all the supported Databases of the Framework.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public enum Database {
    
    /**
     * InMemory is the default database, it is very fast and it should be used when the data fit the memory.
     */
    INMEMORY(InMemoryConfiguration.class),
    
    /**
     * MapDB is an alternative database, which should be used when the data don't fit in memory.
     */
    MAPDB(MapDBConfiguration.class);
    
    private final Class<? extends DatabaseConfiguration> databaseClass;
    
    /**
     * Private constructor for the enum.
     * 
     * @param databaseClass 
     */
    private Database(Class<? extends DatabaseConfiguration> databaseClass) {
        this.databaseClass = databaseClass;
    }
    
    /**
     * Getter for the databaseClass field.
     * 
     * @return 
     */
    public Class<? extends DatabaseConfiguration> getDatabaseClass() {
        return databaseClass;
    }    
    
    /**
     * Returns the Class of the selected database.
     * 
     * @param database
     * @return 
     */
    public static Class<? extends DatabaseConfiguration> getDatabaseClass(String database) {
        if(database != null) {
            database = database.toUpperCase(Locale.ENGLISH);
        } 
        return Database.valueOf(database).getDatabaseClass();
    }
}
