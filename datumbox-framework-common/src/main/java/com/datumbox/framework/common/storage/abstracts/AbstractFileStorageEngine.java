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
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Parent class of all File-based Storage Engines.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <SC>
 */
public abstract class AbstractFileStorageEngine<SC extends AbstractFileStorageConfiguration> extends AbstractStorageEngine<SC> {

    /**
     * @param storageName
     * @param storageConfiguration
     * @see AbstractStorageEngine#AbstractStorageEngine(String, StorageConfiguration)
     */
    protected AbstractFileStorageEngine(String storageName, SC storageConfiguration) {
        super(storageName, storageConfiguration);
    }

    /**
     * Returns the location of the directory from the configuration or the temporary directory if not defined.
     *
     * @return
     */
    protected String getDirectory() {
        //get the default filepath of the permanet storage file
        String directory = storageConfiguration.getDirectory();

        if(directory == null || directory.isEmpty()) {
            directory = System.getProperty("java.io.tmpdir"); //write them to the tmp directory
        }

        return directory;
    }

    /**
     * Returns the root path of the storage.
     *
     * @param storageName
     * @return
     */
    protected Path getRootPath(String storageName) {
        return Paths.get(getDirectory() + File.separator + storageName);
    }

    /**
     * Deletes the file or directory recursively if it exists.
     *
     * @param path
     * @return
     * @throws IOException
     */
    protected boolean deleteIfExistsRecursively(Path path) throws IOException {
        try {
            return Files.deleteIfExists(path);
        }
        catch (DirectoryNotEmptyException ex) {
            //do recursive delete
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
            return true;
        }
    }

    /**
     * Deletes a directory and optionally removes the parent directory if it becomes empty.
     *
     * @param path
     * @param cleanParent
     * @return
     * @throws IOException
     */
    protected boolean deleteDirectory(Path path, boolean cleanParent) throws IOException {
        boolean pathExists = deleteIfExistsRecursively(path);
        if(pathExists && cleanParent) {
            cleanEmptyParentDirectory(path.getParent());
            return true;
        }
        return false;
    }

    /**
     * Removes recursively all empty parent directories up to and excluding the storage directory.
     *
     * @param path
     * @throws IOException
     */
    private void cleanEmptyParentDirectory(Path path) throws IOException {
        Path normPath = path.normalize();
        if(normPath.equals(Paths.get(getDirectory()).normalize()) || normPath.equals(Paths.get(System.getProperty("java.io.tmpdir")).normalize())) { //stop if we reach the output or temporary directory
            return;
        }
        try {
            Files.deleteIfExists(path); //delete the directory if empty
            cleanEmptyParentDirectory(path.getParent()); //do the same with parent directory
        }
        catch(DirectoryNotEmptyException ex) {
            //if directory non-empty ignore exception
        }
    }

    /**
     * Moves a directory in the target location.
     *
     * @param src
     * @param target
     * @return
     * @throws IOException
     */
    protected boolean moveDirectory(Path src, Path target) throws IOException {
        if(Files.exists(src)) {
            createDirectoryIfNotExists(target.getParent());
            deleteDirectory(target, false);
            Files.move(src, target);
            cleanEmptyParentDirectory(src.getParent());
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Creates the directory in the target location if it does not exist.
     *
     * @param path
     * @return
     * @throws IOException
     */
    protected boolean createDirectoryIfNotExists(Path path) throws IOException {
        if(!Files.exists(path)) {
            Files.createDirectories(path);
            return true;
        }
        else {
            return false;
        }
    }
}
