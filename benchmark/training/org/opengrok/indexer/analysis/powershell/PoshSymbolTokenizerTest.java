/**
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License (the "License").
 * You may not use this file except in compliance with the License.
 *
 * See LICENSE.txt included in this distribution for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at LICENSE.txt.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 */
/**
 * Copyright (c) 2010, 2018, Oracle and/or its affiliates. All rights reserved.
 * Portions Copyright (c) 2017, Chris Fraire <cfraire@me.com>.
 */
package org.opengrok.indexer.analysis.powershell;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opengrok.indexer.util.CustomAssertions;


/**
 * Tests the {@link PoshSymbolTokenizer} class.
 */
public class PoshSymbolTokenizerTest {
    /**
     * Test sample.psm1 v. samplesymbols.txt
     *
     * @throws java.lang.Exception
     * 		thrown on error
     */
    @Test
    public void testPowerShellSymbolStream() throws Exception {
        InputStream psres = getClass().getClassLoader().getResourceAsStream("analysis/powershell/sample.psm1");
        Assert.assertNotNull("despite sample.psm1 as resource,", psres);
        InputStream symres = getClass().getClassLoader().getResourceAsStream("analysis/powershell/samplesymbols.txt");
        Assert.assertNotNull("despite samplesymbols.txt as resource,", symres);
        List<String> expectedSymbols = new ArrayList<>();
        try (BufferedReader wdsr = new BufferedReader(new InputStreamReader(symres, "UTF-8"))) {
            String line;
            while ((line = wdsr.readLine()) != null) {
                int hasho = line.indexOf('#');
                if (hasho != (-1))
                    line = line.substring(0, hasho);

                expectedSymbols.add(line.trim());
            } 
        }
        CustomAssertions.assertSymbolStream(PoshSymbolTokenizer.class, psres, expectedSymbols);
    }
}
