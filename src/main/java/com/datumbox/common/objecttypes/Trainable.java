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
package com.datumbox.common.objecttypes;

import com.datumbox.configuration.MemoryConfiguration;

/**
 * This interface is used to mark classes that can be trained and generate a
 * storable database. This interface is meant to be used for classes that
 * perform training/analysis and learn parameters. It does not specify the 
 * training and prediction methods because their signatures heavily depend on
 * the algorithm.
 * 
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public interface Trainable<MP extends Learnable, TP extends Parameterizable> {
    
    /**
     * Get the name of the database.
     * 
     * @return 
     */
    public String getDBname();
    
    /**
     * Short name of the method used for naming the database.
     * 
     * @return 
     */
    public String shortMethodName();
    
    /**
     * Checks if a classifier with the same name already exists.
     * 
     * @return 
     */
    public boolean alreadyExists();

    /**
     * Sets the memory configuration object (Map types, LRU etc).
     * 
     * @param memoryConfiguration 
     */
    public void setMemoryConfiguration(MemoryConfiguration memoryConfiguration);
    
    /**
     * Deletes the particular algorithm and all the associated parameters. 
     * The boolean determines whether the erase will drop the database completely 
     * or the tables. In some db systems dropping the database is very slow and 
     * thus dropping the tables instead is advised.
     * 
     * @param complete
     */
    public void erase(boolean complete);
    
    /**
     * Returns the model parameters that were estimated after training.
     * 
     * @return
     */
    public MP getModelParameters();

    /**
     * It returns the trainingParameters.
     * 
     * @return 
     */
    public TP getTrainingParametersObject();

    /**
     * It returns an empty object with the trainingParameters required by the 
     * algorithm.
     * 
     * @return 
     */
    public TP getEmptyTrainingParametersObject();
    
    /**
     * Sets both TrainingParameters and MemoryConfiguration. MUST be called BEFORE 
     * calling the train(), kFoldCrossValidation() methods.
     * @param trainingParameters
     * @param memoryConfiguration 
     */
    public void initializeTrainingConfiguration(MemoryConfiguration memoryConfiguration, TP trainingParameters);
    
    /**
     * Getter for temporary flag which indicates that the creation of the model
     * is temporary. This flag is used to prevent the algorithm from saving its
     * results to the db and wasting IO. The flag is turned on in cases such as
     * the k-fold cross validation where we don't keep the produced algorithms.
     * 
     * @return 
     */
    public boolean isTemporary();
    
    /**
     * Setter for the temporary flag.
     * 
     * @param temporary 
     */
    public void setTemporary(boolean temporary);
}
