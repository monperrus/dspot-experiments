/**
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.drools.template.parser;


import org.junit.Assert;
import org.junit.Test;


public class ColumnFactoryTest {
    @Test
    public void testGetColumn() {
        ColumnFactory f = new ColumnFactory();
        Column column = f.getColumn("column");
        Assert.assertTrue((column instanceof StringColumn));
        Assert.assertEquals("column", column.getName());
    }

    @Test
    public void testGetStringArrayColumn() {
        ColumnFactory f = new ColumnFactory();
        Column column = f.getColumn("column: String[]");
        Assert.assertTrue((column instanceof ArrayColumn));
        Assert.assertEquals("column", column.getName());
        Assert.assertEquals("StringCell", getCellType());
    }

    @Test
    public void testGetLongArrayColumn() {
        ColumnFactory f = new ColumnFactory();
        Column column = f.getColumn("column: Long[]");
        Assert.assertTrue((column instanceof ArrayColumn));
        Assert.assertEquals("column", column.getName());
        Assert.assertEquals("LongCell", getCellType());
    }

    @Test
    public void testGetArrayColumnSimple() {
        ColumnFactory f = new ColumnFactory();
        Column column = f.getColumn("column[]");
        Assert.assertTrue((column instanceof ArrayColumn));
        Assert.assertEquals("column", column.getName());
        Assert.assertEquals("StringCell", getCellType());
    }

    @Test
    public void testGetLongColumn() {
        ColumnFactory f = new ColumnFactory();
        Column column = f.getColumn("column: Long");
        Assert.assertTrue((column instanceof LongColumn));
        Assert.assertEquals("column", column.getName());
    }

    @Test
    public void testInvalidGetColumn() {
        try {
            ColumnFactory f = new ColumnFactory();
            f.getColumn("column$");
            Assert.fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testGetDollarColumn() {
        ColumnFactory f = new ColumnFactory();
        Column column = f.getColumn("$column");
        Assert.assertTrue((column instanceof StringColumn));
        Assert.assertEquals("$column", column.getName());
        Assert.assertEquals("StringCell", column.getCellType());
    }

    @Test
    public void testGetDollarArrayColumn() {
        ColumnFactory f = new ColumnFactory();
        Column column = f.getColumn("$column[]");
        Assert.assertTrue((column instanceof ArrayColumn));
        Assert.assertEquals("$column", column.getName());
        Assert.assertEquals("StringCell", getCellType());
    }

    @Test
    public void testGetDollarTypedColumn() {
        ColumnFactory f = new ColumnFactory();
        Column column = f.getColumn("$column: Long");
        Assert.assertTrue((column instanceof LongColumn));
        Assert.assertEquals("$column", column.getName());
        Assert.assertEquals("LongCell", column.getCellType());
    }

    @Test
    public void testGetDollarArrayTypedColumn() {
        ColumnFactory f = new ColumnFactory();
        Column column = f.getColumn("$column: Long[]");
        Assert.assertTrue((column instanceof ArrayColumn));
        Assert.assertEquals("$column", column.getName());
        Assert.assertEquals("LongCell", getCellType());
    }
}
