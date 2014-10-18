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
package com.datumbox.framework.machinelearning.common.bases.wrappers;

import com.datumbox.common.objecttypes.Parameterizable;
import com.datumbox.common.persistentstorage.factories.BigDataStructureFactory;
import com.datumbox.common.persistentstorage.interfaces.BigDataStructureContainer;
import com.datumbox.configuration.MemoryConfiguration;
import com.datumbox.configuration.StorageConfiguration;
import com.datumbox.framework.machinelearning.common.bases.BaseTrainable;
import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLmodel;
import com.datumbox.framework.machinelearning.common.dataobjects.TrainableKnowledgeBase;
import com.datumbox.framework.machinelearning.common.bases.datatransformation.DataTransformer;
import com.datumbox.framework.machinelearning.common.bases.featureselection.FeatureSelection;
import java.lang.reflect.InvocationTargetException;

/**
 * The BaseWrapper is a trainable object that uses composition instead of inheritance
 to extend the functionality of a BaseMLmodel. It includes various internal objects
 * and defines differently the training and prediction process. Even though it 
 * is trainable the methods of train(), predict(), etc are not specified in the
 * Trainable interface to give the freedom of defining truly custom expansions
 * on the original models.
 * 
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public abstract class BaseWrapper<MP extends BaseWrapper.ModelParameters, TP extends BaseWrapper.TrainingParameters> extends BaseTrainable<MP, TP, TrainableKnowledgeBase<MP, TP>> {

    
    //internal objects
    protected DataTransformer dataTransformer = null;
    protected FeatureSelection featureSelection = null;
    protected BaseMLmodel mlmodel = null;
    

    public static abstract class ModelParameters implements BigDataStructureContainer {

        @Override
        public void bigDataStructureInitializer(BigDataStructureFactory bdsf, MemoryConfiguration memoryConfiguration) {
            
        }
        
        @Override
        public void bigDataStructureCleaner(BigDataStructureFactory bdsf) {
            
        }
        
    }
    
    public static abstract class TrainingParameters<DT extends DataTransformer, FS extends FeatureSelection, ML extends BaseMLmodel> implements Parameterizable, TrainableKnowledgeBase.SelfConstructible<BaseWrapper.TrainingParameters> {
        
        /**
         * This method allows us to build a new empty object of the current object
         * directly from it. Casting to the appropriate type is required.
         * 
         * @return 
         */
        @Override
        public BaseWrapper.TrainingParameters getEmptyObject() {
            try {
                return this.getClass().getConstructor().newInstance();
            } 
            catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
                throw new RuntimeException(ex);
            }
        }
        
        
        //Classes
        private Class<? extends DT> dataTransformerClass;

        private Class<? extends FS> featureSelectionClass;
        
        private Class<? extends ML> mlmodelClass;
        
        

        //Parameter Objects
        private DT.TrainingParameters dataTransformerTrainingParameters;
        
        private FS.TrainingParameters featureSelectionTrainingParameters;
        
        private ML.TrainingParameters mlmodelTrainingParameters;

        //Getters/Setters
        public Class<? extends DT> getDataTransformerClass() {
            return dataTransformerClass;
        }

        public void setDataTransformerClass(Class<? extends DT> dataTransformerClass) {
            this.dataTransformerClass = dataTransformerClass;
        }

        public Class<? extends FS> getFeatureSelectionClass() {
            return featureSelectionClass;
        }

        public void setFeatureSelectionClass(Class<? extends FS> featureSelectionClass) {
            this.featureSelectionClass = featureSelectionClass;
        }

        public Class<? extends ML> getMLmodelClass() {
            return mlmodelClass;
        }

        public void setMLmodelClass(Class<? extends ML> mlmodelClass) {
            this.mlmodelClass = mlmodelClass;
        }

        public DT.TrainingParameters getDataTransformerTrainingParameters() {
            return dataTransformerTrainingParameters;
        }

        public void setDataTransformerTrainingParameters(DT.TrainingParameters dataTransformerTrainingParameters) {
            this.dataTransformerTrainingParameters = dataTransformerTrainingParameters;
        }

        public FS.TrainingParameters getFeatureSelectionTrainingParameters() {
            return featureSelectionTrainingParameters;
        }

        public void setFeatureSelectionTrainingParameters(FS.TrainingParameters featureSelectionTrainingParameters) {
            this.featureSelectionTrainingParameters = featureSelectionTrainingParameters;
        }

        public ML.TrainingParameters getMLmodelTrainingParameters() {
            return mlmodelTrainingParameters;
        }

        public void setMLmodelTrainingParameters(ML.TrainingParameters mlmodelTrainingParameters) {
            this.mlmodelTrainingParameters = mlmodelTrainingParameters;
        }
        
        
    }

    
    public static <W extends BaseWrapper> W newInstance(Class<W> aClass, String dbName) {
        W algorithm = null;
        try {
            algorithm = (W) aClass.getConstructor(String.class).newInstance(dbName);
        } 
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
        
        return algorithm;
    }
    

    /*
        IMPORTANT METHODS FOR THE FUNCTIONALITY
    */
    protected BaseWrapper(String dbName, Class<MP> mpClass, Class<TP> tpClass) {
        String methodName = shortMethodName(); //this.getClass().getSimpleName();
        if(!dbName.contains(methodName)) { //patch for loading the dbname directly
            dbName += StorageConfiguration.getDBnameSeparator() + methodName;
        }
        
        this.dbName = dbName;
        knowledgeBase = new TrainableKnowledgeBase<>(dbName, mpClass, tpClass);
        knowledgeBase.setOwnerClass(this.getClass());
    }
      
    
    @Override
    public void erase(boolean complete) {
        if(dataTransformer!=null) {
            dataTransformer.erase(complete);
        }
        if(featureSelection!=null) {
            featureSelection.erase(complete);
        }
        if(mlmodel!=null) {
            mlmodel.erase(complete);
        }
        knowledgeBase.erase(complete);
    }

    @Override
    public void setTemporary(boolean temporary) {
        if(dataTransformer!=null) {
            dataTransformer.setTemporary(temporary);
        }
        if(featureSelection!=null) {
            featureSelection.setTemporary(temporary);
        }
        if(mlmodel!=null) {
            mlmodel.setTemporary(temporary);
        }
        this.temporary = temporary;
    }
}
