/**
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.drools.workbench.models.guided.dtable.shared.model;


import java.util.List;
import org.junit.Assert;
import org.junit.Test;


public class ActionWorkItemInsertFactCol52Test extends ColumnTestBase {
    private ActionWorkItemInsertFactCol52 column1;

    private ActionWorkItemInsertFactCol52 column2;

    @Test
    public void testDiffEmpty() {
        checkDiffEmpty(column1, column2);
    }

    @Test
    public void testDiffWorkItemName() {
        column1.setWorkItemName("WID1");
        column2.setWorkItemName("WID2");
        checkSingleDiff(ActionWorkItemInsertFactCol52.FIELD_WORK_ITEM_NAME, "WID1", "WID2", column1, column2);
    }

    @Test
    public void testDiffResultName() {
        column1.setWorkItemResultParameterName("param1");
        column2.setWorkItemResultParameterName("param2");
        checkSingleDiff(ActionWorkItemInsertFactCol52.FIELD_WORK_ITEM_RESULT_PARAM_NAME, "param1", "param2", column1, column2);
    }

    @Test
    public void testDiffResultType() {
        column1.setParameterClassName("Type1");
        column2.setParameterClassName("Type2");
        checkSingleDiff(ActionWorkItemInsertFactCol52.FIELD_PARAMETER_CLASSNAME, "Type1", "Type2", column1, column2);
    }

    @Test
    public void testDiffAll() {
        column1.setWorkItemName("WID1");
        column1.setWorkItemResultParameterName("param1");
        column1.setParameterClassName("Type1");
        column1.setFactType("FactType1");
        column1.setBoundName("$var1");
        column1.setFactField("field1");
        column1.setType("Type1");
        column1.setValueList("a,b");
        column1.setInsertLogical(false);
        column1.setHeader("header1");
        column1.setHideColumn(false);
        column1.setDefaultValue(new DTCellValue52("default1"));
        column2.setWorkItemName("WID2");
        column2.setWorkItemResultParameterName("param2");
        column2.setParameterClassName("Type2");
        column2.setFactType("FactType2");
        column2.setBoundName("$var2");
        column2.setFactField("field2");
        column2.setType("Type2");
        column2.setValueList("b,c");
        column2.setInsertLogical(true);
        column2.setHeader("header2");
        column2.setHideColumn(true);
        column2.setDefaultValue(new DTCellValue52("default2"));
        List<BaseColumnFieldDiff> diff = column1.diff(column2);
        Assert.assertNotNull(diff);
        Assert.assertEquals(12, diff.size());
        Assert.assertEquals(DTColumnConfig52.FIELD_HIDE_COLUMN, diff.get(0).getFieldName());
        Assert.assertEquals(false, diff.get(0).getOldValue());
        Assert.assertEquals(true, diff.get(0).getValue());
        Assert.assertEquals(DTColumnConfig52.FIELD_DEFAULT_VALUE, diff.get(1).getFieldName());
        Assert.assertEquals("default1", diff.get(1).getOldValue());
        Assert.assertEquals("default2", diff.get(1).getValue());
        Assert.assertEquals(DTColumnConfig52.FIELD_HEADER, diff.get(2).getFieldName());
        Assert.assertEquals("header1", diff.get(2).getOldValue());
        Assert.assertEquals("header2", diff.get(2).getValue());
        Assert.assertEquals(ActionInsertFactCol52.FIELD_FACT_TYPE, diff.get(3).getFieldName());
        Assert.assertEquals("FactType1", diff.get(3).getOldValue());
        Assert.assertEquals("FactType2", diff.get(3).getValue());
        Assert.assertEquals(ActionInsertFactCol52.FIELD_BOUND_NAME, diff.get(4).getFieldName());
        Assert.assertEquals("$var1", diff.get(4).getOldValue());
        Assert.assertEquals("$var2", diff.get(4).getValue());
        Assert.assertEquals(ActionInsertFactCol52.FIELD_FACT_FIELD, diff.get(5).getFieldName());
        Assert.assertEquals("field1", diff.get(5).getOldValue());
        Assert.assertEquals("field2", diff.get(5).getValue());
        Assert.assertEquals(ActionInsertFactCol52.FIELD_TYPE, diff.get(6).getFieldName());
        Assert.assertEquals("Type1", diff.get(6).getOldValue());
        Assert.assertEquals("Type2", diff.get(6).getValue());
        Assert.assertEquals(ActionInsertFactCol52.FIELD_VALUE_LIST, diff.get(7).getFieldName());
        Assert.assertEquals("a,b", diff.get(7).getOldValue());
        Assert.assertEquals("b,c", diff.get(7).getValue());
        Assert.assertEquals(ActionInsertFactCol52.FIELD_IS_INSERT_LOGICAL, diff.get(8).getFieldName());
        Assert.assertEquals(false, diff.get(8).getOldValue());
        Assert.assertEquals(true, diff.get(8).getValue());
        Assert.assertEquals(ActionWorkItemInsertFactCol52.FIELD_WORK_ITEM_NAME, diff.get(9).getFieldName());
        Assert.assertEquals("WID1", diff.get(9).getOldValue());
        Assert.assertEquals("WID2", diff.get(9).getValue());
        Assert.assertEquals(ActionWorkItemInsertFactCol52.FIELD_WORK_ITEM_RESULT_PARAM_NAME, diff.get(10).getFieldName());
        Assert.assertEquals("param1", diff.get(10).getOldValue());
        Assert.assertEquals("param2", diff.get(10).getValue());
        Assert.assertEquals(ActionWorkItemInsertFactCol52.FIELD_PARAMETER_CLASSNAME, diff.get(11).getFieldName());
        Assert.assertEquals("Type1", diff.get(11).getOldValue());
        Assert.assertEquals("Type2", diff.get(11).getValue());
    }
}
