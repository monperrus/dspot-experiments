/**
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.db;


import org.ethereum.datasource.QuotientFilter;
import org.junit.Assert;
import org.junit.Test;


/**
 * Created by Anton Nashatyrev on 13.03.2017.
 */
public class QuotientFilterTest {
    @Test
    public void maxDuplicatesTest() {
        QuotientFilter f = QuotientFilter.create(50000000, 1000).withMaxDuplicates(2);
        f.insert(1);
        Assert.assertTrue(f.maybeContains(1));
        f.remove(1);
        Assert.assertFalse(f.maybeContains(1));
        f.insert(1);
        f.insert(1);
        f.insert(2);
        Assert.assertTrue(f.maybeContains(2));
        f.remove(2);
        Assert.assertFalse(f.maybeContains(2));
        f.remove(1);
        f.remove(1);
        Assert.assertTrue(f.maybeContains(1));
        f.insert(3);
        f.insert(3);
        Assert.assertTrue(f.maybeContains(3));
        f.remove(3);
        f.remove(3);
        Assert.assertTrue(f.maybeContains(3));
    }
}
