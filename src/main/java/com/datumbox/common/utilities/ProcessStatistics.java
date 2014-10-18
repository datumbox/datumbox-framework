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
package com.datumbox.common.utilities;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;
import org.apache.commons.lang3.time.StopWatch;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class ProcessStatistics {
    
    StopWatch timer;
    
    public void start() {
        timer = new StopWatch();
        timer.start();
    }
    
    public long stop() {
        timer.stop();
        
        return timer.getTime();
    }
    
    public long peakMemoryBytes() {
        long maxBytes = Long.MIN_VALUE;
        
        List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean pool : pools) {
            MemoryUsage peak = pool.getPeakUsage();
            
            long peakUsed = peak.getUsed();
            if(maxBytes<peakUsed) {
                maxBytes=peakUsed;
            }
        }
        
        return maxBytes;
    }
}
