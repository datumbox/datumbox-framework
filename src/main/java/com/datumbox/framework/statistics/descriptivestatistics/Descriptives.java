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
package com.datumbox.framework.statistics.descriptivestatistics;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.FlatDataList;
import com.datumbox.common.dataobjects.FlatDataCollection;
import com.datumbox.common.dataobjects.TransposeDataList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author bbriniotis
 */
public class Descriptives {
    /**
     * The internalDataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the internalDataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
    
    /**
     * Returns the sum of a Collection
     * 
     * @param flatDataCollection
     * @return
     * @throws IllegalArgumentException 
     */
    public static double sum(FlatDataCollection flatDataCollection) throws IllegalArgumentException {
        int n = flatDataCollection.size();
        if(n==0) {
            throw new IllegalArgumentException();
        }
        
        double sum = 0.0;
        
        Iterator<Double> it = flatDataCollection.iteratorDouble();
        while(it.hasNext()) {
            Double value = it.next();
            if(value!=null) {
                sum+= value;
            }
        }
        
        return sum;
    }
    
    /**
     * Calculates the simple mean
     * 
     * @param flatDataCollection
     * @return
     * @throws IllegalArgumentException 
     */
    public static double mean(FlatDataCollection flatDataCollection) throws IllegalArgumentException {
	int n = flatDataCollection.size();
        if(n==0) {
            throw new IllegalArgumentException();
        }
        
        double mean = 0.0;
        
        mean = sum(flatDataCollection)/n;
        
        return mean;
    }
    
    /**
     * Calculates Standard Error of Mean under SRS
     * 
     * @param flatDataCollection
     * @return 
     */
    public static double meanSE(FlatDataCollection flatDataCollection) {
        double std = std(flatDataCollection, true);
        double meanSE = std/Math.sqrt(flatDataCollection.size());
        
        return meanSE;
    }
    
    /**
     * Calculates the median.
     * 
     * @param flatDataCollection
     * @return
     * @throws IllegalArgumentException 
     */
    public static double median(FlatDataCollection flatDataCollection) throws IllegalArgumentException {
	int n = flatDataCollection.size();
        if(n==0) {
            throw new IllegalArgumentException();
        }
        
        double median=0.0;
                
        Double[] doubleArray = Dataset.copyCollection2DoubleArray(flatDataCollection);
        Arrays.sort(doubleArray);
        
        if(n%2==0) {
            median = (doubleArray[n/2 - 1] + doubleArray[n/2])/2.0;
        }
        else {
            median = doubleArray[n/2];
        }
        
        return median;
    }
    
    /**
     * Calculates Minimum - Nulls are handled as zeros.
     * 
     * @param flatDataCollection
     * @return
     * @throws IllegalArgumentException 
     */
    public static double min(FlatDataCollection flatDataCollection) throws IllegalArgumentException {
	int n = flatDataCollection.size();
        if(n==0) {
            throw new IllegalArgumentException();
        }
        
        double min=Double.MAX_VALUE;
        
        
        Iterator<Double> it = flatDataCollection.iteratorDouble();
        while(it.hasNext()) {
            Double v = it.next();
            if(v==null) {
                v=0.0;
            }
            if(min>v) {
                min=v;
            }
        } 
        
        return min;
    }
    
    /**
     * Calculates Maximum - Nulls are handled as zeros.
     * 
     * @param flatDataCollection
     * @return
     * @throws IllegalArgumentException 
     */
    public static double max(FlatDataCollection flatDataCollection) throws IllegalArgumentException {
	int n = flatDataCollection.size();
        if(n==0) {
            throw new IllegalArgumentException();
        }
        
        double max=Double.NEGATIVE_INFINITY;
            
        Iterator<Double> it = flatDataCollection.iteratorDouble();
        while(it.hasNext()) {
            Double v = it.next();
            if(v==null) {
                v=0.0;
            }
            if(max<v) {
                max=v;
            }
        }          
        
        return max;
    }
    
    /**
     * Calculates Range
     * 
     * @param flatDataCollection
     * @return 
     */
    public static double range(FlatDataCollection flatDataCollection) {
        double range=max(flatDataCollection)-min(flatDataCollection);
        
        return range;
    }
    
    /**
     * Calculates Geometric Mean
     * 
     * @param flatDataCollection
     * @return
     * @throws IllegalArgumentException
     */
    public static double geometricMean(FlatDataCollection flatDataCollection) throws IllegalArgumentException {
	int n = flatDataCollection.size();
        if(n==0) {
            throw new IllegalArgumentException();
        }
        else if(min(flatDataCollection)<=0) {
            throw new IllegalArgumentException();
        }
        
        double geometricMean = 0.0;

        Iterator<Double> it = flatDataCollection.iteratorDouble();
        while(it.hasNext()) {
            geometricMean+= Math.log(it.next());
        }
        
        geometricMean= Math.exp(geometricMean/n);
        
        return geometricMean;
    }
    
    /**
     * Calculates Harmonic Mean
     * 
     * @param flatDataCollection
     * @return
     * @throws IllegalArgumentException 
     */
    public static double harmonicMean(FlatDataCollection flatDataCollection) throws IllegalArgumentException {
	int n = flatDataCollection.size();
        if(n==0) {
            throw new IllegalArgumentException();
        }
        
        double harmonicMean = 0.0;
        
        Iterator<Double> it = flatDataCollection.iteratorDouble();
        while(it.hasNext()) {
            harmonicMean+=1.0/it.next();
        }
        
        harmonicMean=n/harmonicMean;
        
        return harmonicMean;
    }
    
    /**
     * Calculates the Variance 
     * 
     * @param flatDataCollection
     * @param isSample
     * @return
     * @throws IllegalArgumentException 
     */
    public static double variance(FlatDataCollection flatDataCollection, boolean isSample) throws IllegalArgumentException {
        int n = flatDataCollection.size();
        if(n<=1) {
            throw new IllegalArgumentException();
        }
        
        double variance=0.0;
        
        /* Uses the formal Variance = E(X^2) - mean^2 */
        
        double mean = 0.0;
        double squaredMean = 0.0;
        Iterator<Double> it = flatDataCollection.iteratorDouble();
        while(it.hasNext()) {
            double v = it.next();
            mean+=v;
            squaredMean+=v*v;
        }
        
        mean/=n;
        squaredMean/=n;
        
        variance = squaredMean - mean*mean;
        
        /* Unbiased for sample */
        if(isSample) {
            variance*=n/(n-1.0);
        }
        
        return variance;
    }
    
    /**
     * Calculates the Standard Deviation
     * 
     * @param flatDataCollection
     * @param isSample
     * @return 
     */
    public static double std(FlatDataCollection flatDataCollection, boolean isSample) {
        double variance = variance(flatDataCollection, isSample);
        double std = Math.sqrt(variance);
        
        return std;
    }
    
    /**
     * Calculates Coefficient of variation
     * 
     * @param std
     * @param mean
     * @return 
     */
    public static double cv(double std, double mean) {
        if(mean==0) {
            return Double.POSITIVE_INFINITY;
        }
        
        double cv = std/mean;
        
        return cv;
    }
    
    /**
     * Calculates Moment R if the mean is not known.
     * 
     * @param flatDataCollection
     * @param r
     * @return 
     */
    public static double moment(FlatDataCollection flatDataCollection, int r) {
        double mean = mean(flatDataCollection);
        return moment(flatDataCollection, r, mean);
    }
    
    /**
     * Calculates Moment R if the mean is known.
     * 
     * @param flatDataCollection
     * @param r
     * @param mean
     * @return
     * @throws IllegalArgumentException 
     */
    public static double moment(FlatDataCollection flatDataCollection, int r, double mean) throws IllegalArgumentException {
        int n = flatDataCollection.size();
        if(n<=1) {
            throw new IllegalArgumentException();
        }
        
        double moment=0.0;
        
        Iterator<Double> it = flatDataCollection.iteratorDouble();
        while(it.hasNext()) {
            moment+=Math.pow(it.next()-mean, r);
        }
        
        moment/=n;
        
        return moment;
    }
    
    /**
     * Calculates Kurtosis. Uses a formula similar to SPSS as suggested in their documentation (local help)
     * 
     * @param flatDataCollection
     * @return
     * @throws IllegalArgumentException 
     */
    public static double kurtosis(FlatDataCollection flatDataCollection) throws IllegalArgumentException {
	int n = flatDataCollection.size();
        if(n<=3) {
            throw new IllegalArgumentException();
        }
        
        double kurtosis = 0.0;
        
        double mean = mean(flatDataCollection);
        
        double m4 = moment(flatDataCollection, 4, mean);
        double m2 = moment(flatDataCollection, 2, mean);        

        /*
        g2=m4/(m2*m2);

        kurtosis=((n-1.0)/((n-2.0)*(n-3.0)))*((n+1.0)*g2 +6.0);
        //kurtosis=g2/(n-1.0)-3.0;
        //kurtosis=g2;
        */
        
        double s = (n/(n-1.0))*m2;
        
        double Mcapital4=n*m4;
        double Mcapital2=n*m2;
        kurtosis=(n*(n+1.0)*Mcapital4-3*Mcapital2*Mcapital2*(n-1.0))/((n-1.0)*(n-2.0)*(n-3.0)*s*s);
        
        return kurtosis;
    }
    
    /**
     * Calculates Standard Error of Kurtosis. Uses a formula similar to SPSS as 
     * suggested by http://suite101.com/article/kurtosis-and-how-it-is-calculated-by-statistics-software-packages-a235641
     * 
     * @param flatDataCollection
     * @return
     * @throws IllegalArgumentException 
     */
    public static double kurtosisSE(FlatDataCollection flatDataCollection) throws IllegalArgumentException {
	int n = flatDataCollection.size();
        if(n<=3) {
            throw new IllegalArgumentException();
        }
        
        double kurtosisSE = Math.sqrt(24.0/n);
        
        return kurtosisSE;
    }
    
    /**
     * Calculates Skewness. Uses a formula as suggested by http://en.wikipedia.org/wiki/Skewness
     * 
     * @param flatDataCollection
     * @return
     * @throws IllegalArgumentException 
     */
    public static double skewness(FlatDataCollection flatDataCollection) throws IllegalArgumentException {
	int n = flatDataCollection.size();
        if(n<=1) {
            throw new IllegalArgumentException();
        }
        
        double skewness = 0.0;
        
        double mean = mean(flatDataCollection);
        
        double m3 = moment(flatDataCollection, 3, mean);
        double variance = variance(flatDataCollection, false);        
    
        skewness=m3/Math.pow(variance, 3.0/2.0);
        
        return skewness;
    }
    
    /**
     * Calculates Standard Error of Skweness. Uses a formula as suggested by 
     * http://en.wikipedia.org/wiki/Skewness
     * 
     * @param flatDataCollection
     * @return
     * @throws IllegalArgumentException 
     */
    public static double skewnessSE(FlatDataCollection flatDataCollection) throws IllegalArgumentException {
	int n = flatDataCollection.size();
        if(n<=2) {
            throw new IllegalArgumentException();
        }
        
        double skewnessSE=Math.sqrt((6.0*n*(n-1.0))/((n-2.0)*(n+1.0)*(n+3.0)));
        
        return skewnessSE;
    }
    
    /**
     * Calculates the percentiles given a number of cutPoints
     * 
     * @param flatDataCollection
     * @param cutPoints
     * @return
     * @throws IllegalArgumentException 
     */
    public static AssociativeArray percentiles(FlatDataCollection flatDataCollection, int cutPoints) throws IllegalArgumentException {
	int n = flatDataCollection.size();
        if(n<=0 || cutPoints<=0 || n<cutPoints) {
            throw new IllegalArgumentException();
        }
        
        AssociativeArray percintiles = new AssociativeArray(new LinkedHashMap<>());
        
        Double[] doubleArray = Dataset.copyCollection2DoubleArray(flatDataCollection);
        Arrays.sort(doubleArray);
        
        /*
        Uses the Haverage algorithm which is used by SPSS as described at: 
        http://publib.boulder.ibm.com/infocenter/spssstat/v20r0m0/index.jsp?topic=%2Fcom.ibm.spss.statistics.help%2Falg_examine_haverage.htm        
        */
        
        double counter = 1.0;
        while(true) {
            double perc = counter/cutPoints;
            
            Double tc2=(n+1.0)*perc;
                

            int CCk2=tc2.intValue();
            int CCk2_plus1=CCk2+1;

            int Ck2=CCk2-1; //actual pointers on the table. These are used because the arrays start from 0 the counting
            int Ck2_plus1=CCk2_plus1-1;

            double g2Star=tc2-CCk2;
            
            if(Ck2<doubleArray.length) {
                Double key=100*perc;
                Double Ck2Value = doubleArray[Ck2];
                if(Ck2_plus1<doubleArray.length) {
                    Double Ck2_plus1Value = doubleArray[Ck2_plus1];
                    percintiles.put(key, (1-g2Star)*Ck2Value+g2Star*Ck2_plus1Value);
                }
                else {
                    percintiles.put(key, Ck2Value);
                    break;
                }
            }
            else {
                break;
            }

            ++counter;
        }
        
        return percintiles;
    }
    
    /**
     * Calculates the quartiles
     * 
     * @param flatDataCollection
     * @return 
     */
    public static AssociativeArray quartiles(FlatDataCollection flatDataCollection) {
        return percentiles(flatDataCollection, 4);
    }
    
    /**
     * Calculates the covariance for a given transposed array (2xn table)
     * @param transposeDataList
     * @param isSample
     * @return
     * @throws IllegalArgumentException 
     */
    public static double covariance(TransposeDataList transposeDataList, boolean isSample) throws IllegalArgumentException {
        if(transposeDataList.size()!=2) {
            throw new IllegalArgumentException();
        }
        
        double covariance = 0.0;
        
        Object[] keys = transposeDataList.keySet().toArray();
        Object keyX = keys[0];
        Object keyY = keys[1];
        
        FlatDataCollection flatDataCollectionX = transposeDataList.get(keyX).toFlatDataCollection();
        FlatDataCollection flatDataCollectionY = transposeDataList.get(keyY).toFlatDataCollection();
        
        int n = flatDataCollectionX.size();
        if(n<=1 || n!=flatDataCollectionY.size()) {
            throw new IllegalArgumentException();
        }
        
        double meanX = mean(flatDataCollectionX);
        double meanY = mean(flatDataCollectionY);
        
        Iterator<Double> iteratorX = flatDataCollectionX.iteratorDouble();
        Iterator<Double> iteratorY = flatDataCollectionY.iteratorDouble();
        while (iteratorX.hasNext() && iteratorY.hasNext()) {
            covariance+=(iteratorX.next()-meanX)*
                        (iteratorY.next()-meanY);
        }
        
        if(isSample) {
            covariance/=(n-1.0);
        }
        else {
            covariance/=n;
        }
        
        return covariance;
    }
    
    /**
     * Calculates the autocorrelation of a flatDataCollection for a predifined lag
     * 
     * @param flatDataList
     * @param lags
     * @return
     * @throws IllegalArgumentException 
     */
    public static double autocorrelation(FlatDataList flatDataList, int lags) throws IllegalArgumentException {
        int n = flatDataList.size();
        if(n<=0 || lags<=0 || n<lags) {
            throw new IllegalArgumentException();
        }
        
        FlatDataCollection flatDataCollection = flatDataList.toFlatDataCollection();
        double mean = mean(flatDataCollection);
        double variance = variance(flatDataCollection, true);
        
        double Ak=0.0;
        
        int maxI=n-lags;
        for(int i=0;i<maxI;++i) {
            
            Ak += (flatDataList.getDouble(i) - mean)*
                  (flatDataList.getDouble(i+lags) - mean);
        }
        
        Ak/=(n-lags);
        
        double autocorrelation = Ak/variance;
        
        return autocorrelation;
    }
    
    /**
     * Calculates the Frequency Table
     * 
     * @param flatDataCollection
     * @return
     * @throws IllegalArgumentException 
     */
    public static AssociativeArray frequencies(FlatDataCollection flatDataCollection) throws IllegalArgumentException {
        int n = flatDataCollection.size();
        if(n==0) {
            throw new IllegalArgumentException();
        }
        
        AssociativeArray frequencies = new AssociativeArray(new LinkedHashMap<>());
        
        for (Object value : flatDataCollection) {
            Object counter = frequencies.get(value);
            if(counter==null) {
                frequencies.put(value, 1);
            }
            else {
                frequencies.put(value, ((Number)counter).intValue()+1);
            }
        }
        
        return frequencies;
    }
    
    /**
     * Calculates the modes (more than one if found).
     * 
     * @param flatDataCollection
     * @return
     * @throws IllegalArgumentException 
     */
    public static FlatDataCollection mode(FlatDataCollection flatDataCollection) throws IllegalArgumentException {
        int n = flatDataCollection.size();
        if(n==0) {
            throw new IllegalArgumentException();
        }
        
        AssociativeArray frequencies = frequencies(flatDataCollection);
        
        int maxCounter=0;
        
        FlatDataList modeList = new FlatDataList();
        
        for(Map.Entry<Object, Object> entry : frequencies.entrySet()) {
            Object key = entry.getKey();
            int count = ((Number)entry.getValue()).intValue();
            if(maxCounter==count) {
                modeList.add(key);
            }
            else if(maxCounter<count) {
                maxCounter=count;
                modeList.clear();
                modeList.add(key);
            }
        }
        
        return modeList.toFlatDataCollection();
    }
    
    public static void normalize(AssociativeArray associativeArray) {
        double sum = 0.0;
        //Prevents numeric underflow by subtracting the max. References: http://www.youtube.com/watch?v=-RVM21Voo7Q
        for(Map.Entry<Object, Object> entry : associativeArray.entrySet()) {
            Double value = Dataset.toDouble(entry.getValue());
            associativeArray.put(entry.getKey(), value);

            sum += value;
        }
        
        if(sum!=0.0) {
            for(Map.Entry<Object, Object> entry : associativeArray.entrySet()) {
                associativeArray.put(entry.getKey(), Dataset.toDouble(entry.getValue())/sum);
            }
        }
    }
    
    public static void normalizeExp(AssociativeArray associativeArray) {
        double max = max(associativeArray.toFlatDataCollection());

        double sum = 0.0;
        //Prevents numeric underflow by subtracting the max. References: http://www.youtube.com/watch?v=-RVM21Voo7Q
        for(Map.Entry<Object, Object> entry : associativeArray.entrySet()) {
            Double value = Math.exp(Dataset.toDouble(entry.getValue())-max);
            associativeArray.put(entry.getKey(), value);

            sum += value;
        }
        
        if(sum!=0.0) {
            for(Map.Entry<Object, Object> entry : associativeArray.entrySet()) {
                associativeArray.put(entry.getKey(), Dataset.toDouble(entry.getValue())/sum);
            }
        }
    }
}
