/**
 * The MIT License
 * Copyright ? 2010 JmxTrans team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.googlecode.jmxtrans.model.output;


import com.fasterxml.jackson.core.JsonFactory;
import com.google.common.collect.ImmutableList;
import com.googlecode.jmxtrans.model.QueryFixtures;
import com.googlecode.jmxtrans.model.ResultFixtures;
import com.googlecode.jmxtrans.model.ServerFixtures;
import java.io.IOException;
import java.io.StringWriter;
import org.junit.Test;


public class SensuWriter2Test {
    @Test
    public void metricsAreFormattedCorrectly() throws IOException {
        StringWriter writer = new StringWriter();
        SensuWriter2 sensuWriter = new SensuWriter2(new GraphiteWriter2(ImmutableList.<String>of(), null), new JsonFactory());
        sensuWriter.write(writer, ServerFixtures.dummyServer(), QueryFixtures.dummyQuery(), ResultFixtures.dummyResults());
        String lineSep = System.lineSeparator();
        assertThat(writer.toString()).isEqualTo((((((((((("{" + lineSep) + "  \"name\" : \"jmxtrans\",") + lineSep) + "  \"type\" : \"metric\",") + lineSep) + "  \"handler\" : \"graphite\",") + lineSep) + "  \"output\" : \"host_example_net_4321.MemoryAlias.ObjectPendingFinalizationCount 10 0\\n\"") + lineSep) + "}"));
    }
}
