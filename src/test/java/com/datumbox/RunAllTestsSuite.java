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
package com.datumbox;

import java.io.IOException;
import org.junit.extensions.cpsuite.ClasspathSuite;
import org.junit.extensions.cpsuite.ClasspathSuite.SuiteTypes;
import org.junit.extensions.cpsuite.SuiteType;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;


/**
 * Test Suite that runs all the tests of the project.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
@RunWith(ClasspathSuite.class)
@SuiteTypes({SuiteType.TEST_CLASSES})
public class RunAllTestsSuite {
    
    /**
     * Executes in a serial manner all the tests of the project.
     * 
     * @param args
     * @throws InterruptedException 
     * @throws java.io.IOException 
     */
    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println("Datumbox Framework Tests");
        System.out.println("Press any key to continue...");
        System.in.read();  
        JUnitCore.runClasses(RunAllTestsSuite.class);
        System.out.println("Finished");
        System.out.println("Press any key to continue...");
        System.in.read();  
    }
    
}
