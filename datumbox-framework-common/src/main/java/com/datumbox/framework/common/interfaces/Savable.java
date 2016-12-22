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
package com.datumbox.framework.common.interfaces;

/**
 * The Savable interface is implemented by all the objects that can be persisted.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public interface Savable extends AutoCloseable {

    /**
     * Saves the database of the object.
     *
     * @param dbName
     */
    public void save(String dbName);

    /**
     * Deletes the database of the object.
     */
    public void delete();

}
