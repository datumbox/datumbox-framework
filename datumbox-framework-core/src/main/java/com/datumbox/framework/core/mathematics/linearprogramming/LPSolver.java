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
package com.datumbox.framework.core.mathematics.linearprogramming;

import com.datumbox.framework.common.utilities.PHPMethods;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.linear.*;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

import java.util.ArrayList;
import java.util.List;

/**
 * The LPSolver provides an easy way to formulate and solve Linear Programming
 * problems.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class LPSolver {
    
    /**
     * The Result class of the LP problem.
     */
    public static class LPResult {
        private Double objectiveValue;
        private double[] variableValues;
        
        /**
         * Getter for the Objective value.
         * 
         * @return 
         */
        public Double getObjectiveValue() {
            return objectiveValue;
        }
        
        /**
         * Setter for the Objective value.
         * 
         * @param objectiveValue 
         */
        protected void setObjectiveValue(Double objectiveValue) {
            this.objectiveValue = objectiveValue;
        }
        
        /**
         * Getter for the values of the Variables.
         * 
         * @return 
         */
        public double[] getVariableValues() {
            return PHPMethods.array_clone(variableValues);
        }
        
        /**
         * Setter for the values of the Variables.
         * 
         * @param variableValues 
         */
        protected void setVariableValues(double[] variableValues) {
            this.variableValues = PHPMethods.array_clone(variableValues);
        }
    }
    
    /**
     * The LP Constraint is an internal class which stores the the main body of
     * the constraint, the sign and the right value.
     */
    public static class LPConstraint {
        /**
         * The body of the constraint; the left part of the constraint.
         */
        private final double[] contraintBody;
        
        /**
         * The sign symbol: ">=", "<=", "=".
         */
        private final String sign;
        
        /**
         * The value of the constraint; the right part of the constraint.
         */
        private final double value;
        
        /**
         * The constructor of LP Constraint.
         * 
         * @param constraintBody    The array with the parameters of the constrain
         * @param sign              The sign of the constrain
         * @param value             The right part value of the constrain
         */
        public LPConstraint(double[] constraintBody, String sign, double value) {
            this.contraintBody = PHPMethods.array_clone(constraintBody);
            this.sign = sign;
            this.value=value;
        }

        /**
         * Getter for the body of the constraint.
         * 
         * @return 
         */
        public double[] getContraintBody() {
            return PHPMethods.array_clone(contraintBody);
        }
        
        /**
         * Getter for the sign of the constraint.
         * 
         * @return 
         */
        public String getSign() {
            return sign;
        }
        
        /**
         * Getter for the value of the constraint.
         * 
         * @return 
         */
        public double getValue() {
            return value;
        }
    }
    
    /**
     * Solves the LP problem and returns the result.
     * 
     * @param linearObjectiveFunction
     * @param linearConstraintsList
     * @param nonNegative
     * @param maximize
     * @return
     */
    public static LPResult solve(double[] linearObjectiveFunction, List<LPSolver.LPConstraint> linearConstraintsList, boolean nonNegative, boolean maximize) {
        int m = linearConstraintsList.size();

        List<LinearConstraint> constraints = new ArrayList<>(m);
        for(LPSolver.LPConstraint constraint : linearConstraintsList) {
            String sign = constraint.getSign();
            Relationship relationship = null;
            if(">=".equals(sign)) {
                relationship = Relationship.GEQ;
            }
            else if("<=".equals(sign)) {
                relationship = Relationship.LEQ;
            }
            else if("=".equals(sign)) {
                relationship = Relationship.EQ;
            }
            constraints.add(
                    new LinearConstraint(constraint.getContraintBody(),
                    relationship,
                    constraint.getValue())
            );
        }

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(
                new LinearObjectiveFunction(linearObjectiveFunction, 0.0),
                new LinearConstraintSet(constraints),
                maximize?GoalType.MAXIMIZE:GoalType.MINIMIZE,
                new NonNegativeConstraint(nonNegative),
                PivotSelectionRule.BLAND
        );

        LPResult result = new LPResult();
        result.setObjectiveValue(solution.getValue());
        result.setVariableValues(solution.getPoint());

        return result;
    }
}
