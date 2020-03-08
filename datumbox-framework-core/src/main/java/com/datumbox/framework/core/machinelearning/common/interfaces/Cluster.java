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
package com.datumbox.framework.core.machinelearning.common.interfaces;

import com.datumbox.framework.core.common.interfaces.Learnable;


/**
 * The Cluster object stores all the information of the cluster, including the
 * assigned points, the parameters and the label.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public interface Cluster extends Learnable {

    /**
     * Returns the number of records assigned to this cluster.
     * 
     * @return 
     */
    public int size();

}
    