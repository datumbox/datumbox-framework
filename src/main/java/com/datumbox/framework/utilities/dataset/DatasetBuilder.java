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
package com.datumbox.framework.utilities.dataset;

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.configuration.GeneralConfiguration;
import com.datumbox.framework.utilities.text.cleaners.StringCleaner;
import com.datumbox.framework.utilities.text.extractors.TextExtractor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class DatasetBuilder {
    
    public static Map<Object, List<String>> stringListsFromTextFiles(Map<Object, URI> dataset) {
        Map<Object, List<String>> listsMap = new HashMap<>();
        for(Map.Entry<Object, URI> entry : dataset.entrySet()) {
            Object theClass = entry.getKey();
            URI datasetURI = entry.getValue();
            
            List<String> stringList = new ArrayList<>();
            try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(datasetURI)), "UTF8"))) {
                //read strings one by one
                for(String line; (line = br.readLine()) != null; ) {
                    stringList.add(line);
                }
            } 
            catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            
            listsMap.put(theClass, stringList);
        }
        return listsMap;
    }
    
    public static Dataset parseFromTextLists(Map<Object, List<String>> dataset, TextExtractor textExtractor) {
        Dataset data = new Dataset();
        
        //loop throw the map and process each category file
        for(Map.Entry<Object, List<String>> entry : dataset.entrySet()) {
            Object theClass = entry.getKey();
            List<String> textList = entry.getValue();
            
            if(GeneralConfiguration.DEBUG) {
                System.out.println("Dataset Parsing "+theClass);
            }
            
            Dataset classDataset = new Dataset();
            for(String text : textList) {
                Record r = new Record();
                r.setY(theClass);

                //extract features of the string and add every keyword combination in X map
                r.getX().putAll(textExtractor.extract(StringCleaner.clear(text)));
                
                //add each example in the classDataset
                classDataset.add(r); 
            }
            
            
            //merge it with the training dataset
            data.merge(classDataset);
            classDataset = null;
        }    

        return data;
    }
    
    public static Dataset parseFromTextFiles(Map<Object, URI> dataset, TextExtractor textExtractor) {
        Dataset data = new Dataset();
        
        //loop throw the map and process each category file
        for(Map.Entry<Object, URI> entry : dataset.entrySet()) {
            Object theClass = entry.getKey();
            URI datasetURI = entry.getValue();
            
            if(GeneralConfiguration.DEBUG) {
                System.out.println("Dataset Parsing "+theClass);
            }
            
            //process the files line-by-line, assuming there is a single document/case per row
            Dataset classDataset = new Dataset();
            try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(datasetURI)), "UTF8"))) {
                //read strings one by one
                for(String line; (line = br.readLine()) != null; ) {
                    Record r = new Record();
                    r.setY(theClass);
                    
                    //extract features of the string and add every keyword combination in X map
                    r.getX().putAll(textExtractor.extract(StringCleaner.clear(line)));
                    
                    //add each example in the classDataset
                    classDataset.add(r); 
                }
            } 
            catch (IOException ex) {
                throw new RuntimeException(ex);
            } 
            
            //merge it with the training dataset
            data.merge(classDataset);
            classDataset = null;
        }    

        return data;
    }
    
}
