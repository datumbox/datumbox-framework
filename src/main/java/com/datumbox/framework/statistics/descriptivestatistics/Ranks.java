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
package com.datumbox.framework.statistics.descriptivestatistics;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.FlatDataList;
import com.datumbox.common.utilities.MapFunctions;
import com.datumbox.common.utilities.TypeConversions;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class Ranks {

    /**
     * Replaces the actual values of the flatDataCollection with their ranks and
     * returns in the tieCounter the keys that occur more than once and the
     * number of occurrences. The tieCounter does not store the list and ranks
     * of the actual ties because we never use them.
     *
     * @param flatDataCollection
     * @return
     */
    public static AssociativeArray getRanksFromValues(FlatDataList flatDataCollection) {
        AssociativeArray tiesCounter = new AssociativeArray(new LinkedHashMap<>());
        Map<Object, Double> key2AvgRank = new LinkedHashMap<>();
        _buildRankArrays(flatDataCollection, tiesCounter, key2AvgRank);
        int i = 0;
        for (Object value : flatDataCollection) {
            flatDataCollection.set(i++, key2AvgRank.get(value));
        }
        return tiesCounter;
    }

    /**
     * Replaces the actual values of the associativeArray with their ranks and
     * returns in the tieCounter the keys that occur more than once and the
     * number of occurrences. The tieCounter does not store the list and ranks
     * of the actual ties because we never use them.
     *
     * @param associativeArray
     * @return
     */
    public static AssociativeArray getRanksFromValues(AssociativeArray associativeArray) {
        AssociativeArray tiesCounter = new AssociativeArray(new LinkedHashMap<>());
        Map<Object, Double> key2AvgRank = new LinkedHashMap<>();
        _buildRankArrays(associativeArray.toFlatDataList(), tiesCounter, key2AvgRank);
        for (Map.Entry<Object, Object> entry : associativeArray.entrySet()) {
            associativeArray.put(entry.getKey(), key2AvgRank.get(entry.getValue()));
        }
        return tiesCounter;
    }

    /**
     * Internal method used by getRanksFromValues() to produce the tiesCounter
     * and key2AvgRank arrays.
     *
     * @param dataCollection
     * @param tiesCounter
     * @param key2AvgRank
     */
    private static void _buildRankArrays(FlatDataList flatDataCollection, AssociativeArray tiesCounter, Map<Object, Double> key2AvgRank) {
        for (Object value : flatDataCollection) {
            Object count = tiesCounter.get(value);
            if (count == null) {
                count = 0;
            }
            tiesCounter.put(value, ((Number) count).intValue() + 1);
        }
        tiesCounter.overwrite(MapFunctions.<Object, Object>sortNumberMapByKeyAscending(tiesCounter.entrySet()));
        int itemCounter = 0;
        Iterator<Map.Entry<Object, Object>> it = tiesCounter.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Object, Object> entry = it.next();
            Object key = entry.getKey();
            double count = TypeConversions.toDouble(entry.getValue());
            if (count <= 1.0) {
                it.remove();
            }
            double avgRank = ((itemCounter + 1) + (itemCounter + count)) / 2.0;
            key2AvgRank.put(key, avgRank);
            itemCounter += count;
        }
    }
    
}
