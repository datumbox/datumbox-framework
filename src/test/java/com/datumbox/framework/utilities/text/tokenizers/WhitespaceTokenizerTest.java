/**
 * Copyright (C) 2013-2016 Vasilis Vryniotis <bbriniotis@datumbox.com>
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

import com.datumbox.tests.bases.BaseTest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class WhitespaceTokenizerTest extends BaseTest {

    /**
     * Test of tokenize method, of class WhitespaceTokenizer.
     */
    @Test
    public void testTokenize() {
        logger.info("tokenize");
        String text = "In publishing and graphic    design, lorem ipsum[1] is a placeholder text (filler text) commonly used to demonstrate the graphic elements of a document or visual presentation, such as font, typography, and layout, by removing the distraction of meaningful content. The lorem ipsum text is typically a section of a Latin text by Cicero with words altered, added, and removed that make it nonsensical and not proper Latin.[1]";
        WhitespaceTokenizer instance = new WhitespaceTokenizer();
        List<String> expResult = new ArrayList<>(Arrays.asList("In", "publishing", "and", "graphic", "design,", "lorem", "ipsum[1]", "is", "a", "placeholder", "text", "(filler", "text)", "commonly", "used", "to", "demonstrate", "the", "graphic", "elements", "of", "a", "document", "or", "visual", "presentation,", "such", "as", "font,", "typography,", "and", "layout,", "by", "removing", "the", "distraction", "of", "meaningful", "content.", "The", "lorem", "ipsum", "text", "is", "typically", "a", "section", "of", "a", "Latin", "text", "by", "Cicero", "with", "words", "altered,", "added,", "and", "removed", "that", "make", "it", "nonsensical", "and", "not", "proper", "Latin.[1]"));
        List<String> result = instance.tokenize(text);
        assertEquals(expResult, result);
    }
    
}
