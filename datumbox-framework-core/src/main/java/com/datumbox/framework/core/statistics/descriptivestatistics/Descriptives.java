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
package com.datumbox.framework.core.statistics.descriptivestatistics;

import com.datumbox.framework.common.dataobjects.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

/**
 * This class provides several methods to estimate Descriptive Statistics about
 * a particular list of values.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class Descriptives {

    /**
     * Returns the number of not-null items in the iteratable.
     *
     * @param it
     * @return
     */
    public static int count(Iterable it) {
        int n = 0;

        for(Object v: it) {
            if(v != null) {
                ++n;
            }
        }

        return n;
    }

    /**
     * Returns the sum of a Collection
     * 
     * @param flatDataCollection
     * @return
     */
    public static double sum(FlatDataCollection flatDataCollection) {
        double sum = 0.0;
        
        Iterator<Double> it = flatDataCollection.iteratorDouble();
        while(it.hasNext()) {
            Double value = it.next();
            if(value != null) {
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
     */
    public static double mean(FlatDataCollection flatDataCollection) {
        int n = 0;
        double mean = 0.0;
        Iterator<Double> it = flatDataCollection.iteratorDouble();
        while(it.hasNext()) {
            Double value = it.next();
            if(value != null) {
                ++n;
                mean += value;
            }
        }

        if(n==0) {
            throw new IllegalArgumentException("No not null values where found in the collection.");
        }
        mean /= n;
        
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
        double meanSE = std/Math.sqrt(count(flatDataCollection));
        
        return meanSE;
    }
    
    /**
     * Calculates the median.
     * 
     * @param flatDataCollection
     * @return
     */
    public static double median(FlatDataCollection flatDataCollection) {
        double[] doubleArray = flatDataCollection.stream().filter(x -> x!=null).mapToDouble(TypeInference::toDouble).toArray();
        int n = doubleArray.length;
        if(n==0) {
            throw new IllegalArgumentException("The provided collection can't be empty.");
        }
        Arrays.sort(doubleArray);
        
        double median;
        if(n%2==0) {
            median = (doubleArray[n/2 - 1] + doubleArray[n/2])/2.0;
        }
        else {
            median = doubleArray[n/2];
        }
        
        return median;
    }
    
    /**
     * Calculates Minimum.
     * 
     * @param flatDataCollection
     * @return
     */
    public static double min(FlatDataCollection flatDataCollection) {
        double min=Double.POSITIVE_INFINITY;
        
        Iterator<Double> it = flatDataCollection.iteratorDouble();
        while(it.hasNext()) {
            Double v = it.next();
            if(v != null && min > v) {
                min=v;
            }
        } 
        
        return min;
    }
    
    /**
     * Calculates Maximum.
     * 
     * @param flatDataCollection
     * @return
     */
    public static double max(FlatDataCollection flatDataCollection) {
        double max=Double.NEGATIVE_INFINITY;
            
        Iterator<Double> it = flatDataCollection.iteratorDouble();
        while(it.hasNext()) {
            Double v = it.next();
            if(v != null && max < v) {
                max=v;
            }
        }          
        
        return max;
    }

    /**
     * Calculates Minimum absolute value.
     *
     * @param flatDataCollection
     * @return
     */
    public static double minAbsolute(FlatDataCollection flatDataCollection) {
        double minAbs=Double.POSITIVE_INFINITY;

        Iterator<Double> it = flatDataCollection.iteratorDouble();
        while(it.hasNext()) {
            Double v = it.next();
            if(v != null) {
                minAbs= Math.min(minAbs, Math.abs(v));
            }
        }

        return minAbs;
    }

    /**
     * Calculates Maximum absolute value.
     *
     * @param flatDataCollection
     * @return
     */
    public static double maxAbsolute(FlatDataCollection flatDataCollection) {
        double maxAbs=0.0;

        Iterator<Double> it = flatDataCollection.iteratorDouble();
        while(it.hasNext()) {
            Double v = it.next();
            if(v != null) {
                maxAbs= Math.max(maxAbs, Math.abs(v));
            }
        }

        return maxAbs;
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
     */
    public static double geometricMean(FlatDataCollection flatDataCollection) {
	    int n = 0;
        double geometricMean = 0.0;

        Iterator<Double> it = flatDataCollection.iteratorDouble();
        while(it.hasNext()) {
            Double v = it.next();
            if(v != null) {
                if(v <= 0.0) {
                    throw new IllegalArgumentException("Negative or zero values are not allowed.");
                }
                ++n;
                geometricMean+= Math.log(v);
            }
        }
        
        geometricMean= Math.exp(geometricMean/n);
        
        return geometricMean;
    }
    
    /**
     * Calculates Harmonic Mean
     * 
     * @param flatDataCollection
     * @return
     */
    public static double harmonicMean(FlatDataCollection flatDataCollection) {
	    int n = 0;
        double harmonicMean = 0.0;
        
        Iterator<Double> it = flatDataCollection.iteratorDouble();
        while(it.hasNext()) {
            Double v = it.next();
            if(v!=null) {
                ++n;
                harmonicMean+=1.0/v;
            }
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
     */
    public static double variance(FlatDataCollection flatDataCollection, boolean isSample) {
        /* Uses the formal Variance = E(X^2) - mean^2 */
        int n = 0;
        double mean = 0.0;
        double squaredMean = 0.0;
        Iterator<Double> it = flatDataCollection.iteratorDouble();
        while(it.hasNext()) {
            Double v = it.next();
            if(v != null) {
                ++n;
                mean+=v;
                squaredMean+=v*v;
            }
        }

        if(n<=1) {
            throw new IllegalArgumentException("The provided collection must have more than 1 elements.");
        }
        
        mean/=n;
        squaredMean/=n;
        
        double variance = squaredMean - mean*mean;
        
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
     */
    public static double moment(FlatDataCollection flatDataCollection, int r, double mean) {
        int n = 0;
        double moment=0.0;
        
        Iterator<Double> it = flatDataCollection.iteratorDouble();
        while(it.hasNext()) {
            Double v = it.next();
            if(v != null) {
                ++n;
                moment+=Math.pow(v-mean, r);
            }
        }

        if(n<=1) {
            throw new IllegalArgumentException("The provided collection must have more than 1 elements.");
        }
        
        moment/=n;
        
        return moment;
    }
    
    /**
     * Calculates Kurtosis. Uses a formula similar to SPSS as suggested in their documentation (local help)
     * 
     * @param flatDataCollection
     * @return
     */
    public static double kurtosis(FlatDataCollection flatDataCollection) {
	    int n = count(flatDataCollection);
        if(n<=3) {
            throw new IllegalArgumentException("The provided collection must have more than 3 elements.");
        }
        
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
        double kurtosis=(n*(n+1.0)*Mcapital4-3*Mcapital2*Mcapital2*(n-1.0))/((n-1.0)*(n-2.0)*(n-3.0)*s*s);
        
        return kurtosis;
    }
    
    /**
     * Calculates Standard Error of Kurtosis. Uses a formula similar to SPSS as 
     * suggested by http://suite101.com/article/kurtosis-and-how-it-is-calculated-by-statistics-software-packages-a235641
     * 
     * @param flatDataCollection
     * @return
     */
    public static double kurtosisSE(FlatDataCollection flatDataCollection) {
	    int n = count(flatDataCollection);
        if(n<=3) {
            throw new IllegalArgumentException("The provided collection must have more than 3 elements.");
        }
        
        double kurtosisSE = Math.sqrt(24.0/n);
        
        return kurtosisSE;
    }
    
    /**
     * Calculates Skewness. Uses a formula as suggested by http://en.wikipedia.org/wiki/Skewness
     * 
     * @param flatDataCollection
     * @return
     */
    public static double skewness(FlatDataCollection flatDataCollection) {
	    int n = count(flatDataCollection);
        if(n<=1) {
            throw new IllegalArgumentException("The provided collection must have more than 1 elements.");
        }
        
        double mean = mean(flatDataCollection);
        
        double m3 = moment(flatDataCollection, 3, mean);
        double variance = variance(flatDataCollection, false);        
    
        double skewness=m3/Math.pow(variance, 3.0/2.0);
        
        return skewness;
    }
    
    /**
     * Calculates Standard Error of Skweness. Uses a formula as suggested by 
     * http://en.wikipedia.org/wiki/Skewness
     * 
     * @param flatDataCollection
     * @return
     */
    public static double skewnessSE(FlatDataCollection flatDataCollection) {
	    int n = count(flatDataCollection);
        if(n<=2) {
            throw new IllegalArgumentException("The provided collection must have more than 2 elements.");
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
     */
    public static AssociativeArray percentiles(FlatDataCollection flatDataCollection, int cutPoints) {
        double[] doubleArray = flatDataCollection.stream().filter(x -> x!=null).mapToDouble(TypeInference::toDouble).toArray();
        int n = doubleArray.length;
        if(n<=0 || cutPoints<=0 || n<cutPoints) {
            throw new IllegalArgumentException("All the parameters must be positive and n larger than cutPoints.");
        }
        Arrays.sort(doubleArray);
        
        /*
        Uses the Haverage algorithm which is used by SPSS as described at: 
        http://publib.boulder.ibm.com/infocenter/spssstat/v20r0m0/index.jsp?topic=%2Fcom.ibm.spss.statistics.help%2Falg_examine_haverage.htm        
        */

        AssociativeArray percintiles = new AssociativeArray();
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
     */
    public static double covariance(TransposeDataList transposeDataList, boolean isSample) {
        if(transposeDataList.size()!=2) {
            throw new IllegalArgumentException("The collection must contain observations from 2 groups.");
        }
        
        double covariance = 0.0;
        
        Object[] keys = transposeDataList.keySet().toArray();
        Object keyX = keys[0];
        Object keyY = keys[1];
        
        FlatDataCollection flatDataCollectionX = transposeDataList.get(keyX).toFlatDataCollection();
        FlatDataCollection flatDataCollectionY = transposeDataList.get(keyY).toFlatDataCollection();
        
        int n = flatDataCollectionX.size();
        if(n<=1 || n!=flatDataCollectionY.size()) {
            throw new IllegalArgumentException("The number of observations in each group must be equal and larger than 1.");
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
     */
    public static double autocorrelation(FlatDataList flatDataList, int lags) {
        int n = count(flatDataList);
        if(n<=0 || lags<=0 || n<lags) {
            throw new IllegalArgumentException("All the parameters must be positive and n larger than lags.");
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
     */
    public static AssociativeArray frequencies(FlatDataCollection flatDataCollection) {
        AssociativeArray frequencies = new AssociativeArray();
        
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
     */
    public static FlatDataCollection mode(FlatDataCollection flatDataCollection) {
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
    
    /**
     * Normalizes the provided associative array by dividing its values with the
     * sum of the observations.
     * 
     * @param associativeArray 
     */
    public static void normalize(AssociativeArray associativeArray) {
        double sum = 0.0;
        //Prevents numeric underflow by subtracting the max. References: http://www.youtube.com/watch?v=-RVM21Voo7Q
        for(Map.Entry<Object, Object> entry : associativeArray.entrySet()) {
            Double value = TypeInference.toDouble(entry.getValue());
            associativeArray.put(entry.getKey(), value);

            sum += value;
        }
        
        if(sum!=0.0) {
            for(Map.Entry<Object, Object> entry : associativeArray.entrySet()) {
                associativeArray.put(entry.getKey(), TypeInference.toDouble(entry.getValue())/sum);
            }
        }
    }
    
    /**
     * Normalizes the exponentials of provided associative array by using the 
     * log-sum-exp trick.
     * 
     * @param associativeArray 
     */
    public static void normalizeExp(AssociativeArray associativeArray) {
        double max = max(associativeArray.toFlatDataCollection());

        double sum = 0.0;
        //Prevents numeric underflow by subtracting the max. References: http://www.youtube.com/watch?v=-RVM21Voo7Q
        for(Map.Entry<Object, Object> entry : associativeArray.entrySet()) {
            Double value = Math.exp(TypeInference.toDouble(entry.getValue())-max);
            associativeArray.put(entry.getKey(), value);

            sum += value;
        }
        
        if(sum!=0.0) {
            for(Map.Entry<Object, Object> entry : associativeArray.entrySet()) {
                associativeArray.put(entry.getKey(), TypeInference.toDouble(entry.getValue())/sum);
            }
        }
    }
}
