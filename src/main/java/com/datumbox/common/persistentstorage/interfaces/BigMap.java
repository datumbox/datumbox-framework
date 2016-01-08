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
package com.datumbox.common.persistentstorage.interfaces;

import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector.MapType;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector.StorageHint;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * BigMap annotation is used to declare large Maps in the ModelParameters classes.
 * Fields that are annotated with this annotation, are automatically initialized
 * by the BaseModelParameters object.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
@Target(ElementType.FIELD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface BigMap {
    /**
     * Parameter that passes the MapType of the BigMap.
     * 
     * @return 
     */
    public MapType mapType();
    
    /**
     * Parameter that passes the StorageHint of the BigMap.
     * 
     * @return 
     */
    public StorageHint storageHint();
    
    /**
     * Parameter that passes whether the BigMap should be tread-safe.
     * 
     * @return 
     */
    public boolean concurrent();
}