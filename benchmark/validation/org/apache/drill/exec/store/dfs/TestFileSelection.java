/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.drill.exec.store.dfs;


import java.util.List;
import junit.framework.TestCase;
import org.apache.drill.exec.util.DrillFileSystemUtil;
import org.apache.drill.shaded.guava.com.google.common.collect.ImmutableList;
import org.apache.drill.test.BaseTestQuery;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.junit.Assert;
import org.junit.Test;


public class TestFileSelection extends BaseTestQuery {
    private static final List<FileStatus> EMPTY_STATUSES = ImmutableList.of();

    private static final List<Path> EMPTY_FILES = ImmutableList.of();

    private static final String EMPTY_ROOT = "";

    @Test
    public void testCreateReturnsNullWhenArgumentsAreIllegal() {
        for (final Object statuses : new Object[]{ null, TestFileSelection.EMPTY_STATUSES }) {
            for (final Object files : new Object[]{ null, TestFileSelection.EMPTY_FILES }) {
                for (final Object root : new Object[]{ null, TestFileSelection.EMPTY_ROOT }) {
                    FileSelection selection = FileSelection.create(((List<FileStatus>) (statuses)), ((List<Path>) (files)), DrillFileSystemUtil.createPathSafe(((String) (root))));
                    Assert.assertNull(selection);
                }
            }
        }
    }

    @Test
    public void testBackPathBad() throws Exception {
        final String[][] badPaths = new String[][]{ new String[]{ "/tmp", "../../bad" }// goes beyond root and outside parent; resolves to /../bad
        // goes beyond root and outside parent; resolves to /../bad
        // goes beyond root and outside parent; resolves to /../bad
        , new String[]{ "/tmp", "../etc/bad" }// goes outside parent; resolves to /etc/bad
        // goes outside parent; resolves to /etc/bad
        // goes outside parent; resolves to /etc/bad
        , new String[]{ "", "/bad" }// empty parent
        // empty parent
        // empty parent
        , new String[]{ "/", "" }// empty path
        // empty path
        // empty path
         };
        for (int i = 0; i < (badPaths.length); i++) {
            boolean isPathGood = true;
            try {
                String parent = badPaths[i][0];
                String subPath = FileSelection.removeLeadingSlash(badPaths[i][1]);
                String path = new Path(parent, subPath).toString();
                FileSelection.checkBackPaths(parent, path, subPath);
            } catch (IllegalArgumentException e) {
                isPathGood = false;
            }
            if (isPathGood) {
                TestCase.fail("Failed to catch invalid file selection paths.");
            }
        }
    }

    @Test
    public void testBackPathGood() throws Exception {
        final String[][] goodPaths = new String[][]{ new String[]{ "/tmp", "../tmp/good" }, new String[]{ "/", "/tmp/good/../../good" }, new String[]{ "/", "etc/tmp/../../good" }// no leading slash in path
        // no leading slash in path
        // no leading slash in path
        , new String[]{ "/", "../good" }, // resolves to /../good which is OK
        new String[]{ "/", "/good" } };
        for (int i = 0; i < (goodPaths.length); i++) {
            try {
                String parent = goodPaths[i][0];
                String subPath = FileSelection.removeLeadingSlash(goodPaths[i][1]);
                String path = new Path(parent, subPath).toString();
                FileSelection.checkBackPaths(parent, path, subPath);
            } catch (IllegalArgumentException e) {
                TestCase.fail("Valid path not allowed by selection path validation.");
            }
        }
    }
}
