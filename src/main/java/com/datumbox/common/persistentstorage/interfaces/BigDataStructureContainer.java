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

import com.datumbox.common.objecttypes.Learnable;
import com.datumbox.common.persistentstorage.factories.BigDataStructureFactory;
import com.datumbox.configuration.MemoryConfiguration;

/**
 * This interface is used to mark Objects which are added in the database
 * and contain big data structures (maps, sets, collections etc). When objects contain such
 objects they must be identified by the preSave() method of the StructureFactory
 and saved appropriately in the memory/db. Note that BigDataStructureContainers
 can contain other containers.
 * 
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public interface BigDataStructureContainer extends Learnable {
    /**
     * Links the large variables with DB-backed collections.
     * 
     * @param bdsf 
     * @param memoryConfiguration 
     */
    
    public void bigDataStructureInitializer(BigDataStructureFactory bdsf, MemoryConfiguration memoryConfiguration);
    
    /**
     * Clears unnecessary large variables before they are saved in the DB-backed Collection.
     * 
     * @param bdsf 
     */
    public void bigDataStructureCleaner(BigDataStructureFactory bdsf);
}
