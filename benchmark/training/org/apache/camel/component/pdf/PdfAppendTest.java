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
package org.apache.camel.component.pdf;


import PDType1Font.HELVETICA;
import PdfHeaderConstants.DECRYPTION_MATERIAL_HEADER_NAME;
import PdfHeaderConstants.PDF_DOCUMENT_HEADER_NAME;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardDecryptionMaterial;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.text.PDFTextStripper;
import org.hamcrest.Matchers;
import org.junit.Test;


public class PdfAppendTest extends CamelTestSupport {
    @EndpointInject(uri = "mock:result")
    protected MockEndpoint resultEndpoint;

    @Test
    public void testAppend() throws Exception {
        final String originalText = "Test";
        final String textToAppend = "Append";
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        contentStream.setFont(HELVETICA, 12);
        contentStream.beginText();
        contentStream.moveTextPositionByAmount(20, 400);
        contentStream.drawString(originalText);
        contentStream.endText();
        contentStream.close();
        template.sendBodyAndHeader("direct:start", textToAppend, PDF_DOCUMENT_HEADER_NAME, document);
        resultEndpoint.setExpectedMessageCount(1);
        resultEndpoint.expectedMessagesMatches(new Predicate() {
            @Override
            public boolean matches(Exchange exchange) {
                Object body = exchange.getIn().getBody();
                assertThat(body, Matchers.instanceOf(ByteArrayOutputStream.class));
                try {
                    PDDocument doc = PDDocument.load(new ByteArrayInputStream(((ByteArrayOutputStream) (body)).toByteArray()));
                    PDFTextStripper pdfTextStripper = new PDFTextStripper();
                    String text = pdfTextStripper.getText(doc);
                    assertEquals(2, doc.getNumberOfPages());
                    assertThat(text, Matchers.containsString(originalText));
                    assertThat(text, Matchers.containsString(textToAppend));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return true;
            }
        });
        resultEndpoint.assertIsSatisfied();
    }

    @Test
    public void testAppendEncrypted() throws Exception {
        final String originalText = "Test";
        final String textToAppend = "Append";
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        contentStream.setFont(HELVETICA, 12);
        contentStream.beginText();
        contentStream.moveTextPositionByAmount(20, 400);
        contentStream.drawString(originalText);
        contentStream.endText();
        contentStream.close();
        final String ownerPass = "ownerPass";
        final String userPass = "userPass";
        AccessPermission accessPermission = new AccessPermission();
        accessPermission.setCanExtractContent(false);
        StandardProtectionPolicy protectionPolicy = new StandardProtectionPolicy(ownerPass, userPass, accessPermission);
        protectionPolicy.setEncryptionKeyLength(128);
        document.protect(protectionPolicy);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        document.save(output);
        // Encryption happens after saving.
        PDDocument encryptedDocument = PDDocument.load(new ByteArrayInputStream(output.toByteArray()), userPass);
        Map<String, Object> headers = new HashMap<>();
        headers.put(PDF_DOCUMENT_HEADER_NAME, encryptedDocument);
        headers.put(DECRYPTION_MATERIAL_HEADER_NAME, new StandardDecryptionMaterial(userPass));
        template.sendBodyAndHeaders("direct:start", textToAppend, headers);
        resultEndpoint.setExpectedMessageCount(1);
        resultEndpoint.expectedMessagesMatches(new Predicate() {
            @Override
            public boolean matches(Exchange exchange) {
                Object body = exchange.getIn().getBody();
                assertThat(body, Matchers.instanceOf(ByteArrayOutputStream.class));
                try {
                    PDDocument doc = PDDocument.load(new ByteArrayInputStream(((ByteArrayOutputStream) (body)).toByteArray()), userPass);
                    PDFTextStripper pdfTextStripper = new PDFTextStripper();
                    String text = pdfTextStripper.getText(doc);
                    assertEquals(2, doc.getNumberOfPages());
                    assertThat(text, Matchers.containsString(originalText));
                    assertThat(text, Matchers.containsString(textToAppend));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return true;
            }
        });
        resultEndpoint.assertIsSatisfied();
    }
}
