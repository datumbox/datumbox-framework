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
package com.datumbox.common.persistentstorage;

import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Any class that inherits from the abstract AutoCloseConnector class can be used
 * in a try-with-resources statement block. Moreover this class setups a shutdown
 * hook which ensures that the Connector will automatically call close() before the
 * JVM is terminated.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public abstract class AutoCloseConnector implements DatabaseConnector {
    
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    
    private Thread hook;
    
    /**
     * Protected Constructor which is responsible for adding the Shutdown hook.
     */
    protected AutoCloseConnector() {
        hook = new Thread(() -> {
            AutoCloseConnector.this.hook = null;
            if(AutoCloseConnector.this.isClosed()) {
                return;
            }
            AutoCloseConnector.this.close();
        });
        Runtime.getRuntime().addShutdownHook(hook);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isClosed() {
        return isClosed.get();
    }
    
    /** {@inheritDoc} */
    @Override
    public void close() {
        if(isClosed() == false && hook != null) {
            //remove hook to save memory
            Runtime.getRuntime().removeShutdownHook(hook);
            hook = null;
        }
        isClosed.set(true);
    }
    
    /**
     * Ensures the connection is not closed.
     */
    protected void assertConnectionOpen() {
        if(isClosed()) {
            throw new RuntimeException("The connection is already closed.");
        }
    }
    
}
