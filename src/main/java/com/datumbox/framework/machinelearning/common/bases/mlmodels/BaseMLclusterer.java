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
package com.datumbox.framework.machinelearning.common.bases.mlmodels;

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.objecttypes.Learnable;
import com.datumbox.common.persistentstorage.factories.BigDataStructureFactory;
import com.datumbox.common.utilities.DeepCopy;
import com.datumbox.configuration.MemoryConfiguration;
import com.datumbox.framework.machinelearning.common.validation.ClustererValidation;
import com.datumbox.framework.machinelearning.common.bases.validation.ModelValidation;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.PostLoad;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Transient;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 * @param <CL>
 * @param <MP>
 * @param <TP>
 * @param <VM>
 */
public abstract class BaseMLclusterer<CL extends BaseMLclusterer.Cluster, MP extends BaseMLclusterer.ModelParameters, TP extends BaseMLclusterer.TrainingParameters, VM extends BaseMLclusterer.ValidationMetrics> extends BaseMLmodel<MP, TP, VM> {
    
    @Embedded
    public static abstract class Cluster implements Learnable, Iterable<Record> {
        //variables
        protected Integer clusterId;
        
        protected int size; //we store this value because we want to know the number of elements in the cluster even when clear() was called
        
        @Transient
        protected transient Set<Record> recordSet; //it is not stored in the DB and it is cleared after the end of training
        
        protected Object labelY = null;
        
        public Cluster(Integer clusterId) {
            this.clusterId = clusterId;
            size = 0;
            recordSet = new HashSet<>(); //This is large but it should store only references of the Records not the actualy objects
        }
        
        //Useful methods for managing the cluster
        
        public int size() {
            if(recordSet!=null) {
                size = recordSet.size();
            }
            return size;
        }
        
        public void clear() {
            if(recordSet!=null) {
                size = 0;
                recordSet.clear();
            }
        }

        
        //Wrapper methods for the recordSet
        
        public boolean isEmpty() {
            return recordSet.isEmpty();
        }
        
        public boolean contains(Record r) {
            return recordSet.contains(r);
        }
        
        /**
         * Implementing a read-only iterator on clusterList to use it in loops.
         * 
         * @return 
         */
        @Override
        public Iterator<Record> iterator() {
            return new Iterator<Record>() {
                private final Iterator<Record> it = recordSet.iterator();

                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public Record next() {
                    return it.next();
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
        
        //internal methods
        
        @Override
        public int hashCode() {
            int hash = 7;
            hash = 89 * hash + this.clusterId;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Cluster other = (Cluster) obj;
            if (!Objects.equals(this.clusterId, other.clusterId)) {
                return false;
            }
            return true;
        }

        //Getters
        
        public Integer getClusterId() {
            return clusterId;
        }

        //abstract methods that modify the cluster set and should update its statistics in each implementation
        
        public abstract boolean add(Record r);
        
        public abstract boolean addAll(Collection<Record> c);
        
        public abstract boolean remove(Record r);

        public Object getLabelY() {
            return labelY;
        }

        protected void setLabelY(Object labelY) {
            this.labelY = labelY;
        }
        
    }
    
    public static abstract class ModelParameters<CL extends BaseMLclusterer.Cluster> extends BaseMLmodel.ModelParameters {
        //number of observations used for training
        private Integer n =0 ;
        
        //number of features in data. IN DATA not in the algorithm.
        private Integer d =0 ;
        
        //number of clusters
        private Integer c =0;
        
        //number of classes if the dataset is annotated. Use Linked Hash Set to ensure that the order of classes will be maintained. 
        private Set<Object> goldStandardClasses = new LinkedHashSet<>(); //this is small. Size equal to class numbers;
        
        
        
        
        //MORPHIA does not support complex objects in maps inside it. Thus to 
        //store it I have to serialize deserialize it and store it as binary.
        @Transient
        private Map<Integer, CL> clusterList; //the cluster objects of the model
        private transient byte[] clusterList_serialized;
        
        @PrePersist
        public void prePersist(){
            if(clusterList != null && clusterList_serialized==null){
                clusterList_serialized = DeepCopy.serialize(clusterList);
            }
        }

        @PostLoad
        public void postLoad(){
            if(clusterList==null && clusterList_serialized!=null){
                clusterList = (Map<Integer, CL>) DeepCopy.deserialize(clusterList_serialized);
            }
        }
        
        
        
        @Override
        public void bigDataStructureInitializer(BigDataStructureFactory bdsf, MemoryConfiguration memoryConfiguration) {
            super.bigDataStructureInitializer(bdsf, memoryConfiguration);
            
            //BigDataStructureFactory.MapType mapType = memoryConfiguration.getMapType();
            //int LRUsize = memoryConfiguration.getLRUsize();
            
            //clusterList = bdsf.getMap("clusterList", mapType, LRUsize);
            
            if(clusterList==null) {
                clusterList = new HashMap<>();
            }
        }
        
        
        //Getters / Setters
        
        public Integer getN() {
            return n;
        }

        public void setN(Integer n) {
            this.n = n;
        }

        public Integer getD() {
            return d;
        }

        public void setD(Integer d) {
            this.d = d;
        }

        public Integer getC() {
            return c;
        }

        public void setC(Integer c) {
            this.c = c;
        }

        public Set<Object> getGoldStandardClasses() {
            return goldStandardClasses;
        }

        public void setGoldStandardClasses(Set<Object> goldStandardClasses) {
            this.goldStandardClasses = goldStandardClasses;
        }

        public Map<Integer, CL> getClusterList() {
            return clusterList;
        }

        public void setClusterList(Map<Integer, CL> clusterList) {
            this.clusterList = clusterList;
        }
        
        
    } 
    
    
    public static abstract class TrainingParameters extends BaseMLmodel.TrainingParameters {    

    } 

    public static abstract class ValidationMetrics extends BaseMLmodel.ValidationMetrics {
        //References: http://nlp.stanford.edu/IR-book/html/htmledition/evaluation-of-clustering-1.html
        //http://thesis.neminis.org/wp-content/plugins/downloads-manager/upload/masterThesis-VR.pdf
        private Double purity = null;
        private Double NMI = null; //Normalized Mutual Information: I(Omega,Gama) calculation
        
        public Double getPurity() {
            return purity;
        }

        public void setPurity(Double purity) {
            this.purity = purity;
        }

        public Double getNMI() {
            return NMI;
        }

        public void setNMI(Double NMI) {
            this.NMI = NMI;
        }
        
    }
    
    protected BaseMLclusterer(String dbName, Class<MP> mpClass, Class<TP> tpClass, Class<VM> vmClass) {
        super(dbName, mpClass, tpClass, vmClass, new ClustererValidation<>());
    } 
    
    protected BaseMLclusterer(String dbName, Class<MP> mpClass, Class<TP> tpClass, Class<VM> vmClass, ModelValidation<MP, TP, VM> modelValidator) {
        super(dbName, mpClass, tpClass, vmClass, modelValidator);
    } 
    
    
    @Override
    protected VM validateModel(Dataset validationData) {
        predictDataset(validationData);
        
        int n = validationData.size();
        
        MP modelParameters = knowledgeBase.getModelParameters();
        Map<Integer, CL> clusterList = modelParameters.getClusterList();
        Set<Object> goldStandardClassesSet = modelParameters.getGoldStandardClasses();
        
        //create new validation metrics object
        VM validationMetrics = knowledgeBase.getEmptyValidationMetricsObject();
        
        if(goldStandardClassesSet.isEmpty()) {
            return validationMetrics;
        }
        
        //We don't store the Contingency Table because we can't average it with
        //k-cross fold validation. Each clustering produces a different number
        //of clusters and thus different enumeration. Thus averaging the results
        //is impossible and that is why we don't store it in the validation object.
        
        //List<Object> = [Clusterid,GoldStandardClass]
        Map<List<Object>, Double> ctMap = new HashMap<>();
        
        //frequency tables
        Map<Integer, Double> countOfW = new HashMap<>(); //this is small equal to number of clusters
        Map<Object, Double> countOfC = new HashMap<>(); //this is small equal to number of classes
        
        //initialize the tables with zeros
        for(CL cluster : clusterList.values()) {
            Integer clusterId = cluster.getClusterId();
            countOfW.put(clusterId, 0.0);
            for(Object theClass : modelParameters.getGoldStandardClasses()) {
                ctMap.put(Arrays.<Object>asList(clusterId, theClass), 0.0);
                
                countOfC.put(theClass, 0.0);
            }
        }
        
        
        //count the co-occurrences of ClusterId-GoldStanardClass
        for(Record r : validationData) {
            Integer clusterId = (Integer) r.getYPredicted(); //fetch cluster assignment
            Object goldStandardClass = r.getY(); //the original class of the objervation
            List<Object> tpk = Arrays.<Object>asList(clusterId, goldStandardClass);
            ctMap.put(tpk, ctMap.get(tpk) + 1.0);
            
            //update cluster and class counts
            countOfW.put(clusterId, countOfW.get(clusterId)+1.0);
            countOfC.put(goldStandardClass, countOfC.get(goldStandardClass)+1.0);
        }
        
        double logN = Math.log((double)n);
        double purity=0.0;
        double Iwc=0.0; //http://nlp.stanford.edu/IR-book/html/htmledition/evaluation-of-clustering-1.html
        for(CL cluster : clusterList.values()) {
            Integer clusterId = cluster.getClusterId();
            double maxCounts=Double.NEGATIVE_INFINITY;
            
            //loop through the possible classes and find the most popular one
            for(Object goldStandardClass : modelParameters.getGoldStandardClasses()) {
                List<Object> tpk = Arrays.<Object>asList(clusterId, goldStandardClass);
                double Nwc = ctMap.get(tpk);
                if(Nwc>maxCounts) {
                    maxCounts=Nwc;
                    cluster.setLabelY(goldStandardClass);
                }
                
                if(Nwc>0) {
                    Iwc+= (Nwc/n)*(Math.log(Nwc) -Math.log(countOfC.get(goldStandardClass))
                                   -Math.log(countOfW.get(clusterId)) + logN);
                }
            }
            purity += maxCounts;
        }
        ctMap = null;
        purity/=n;
        
        validationMetrics.setPurity(purity);
        
        double entropyW=0.0;
        for(Double Nw : countOfW.values()) {
            entropyW-=(Nw/n)*(Math.log(Nw)-logN);
        }
        
        double entropyC=0.0;
        for(Double Nc : countOfW.values()) {
            entropyC-=(Nc/n)*(Math.log(Nc)-logN);
        }
        
        validationMetrics.setNMI(Iwc/((entropyW+entropyC)/2.0));
        
        return validationMetrics;
    }
    
    public Map<Integer, CL> getClusters() {
        if(knowledgeBase==null) {
            return null;
        }
        
        //clone object ot avoid modifications
        return DeepCopy.cloneObject(knowledgeBase.getModelParameters().getClusterList());
    }
}