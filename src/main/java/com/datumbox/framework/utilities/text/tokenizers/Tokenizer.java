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
package com.datumbox.framework.utilities.text.tokenizers;

import java.util.List;

/**
 * This interface should be implemented by classes that are responsible to get
 * a string as input and separate it into tokens/keywords.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public interface Tokenizer {
    
    /**
     * The tokenize method accepts a string as input and returns a list of tokens.
     * 
     * @param text
     * @return 
     */
    public List<String> tokenize(String text);
}
