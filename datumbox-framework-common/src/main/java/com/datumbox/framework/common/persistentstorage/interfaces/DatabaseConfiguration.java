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
package com.datumbox.framework.common.persistentstorage.interfaces;

import com.datumbox.framework.common.interfaces.Configurable;

/**
 * This interface should be implemented by objects that store the configuration 
 * of DB connectors.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public interface DatabaseConfiguration extends Configurable {
    
    /**
     * Returns the separator that is used in the DB names. Usually the database
     * names used by the algorithms are concatenations of various words separated
     * by this character.
     *
     * @return
     */
    public String getDBNameSeparator();

    /**
     * Initializes and returns a connection to the Database.
     * 
     * @param dbName
     * @return 
     */
    public DatabaseConnector getConnector(String dbName);
    
}
