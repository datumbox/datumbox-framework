/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis at datumbox.com>
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

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        Logger logger = LoggerFactory.getLogger(DatasetBuilder.class);
        
        //loop throw the map and process each category file
        for(Map.Entry<Object, List<String>> entry : dataset.entrySet()) {
            Object theClass = entry.getKey();
            List<String> textList = entry.getValue();
            
            logger.info("Dataset Parsing "+theClass);
            
            for(String text : textList) {
                //extract features of the string and add every keyword combination in X map
                data.add(new Record(new AssociativeArray(textExtractor.extract(StringCleaner.clear(text))), theClass)); 
            }
        }    

        return data;
    }
    
    public static Dataset parseFromTextFiles(Map<Object, URI> dataset, TextExtractor textExtractor) {
        Dataset data = new Dataset();
        Logger logger = LoggerFactory.getLogger(DatasetBuilder.class);
        
        //loop throw the map and process each category file
        for(Map.Entry<Object, URI> entry : dataset.entrySet()) {
            Object theClass = entry.getKey();
            URI datasetURI = entry.getValue();
            
            logger.info("Dataset Parsing "+theClass);
            
            //process the files line-by-line, assuming there is a single document/case per row
            try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(datasetURI)), "UTF8"))) {
                //read strings one by one
                for(String line; (line = br.readLine()) != null; ) {
                    //extract features of the string and add every keyword combination in X map
                    data.add(new Record(new AssociativeArray(textExtractor.extract(StringCleaner.clear(line))), theClass)); 
                }
            } 
            catch (IOException ex) {
                throw new RuntimeException(ex);
            } 
        }    

        return data;
    }
    
}
