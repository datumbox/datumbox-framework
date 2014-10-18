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

/**
 *
 * @author bbriniotis
 * @param <T>
 */
public class ArrayIndexReverseComparator<T extends Comparable<T>> extends ArrayIndexComparator<T> {

    public ArrayIndexReverseComparator(T[] array) {
        super(array);
    }
    
    @Override
    public int compare(Integer index1, Integer index2) {
        return array[index2].compareTo(array[index1]);
    }
}
