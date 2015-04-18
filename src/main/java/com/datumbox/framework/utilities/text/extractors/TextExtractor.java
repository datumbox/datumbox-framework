/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
import com.datumbox.framework.utilities.text.tokenizers.Tokenizer;
import com.datumbox.framework.utilities.text.tokenizers.WhitespaceTokenizer;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <TP>
 * @param <K>
 * @param <V>
 */
public abstract class TextExtractor<TP extends TextExtractor.Parameters, K, V> {
    
    public static abstract class Parameters implements Parameterizable {         
        
        private Class<? extends Tokenizer> tokenizer = WhitespaceTokenizer.class;

        public Tokenizer generateTokenizer() {
            if(tokenizer==null) {
                return null;
            }
            
            try {
                return tokenizer.newInstance();
            } 
            catch (InstantiationException | IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        }

        public Class<? extends Tokenizer> getTokenizer() {
            return tokenizer;
        }

        public void setTokenizer(Class<? extends Tokenizer> tokenizer) {
            this.tokenizer = tokenizer;
        }

    }
    
    protected TP parameters;
    
    public TextExtractor(TP parameters) {
        this.parameters = parameters;
    }
    
    public abstract Map<K, V> extract(final String text);
    
    /**
     * Generates a new instance of a TextExtractor by providing the Class of the
     * TextExtractor.
     * 
     * @param <T>
     * @param <TP>
     * @param tClass
     * @param parameters
     * @return 
     */
    public static <T extends TextExtractor, TP extends TextExtractor.Parameters> T newInstance(Class<T> tClass, TP parameters) {
        T textExtractor = null;
        try {
            textExtractor = (T) tClass.getConstructors()[0].newInstance(parameters);
        } 
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
        
        return textExtractor;
    }
}
