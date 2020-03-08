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
package com.datumbox.framework.core.common.text.extractors;

import com.datumbox.framework.core.common.interfaces.Extractable;
import com.datumbox.framework.core.common.interfaces.Parameterizable;
import com.datumbox.framework.core.common.text.tokenizers.AbstractTokenizer;
import com.datumbox.framework.core.common.text.tokenizers.WhitespaceTokenizer;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Base class for all Text exactor classes. The Text Extractors are parameterized
 with a AbstractParameters class and they take as input strings.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <TP>
 * @param <K>
 * @param <V>
 */
public abstract class AbstractTextExtractor<TP extends AbstractTextExtractor.AbstractParameters, K, V> implements Extractable<K, V, String> {
    
    /**
     * AbstractParameters of the AbstractTextExtractor.
     */
    public static abstract class AbstractParameters implements Parameterizable {
        
        private Class<? extends AbstractTokenizer> tokenizer = WhitespaceTokenizer.class;

        /**
         * Getter of the AbstractTokenizer class.
         * 
         * @return 
         */
        public Class<? extends AbstractTokenizer> getTokenizer() {
            return tokenizer;
        }
        
        /**
         * Setter of the AbstractTokenizer class.
         * 
         * @param tokenizer 
         */
        public void setTokenizer(Class<? extends AbstractTokenizer> tokenizer) {
            this.tokenizer = tokenizer;
        }

    }
    
    /**
     * The AbstractParameters of the AbstractTextExtractor.
     */
    protected final TP parameters;
    
    /**
     * Public constructor that accepts as arguments the AbstractParameters object.
     * 
     * @param parameters 
     */
    public AbstractTextExtractor(TP parameters) {
        this.parameters = parameters;
    }


    /**
     * Generates a new AbstractTokenizer object by using the provided tokenizer class.
     * 
     * @return 
     */
    protected AbstractTokenizer generateTokenizer() {
        Class<? extends AbstractTokenizer> tokenizer = parameters.getTokenizer();
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
     * This method gets as input a string and returns a map with the result of
     * the analysis. The type and the contents of the map depend on the implementation
     * of the extractor. Some extractors provide the tokens (keywords) along with 
     * metrics (frequencies, occurrences, scores etc) while others return the
     * text as a sequence of words.
     * 
     * @param text
     * @return 
     */
    @Override
    public abstract Map<K, V> extract(final String text);
    
    /**
     * Generates a new instance of a AbstractTextExtractor by providing the Class of the
 AbstractTextExtractor.
     * 
     * @param <T>
     * @param <TP>
     * @param parameters
     * @return 
     */
    public static <T extends AbstractTextExtractor, TP extends AbstractTextExtractor.AbstractParameters> T newInstance(TP parameters) {
        try {
            //By convention the Parameters are enclosed in the Extactor.
            Class<T> tClass = (Class<T>) parameters.getClass().getEnclosingClass();
            return tClass.getConstructor(parameters.getClass()).newInstance(parameters);
        } 
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException | NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }
    }
}
