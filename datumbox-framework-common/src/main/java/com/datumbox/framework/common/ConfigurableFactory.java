/**
 * Copyright (C) 2013-2019 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
package com.datumbox.framework.common;

import com.datumbox.framework.common.interfaces.Configurable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.Properties;

/**
 * Factory that initializes and returns the Configurable objects based on the datumbox configuration properties file.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class ConfigurableFactory {

    private static final String DEFAULT_POSTFIX = ".default";

    private static final Logger logger = LoggerFactory.getLogger(ConfigurableFactory.class);
    
    /**
     * Initializes the Configuration Object based on the configuration file.
     * 
     * @param <C>
     * @param klass
     * @return 
     */
    public static <C extends Configurable> C getConfiguration(Class<C> klass) {
        String defaultPropertyFile = "datumbox." + klass.getSimpleName().toLowerCase(Locale.ENGLISH) + DEFAULT_POSTFIX + ".properties";

        Properties properties = new Properties();
        
        ClassLoader cl = klass.getClassLoader();
        
        //Load default properties from jar
        try (InputStream in = cl.getResourceAsStream(defaultPropertyFile)) {
            properties.load(in);
        }
        catch(IOException ex) {
            throw new UncheckedIOException(ex);
        }
        
        //Look for user defined properties
        String propertyFile = defaultPropertyFile.replaceFirst(DEFAULT_POSTFIX, "");
        if(cl.getResource(propertyFile)!=null) {
            //Override the default if they exist
            try (InputStream in = cl.getResourceAsStream(propertyFile)) {
                properties.load(in);
            }
            catch(IOException ex) {
                throw new UncheckedIOException(ex);
            }
            logger.trace("Loading properties file {}: {}", propertyFile, properties);
        }
        else {
            logger.warn("Using default properties file {}: {}", defaultPropertyFile, properties);
        }

        return getConfiguration(klass, properties);
    }

    /**
     * Initializes the Configuration Object using a properties object.
     *
     * @param klass
     * @param properties
     * @param <C>
     * @return
     */
    public static <C extends Configurable> C getConfiguration(Class<C> klass, Properties properties) {
        //Initialize configuration object
        C configuration;
        try {
            Constructor<C> constructor = klass.getDeclaredConstructor();
            constructor.setAccessible(true);
            configuration = constructor.newInstance();
        }
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException(ex);
        }

        configuration.load(properties);

        return configuration;
    }
}
