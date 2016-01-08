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
package com.datumbox.framework.machinelearning.common.abstracts;

import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.interfaces.Learnable;
import java.util.Iterator;
import java.util.Set;


/**
 * The Cluster class stores all the information of the cluster, including the
 * assigned points, the parameters and the label.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public abstract class AbstractCluster implements Learnable, Iterable<Integer> {

    /**
     * Getter for cluster id.
     * 
     * @return 
     */
    public abstract Integer getClusterId();

    /**
     * Getter for the set of Record Ids assigned to this cluster.
     * 
     * @return 
     */
    public abstract Set<Integer> getRecordIdSet();

    /**
     * Getter for the label Y of the cluster.
     * 
     * @return 
     */
    public abstract Object getLabelY();

    /**
     * Setter for the label Y of the cluster.
     * 
     * @param labelY 
     */
    protected abstract void setLabelY(Object labelY);

    /**
     * Returns the number of records assigned to this cluster.
     * 
     * @return 
     */
    public abstract int size();

    /** {@inheritDoc} */
    @Override
    public abstract Iterator<Integer> iterator();

    /**
     * Clears all the assignments and internal parameters of the cluster.
     */
    protected abstract void clear();

    //abstract methods that modify the cluster set and should update its statistics in each implementation

    /**
     * Adds the Record r with unique identified rId in the cluster and
     * updates the cluster's parameters.
     * 
     * @param rId
     * @param r
     * @return 
     */
    protected abstract boolean add(Integer rId, Record r);

    /**
     * Removes the Record r with unique identified rId from the cluster and
     * updates the cluster's parameters
     * 
     * @param rId
     * @param r
     * @return 
     */
    protected abstract boolean remove(Integer rId, Record r);

}
    