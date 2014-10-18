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
package com.datumbox.framework.statistics.anova;

import com.datumbox.common.dataobjects.AssociativeArray2D;
import com.datumbox.common.dataobjects.FlatDataCollection;
import com.datumbox.common.dataobjects.TransposeDataCollection;
import com.datumbox.common.dataobjects.TransposeDataCollection2D;
import com.datumbox.framework.statistics.descriptivestatistics.Descriptives;
import com.datumbox.framework.statistics.distributions.ContinuousDistributions;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author bbriniotis
 */
public class Anova {
    /**
     * The internalDataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the internalDataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
    /**
     * Performs One-Way ANOVA Test on the Transpose Data Table when variances are 
     * considered equal. Pass an empty array on the $outputTable variable to get 
     * statistics concerning the ANOVA.
     * 
     * @param transposeDataCollection
     * @param aLevel
     * @param outputTable
     * @return
     * @throws IllegalArgumentException 
     */
    public static boolean oneWayTestEqualVars(TransposeDataCollection transposeDataCollection, double aLevel, AssociativeArray2D outputTable) throws IllegalArgumentException {
        int n = 0;
        int k = transposeDataCollection.size();
        if(k<=1) {
            throw new IllegalArgumentException();
        }

        Map<Object, Integer> nj = new HashMap<>();
        double Ymean = 0.0; //Total Y mean for 
        Map<Object, Double> Yjmean = new HashMap<>();
        for(Map.Entry<Object, FlatDataCollection> entry : transposeDataCollection.entrySet()) {
            Object j = entry.getKey();
            FlatDataCollection flatDataCollection = entry.getValue();
            
            int m = flatDataCollection.size();
            if(m==0) {
                throw new IllegalArgumentException();
            }
            nj.put(j, m); //get the number of observation for this category
            n+=m;
            
            double sum = Descriptives.sum(flatDataCollection);
            
            Yjmean.put(j, sum/m);
            Ymean+=sum;
        }
        
        if(n-k<=0) {
            throw new IllegalArgumentException();
        }
        
        Ymean/=n;
        
        double RSS=0.0;
        double BSS=0.0;
        for(Map.Entry<Object, FlatDataCollection> entry : transposeDataCollection.entrySet()) {
            Object j = entry.getKey();
            FlatDataCollection flatDataCollection = entry.getValue();
            
            Iterator<Double> it = flatDataCollection.iteratorDouble();
            while(it.hasNext()) {
                RSS+=Math.pow( it.next() - Yjmean.get(j), 2);
            }
            BSS+=nj.get(j) * Math.pow( Yjmean.get(j) - Ymean, 2);
        }
        
        double F = (BSS/(k-1))/(RSS/(n-k));
        
        double pvalue = ContinuousDistributions.FCdf(F, k-1, n-k);
        
        boolean rejectH0=false;
            
        double a=aLevel/2;
        if(pvalue<=a || pvalue>=(1-a)) {
            rejectH0=true; 
        }

        //if we requested an outputTable
        if(outputTable!=null) {
            double TSS = RSS+BSS;
            
            outputTable.clear();
            outputTable.put2d("BG", "SSq", BSS);
            outputTable.put2d("BG", "DF", k-1);
            outputTable.put2d("BG", "MSq", BSS/(k-1));
            outputTable.put2d("BG", "F", F);
            outputTable.put2d("BG", "p", 1-pvalue);
            outputTable.put2d("WG", "SSq", RSS);
            outputTable.put2d("WG", "DF", n-k);
            outputTable.put2d("WG", "MSq", RSS/(n-k));
            outputTable.put2d("R", "SSq", TSS);
            outputTable.put2d("R", "DF", n-1);
        }

        return rejectH0;
    }
    
    /**
     * Wrapper function for oneWayTestEqualVars, without passing the optional
     * output table.
     * 
     * @param transposeDataCollection
     * @param aLevel
     * @return 
     */
    public static boolean oneWayTestEqualVars(TransposeDataCollection transposeDataCollection, double aLevel) {
        return oneWayTestEqualVars(transposeDataCollection, aLevel, null);
    }
    
    /**
     * Performs One-Way ANOVA Test on the Transpose Data Table when variances are 
     * considered NOT equal. Pass an empty array on the $outputTable variable to 
     * get statistics concerning the ANOVA.
     * 
     * @param transposeDataCollection
     * @param aLevel
     * @param outputTable
     * @return
     * @throws IllegalArgumentException 
     */
    public static boolean oneWayTestNotEqualVars(TransposeDataCollection transposeDataCollection, double aLevel, AssociativeArray2D outputTable) throws IllegalArgumentException {
        int n = 0;
        int k = transposeDataCollection.size();
        if(k<=1) {
            throw new IllegalArgumentException();
        }

        Map<Object, Integer> nj = new HashMap<>();
        double Ymean = 0.0; //Total Y mean for 
        Map<Object, Double> Yjmean = new HashMap<>();
        Map<Object, Double> Yjvariance = new HashMap<>();
        for(Map.Entry<Object, FlatDataCollection> entry : transposeDataCollection.entrySet()) {
            Object j = entry.getKey();
            FlatDataCollection flatDataCollection = entry.getValue();
            
            int m = flatDataCollection.size();
            if(m==0) {
                throw new IllegalArgumentException();
            }
            nj.put(j, m); //get the number of observation for this category
            n+=m;
            
            double sum = Descriptives.sum(flatDataCollection);
            
            Yjmean.put(j, sum/m);
            Ymean+=sum;
            
            Yjvariance.put(j, Descriptives.variance(flatDataCollection, true));
        }
        
        if(n-k<=0) {
            throw new IllegalArgumentException();
        }
        
        Ymean/=n;
        
        double BSS = 0.0;
        Map<Object, Double> mj = new HashMap<>();
        double mjSum = 0.0;
        for(Map.Entry<Object, FlatDataCollection> entry : transposeDataCollection.entrySet()) {
            Object j = entry.getKey();
            //FlatDataCollection flatDataCollection = entry.getValue();
        
            BSS += nj.get(j)*Math.pow(Yjmean.get(j)-Ymean, 2);
            mj.put(j, (1.0- (double)nj.get(j)/n)*Yjvariance.get(j));
            mjSum+=mj.get(j);
        }
        
        double dfDenominator = 0.0;
        for(Map.Entry<Object, FlatDataCollection> entry : transposeDataCollection.entrySet()) {
            Object j = entry.getKey();
            //FlatDataCollection flatDataCollection = entry.getValue();
            dfDenominator+=Math.pow(mj.get(j)/mjSum, 2)/(nj.get(j)-1.0);
        }
        
        int df = (int)Math.round(1.0/dfDenominator);
        
        double Fstar = BSS/mjSum;
        
        double pvalue = ContinuousDistributions.FCdf(Fstar, k-1, df);
        
        boolean rejectH0=false;
            
        double a=aLevel/2;
        if(pvalue<=a || pvalue>=(1-a)) {
            rejectH0=true; 
        }

        //if we requested an outputTable
        if(outputTable!=null) {
         
            outputTable.clear();
            outputTable.put2d("BG", "Fparts", BSS);
            outputTable.put2d("BG", "DF", k-1);
            outputTable.put2d("BG", "F", Fstar);
            outputTable.put2d("BG", "p", 1.0-pvalue);
            outputTable.put2d("WG", "Fparts", mjSum);
            outputTable.put2d("WG", "DF", df);
            
        }
        
        return rejectH0;
    }
    
    /**
     * Wrapper function for oneWayTestNotEqualVars, without passing the optional
     * output table.
     * 
     * @param transposeDataCollection
     * @param aLevel
     * @return 
     */
    public static boolean oneWayTestNotEqualVars(TransposeDataCollection transposeDataCollection, double aLevel) {
        return oneWayTestNotEqualVars(transposeDataCollection, aLevel, null);
    }
    
    /**
     * Performs Two-Way ANOVA Test on the TransposeDataCollection2D when variances 
 are considered equal.
     * 
     * @param twoFactorDataCollection
     * @param aLevel
     * @param outputTable
     * @return
     * @throws IllegalArgumentException 
     */
    public static boolean twoWayTestEqualCellsEqualVars(TransposeDataCollection2D twoFactorDataCollection, double aLevel, AssociativeArray2D outputTable) throws IllegalArgumentException {
        //NOTE! This is correct ONLY when all the samples have the same size! Also we assume equal variances. We follow the method described at Ntzoufras' English anova.pdf but we degrees of freedom are calculated slighty differently. The outcome though is the same (the df are correct). We don't follow the notes in order to make it feasible to use the method for Unequal cells. In that case the analysis will not be correct!
        //To expand the test for Unequal Cells and Unequal Vars we must implement a different function described in the below papers:
        //http://www3.stat.sinica.edu.tw/statistica/oldpdf/a7n35.pdf
        //http://sites.stat.psu.edu/~jls/stat512/lectures/lec23.pdf
        
        Integer Itotal = twoFactorDataCollection.size();
        Integer Jtotal = null;
        for(Map.Entry<Object, TransposeDataCollection> entry : twoFactorDataCollection.entrySet()) {
            if(Jtotal!=null) {
                if(Jtotal!=entry.getValue().size()) {
                    throw new IllegalArgumentException(); //cells must be of equal size
                }
            }
            else {
                Jtotal = entry.getValue().size();
                if(Jtotal==0) {
                    throw new IllegalArgumentException();
                }
            }
        }

        int n=0;
        Map<Object, Integer> nidotdot = new HashMap<>();
        Map<Object, Integer> ndotjdot = new HashMap<>();
        Map<Object, Map<Object, Integer>> nijdot = new HashMap<>();

        double Ydotdotdot=0.0; //Total Y
        Map<Object, Double> Yidotdot = new HashMap<>();
        Map<Object, Double> Ydotjdot = new HashMap<>();
        Map<Object, Map<Object, Double>> Yijdot = new HashMap<>();

        //calculate sums
        for(Map.Entry<Object, TransposeDataCollection> entry1 : twoFactorDataCollection.entrySet()) {
            Object IfactorAlevel = entry1.getKey();
            TransposeDataCollection listOfBlevels = entry1.getValue();
            
            if(!Yidotdot.containsKey(IfactorAlevel)) {
                Yidotdot.put(IfactorAlevel, 0.0);
                nidotdot.put(IfactorAlevel, 0);
                Yijdot.put(IfactorAlevel, new HashMap<>());
                nijdot.put(IfactorAlevel, new HashMap<>());
            }
            
            for(Map.Entry<Object, FlatDataCollection> entry2 : listOfBlevels.entrySet()) {
                Object JfactorBlevel = entry2.getKey();
                FlatDataCollection flatDataCollection = entry2.getValue();
                
                if(!Ydotjdot.containsKey(JfactorBlevel)) {
                    Ydotjdot.put(JfactorBlevel, 0.0);
                    ndotjdot.put(JfactorBlevel, 0);
                }
                
                if(!Yijdot.get(IfactorAlevel).containsKey(JfactorBlevel)) {
                    Yijdot.get(IfactorAlevel).put(JfactorBlevel, 0.0);
                    nijdot.get(IfactorAlevel).put(JfactorBlevel, 0);
                }
                
                Iterator<Double> it = flatDataCollection.iteratorDouble();
                while(it.hasNext()) {
                    Double value = it.next();
                    
                    Ydotdotdot+=value;
                    ++n;
                    
                    Yidotdot.put(IfactorAlevel, Yidotdot.get(IfactorAlevel)+value);
                    nidotdot.put(IfactorAlevel, nidotdot.get(IfactorAlevel)+1);

                    Ydotjdot.put(JfactorBlevel, Ydotjdot.get(JfactorBlevel)+value);
                    ndotjdot.put(JfactorBlevel, ndotjdot.get(JfactorBlevel)+1);
                    
                    Yijdot.get(IfactorAlevel).put(JfactorBlevel, Yijdot.get(IfactorAlevel).get(JfactorBlevel) + value);
                    nijdot.get(IfactorAlevel).put(JfactorBlevel, nijdot.get(IfactorAlevel).get(JfactorBlevel) + 1);
                }
            }
        }

        //calculate averages
        Ydotdotdot/=n;
        
        for(Map.Entry<Object, Double> entry : Yidotdot.entrySet()) {
            Object i = entry.getKey();
            Yidotdot.put(i, entry.getValue()/nidotdot.get(i));
        }
        for(Map.Entry<Object, Double> entry : Ydotjdot.entrySet()) {
            Object j = entry.getKey();
            Ydotjdot.put(j, entry.getValue()/ndotjdot.get(j));
        }
        for(Map.Entry<Object, Map<Object, Double>> entry1 : Yijdot.entrySet()) {
            Object i = entry1.getKey();
            Map<Object, Double> listOfYj = entry1.getValue();
            
            for(Map.Entry<Object, Double> entry2 : listOfYj.entrySet()) {
                Object j = entry2.getKey();
                Double value = entry2.getValue();
                Yijdot.get(i).put(j, value/nijdot.get(i).get(j));
            }
        }
        nidotdot = null;
        ndotjdot = null;
        nijdot = null;


        //Caclulate ANOVA SSq
        double SSA=0;
        double SSB=0;
        double SSAB=0;
        double SSE=0;
        double SST=0;

        for(Map.Entry<Object, TransposeDataCollection> entry1 : twoFactorDataCollection.entrySet()) {
            Object IfactorAlevel = entry1.getKey();
            TransposeDataCollection listOfBlevels = entry1.getValue();
            
            for(Map.Entry<Object, FlatDataCollection> entry2 : listOfBlevels.entrySet()) {
                Object JfactorBlevel = entry2.getKey();
                FlatDataCollection flatDataCollection = entry2.getValue();
                
                
                Iterator<Double> it = flatDataCollection.iteratorDouble();
                while(it.hasNext()) {
                    Double value = it.next();
                    
                    SST+=Math.pow(value-Ydotdotdot,2);
                    SSA+=Math.pow(Yidotdot.get(IfactorAlevel)-Ydotdotdot,2);
                    SSB+=Math.pow(Ydotjdot.get(JfactorBlevel)-Ydotdotdot,2);
                    SSAB+=Math.pow(Yijdot.get(IfactorAlevel).get(JfactorBlevel) 
                                   -Yidotdot.get(IfactorAlevel)
                                   -Ydotjdot.get(JfactorBlevel) + Ydotdotdot
                          ,2);
		}
	    }
	}

        Yidotdot = null;
        Ydotjdot = null;
        Yijdot = null;

        SSE=SST-SSA-SSB-SSAB;
        double SSR=SSA+SSB+SSAB;

        int dfA=(Itotal-1);
        double MSA=SSA/dfA;

        int dfB=(Jtotal-1);
        double MSB=SSB/dfB;

        int dfAB=(Itotal-1)*(Jtotal-1);
        double MSAB=SSAB/dfAB;

        int dfE=(n-Itotal*Jtotal);
        double MSE=SSE/dfE;

        int dfR=dfA+dfB+dfAB;
        double MSR=SSR/dfR;

        double FA=MSA/MSE;
        double Apvalue=ContinuousDistributions.FCdf(FA,dfA,dfE);

        double FB=MSB/MSE;
        double Bpvalue=ContinuousDistributions.FCdf(FB,dfB,dfE);

        double FAB=MSAB/MSE;
        double ABpvalue=ContinuousDistributions.FCdf(FAB,dfAB,dfE);

        double FR=MSR/MSE;
        double Rpvalue=ContinuousDistributions.FCdf(FR,dfR,dfE);


        boolean rejectH0=false;

        double a=aLevel/2;
        if(Rpvalue<=a || Rpvalue>=(1-a)) {
            rejectH0=true; 
        }


        //if we requested an outputTable
        if(outputTable!=null) {
            outputTable.clear();
            outputTable.put2d("Model", "SSq", SSR);
            outputTable.put2d("Model", "DF", dfR);
            outputTable.put2d("Model", "MSq", MSR);
            outputTable.put2d("Model", "F", FR);
            outputTable.put2d("Model", "p", 1.0-Rpvalue);
            
            outputTable.put2d("AFactor", "SSq", SSA);
            outputTable.put2d("AFactor", "DF", dfA);
            outputTable.put2d("AFactor", "MSq", MSA);
            outputTable.put2d("AFactor", "F", FA);
            outputTable.put2d("AFactor", "p", 1.0-Apvalue);
            
            outputTable.put2d("BFactor", "SSq", SSB);
            outputTable.put2d("BFactor", "DF", dfB);
            outputTable.put2d("BFactor", "MSq", MSB);
            outputTable.put2d("BFactor", "F", FB);
            outputTable.put2d("BFactor", "p", 1.0-Bpvalue);
            
            outputTable.put2d("A*BFactor", "SSq", SSAB);
            outputTable.put2d("A*BFactor", "DF", dfAB);
            outputTable.put2d("A*BFactor", "MSq", MSAB);
            outputTable.put2d("A*BFactor", "F", FAB);
            outputTable.put2d("A*BFactor", "p", 1.0-ABpvalue);
            
            outputTable.put2d("Error", "SSq", SSE);
            outputTable.put2d("Error", "DF", dfE);
            outputTable.put2d("Error", "MSq", MSE);
            
            outputTable.put2d("Total", "SSq", SST);
            outputTable.put2d("Total", "DF", n-1);
        }

        return rejectH0;
    }
    
    /**
     * Wrapper function for twoWayTestEqualCellsEqualVars, without passing the 
     * optional output table.
     * 
     * @param twoFactorDataCollection
     * @param aLevel
     * @return 
     */
    public static boolean twoWayTestEqualCellsEqualVars(TransposeDataCollection2D twoFactorDataCollection, double aLevel) {
        return twoWayTestEqualCellsEqualVars(twoFactorDataCollection, aLevel, null);
    }
}
