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
package com.datumbox.framework.common.storageengines.abstracts;

import com.datumbox.framework.common.storageengines.interfaces.StorageConfiguration;

import java.io.File;

/**
 * Parent class of all File-based Storage Configurations.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public abstract class AbstractFileStorageConfiguration implements StorageConfiguration {

    /**
     * The output directory of the models.
     */
    protected String outputDirectory = null;

    /** {@inheritDoc} */
    @Override
    public String getStorageNameSeparator() {
        return File.separator;
    }

    /**
     * Getter for the output directory where the InMemory data files are stored.
     *
     * @return
     */
    public String getOutputDirectory() {
        return outputDirectory;
    }

    /**
     * Setter for the output directory where the InMemory data files are stored.
     *
     * @param outputDirectory
     */
    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

}
