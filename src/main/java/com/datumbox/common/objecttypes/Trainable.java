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
 * This interface is used to mark classes that can be trained. This interface 
 * used for classes that perform training/analysis and learn parameters. 
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
    public void fit(Dataset trainingData, TP trainingParameters);
    
    /**
     * Returns whether the algorithm modifies the provided data.
     * 
     * @return 
     */
    public boolean modifiesData();
            
    /**
     * Deletes the database of the algorithm. 
     */
    public void erase();
}
