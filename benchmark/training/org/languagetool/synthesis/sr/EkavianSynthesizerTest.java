/**
 * LanguageTool, a natural language style checker
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.synthesis.sr;


import java.io.IOException;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;


public class EkavianSynthesizerTest {
    @Test
    public final void testSynthesizeString() throws IOException {
        EkavianSynthesizer synth = new EkavianSynthesizer();
        // Negative test - POS tag that does not exist
        Assert.assertEquals(synth.synthesize(dummyToken("???????????"), "???????????").length, 0);
        // Check all cases
        Assert.assertEquals("[??????]", Arrays.toString(synth.synthesize(dummyToken("??????"), "IM:ZA:ZE:0J:NO")));
        Assert.assertEquals("[??????]", Arrays.toString(synth.synthesize(dummyToken("??????"), "IM:ZA:ZE:0J:GE")));
        Assert.assertEquals("[?????]", Arrays.toString(synth.synthesize(dummyToken("?????"), "IM:ZA:ZE:0J:DA")));
        Assert.assertEquals("[??????]", Arrays.toString(synth.synthesize(dummyToken("??????"), "IM:ZA:ZE:0J:AK")));
        Assert.assertEquals("[?????]", Arrays.toString(synth.synthesize(dummyToken("?????"), "IM:ZA:ZE:0J:VO", true)));
        Assert.assertEquals("[???????]", Arrays.toString(synth.synthesize(dummyToken("??????"), "IM:ZA:ZE:0J:IN")));
        Assert.assertEquals("[??????]", Arrays.toString(synth.synthesize(dummyToken("??????"), "IM:ZA:ZE:0J:LO")));
        // regular expressions
        Assert.assertEquals("[??????, ??????, ??????, ??????, ???????, ??????, ??????, ??????]", Arrays.toString(getSortedArray(synth.synthesize(dummyToken("??????"), "IM:ZA:ZE:0J:.*", true))));
        Assert.assertEquals("[?????, ?????, ?????, ?????, ?????, ?????, ?????, ?????, ?????, ?????, ?????, ?????, ?????, ?????, ?????, ?????, ??????, ??????, ??????, ??????, ??????, ??????, ??????, ??????, ??????, ??????, ??????, ???????, ???????, ???????, ???????, ???????, ???????, ???????, ???????, ???????, ??????, ??????, ??????, ?????, ?????, ?????, ??????, ??????, ??????, ???????, ???????, ???????, ??????, ??????, ??????, ??????, ??????, ???????, ???????, ???????, ???????, ???????, ???????, ???????, ???????, ??????, ??????, ?????]", Arrays.toString(getSortedArray(synth.synthesize(dummyToken("?????"), "BR:.*:.*:.*:.*", true))));
        Assert.assertEquals("[?????, ?????, ?????, ?????, ?????, ????]", Arrays.toString(getSortedArray(synth.synthesize(dummyToken("?????"), "GL:.*:.*:.*:.*", true))));
        Assert.assertEquals("[???]", Arrays.toString(getSortedArray(synth.synthesize(dummyToken("???"), "VE:.*", true))));
    }
}
