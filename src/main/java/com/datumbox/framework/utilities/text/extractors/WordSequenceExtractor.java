/* 
 * Copyright (C) 2014 Vasilis Vryniotis <bbriniotis at datumbox.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.datumbox.framework.utilities.text.extractors;

import com.datumbox.framework.utilities.text.tokenizers.Tokenizer;
import com.datumbox.framework.utilities.text.tokenizers.WhitespaceTokenizer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class WordSequenceExtractor extends TextExtractor<WordSequenceExtractor.Parameters, Integer, String> {
    
    public static class Parameters extends TextExtractor.Parameters {           
        private Class<? extends Tokenizer> tokenizer = WhitespaceTokenizer.class;

        public Class<? extends Tokenizer> getTokenizer() {
            return tokenizer;
        }

        public void setTokenizer(Class<? extends Tokenizer> tokenizer) {
            this.tokenizer = tokenizer;
        }
    }
    @Override
    public Map<Integer, String> extract(final String text) {
        Tokenizer tokenizer = null;
        try {
            tokenizer = parameters.getTokenizer().newInstance();
        } 
        catch (InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
        List<String> tmpKwd = tokenizer.tokenize(text);
        
        Map<Integer, String> keywordSequence = new LinkedHashMap<>();
        
        int position = 0;
        for(String keyword : tmpKwd) {
            keywordSequence.put(position, keyword);
            ++position;
        }
        
        return keywordSequence;
    }
   
}
