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
package com.datumbox.framework.utilities.text.extractors;

import com.datumbox.common.objecttypes.Parameterizable;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 * @param <TP>
 */
public abstract class TextExtractor<TP extends TextExtractor.Parameters, K, V> {
    
    public static abstract class Parameters implements Parameterizable {

    }
    
    protected TP parameters;
    
    public TP getParameters() {
        return parameters;
    }

    public void setParameters(TP parameters) {
        this.parameters = parameters;
    }     
     
    public abstract Map<K, V> extract(final String text);
    
    /**
     * Generates a new instance of a TextExtractor by providing the Class of the
     * TextExtractor.
     * 
     * @param <T>
     * @param tClass
     * @return 
     */
    public static <T extends TextExtractor> T newInstance(Class<T> tClass) {
        T textExtractor = null;
        try {
            textExtractor = (T) tClass.getConstructor().newInstance();
        } 
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
        
        return textExtractor;
    }
}
