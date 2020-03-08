/**
 * Copyright (C) 2013-2020 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
package com.datumbox.framework.common.dataobjects;

import java.util.Locale;

/**
 * This class is responsible for inferring the internal DataType of the objects
 * and for converting safely their values to a specified type.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class TypeInference {

    /**
     * Internal DataTypes used by the Framework.
     */
    public enum DataType {
        /**
         * Boolean/Dummy Variable.
         * Stored as boolean.
         */
        BOOLEAN(Boolean.class), 
        
        /**
         * Ordinal variable. 
         * Stored as short by convention.
         */
        ORDINAL(Short.class),
        
        /**
         * Numerical variable.
         * Any numeric except of short which is reserved for ordinal variables.
         */
        NUMERICAL(Number.class),
        
        /**
         * Categorical variable.
         * Any value that is non numeric or boolean.
         */
        CATEGORICAL(Object.class); 

        /**
         * Takes a String and tries to translate it to the provided DataType.
         * The result value is always casted to Object.
         * 
         * @param s
         * @param dataType
         * @return 
         */
        public static Object parse(String s, DataType dataType) {
            if(s==null || s.isEmpty() || "null".equalsIgnoreCase(s)) {
                return null;
            }

            if(dataType == DataType.BOOLEAN) {
                switch (s.toLowerCase(Locale.ENGLISH)) {
                    case "1":
                    case "true":
                    case "yes":
                        return Boolean.TRUE;
                    case "0":
                    case "false":
                    case "no":
                        return Boolean.FALSE;
                    default:
                        return null;
                }
            }
            else if (dataType == DataType.ORDINAL) {
                return Short.valueOf(s);
            }
            else if (dataType == DataType.NUMERICAL) {
                return Double.valueOf(s);
            }
            else if (dataType == DataType.CATEGORICAL) {
                return s;
            }
            else {
                //can happen if null DataType is provided
                throw new IllegalArgumentException("Unknown Datatype.");
            }
        }

        
        private final Class klass;
        
        /**
         * Private enum constructor.
         * 
         * @param klass 
         */
        private DataType(Class klass) {
            this.klass = klass;
        }
        
        /**
         * Returns whether the object is instance of the particular DataType.
         * 
         * @param v
         * @return 
         */
        private boolean isInstance(Object v) {
            return klass.isInstance(v);
        }
        
    }
    
    
    /**
     * Detects the DataType of a particular value.
     * 
     * @param v
     * @return 
     */
    public static DataType getDataType(Object v) {
        //NOTE: DO NOT CHANGE THE ORDER OF THE IFS!!!
        if(DataType.BOOLEAN.isInstance(v)) {
            return DataType.BOOLEAN;
        }
        else if(DataType.ORDINAL.isInstance(v)) {
            return DataType.ORDINAL;
        }
        else if(DataType.NUMERICAL.isInstance(v)) {
            return DataType.NUMERICAL;
        }
        else if(DataType.CATEGORICAL.isInstance(v)) {
            return DataType.CATEGORICAL;
        }
        else {
            //We will reach this point ONLY if the value is null. 
            //This is because the last enum value in the loop is Categorical which uses the Object class.
            return null; 
        }
    }
    
    /**
     * Converts safely any Number to Double.
     * 
     * @param v
     * @return
     */
    public static Double toDouble(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Boolean) {
            return ((Boolean) v) ? 1.0 : 0.0;
        }
        return ((Number) v).doubleValue();
    }

    /**
     * Converts safely any Number to Integer.
     * 
     * @param v
     * @return
     */
    public static Integer toInteger(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Boolean) {
            return ((Boolean) v) ? 1 : 0;
        }
        return ((Number) v).intValue();
    }
    
}
