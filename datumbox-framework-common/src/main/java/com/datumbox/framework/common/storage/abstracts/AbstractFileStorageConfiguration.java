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
package com.datumbox.framework.common.storage.abstracts;

import com.datumbox.framework.common.storage.interfaces.StorageConfiguration;

import java.io.File;

/**
 * Parent class of all File-based Storage Configurations.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public abstract class AbstractFileStorageConfiguration implements StorageConfiguration {

    /**
     * The directory of the models.
     */
    protected String directory = null;

    /** {@inheritDoc} */
    @Override
    public String getStorageNameSeparator() {
        return File.separator;
    }

    /**
     * Getter for the directory where the data files are stored.
     *
     * @return
     */
    public String getDirectory() {
        return directory;
    }

    /**
     * Setter for the directory where the data files are stored.
     *
     * @param directory
     */
    public void setDirectory(String directory) {
        this.directory = directory;
    }

}
