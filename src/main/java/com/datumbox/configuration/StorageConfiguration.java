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

import com.datumbox.common.persistentstorage.factories.BigDataStructureFactory;
import com.datumbox.common.persistentstorage.factories.InMemoryStructureFactory;
import com.datumbox.common.persistentstorage.factories.MongoDBStructureFactory;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class StorageConfiguration {   
    
    //public static final Class PERMANENT_STORAGE = MongoDB.class;
    public static final Class PERMANENT_STORAGE = InMemory.class;   
    
    public static final boolean IN_MEMORY_TRAINING = true; //this causes the memoryconfiguration object to be initialized for in-memory storage but the user can override this
    
    public static class MongoDB {
        //Mandatory constants
        public static final Class<? extends BigDataStructureFactory> STRUCTURE_FRACTORY_CLASS = MongoDBStructureFactory.class;
        public static final String DBNAME_SEPARATOR = "_"; //NOT permitted characthers are: /\. "*<>:|?
        public static final String TMP_PREFIX = "TMP_";
        
        //DB specific constants
        public static final Boolean USE_HASH_INDEXES_IN_MAPS = false; //DO NOT TURN ON, MONGO DOES NOT SUPPORT LISTS WITH HASH INDEXES YET

        public static final List<ServerAddress> SERVER_LIST;
        public static final List<MongoCredential> CREDENTIAL_LIST;
        
        static {
            try {
                SERVER_LIST = Arrays.asList(new ServerAddress("localhost", 27017));
            } 
            catch (UnknownHostException ex) {
                throw new RuntimeException(ex);
            }

            //MongoCredential mc1 = MongoCredential.createMongoCRCredential("user", "database", "password".toCharArray());
            CREDENTIAL_LIST = Arrays.asList( /* mc1 */ );
        }
    }
    
    public static class InMemory {
        //Mandatory constants
        public static final Class<? extends BigDataStructureFactory> STRUCTURE_FRACTORY_CLASS = InMemoryStructureFactory.class;
        public static final String DBNAME_SEPARATOR = "_"; //NOT permitted characters are: <>:"/\|?*
        public static final String TMP_PREFIX = "TMP_";
        
        //DB specific constants
        public static final String DB_ROOT_FOLDER = "";
    }
    
    
    //Useful Static methods
    
    public static Class<? extends BigDataStructureFactory> getSelectedBDSFClass() {
        //get from General StorageConfiguration the class that handles permanent storage and call the default*Type methods
        Class<? extends BigDataStructureFactory> selectedBDSFClass = (Class<? extends BigDataStructureFactory>) getStorageConstant("STRUCTURE_FRACTORY_CLASS");
        return selectedBDSFClass;
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
