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
package com.datumbox.framework.machinelearning.clustering;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.factories.BigDataStructureFactory;
import com.datumbox.common.persistentstorage.interfaces.BigDataStructureMarker;
import com.datumbox.common.utilities.MapFunctions;
import com.datumbox.common.utilities.PHPfunctions;
import com.datumbox.configuration.GeneralConfiguration;
import com.datumbox.configuration.MemoryConfiguration;
import com.datumbox.configuration.StorageConfiguration;
import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLclusterer;
import com.datumbox.framework.mathematics.distances.Distance;
import com.datumbox.framework.statistics.descriptivestatistics.Descriptives;
import com.datumbox.framework.statistics.sampling.SRS;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.mongodb.morphia.annotations.Transient;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class Kmeans extends BaseMLclusterer<Kmeans.Cluster, Kmeans.ModelParameters, Kmeans.TrainingParameters, Kmeans.ValidationMetrics> {

    /**
     * The internalDataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the internalDataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
    public static final String SHORT_METHOD_NAME = "Kmean";
    
    
    public static class Cluster extends BaseMLclusterer.Cluster {
        
        private final Record centroid;

        public Cluster(int clusterId) {
            super(clusterId);
            centroid = new Record();
        }
    
        public Record getCentroid() {
            return centroid;
        }

        @Override
        public boolean add(Record r) {
            boolean result = recordSet.add(r);
            if(result) {
                ++size;
            }
            return result;
        }
        
        @Override
        public boolean addAll(Collection<Record> c) {
            boolean result = recordSet.addAll(c);
            if(result) {
                size+=c.size();
            }
            return result;
        }
        
        @Override
        public boolean remove(Record r) {
            boolean result = recordSet.remove(r);
            if(result) {
                --size;
            }
            return result;
        }
        
        public boolean updateClusterParameters() {
            boolean changed=false;
            
            size = recordSet.size();
            
            AssociativeArray centoidValues = new AssociativeArray(new LinkedHashMap<>());
            for(Record r : recordSet) {
                centoidValues.addValues(r.getX());
            }
            
            if(size>0) {
                centoidValues.multiplyValues(1.0/size);
            }
            if(!centroid.getX().equals(centoidValues)) {
                changed=true;
                centroid.setX(centoidValues);
            }
            
            return changed;
        }
                
    }
    
    public static class ModelParameters extends BaseMLclusterer.ModelParameters<Kmeans.Cluster> {
        
        private int totalIterations;
        
        @BigDataStructureMarker
        @Transient
        private Map<Object, Double> featureWeights; 
        
        
        @Override
        public void bigDataStructureInitializer(BigDataStructureFactory bdsf, MemoryConfiguration memoryConfiguration) {
            super.bigDataStructureInitializer(bdsf, memoryConfiguration);
            
            BigDataStructureFactory.MapType mapType = memoryConfiguration.getMapType();
            int LRUsize = memoryConfiguration.getLRUsize();
            
            featureWeights = bdsf.getMap("featureWeights", mapType, LRUsize);
        }
        
        
        public int getTotalIterations() {
            return totalIterations;
        }

        public void setTotalIterations(int totalIterations) {
            this.totalIterations = totalIterations;
        }

        public Map<Object, Double> getFeatureWeights() {
            return featureWeights;
        }

        public void setFeatureWeights(Map<Object, Double> featureWeights) {
            this.featureWeights = featureWeights;
        }
        

        
        
    } 
    
    
    public static class TrainingParameters extends BaseMLclusterer.TrainingParameters {    

        public enum Initialization {
            FORGY, //Forgy
            RANDOM_PARTITION, //Random Partition
            SET_FIRST_K, //Set First K points as Initial Centroid
            FURTHEST_FIRST, //Furthest First: http://www.cs.utexas.edu/users/inderjit/elkankmeans.ppt
            SUBSET_FURTHEST_FIRST, //Subset Furthest First: http://www.cs.utexas.edu/users/inderjit/elkankmeans.ppt
            PLUS_PLUS; //Kmeans++: http://ilpubs.stanford.edu:8090/778/1/2006-13.pdf    http://www.ima.umn.edu/~iwen/REU/BATS-Means.pdf
        }
        
        public enum Distance {
            EUCLIDIAN,
            MANHATTAN;
        }
        
        //Vars
        
        private int k = 2;
        
        private Initialization initMethod = Initialization.PLUS_PLUS;
        
        private Distance distanceMethod = Distance.EUCLIDIAN;
        
        private int maxIterations = 200;

        private double subsetFurthestFirstcValue = 2;//c>1 This value is used for c*k*log k, Readmore: http://web.cs.swarthmore.edu/~turnbull/Papers/Turnbull_GenreRBF_KDE05.pdf

        private double categoricalGamaMultiplier = 1.0;  //used by Kprototype algorithm, multiplies the the categorical distance with this weight
        
        private boolean weighted = false; //whether the weighted version of the algorithm will run. The weighted version estimates weights for every feature
        
        //Getters Setters
        
        public int getK() {
            return k;
        }

        public void setK(int k) {
            this.k = k;
        }
        
        public Initialization getInitMethod() {
            return initMethod;
        }

        public void setInitMethod(Initialization initMethod) {
            this.initMethod = initMethod;
        }

        public Distance getDistanceMethod() {
            return distanceMethod;
        }

        public void setDistanceMethod(Distance distanceMethod) {
            this.distanceMethod = distanceMethod;
        }

        public int getMaxIterations() {
            return maxIterations;
        }

        public void setMaxIterations(int maxIterations) {
            this.maxIterations = maxIterations;
        }

        public double getSubsetFurthestFirstcValue() {
            return subsetFurthestFirstcValue;
        }

        public void setSubsetFurthestFirstcValue(double subsetFurthestFirstcValue) {
            this.subsetFurthestFirstcValue = subsetFurthestFirstcValue;
        }

        public double getCategoricalGamaMultiplier() {
            return categoricalGamaMultiplier;
        }

        public void setCategoricalGamaMultiplier(double categoricalGamaMultiplier) {
            this.categoricalGamaMultiplier = categoricalGamaMultiplier;
        }

        public boolean isWeighted() {
            return weighted;
        }

        public void setWeighted(boolean weighted) {
            this.weighted = weighted;
        }
        
    } 

    
    public static class ValidationMetrics extends BaseMLclusterer.ValidationMetrics {
        
    }
    
    
    public Kmeans(String dbName) {
        super(dbName, Kmeans.ModelParameters.class, Kmeans.TrainingParameters.class, Kmeans.ValidationMetrics.class);
    } 

    @Override
    public final String shortMethodName() {
        return SHORT_METHOD_NAME;
    }
    
    @Override
    protected void predictDataset(Dataset newData) { 
        if(newData.isEmpty()) {
            return;
        }
        
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        Map<Integer, Cluster> clusterList = modelParameters.getClusterList();
        
        for(Record r : newData) {
            
            AssociativeArray clusterDistances = new AssociativeArray();
            for(Cluster c : clusterList.values()) {
                double distance = calculateDistance(r, c.getCentroid());
                clusterDistances.put(c.getClusterId(), distance);
            }
            
            r.setYPredicted(getSelectedClusterFromDistances(clusterDistances));
            
            Descriptives.normalize(clusterDistances);
            r.setYPredictedProbabilities(clusterDistances);
        }
        
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected void estimateModelParameters(Dataset trainingData) {
        int n = trainingData.size();
        int d = trainingData.getColumnSize();
        
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        Map<Integer, Cluster> clusterList = modelParameters.getClusterList();
        
        //initialization
        modelParameters.setN(n);
        modelParameters.setD(d);
        
        Set<Object> goldStandardClasses = modelParameters.getGoldStandardClasses();
        
        //check if there are any gold standard classes
        for(Record r : trainingData) {
            Object theClass=r.getY();
            if(theClass!=null) {
                goldStandardClasses.add(theClass); 
            }
        }
        
        //calculate the weights of the features
        calculateFeatureWeights(trainingData);
        
        //initialize clusters
        initializeClusters(trainingData);
        
        //calculate clusters
        calculateClusters(trainingData);
        
        
        //update the number of clusters
        modelParameters.setC(clusterList.size());
        
        //clear dataclusters
        for(Cluster c : clusterList.values()) {
            c.clear();
        }
    }
    
    /**
     * Estimate the weights of each feature.
     * 
     * @param trainingData 
     */
    private void calculateFeatureWeights(Dataset trainingData) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        Map<Object, Dataset.ColumnType> columnTypes = trainingData.getColumns();
        
        Map<Object, Double> featureWeights = modelParameters.getFeatureWeights();
        
        if(trainingParameters.isWeighted()==false) {
            //the unweighted version of the algorithm
            double gammaWeight = trainingParameters.getCategoricalGamaMultiplier(); 
            for(Record r : trainingData) {
                for(Object feature : r.getX().keySet()) {
                    //standard kmeans has equal weights in all numeric features
                    //for categorical, dummy or ordinal feature, use the Gamma Multiplier of Kprototypes
                    double weight = (columnTypes.get(feature)!=Dataset.ColumnType.NUMERICAL)?gammaWeight:1.0;
                    
                    featureWeights.put(feature, weight); 
                }
            }
        }
        else {
            //calculate weights for the features
            String tmpPrefix=StorageConfiguration.getTmpPrefix();
            
            int n = modelParameters.getN();
            
            BigDataStructureFactory bdsf = knowledgeBase.getBdsf();
            
            BigDataStructureFactory.MapType mapType = knowledgeBase.getMemoryConfiguration().getMapType();
            int LRUsize = knowledgeBase.getMemoryConfiguration().getLRUsize();
            
            Map<Object, Double> categoricalFrequencies = bdsf.getMap(tmpPrefix+"categoricalFrequencies", mapType, LRUsize);
            Map<Object, Double> varianceSumX = bdsf.getMap(tmpPrefix+"varianceSumX", mapType, LRUsize);
            Map<Object, Double> varianceSumXsquare = bdsf.getMap(tmpPrefix+"varianceSumXsquare", mapType, LRUsize);
        
            //calculate variance and frequencies
            for(Record r : trainingData) {
                for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                    Double value = Dataset.toDouble(entry.getValue());
                    if(value==null || value==0.0) {
                        continue;
                    }
                    
                    Object feature = entry.getKey();
                    
                    if(columnTypes.get(feature)!=Dataset.ColumnType.NUMERICAL) {
                        Double previousValue = categoricalFrequencies.get(feature);
                        if(previousValue==null) {
                            previousValue = 0.0;
                        }
                        categoricalFrequencies.put(feature, previousValue+1.0);
                    }
                    else {
                        Double previousValueSumX = varianceSumX.get(feature);
                        Double previousValueSumXsquare = varianceSumXsquare.get(feature);
                        if(previousValueSumX==null) {
                            previousValueSumX = 0.0;
                            previousValueSumXsquare = 0.0;
                        }
                        varianceSumX.put(feature, previousValueSumX+value);
                        varianceSumXsquare.put(feature, previousValueSumXsquare+value*value);
                    }
                    
                }
            }
            
            double gammaWeight = trainingParameters.getCategoricalGamaMultiplier();
            
            //estimate weights
            for(Map.Entry<Object, Dataset.ColumnType> entry : columnTypes.entrySet()) {
                Object feature = entry.getKey();
                Dataset.ColumnType type = entry.getValue();
                
                double weight;
                if(type!=Dataset.ColumnType.NUMERICAL) {
                    double percentage = categoricalFrequencies.get(feature)/n;
                    weight = 1.0 -percentage*percentage;
                }
                else {
                    double mean = varianceSumX.get(feature)/n;
                    weight = 2.0*((varianceSumXsquare.get(feature)/n)-mean*mean);
                }
                
                
                if(weight>0) {
                    weight = 1/weight;
                }
                
                //apply muliplier if categorical
                if(type!=Dataset.ColumnType.NUMERICAL) {
                    weight *= gammaWeight;
                }
                
                featureWeights.put(feature, weight);
            }
            
            //Drop the temporary Collection
            bdsf.dropTable(tmpPrefix+"categoricalFrequencies", categoricalFrequencies);
            bdsf.dropTable(tmpPrefix+"varianceSumX", categoricalFrequencies);
            bdsf.dropTable(tmpPrefix+"varianceSumXsquare", categoricalFrequencies);
        }
    }
    
    private double calculateDistance(Record r1, Record r2) {        
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        Map<Object, Double> featureWeights = modelParameters.getFeatureWeights();
        
        double distance = 0.0;
        TrainingParameters.Distance distanceMethod = trainingParameters.getDistanceMethod();
        if(distanceMethod==TrainingParameters.Distance.EUCLIDIAN) {
            distance = Distance.euclideanWeighhted(r1.getX(), r2.getX(), featureWeights);
        }
        else if(distanceMethod==TrainingParameters.Distance.MANHATTAN) {
            distance = Distance.manhattanWeighhted(r1.getX(), r2.getX(), featureWeights);
        }
        else {
            throw new RuntimeException("Unsupported Distance method");
        }
        
        return distance;
    } 
    
    private Object getSelectedClusterFromDistances(AssociativeArray clusterDistances) {
        Map.Entry<Object, Object> minEntry = MapFunctions.selectMinKeyValue(clusterDistances);
        
        return minEntry.getKey();
    }
    
    private void initializeClusters(Dataset trainingData) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        
        int k = trainingParameters.getK();
        TrainingParameters.Initialization initMethod = trainingParameters.getInitMethod();
        
        boolean randomize = (initMethod==TrainingParameters.Initialization.FORGY || 
                            initMethod==TrainingParameters.Initialization.RANDOM_PARTITION || 
                            initMethod==TrainingParameters.Initialization.SUBSET_FURTHEST_FIRST ||
                            initMethod==TrainingParameters.Initialization.PLUS_PLUS);
        
        if(randomize) {
            trainingData.shuffle();
        }
        
        Map<Integer, Cluster> clusterList = modelParameters.getClusterList();
        if(initMethod==TrainingParameters.Initialization.SET_FIRST_K || 
           initMethod==TrainingParameters.Initialization.FORGY) {
            for(int i=0;i<k;++i) {
                Integer clusterId= i;
                Cluster c = new Cluster(clusterId);
                c.add(trainingData.get(i));
                c.updateClusterParameters();
                clusterList.put(clusterId, c);
            }
        }
        else if(initMethod==TrainingParameters.Initialization.RANDOM_PARTITION) {
            int i = 0;
            
            for(Record r : trainingData) {
                Integer clusterId = i%k;
                
                Cluster c = clusterList.get(clusterId);
                if(c==null) {
                    c = new Cluster(clusterId);
                    clusterList.put(clusterId, c);
                }
                
                c.add(r);
                ++i;
            }
            
            for(Cluster c : clusterList.values()) {
                c.updateClusterParameters();
            }
        }
        else if(initMethod==TrainingParameters.Initialization.FURTHEST_FIRST ||
                initMethod==TrainingParameters.Initialization.SUBSET_FURTHEST_FIRST) {
            
            int sampleSize = modelParameters.getN();
            if(initMethod==TrainingParameters.Initialization.SUBSET_FURTHEST_FIRST) {
                sampleSize = (int)Math.max(Math.ceil(trainingParameters.getSubsetFurthestFirstcValue()*k*PHPfunctions.log(k, 2)), k);
            }
            
            Set<Integer> alreadyAddedPoints = new HashSet(); //this is small. equal to k
            for(int i = 0; i<k; ++i) {
                Integer selectedRecordId = null;
                double maxMinDistance = 0.0;
                
                int samplePointCounter = 0;
                for(Record r : trainingData) {
                    if(samplePointCounter>sampleSize) {
                        break; //we only check a small part of the trainingData. This is equivalent to samplying only few observations from, the dataset
                    }
                    else if(alreadyAddedPoints.contains(r.getId())) {
                        continue; //ignore points that are already selected
                    }
                    
                    double minClusterDistance = Double.MAX_VALUE;
                    for(Cluster c : clusterList.values()) {
                        double distance = calculateDistance(r, c.getCentroid());
                        
                        if(distance<minClusterDistance) {
                            minClusterDistance=distance;
                        }
                    }
                    
                    if(minClusterDistance>maxMinDistance) {
                        maxMinDistance = minClusterDistance;
                        selectedRecordId = r.getId();
                    }
                    
                    ++samplePointCounter;
                }
                
                alreadyAddedPoints.add(selectedRecordId);
                
                Integer clusterId = clusterList.size();
                Cluster c = new Cluster(clusterId);
                c.add(trainingData.get(selectedRecordId));
                c.updateClusterParameters();
                
                clusterList.put(clusterId, c);
            }
            alreadyAddedPoints = null;
        }
        else if(initMethod==TrainingParameters.Initialization.PLUS_PLUS) {

            Set<Integer> alreadyAddedPoints = new HashSet(); //this is small. equal to k
            for(int i = 0; i < k; ++i) {
                AssociativeArray minClusterDistanceArray = new AssociativeArray();
                
                for(Record r : trainingData) {
                    if(alreadyAddedPoints.contains(r.getId())) {
                        continue; //ignore points that are already selected
                    }
                    
                    double minClusterDistance = 1.0;
                    if(clusterList.size()>0) {
                        minClusterDistance = Double.MAX_VALUE;
                        for(Cluster c : clusterList.values()) {
                            double distance = calculateDistance(r, c.getCentroid());

                            if(distance<minClusterDistance) {
                                minClusterDistance=distance;
                            }
                        }
                    }
                    
                    minClusterDistanceArray.put(r.getId(), minClusterDistance);
                }
                
                Descriptives.normalize(minClusterDistanceArray);
                Integer selectedRecordId = (Integer)SRS.weightedProbabilitySampling(minClusterDistanceArray, 1, true).iterator().next();
                
                alreadyAddedPoints.add(selectedRecordId);
                
                Integer clusterId = clusterList.size();
                Cluster c = new Cluster(clusterId);
                c.add(trainingData.get(selectedRecordId));
                c.updateClusterParameters();
                
                clusterList.put(clusterId, c);
            }
            alreadyAddedPoints = null;
        }
    }

    private void calculateClusters(Dataset trainingData) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        Map<Integer, Cluster> clusterList = modelParameters.getClusterList();
        
        int maxIterations = trainingParameters.getMaxIterations();
        modelParameters.setTotalIterations(maxIterations);
        
        AssociativeArray clusterDistances = new AssociativeArray();
        for(int iteration=0;iteration<maxIterations;++iteration) {
            if(GeneralConfiguration.DEBUG) {
                System.out.println("Iteration "+iteration);
            }
            
            //reset cluster points
            for(Cluster c : clusterList.values()) {
                c.clear();
            }
            
            //assign records in clusters
            for(Record r : trainingData) {
                //we are storing cluster references not clusterIds
                for(Cluster c : clusterList.values()) {
                    clusterDistances.put(c, calculateDistance(r, c.getCentroid()));
                }
                
                //find the closest cluster
                Cluster selectedCluster = (Cluster)getSelectedClusterFromDistances(clusterDistances);
                
                //add the record in the cluster
                selectedCluster.add(r);
                
                //clear the distances
                clusterDistances.clear();
            }
            
            //update clusters
            boolean changed=false;
            for(Cluster c : clusterList.values()) {
                changed|=c.updateClusterParameters();
            }
            
            //if none of the clusters changed then exit
            if(changed==false) {
                modelParameters.setTotalIterations(iteration);
                break;
            }
        }
    }
}