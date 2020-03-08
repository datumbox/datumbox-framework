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
package com.datumbox.framework.core.common.dataobjects;

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.OpenMapRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 * The MapRealVector class is a RealVector implementation which stores the data in a Map.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class MapRealVector extends OpenMapRealVector {
    private static final long serialVersionUID = 1L;

    /**
     * Build a 0-length vector.
     */
    public MapRealVector() {
        super();
    }

    /**
     * Construct a vector of zeroes.
     *
     * @param dimension Size of the vector.
     */
    public MapRealVector(int dimension) {
        super(dimension);
    }

    /**
     * Construct a vector of zeroes, specifying zero tolerance.
     *
     * @param dimension Size of the vector.
     * @param epsilon Tolerance below which a value considered zero.
     */
    public MapRealVector(int dimension, double epsilon) {
        super(dimension, epsilon);
    }

    /**
     * Build a resized vector, for use with append.
     *
     * @param v Original vector.
     * @param resize Amount to add.
     */
    protected MapRealVector(MapRealVector v, int resize) {
        super((OpenMapRealVector)v, resize);
    }

    /**
     * Build a resized vector, for use with append.
     *
     * @param v Original vector.
     * @param resize Amount to add.
     */
    protected MapRealVector(OpenMapRealVector v, int resize) {
        super(v, resize);
    }

    /**
     * Build a vector with known the sparseness (for advanced use only).
     *
     * @param dimension Size of the vector.
     * @param expectedSize The expected number of non-zero entries.
     */
    public MapRealVector(int dimension, int expectedSize) {
        super(dimension, expectedSize);
    }

    /**
     * Build a vector with known the sparseness and zero tolerance
     * setting (for advanced use only).
     *
     * @param dimension Size of the vector.
     * @param expectedSize Expected number of non-zero entries.
     * @param epsilon Tolerance below which a value is considered zero.
     */
    public MapRealVector(int dimension, int expectedSize, double epsilon) {
        super(dimension, expectedSize, epsilon);
    }

    /**
     * Create from an array.
     * Only non-zero entries will be stored.
     *
     * @param values Set of values to create from.
     */
    public MapRealVector(double[] values) {
        super(values);
    }

    /**
     * Create from an array, specifying zero tolerance.
     * Only non-zero entries will be stored.
     *
     * @param values Set of values to create from.
     * @param epsilon Tolerance below which a value is considered zero.
     */
    public MapRealVector(double[] values, double epsilon) {
        super(values, epsilon);
    }

    /**
     * Create from an array.
     * Only non-zero entries will be stored.
     *
     * @param values The set of values to create from
     */
    public MapRealVector(Double[] values) {
        super(values, DEFAULT_ZERO_TOLERANCE);
    }

    /**
     * Create from an array.
     * Only non-zero entries will be stored.
     *
     * @param values Set of values to create from.
     * @param epsilon Tolerance below which a value is considered zero.
     */
    public MapRealVector(Double[] values, double epsilon) {
        super(values, epsilon);
    }

    /**
     * Copy constructor.
     *
     * @param v Instance to copy from.
     */
    public MapRealVector(MapRealVector v) {
        super((OpenMapRealVector)v);
    }

    /**
     * Copy constructor.
     *
     * @param v Instance to copy from.
     */
    public MapRealVector(OpenMapRealVector v) {
        super(v);
    }

    /**
     * Generic copy constructor.
     *
     * @param v Instance to copy from.
     */
    public MapRealVector(RealVector v) {
        super(v);
    }

    /** {@inheritDoc} */
    @Override
    public RealMatrix outerProduct(RealVector v) {
        final int m = this.getDimension();
        final int n = v.getDimension();
        final RealMatrix product;
        if(m > 1000000) { //use only in big values
            product = new MapRealMatrix(m, n);
        }
        else {
            product = new OpenMapRealMatrix(m, n);
        }
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                product.setEntry(i, j, this.getEntry(i) * v.getEntry(j));
            }
        }
        return product;
    }
}
