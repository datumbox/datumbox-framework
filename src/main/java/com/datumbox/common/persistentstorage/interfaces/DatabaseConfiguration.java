/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis at datumbox.com>
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

/**
 * DB connectors can be configured by objects that implement this interface.
 * 
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public interface DatabaseConfiguration {
    
    /**
     * Returns the separator that is used in the DB names. Usually the database
     * names used by the algorithms are concatenations of various words separated
     * by this character.
     * 
     * @return 
     */
    public String getDBnameSeparator();
    
    /**
     * Initializes and returns a connection to the Database.
     * 
     * @param database
     * @return 
     */
    public DatabaseConnector getConnector(String database);
}
