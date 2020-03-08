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
package com.datumbox.framework.core.statistics.descriptivestatistics;

import com.datumbox.framework.common.dataobjects.AssociativeArray;
import com.datumbox.framework.common.dataobjects.FlatDataList;
import com.datumbox.framework.common.dataobjects.TypeInference;
import com.datumbox.framework.core.common.utilities.MapMethods;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility class which provides methods related to Rank estimation.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
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
        AssociativeArray tiesCounter = new AssociativeArray();
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
        AssociativeArray tiesCounter = new AssociativeArray();
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
     * @param flatDataCollection
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
        tiesCounter.overwrite(MapMethods.<Object, Object>sortNumberMapByKeyAscending(tiesCounter.entrySet()));
        int itemCounter = 0;
        Iterator<Map.Entry<Object, Object>> it = tiesCounter.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Object, Object> entry = it.next();
            Object key = entry.getKey();
            double count = TypeInference.toDouble(entry.getValue());
            if (count <= 1.0) {
                it.remove();
            }
            double avgRank = ((itemCounter + 1) + (itemCounter + count)) / 2.0;
            key2AvgRank.put(key, avgRank);
            itemCounter += count;
        }
    }
    
}
