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

import com.datumbox.framework.utilities.text.extractors.NgramsExtractor;
import com.datumbox.configuration.TestConfiguration;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class NgramsExtractorTest {
    
    public NgramsExtractorTest() {
    }

    /**
     * Test of extract method, of class NgramsExtractor.
     */
    @Test
    public void testExtract() throws Exception {
        System.out.println("extract");
        String text = "In publishing and graphic design, lorem ipsum[1] is a placeholder text (filler text) commonly used to demonstrate the graphic elements of a document or visual presentation, such as font, typography, and layout, by removing the distraction of meaningful content. The lorem ipsum text is typically a section of a Latin text by Cicero with words altered, added, and removed that make it nonsensical and not proper Latin.[1] In publishing and graphic design, lorem ipsum[1] is a placeholder text (filler text) commonly used to demonstrate the graphic elements of a document or visual presentation, such as font, typography, and layout, by removing the distraction of meaningful content. The lorem ipsum text is typically a section of a Latin text by Cicero with words altered, added, and removed that make it nonsensical and not proper Latin.[1]";
        NgramsExtractor instance = new NgramsExtractor();
        instance.setParameters(new NgramsExtractor.Parameters());
        Map<String, Double> expResult = new HashMap<>();
        
        expResult.put("In", 2.0);
        expResult.put("publishing", 2.0);
        expResult.put("and", 8.0);
        expResult.put("graphic", 4.0);
        expResult.put("design,", 2.0);
        expResult.put("lorem", 4.0);
        expResult.put("ipsum[1]", 2.0);
        expResult.put("is", 4.0);
        expResult.put("a", 8.0);
        expResult.put("placeholder", 2.0);
        expResult.put("text", 6.0);
        expResult.put("(filler", 2.0);
        expResult.put("text)", 2.0);
        expResult.put("commonly", 2.0);
        expResult.put("used", 2.0);
        expResult.put("to", 2.0);
        expResult.put("demonstrate", 2.0);
        expResult.put("the", 4.0);
        expResult.put("elements", 2.0);
        expResult.put("of", 6.0);
        expResult.put("document", 2.0);
        expResult.put("or", 2.0);
        expResult.put("visual", 2.0);
        expResult.put("presentation,", 2.0);
        expResult.put("such", 2.0);
        expResult.put("as", 2.0);
        expResult.put("font,", 2.0);
        expResult.put("typography,", 2.0);
        expResult.put("layout,", 2.0);
        expResult.put("by", 4.0);
        expResult.put("removing", 2.0);
        expResult.put("distraction", 2.0);
        expResult.put("meaningful", 2.0);
        expResult.put("content.", 2.0);
        expResult.put("The", 2.0);
        expResult.put("ipsum", 2.0);
        expResult.put("typically", 2.0);
        expResult.put("section", 2.0);
        expResult.put("Latin", 2.0);
        expResult.put("Cicero", 2.0);
        expResult.put("with", 2.0);
        expResult.put("words", 2.0);
        expResult.put("altered,", 2.0);
        expResult.put("added,", 2.0);
        expResult.put("removed", 2.0);
        expResult.put("that", 2.0);
        expResult.put("make", 2.0);
        expResult.put("it", 2.0);
        expResult.put("nonsensical", 2.0);
        expResult.put("not", 2.0);
        expResult.put("proper", 2.0);
        expResult.put("Latin.[1]", 2.0);


        expResult.put("In publishing", 2.0);
        expResult.put("publishing and", 2.0);
        expResult.put("and graphic", 2.0);
        expResult.put("graphic design,", 2.0);
        expResult.put("design, lorem", 2.0);
        expResult.put("lorem ipsum[1]", 2.0);
        expResult.put("ipsum[1] is", 2.0);
        expResult.put("is a", 2.0);
        expResult.put("a placeholder", 2.0);
        expResult.put("placeholder text", 2.0);
        expResult.put("text (filler", 2.0);
        expResult.put("(filler text)", 2.0);
        expResult.put("text) commonly", 2.0);
        expResult.put("commonly used", 2.0);
        expResult.put("used to", 2.0);
        expResult.put("to demonstrate", 2.0);
        expResult.put("demonstrate the", 2.0);
        expResult.put("the graphic", 2.0);
        expResult.put("graphic elements", 2.0);
        expResult.put("elements of", 2.0);
        expResult.put("of a", 4.0);
        expResult.put("a document", 2.0);
        expResult.put("document or", 2.0);
        expResult.put("or visual", 2.0);
        expResult.put("visual presentation,", 2.0);
        expResult.put("presentation, such", 2.0);
        expResult.put("such as", 2.0);
        expResult.put("as font,", 2.0);
        expResult.put("font, typography,", 2.0);
        expResult.put("typography, and", 2.0);
        expResult.put("and layout,", 2.0);
        expResult.put("layout, by", 2.0);
        expResult.put("by removing", 2.0);
        expResult.put("removing the", 2.0);
        expResult.put("the distraction", 2.0);
        expResult.put("distraction of", 2.0);
        expResult.put("of meaningful", 2.0);
        expResult.put("meaningful content.", 2.0);
        expResult.put("content. The", 2.0);
        expResult.put("The lorem", 2.0);
        expResult.put("lorem ipsum", 2.0);
        expResult.put("ipsum text", 2.0);
        expResult.put("text is", 2.0);
        expResult.put("is typically", 2.0);
        expResult.put("typically a", 2.0);
        expResult.put("a section", 2.0);
        expResult.put("section of", 2.0);
        expResult.put("a Latin", 2.0);
        expResult.put("Latin text", 2.0);
        expResult.put("text by", 2.0);
        expResult.put("by Cicero", 2.0);
        expResult.put("Cicero with", 2.0);
        expResult.put("with words", 2.0);
        expResult.put("words altered,", 2.0);
        expResult.put("altered, added,", 2.0);
        expResult.put("added, and", 2.0);
        expResult.put("and removed", 2.0);
        expResult.put("removed that", 2.0);
        expResult.put("that make", 2.0);
        expResult.put("make it", 2.0);
        expResult.put("it nonsensical", 2.0);
        expResult.put("nonsensical and", 2.0);
        expResult.put("and not", 2.0);
        expResult.put("not proper", 2.0);
        expResult.put("proper Latin.[1]", 2.0);
        expResult.put("Latin.[1] In", 1.0);


        expResult.put("In publishing and", 2.0);
        expResult.put("publishing and graphic", 2.0);
        expResult.put("and graphic design,", 2.0);
        expResult.put("graphic design, lorem", 2.0);
        expResult.put("design, lorem ipsum[1]", 2.0);
        expResult.put("lorem ipsum[1] is", 2.0);
        expResult.put("ipsum[1] is a", 2.0);
        expResult.put("is a placeholder", 2.0);
        expResult.put("a placeholder text", 2.0);
        expResult.put("placeholder text (filler", 2.0);
        expResult.put("text (filler text)", 2.0);
        expResult.put("(filler text) commonly", 2.0);
        expResult.put("text) commonly used", 2.0);
        expResult.put("commonly used to", 2.0);
        expResult.put("used to demonstrate", 2.0);
        expResult.put("to demonstrate the", 2.0);
        expResult.put("demonstrate the graphic", 2.0);
        expResult.put("the graphic elements", 2.0);
        expResult.put("graphic elements of", 2.0);
        expResult.put("elements of a", 2.0);
        expResult.put("of a document", 2.0);
        expResult.put("a document or", 2.0);
        expResult.put("document or visual", 2.0);
        expResult.put("or visual presentation,", 2.0);
        expResult.put("visual presentation, such", 2.0);
        expResult.put("presentation, such as", 2.0);
        expResult.put("such as font,", 2.0);
        expResult.put("as font, typography,", 2.0);
        expResult.put("font, typography, and", 2.0);
        expResult.put("typography, and layout,", 2.0);
        expResult.put("and layout, by", 2.0);
        expResult.put("layout, by removing", 2.0);
        expResult.put("by removing the", 2.0);
        expResult.put("removing the distraction", 2.0);
        expResult.put("the distraction of", 2.0);
        expResult.put("distraction of meaningful", 2.0);
        expResult.put("of meaningful content.", 2.0);
        expResult.put("meaningful content. The", 2.0);
        expResult.put("content. The lorem", 2.0);
        expResult.put("The lorem ipsum", 2.0);
        expResult.put("lorem ipsum text", 2.0);
        expResult.put("ipsum text is", 2.0);
        expResult.put("text is typically", 2.0);
        expResult.put("is typically a", 2.0);
        expResult.put("typically a section", 2.0);
        expResult.put("a section of", 2.0);
        expResult.put("section of a", 2.0);
        expResult.put("of a Latin", 2.0);
        expResult.put("a Latin text", 2.0);
        expResult.put("Latin text by", 2.0);
        expResult.put("text by Cicero", 2.0);
        expResult.put("by Cicero with", 2.0);
        expResult.put("Cicero with words", 2.0);
        expResult.put("with words altered,", 2.0);
        expResult.put("words altered, added,", 2.0);
        expResult.put("altered, added, and", 2.0);
        expResult.put("added, and removed", 2.0);
        expResult.put("and removed that", 2.0);
        expResult.put("removed that make", 2.0);
        expResult.put("that make it", 2.0);
        expResult.put("make it nonsensical", 2.0);
        expResult.put("it nonsensical and", 2.0);
        expResult.put("nonsensical and not", 2.0);
        expResult.put("and not proper", 2.0);
        expResult.put("not proper Latin.[1]", 2.0);
        expResult.put("proper Latin.[1] In", 1.0);
        expResult.put("Latin.[1] In publishing", 1.0);
        
        Map<String, Double> result = instance.extract(text);
        assertEquals(expResult, result);
    }

    /**
     * Test of numberOfOccurrences method, of class NgramsExtractor.
     */
    @Test
    public void testNumberOfOccurrences() {
        System.out.println("numberOfOccurrences");
        String keyword = "lorem ipsum is";
        String text = "In publishing and graphic design, lorem ipsum is a placeholder text and used to demonstrate. In publishing and graphic design, lorem ipsum is a placeholder text and used to demonstrate.";
        NgramsExtractor instance = new NgramsExtractor();
        instance.setParameters(new NgramsExtractor.Parameters());
        double expResult = 2.0;
        double result = instance.numberOfOccurrences(keyword, text);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }
    
}
