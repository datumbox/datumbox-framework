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
package com.datumbox.common.persistentstorage.interfaces;

import com.datumbox.framework.machinelearning.common.dataobjects.KnowledgeBase;
import java.util.Map;

/**
 * DB Drivers which can be used to permanently store/handle the parameters of the
 * models should implement this interface. This interface defines the methods which
 * are used to initialize db-backed collections and objects used for storing the
 * data. It is also responsible for connecting, clearing and managing the dbs.
 * 
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public interface DatabaseConnector {
    
    public <KB extends KnowledgeBase> void save(KB holderObject);
    
    public <KB extends KnowledgeBase> KB load(Class<KB> klass);
    
    public boolean existsDatabase();
    
    public void dropDatabase();
        
    public <T extends Map> void dropBigMap(String name, T map);
    
    public <K,V> Map<K,V> getBigMap(String name);
    
}
