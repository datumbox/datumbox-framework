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
package com.datumbox.framework.algorithms.dea;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.FlatDataList;
import com.datumbox.framework.mathematics.linearprogramming.LPSolver;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lpsolve.LpSolve;
import lpsolve.LpSolveException;

/**
 * Class that performs Data Envelopment Analysis. It requires the installation of
 * lpsolve 5.5.2.0. http://lpsolve.sourceforge.net/5.5/
 * References: http://en.wikipedia.org/wiki/Data_envelopment_analysis and http://deazone.com/en/resources/tutorial/weights-to-determine-relative-efficiency
 * 
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class DataEnvelopmentAnalysis {
    /**
     * The internalDataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the internalDataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
    /**
    * DEA method separates the features of the observations in input and output; 
    * The DeaRecord is a wrapper Object which stores the input and output parts of our
    * data points.
    * 
    * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
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
         * Getter for input
         * 
         * @return  Returns the input part of our data.
         */
        public double[] getInput() {
            return input;
        }

        /**
         * Getter for output
         * 
         * @return  Returns the output part of our data.
         */
        public double[] getOutput() {
            return output;
        }
    }
    
    /**
     * Estimates the efficiency of the records by running DEA
     * 
     * @param id2DeaRecordMapDatabase   AssociativeArray with the DeaRecords
     * @param id2DeaRecordMapEvaluation
     * @return          Map with the scores of the records 
     */
    public AssociativeArray estimateEfficiency(Map<Object, DeaRecord> id2DeaRecordMapDatabase, Map<Object, DeaRecord> id2DeaRecordMapEvaluation) {        
        AssociativeArray evaluatedResults = new AssociativeArray(new LinkedHashMap<>());
        
        List<LPSolver.LPConstraint> constraints = new ArrayList<>();
        
        //initialize the constraints list
        Integer totalColumns = null;
        boolean hasInput = false;
        for(Map.Entry<Object, DeaRecord> entry : id2DeaRecordMapDatabase.entrySet()) {
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
                constraints.add(new LPSolver.LPConstraint(currentRecord.getOutput(), LpSolve.LE, 1.0)); //less than 1
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
                conOutput=null;
                conInput=null;
                
                //add the constrain on the list
                constraints.add(new LPSolver.LPConstraint(currentConstraintBody, LpSolve.LE, 0.0)); //less than 0
            }    
        }
        
        
        for(Map.Entry<Object, DeaRecord> entry : id2DeaRecordMapEvaluation.entrySet()) {
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
                conInput=null;
                conOutput=null;
                
                //set the denominator equal to 1
                constraints.add(new LPSolver.LPConstraint(denominatorConstraintBody, LpSolve.EQ, 1.0));                
            }
            
            double[] lowBoundsOfVariables = null;
            double[] upBoundsOfVariables = null;
            boolean[] strictlyIntegerVariables = null;
            
            /*
            lowBoundsOfVariables = new double[totalColumns];
            upBoundsOfVariables = new double[totalColumns];
            strictlyIntegerVariables = new boolean[totalColumns];
            for(int i =0; i<totalColumns;++i) {
                lowBoundsOfVariables[i]=0;
                upBoundsOfVariables[i]=Double.MAX_VALUE;
                strictlyIntegerVariables[i]=false;
            }
            */
            
            Integer scalingMode = LpSolve.SCALE_GEOMETRIC;
            
            //RUN SOLVE
            Double objectiveValue = null;
            try {
                LPSolver.LPResult result = LPSolver.solve(objectiveFunction, constraints, lowBoundsOfVariables, upBoundsOfVariables, strictlyIntegerVariables, scalingMode);
                objectiveValue = result.getObjectiveValue();
            } 
            catch (LpSolveException ex) {
                throw new RuntimeException(ex);
            }
            
            if(hasInput) {
                constraints.remove(constraints.size()-1); //remove the last constraint that you put it
            }
            
            evaluatedResults.put(currentRecordId, objectiveValue);
            
        }
        
        
        return evaluatedResults;
    }
}
