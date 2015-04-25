/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
package com.datumbox.common.persistentstorage;

import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;

/**
 * Any class that inherits from the abstract AutoCloseConnector class can be used
 * in a try-with-resources statement block. Moreover this class setups a shutdown
 * hook which ensures that the Connector will automatically call close() before the
 * JVM is terminated.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public abstract class AutoCloseConnector implements DatabaseConnector, AutoCloseable {
    
    private boolean isClosed = false;
    
    /**
     * Protected Constructor which is responsible for adding the Shutdown hook.
     */
    protected AutoCloseConnector() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                AutoCloseConnector.this.close();
            }
        });
    }
    
    /**
     * Checks if the connector is closed.
     * 
     * @return 
     */
    @Override
    public boolean isClosed() {
        return isClosed;
    }
    
    /**
     * Marks the connector as closed.
     */
    @Override
    public void close() {
        isClosed = true;
    }
    
    /**
     * Ensures the connection is not closed.
     */
    protected void ensureNotClosed() {
        if(isClosed) {
            throw new RuntimeException("The connector is already closed");
        }
    }
}
