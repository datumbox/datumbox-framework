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
package com.datumbox.framework.utilities.text.tokenizers;

import com.datumbox.framework.utilities.text.tokenizers.WhitespaceTokenizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class WhitespaceTokenizerTest {
    
    public WhitespaceTokenizerTest() {
    }

    /**
     * Test of tokenize method, of class WhitespaceTokenizer.
     */
    @Test
    public void testTokenize() {
        System.out.println("tokenize");
        String text = "In publishing and graphic    design, lorem ipsum[1] is a placeholder text (filler text) commonly used to demonstrate the graphic elements of a document or visual presentation, such as font, typography, and layout, by removing the distraction of meaningful content. The lorem ipsum text is typically a section of a Latin text by Cicero with words altered, added, and removed that make it nonsensical and not proper Latin.[1]";
        WhitespaceTokenizer instance = new WhitespaceTokenizer();
        List<String> expResult = new ArrayList<>(Arrays.asList("In", "publishing", "and", "graphic", "design,", "lorem", "ipsum[1]", "is", "a", "placeholder", "text", "(filler", "text)", "commonly", "used", "to", "demonstrate", "the", "graphic", "elements", "of", "a", "document", "or", "visual", "presentation,", "such", "as", "font,", "typography,", "and", "layout,", "by", "removing", "the", "distraction", "of", "meaningful", "content.", "The", "lorem", "ipsum", "text", "is", "typically", "a", "section", "of", "a", "Latin", "text", "by", "Cicero", "with", "words", "altered,", "added,", "and", "removed", "that", "make", "it", "nonsensical", "and", "not", "proper", "Latin.[1]"));
        List<String> result = instance.tokenize(text);
        assertEquals(expResult, result);
    }
    
}
