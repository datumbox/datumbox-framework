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
package com.datumbox.framework.core.machinelearning.common.dataobjects;

import com.datumbox.framework.core.common.interfaces.Savable;
import com.datumbox.framework.core.machinelearning.common.interfaces.Trainable;
import com.datumbox.framework.core.machinelearning.common.interfaces.Parallelizable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This object stores a bundle of Trainables and it is used by algorithms that have other Trainables internally.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class TrainableBundle implements Savable {

    /**
     * The storage name separator used in the underlying Trainables.
     */
    private final String storageNameSeparator;

    /**
     * Public constructor.
     *
     * @param storageNameSeparator
     */
    public TrainableBundle(String storageNameSeparator) {
        this.storageNameSeparator = storageNameSeparator;
    }

    /**
     * Keeps a reference of all the wrapped algorithms.
     */
    private final Map<String, Trainable> bundle = new HashMap<>();

    /**
     * Returns a set with all the keys.
     *
     * @return
     */
    public Set<String> keySet() {
         return bundle.keySet();
    }

    /**
     * Returns whether the bundle contains the specified key.
     *
     * @param key
     * @return
     */
    public boolean containsKey(String key) {
        return bundle.containsKey(key);
    }

    /**
     * Returns the trainable with the specific key or null if the key is missing.
     *
     * @param key
     * @return
     */
    public Trainable get(String key) {
        return bundle.get(key);
    }

    /**
     * Puts the trainable in the bundle using a specific key and returns the previous entry or null.
     *
     * @param key
     * @param value
     * @return
     */
    public Trainable put(String key, Trainable value) {
        return bundle.put(key, value);
    }

    /**
     * Updates the parallelized flag of all wrapped algorithms.
     *
     * @param parallelized
     */
    public void setParallelized(boolean parallelized) {
        for(Trainable t : bundle.values()) {
            if (t !=null && t instanceof Parallelizable) {
                ((Parallelizable)t).setParallelized(parallelized);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void save(String storageName) {
        for(Map.Entry<String, Trainable> e : bundle.entrySet()) {
            Trainable t = e.getValue();
            if(t != null) {
                t.save(storageName + storageNameSeparator + e.getKey());
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void delete() {
        for(Trainable t : bundle.values()) {
            if(t != null) {
                t.delete();
            }
        }
        bundle.clear();
    }
      
    /** {@inheritDoc} */
    @Override
    public void close() {
        for(Trainable t : bundle.values()) {
            if(t != null) {
                try {
                    t.close();
                }
                catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        bundle.clear();
    }
}
