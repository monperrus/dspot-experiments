/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.dataformat.asn1;


import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;


public class ASN1DataFormatWithStreamTest extends CamelTestSupport {
    private ASN1DataFormat asn1;

    private String fileName = "src/test/resources/asn1_data/SMS_SINGLE.tt";

    @Test
    public void testUnmarshalReturnOutputStream() throws Exception {
        baseASN1DataFormatWithStreamTest("mock:unmarshal", "direct:unmarshal");
    }

    @Test
    public void testUnmarshalReturnOutputStreamDsl() throws Exception {
        baseASN1DataFormatWithStreamTest("mock:unmarshaldsl", "direct:unmarshaldsl");
    }

    @Test
    public void testUnmarshalMarshalReturnOutputStream() throws Exception {
        baseASN1DataFormatWithStreamTest("mock:marshal", "direct:unmarshalthenmarshal");
    }

    @Test
    public void testUnmarshalMarshalReturnOutputStreamDsl() throws Exception {
        baseASN1DataFormatWithStreamTest("mock:marshaldsl", "direct:unmarshalthenmarshaldsl");
    }
}
