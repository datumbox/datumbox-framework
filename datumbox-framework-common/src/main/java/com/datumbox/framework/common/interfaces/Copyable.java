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
package com.datumbox.framework.common.interfaces;

/**
 * This interface is implemented by all classes which implement the copy() method.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <T>
 */
public interface Copyable<T extends Copyable> {
    
    /**
     * Copies itself and returns a new instance of the same type.
     * 
     * @return 
     */
    public T copy();
    
}
