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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This extractor class extracts the keywords of a string as a sequence of words.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class WordSequenceExtractor extends AbstractTextExtractor<WordSequenceExtractor.Parameters, Integer, String> {
    
    /**
     * AbstractParameters of the WordSequenceExtractor.
     */
    public static class Parameters extends AbstractTextExtractor.AbstractParameters {   
        private static final long serialVersionUID = 1L;
        
    }
    
    /**
     * Public constructor that accepts as arguments the AbstractParameters object.
     * 
     * @param parameters 
     */
    public WordSequenceExtractor(Parameters parameters) {
        super(parameters);
    }
    
    /**
     * This method gets as input a string and returns as output a numbered sequence
     * of the tokens. In the returned map as keys we store the position of the word
     * in the original string and as value the actual token in that position.
     * 
     * @param text
     * @return 
     */
    @Override
    public Map<Integer, String> extract(final String text) {
        List<String> tmpKwd = generateTokenizer().tokenize(text);
        
        Map<Integer, String> keywordSequence = new LinkedHashMap<>();
        
        int position = 0;
        for(String keyword : tmpKwd) {
            keywordSequence.put(position, keyword);
            ++position;
        }
        
        return keywordSequence;
    }
   
}
