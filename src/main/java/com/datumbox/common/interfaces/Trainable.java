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
package com.datumbox.common.interfaces;

import com.datumbox.common.Configuration;
import com.datumbox.common.dataobjects.Dataframe;
import com.datumbox.framework.machinelearning.common.abstracts.AbstractTrainer;
import java.lang.reflect.InvocationTargetException;

/**
 * This interface is used to mark classes that can be trained. This interface 
 * used for classes that perform training/analysis and learn parameters. 
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public interface Trainable<MP extends Learnable, TP extends Parameterizable> extends AutoCloseable {

    /**
     * Generates a new instance of a AbstractTrainer by providing the Class of 
     * the algorithm.
     * 
     * @param <BT>
     * @param aClass
     * @param dbName
     * @param conf
     * @return 
     */
    public static <BT extends AbstractTrainer> BT newInstance(Class<BT> aClass, String dbName, Configuration conf) {
        BT algorithm = null;
        try {
            algorithm = aClass.getConstructor(String.class, Configuration.class).newInstance(dbName, conf);
        } 
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException(ex);
        }

        return algorithm;
    }
    
    /**
     * Returns the model parameters that were estimated after training.
     * 
     * @return
     */
    public MP getModelParameters();

    /**
     * It returns the training parameters that configure the algorithm.
     * 
     * @return 
     */
    public TP getTrainingParameters();
    
    /**
     * Trains a model using the provided training parameters and data.
     * 
     * @param trainingData
     * @param trainingParameters
     */
    public void fit(Dataframe trainingData, TP trainingParameters);
            
    /**
     * Deletes the database of the algorithm. 
     */
    public void delete();
    
}
