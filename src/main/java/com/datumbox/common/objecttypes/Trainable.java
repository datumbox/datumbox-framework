/* 
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
package com.datumbox.common.objecttypes;

import com.datumbox.common.dataobjects.Dataset;

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
    public TP getTrainingParameters();
    
    /**
     * Trains the model.
     * 
     * @param trainingData
     * @param trainingParameters
     */
    public void fit(Dataset trainingData, TP trainingParameters);
    
    /**
     * Returns whether the algorithm modifies the data.
     * 
     * @return 
     */
    public boolean modifiesData();
            
    /**
     * Deletes the particular algorithm and all the associated parameters. 
     */
    public void erase();
}
