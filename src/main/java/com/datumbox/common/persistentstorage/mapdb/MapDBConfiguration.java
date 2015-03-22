/*
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
package com.datumbox.common.persistentstorage.mapdb;

import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class MapDBConfiguration implements DatabaseConfiguration {
    
    //Mandatory constants
    private static final String DBNAME_SEPARATOR = "_"; //NOT permitted characters are: <>:"/\|?*
    private static final String TMP_PREFIX = "TMP_";

    //DB specific properties
    private String dbRootFolder = "";

    @Override
    public DatabaseConnector getConnector(String database) {
        return new MapDBConnector(database, this);
    }

    @Override
    public String getDBnameSeparator() {
        return DBNAME_SEPARATOR;
    }

    @Override
    public String getTmpPrefix() {
        return TMP_PREFIX;
    }

    public String getDbRootFolder() {
        return dbRootFolder;
    }

    public void setDbRootFolder(String dbRootFolder) {
        this.dbRootFolder = dbRootFolder;
    }
}
