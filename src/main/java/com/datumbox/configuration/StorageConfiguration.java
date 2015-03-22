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

import com.datumbox.common.persistentstorage.DatabaseFactory;
import com.datumbox.common.persistentstorage.InMemoryFactory;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class StorageConfiguration {   
    
    public static final Class PERMANENT_STORAGE = InMemory.class;   
    
    public static class InMemory {
        //Mandatory constants
        public static final Class<? extends DatabaseFactory> STRUCTURE_FRACTORY_CLASS = InMemoryFactory.class;
        public static final String DBNAME_SEPARATOR = "_"; //NOT permitted characters are: <>:"/\|?*
        public static final String TMP_PREFIX = "TMP_";
        
        //DB specific constants
        public static final String DB_ROOT_FOLDER = "";
    }
    
    
    //Useful Static methods
    
    public static Class<? extends DatabaseFactory> getSelectedDBFClass() {
        //get from General StorageConfiguration the class that handles permanent storage and call the default*Type methods
        Class<? extends DatabaseFactory> selectedDBFClass = (Class<? extends DatabaseFactory>) getStorageConstant("STRUCTURE_FRACTORY_CLASS");
        return selectedDBFClass;
    }

    public static String getDBnameSeparator() {
        String selectedDBnameSeparator = (String) getStorageConstant("DBNAME_SEPARATOR");
        return selectedDBnameSeparator;
    }

    public static String getTmpPrefix() {
        String selectedTmpPrefix = (String) getStorageConstant("TMP_PREFIX");
        return selectedTmpPrefix;
    }
    
    private static Object getStorageConstant(String constantFieldName) {
        try {
            return StorageConfiguration.PERMANENT_STORAGE.getDeclaredField(constantFieldName).get(Class.class);
        } 
        catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }
}
