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

import com.datumbox.framework.utilities.text.tokenizers.Tokenizer;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class UniqueWordSequenceExtractor extends TextExtractor<UniqueWordSequenceExtractor.Parameters, Integer, String> {
    
    public static class Parameters extends TextExtractor.Parameters {  
        
    }
    
    public UniqueWordSequenceExtractor(Parameters parameters) {
        super(parameters);
    }
    
    @Override
    public Map<Integer, String> extract(final String text) {
        Tokenizer tokenizer = parameters.generateTokenizer();
        
        Set<String> tmpKwd = new LinkedHashSet<>(tokenizer.tokenize(text));
        
        Map<Integer, String> keywordSequence = new LinkedHashMap<>();
        
        int position = 0;
        for(String keyword : tmpKwd) {
            keywordSequence.put(position, keyword);
            ++position;
        }
        
        return keywordSequence;
    }
   
}
