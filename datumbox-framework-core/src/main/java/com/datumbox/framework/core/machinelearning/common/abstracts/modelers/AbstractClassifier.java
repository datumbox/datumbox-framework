/**
 * Copyright (C) 2013-2020 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
package com.datumbox.framework.core.machinelearning.common.abstracts.modelers;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.dataobjects.AssociativeArray;
import com.datumbox.framework.common.storage.interfaces.StorageEngine;
import com.datumbox.framework.core.common.utilities.MapMethods;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;

import java.util.*;

/**
 * Base Class for all the Classifier algorithms.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public abstract class AbstractClassifier<MP extends AbstractClassifier.AbstractModelParameters, TP extends AbstractClassifier.AbstractTrainingParameters> extends AbstractModeler<MP, TP> {

    /** {@inheritDoc} */
    public static abstract class AbstractModelParameters extends AbstractModeler.AbstractModelParameters {
        
        //Set with all the supported classes. Use LinkedHashSet to ensure that the order of classes will be maintained. Some method requires that (ordinal regression)
        private Set<Object> classes = new LinkedHashSet<>();
        
        /** 
         * @param storageEngine
         * @see AbstractModeler.AbstractModelParameters#AbstractModelParameters(StorageEngine)
         */
        protected AbstractModelParameters(StorageEngine storageEngine) {
            super(storageEngine);
        }
        
        /**
         * Returns the size of the classes.
         * 
         * @return 
         */
        public Integer getC() {
            return classes.size();
        }

        /**
         * Getter for the set of classes.
         * 
         * @return 
         */
        public Set<Object> getClasses() {
            return classes;
        }
        
        /**
         * Setter for the set of classes.
         * 
         * @param classes 
         */
        protected void setClasses(Set<Object> classes) {
            this.classes = classes;
        }
        
    }

    /**
     * @param trainingParameters
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(AbstractTrainingParameters, Configuration)
     */
    protected AbstractClassifier(TP trainingParameters, Configuration configuration) {
        super(trainingParameters, configuration);
    }

    /**
     * @param storageName
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(String, Configuration)
     */
    protected AbstractClassifier(String storageName, Configuration configuration) {
        super(storageName, configuration);
    }

    /**
     * Estimates the selected class from the prediction scores.
     * 
     * @param predictionScores
     * @return 
     */
    protected Object getSelectedClassFromClassScores(AssociativeArray predictionScores) {
        Map.Entry<Object, Object> maxEntry = MapMethods.selectMaxKeyValue(predictionScores);
        
        return maxEntry.getKey();
    }

}