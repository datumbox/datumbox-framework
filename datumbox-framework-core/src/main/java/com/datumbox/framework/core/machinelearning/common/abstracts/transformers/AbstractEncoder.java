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
package com.datumbox.framework.core.machinelearning.common.abstracts.transformers;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.dataobjects.TypeInference;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Base class for all categorical/ordinal encoders of the framework.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public abstract class AbstractEncoder<MP extends AbstractEncoder.AbstractModelParameters, TP extends AbstractEncoder.AbstractTrainingParameters> extends AbstractTransformer<MP, TP> {

    /**
     * @param trainingParameters
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(AbstractTrainer.AbstractTrainingParameters, Configuration)
     */
    protected AbstractEncoder(TP trainingParameters, Configuration configuration) {
        super(trainingParameters, configuration);
    }

    /**
     * @param storageName
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(String, Configuration)
     */
    protected AbstractEncoder(String storageName, Configuration configuration) {
        super(storageName, configuration);
    }

    /** {@inheritDoc} */
    @Override
    protected Set<TypeInference.DataType> getSupportedXDataTypes() {
        return new HashSet<>(Arrays.asList(TypeInference.DataType.CATEGORICAL, TypeInference.DataType.ORDINAL));
    }
}
