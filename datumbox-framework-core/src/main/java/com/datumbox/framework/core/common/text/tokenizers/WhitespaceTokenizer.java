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
package com.datumbox.framework.core.common.text.tokenizers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The white space tokenizer separates the keywords of a string using the white
 * space.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class WhitespaceTokenizer extends AbstractTokenizer {
    
    /**
     * Separates the tokens of a string by splitting it on white space.
     * 
     * @param text
     * @return 
     */
    @Override
    public List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>(Arrays.asList(text.split("[\\p{Z}\\p{C}]+")));
        return tokens;
    }
    
}
