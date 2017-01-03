/**
 * Copyright (C) 2013-2017 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
package com.datumbox.framework.core.mathematics.linearprogramming;

import com.datumbox.framework.common.dataobjects.AssociativeArray;
import com.datumbox.framework.common.dataobjects.FlatDataList;
import com.datumbox.framework.core.common.utilities.PHPMethods;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Class that performs Data Envelopment Analysis. It requires the installation of
 * lpsolve 5.5.2.0. http://lpsolve.sourceforge.net/5.5/
 * 
 * References: 
 * http://en.wikipedia.org/wiki/Data_envelopment_analysis 
 * http://deazone.com/en/resources/tutorial/weights-to-determine-relative-efficiency
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class DataEnvelopmentAnalysis {
    
    /**
    * DEA method separates the features of the observations in input and output; 
    * The DeaRecord is a wrapper Object which stores the input and output parts of our
    * data points.
    */
    public static class DeaRecord {
        /**
         * The values of features that are considered as input. It can be empty if 
         * if no input is assumed. 
         */
        private final double[] input;

        /**
         * The values of features that are considered as output.
         */
        private final double[] output;

        /**
         * Constructor of DEA Record with only output
         * 
         * @param output    The output part of our data record
         */
        public DeaRecord(FlatDataList output) {
            this.output=new double[output.size()];

            int column=0;
            Iterator<Double> it = output.iteratorDouble();
            while(it.hasNext()) {
                this.output[column]=it.next();
                ++column;
            }

            this.input= new double[0];
        }

        /**
         * Constructor of DEA Record with both input and output
         * 
         * @param output    The output part of our data record
         * @param input     The input part of our data record
         */
        public DeaRecord(FlatDataList output, FlatDataList input) {
            this.output=new double[output.size()];
            int column=0;
            Iterator<Double> it = output.iteratorDouble();
            while(it.hasNext()) {
                this.output[column]=it.next();
                ++column;
            }

            this.input=new double[input.size()];
            column=0;
            it = input.iteratorDouble();
            while(it.hasNext()) {
                this.input[column]=it.next();
                ++column;
            }
        }

        /**
         * Getter for input.
         * 
         * @return  Returns the input part of our data.
         */
        public double[] getInput() {
            return PHPMethods.array_clone(input);
        }

        /**
         * Getter for output.
         * 
         * @return  Returns the output part of our data.
         */
        public double[] getOutput() {
            return PHPMethods.array_clone(output);
        }
    }
    
    /**
     * Estimates the efficiency of the records by running DEA
     * 
     * @param id2DeaRecordMapInput   AssociativeArray with the DeaRecords
     * @param id2DeaRecordMapOutput
     * @return          Map with the scores of the records 
     */
    public AssociativeArray estimateEfficiency(Map<Object, DeaRecord> id2DeaRecordMapInput, Map<Object, DeaRecord> id2DeaRecordMapOutput) {
        AssociativeArray evaluatedResults = new AssociativeArray();
        
        List<LPSolver.LPConstraint> constraints = new ArrayList<>();
        
        //initialize the constraints list
        Integer totalColumns = null;
        boolean hasInput = false;
        for(Map.Entry<Object, DeaRecord> entry : id2DeaRecordMapInput.entrySet()) {
            DeaRecord currentRecord = entry.getValue();
            int currentColumns = currentRecord.getInput().length; //add the size of input array
            boolean currentHasInput=(currentColumns > 0); //check if the input is defined
            
            currentColumns+=currentRecord.getOutput().length; //add the size of output array
            
            
            if(totalColumns==null) { //if totalColumns is not set, then set them
                //the totalColumns is the sum of input and output columns
                totalColumns=currentColumns;
                hasInput=currentHasInput;
            }
            else { //if totalColumns is initialized, validate that the record has exactly this amount of columns
                if(totalColumns!=currentColumns) {
                    throw new IllegalArgumentException("The input and output columns do not match in all records.");
                }
                if(hasInput!=currentHasInput) {
                    throw new IllegalArgumentException("The input should be used in all records or in none.");
                }
            }
            
            //We have two cases. Either an input is defined for the records or not.
            //The mathematical model is formulated differently depending the case
            if(hasInput==false) {
                //if no input then change the way that the linear problem formulates
                constraints.add(new LPSolver.LPConstraint(currentRecord.getOutput(), LPSolver.LEQ, 1.0)); //less than 1
            }
            else {
                //create a double[] with size both of the input and output
                double[] currentConstraintBody = new double[totalColumns];
                
                //set the values of output first on the new array
                double[] conOutput=currentRecord.getOutput();
                for(int i=0;i<conOutput.length;++i) {
                    currentConstraintBody[i]=conOutput[i];
                }
                
                //now set the input by negatiting the values
                double[] conInput=currentRecord.getInput();
                for(int i=0;i<conInput.length;++i) {
                    currentConstraintBody[conOutput.length+i]=-conInput[i];
                }
                //conOutput=null;
                //conInput=null;
                
                //add the constrain on the list
                constraints.add(new LPSolver.LPConstraint(currentConstraintBody, LPSolver.LEQ, 0.0)); //less than 0
            }    
        }
        
        
        for(Map.Entry<Object, DeaRecord> entry : id2DeaRecordMapOutput.entrySet()) {
            Object currentRecordId = entry.getKey();
            DeaRecord currentRecord = entry.getValue();
            
            double[] objectiveFunction;
            if(hasInput==false) {
                //set the Objection function equal to the output of the record
                objectiveFunction=currentRecord.getOutput();
            }
            else {
                //create a double[] with size both of the input and output
                objectiveFunction = new double[totalColumns];
                double[] denominatorConstraintBody = new double[totalColumns];
                
                //set the values of output first on the new array
                double[] conOutput=currentRecord.getOutput();
                for(int i=0;i<conOutput.length;++i) { 
                    objectiveFunction[i]=conOutput[i]; //set the output to the objective function
                    denominatorConstraintBody[i]=0.0; //set zero to the constraint
                }
                
                //set the values of input first on the new array
                double[] conInput=currentRecord.getInput();
                for(int i=0;i<conInput.length;++i) {
                    objectiveFunction[conOutput.length+i]=0.0; //set zeros on objective function for input
                    denominatorConstraintBody[conOutput.length+i]=conInput[i]; //set the input to the constraint
                }
                //conInput=null;
                //conOutput=null;
                
                //set the denominator equal to 1
                constraints.add(new LPSolver.LPConstraint(denominatorConstraintBody, LPSolver.EQ, 1.0));
            }
            
            //RUN SOLVE
            LPSolver.LPResult result = LPSolver.solve(objectiveFunction, constraints, true, true);
            Double objectiveValue = result.getObjectiveValue();
            
            if(hasInput) {
                constraints.remove(constraints.size()-1); //remove the last constraint that you put it
            }
            
            evaluatedResults.put(currentRecordId, objectiveValue);
            
        }
        
        
        return evaluatedResults;
    }
}
