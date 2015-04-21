/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
package com.datumbox.framework.mathematics.linearprogramming;

import com.datumbox.configuration.TestConfiguration;
import com.datumbox.tests.bases.BaseTest;
import com.datumbox.tests.utilities.TestUtils;
import java.util.ArrayList;
import java.util.List;
import lpsolve.LpSolve;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class LPSolverTest extends BaseTest {

    /**
     * Test of solve method, of class LPSolver.
     */
    @Test
    public void testSolve() throws Exception {
        logger.info("solve");
        
        //Example from http://lpsolve.sourceforge.net/5.5/PHP.htm
        double[] linearObjectiveFunction = {143.0, 60.0, 195.0};
        List<LPSolver.LPConstraint> linearConstraintsList = new ArrayList<>();
        linearConstraintsList.add(new LPSolver.LPConstraint(new double[]{120.0, 210.0, 150.75}, LpSolve.LE, 15000.0));
        linearConstraintsList.add(new LPSolver.LPConstraint(new double[]{110.0, 30.0, 125.0}, LpSolve.LE, 4000.0));
        linearConstraintsList.add(new LPSolver.LPConstraint(new double[]{1.0, 1.0, 1.0}, LpSolve.LE, 75.0));
        
        double[] lowBoundsOfVariables = null;
        double[] upBoundsOfVariables = null;
        boolean[] strictlyIntegerVariables = null;
        Integer scalingMode = LpSolve.SCALE_EXTREME;
        
        LPSolver.LPResult expResult = new LPSolver.LPResult(3, 3);
        expResult.setObjectiveValue((Double) 6986.8421052632);
        expResult.setVariableValues(new double[]{0.0, 56.578947368421, 18.421052631579});
        expResult.setDualSolution(new double[]{1.0, 0.0, 1.4210526315789, 17.368421052632, -30.684210526315788, 0.0, 0.0});
        
        LPSolver.LPResult result = LPSolver.solve(linearObjectiveFunction, linearConstraintsList, lowBoundsOfVariables, upBoundsOfVariables, strictlyIntegerVariables, scalingMode);
        assertEquals(expResult.getObjectiveValue(), result.getObjectiveValue(), TestConfiguration.DOUBLE_ACCURACY_HIGH);
        assertArrayEquals(expResult.getVariableValues(), result.getVariableValues(), TestConfiguration.DOUBLE_ACCURACY_HIGH);
        assertArrayEquals(expResult.getDualSolution(), result.getDualSolution(), TestConfiguration.DOUBLE_ACCURACY_HIGH);
        
    }
    
}
