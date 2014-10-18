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
package com.datumbox.framework.mathematics.linearprogramming;

import com.datumbox.framework.mathematics.linearprogramming.LPSolver;
import com.datumbox.configuration.TestConfiguration;
import java.util.ArrayList;
import java.util.List;
import lpsolve.LpSolve;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class LPSolverTest {
    
    public LPSolverTest() {
    }

    /**
     * Test of solve method, of class LPSolver.
     */
    @Test
    public void testSolve() throws Exception {
        System.out.println("solve");
        
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
