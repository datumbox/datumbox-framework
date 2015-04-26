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
 * Base class for all Text exactor classes. The Text Extractors are parameterized
 * with a Parameters class and they take as input strings.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <TP>
 * @param <K>
 * @param <V>
 */
public abstract class TextExtractor<TP extends TextExtractor.Parameters, K, V> {
    
    /**
     * Parameters of the TextExtractor.
     */
    public static abstract class Parameters implements Parameterizable {         
        
        private Class<? extends Tokenizer> tokenizer = WhitespaceTokenizer.class;

        /**
         * Generates a new Tokenizer object by using the provided tokenizer class.
         * 
         * @return 
         */
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

        /**
         * Getter of the Tokenizer class.
         * 
         * @return 
         */
        public Class<? extends Tokenizer> getTokenizer() {
            return tokenizer;
        }
        
        /**
         * Setter of the Tokenizer class.
         * 
         * @param tokenizer 
         */
        public void setTokenizer(Class<? extends Tokenizer> tokenizer) {
            this.tokenizer = tokenizer;
        }

    }
    
    protected TP parameters;
    
    /**
     * Public constructor that accepts as arguments the Parameters object.
     * 
     * @param parameters 
     */
    public TextExtractor(TP parameters) {
        this.parameters = parameters;
    }
    
    /**
     * This method gets as input a string and returns a map with the result of
     * the analysis. The type and the contents of the map depend on the implementation
     * of the extractor. Some extractors provide the tokens (keywords) along with 
     * metrics (frequencies, occurrences, scores etc) while others return the
     * text as a sequence of words.
     * 
     * @param text
     * @return 
     */
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
