/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis at datumbox.com>
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
package com.datumbox.framework.utilities.text.extractors;

import com.datumbox.common.objecttypes.Parameterizable;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 * @param <TP>
 */
public abstract class TextExtractor<TP extends TextExtractor.Parameters, K, V> {
    
    public static abstract class Parameters implements Parameterizable {

    }
    
    protected TP parameters;
    
    public TP getParameters() {
        return parameters;
    }

    public void setParameters(TP parameters) {
        this.parameters = parameters;
    }     
     
    public abstract Map<K, V> extract(final String text);
    
    /**
     * Generates a new instance of a TextExtractor by providing the Class of the
     * TextExtractor.
     * 
     * @param <T>
     * @param tClass
     * @return 
     */
    public static <T extends TextExtractor> T newInstance(Class<T> tClass) {
        T textExtractor = null;
        try {
            textExtractor = (T) tClass.getConstructor().newInstance();
        } 
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
        
        return textExtractor;
    }
}
