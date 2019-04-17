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
package org.apache.camel.builder;


import java.io.StringReader;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;


/**
 *
 */
public class XMLTokenExpressionIteratorInvalidXMLTest extends Assert {
    private static final String DATA_TEMPLATE = "<?xml version=\"1.0\" encoding=\"utf-u\"?>" + ((("<Statements xmlns=\"http://www.apache.org/xml/test\">" + "    <statement>Hello World</statement>") + "    <statement>{0}</statement>") + "</Statements>");

    private static final Map<String, String> NSMAP = Collections.singletonMap("", "http://www.apache.org/xml/test");

    @Test
    public void testExtractToken() throws Exception {
        String data = MessageFormat.format(XMLTokenExpressionIteratorInvalidXMLTest.DATA_TEMPLATE, "Have a nice day");
        XMLTokenExpressionIterator xtei = new XMLTokenExpressionIterator("//statement", 'i');
        xtei.setNamespaces(XMLTokenExpressionIteratorInvalidXMLTest.NSMAP);
        invokeAndVerify(xtei.createIterator(new StringReader(data)), false);
        data = MessageFormat.format(XMLTokenExpressionIteratorInvalidXMLTest.DATA_TEMPLATE, "Have a nice< day");
        xtei = new XMLTokenExpressionIterator("//statement", 'i');
        xtei.setNamespaces(XMLTokenExpressionIteratorInvalidXMLTest.NSMAP);
        invokeAndVerify(xtei.createIterator(new StringReader(data)), true);
    }
}
