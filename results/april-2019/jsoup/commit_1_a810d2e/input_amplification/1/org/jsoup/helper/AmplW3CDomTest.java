package org.jsoup.helper;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.integration.ParseTest;
import org.jsoup.nodes.Document;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Node;


public class AmplW3CDomTest {
    @Test(timeout = 10000)
    public void convertsGoogle_literalMutationString328_failAssert0() throws IOException {
        try {
            File in = ParseTest.getFile("");
            Document doc = Jsoup.parse(in, "UTF8");
            W3CDom w3c = new W3CDom();
            org.w3c.dom.Document wDoc = w3c.fromJsoup(doc);
            Node htmlEl = wDoc.getChildNodes().item(0);
            htmlEl.getNamespaceURI();
            htmlEl.getLocalName();
            htmlEl.getNodeName();
            String out = w3c.asString(wDoc);
            out.contains("ipod");
            org.junit.Assert.fail("convertsGoogle_literalMutationString328 should have thrown FileNotFoundException");
        } catch (FileNotFoundException expected) {
            Assert.assertEquals("/tmp/dspot-experiments/dataset/april-2019/jsoup_parent/target/test-classes/org/jsoup/integration (Is a directory)", expected.getMessage());
        }
    }

    @Test(timeout = 10000)
    public void convertsGoogleLocation_literalMutationString1_failAssert0() throws IOException {
        try {
            File in = ParseTest.getFile("");
            Document doc = Jsoup.parse(in, "UTF8");
            W3CDom w3c = new W3CDom();
            org.w3c.dom.Document wDoc = w3c.fromJsoup(doc);
            String out = w3c.asString(wDoc);
            doc.location();
            wDoc.getDocumentURI();
            org.junit.Assert.fail("convertsGoogleLocation_literalMutationString1 should have thrown FileNotFoundException");
        } catch (FileNotFoundException expected) {
            Assert.assertEquals("/tmp/dspot-experiments/dataset/april-2019/jsoup_parent/target/test-classes/org/jsoup/integration (Is a directory)", expected.getMessage());
        }
    }

    @Test(timeout = 10000)
    public void namespacePreservation_literalMutationString461_failAssert0() throws IOException {
        try {
            File in = ParseTest.getFile("");
            Document jsoupDoc;
            jsoupDoc = Jsoup.parse(in, "UTF-8");
            org.w3c.dom.Document doc;
            W3CDom jDom = new W3CDom();
            doc = jDom.fromJsoup(jsoupDoc);
            Node htmlEl = doc.getChildNodes().item(0);
            htmlEl.getNamespaceURI();
            htmlEl.getLocalName();
            htmlEl.getNodeName();
            Node head = htmlEl.getFirstChild();
            head.getNamespaceURI();
            head.getLocalName();
            head.getNodeName();
            Node epubTitle = htmlEl.getChildNodes().item(2).getChildNodes().item(3);
            epubTitle.getTextContent();
            epubTitle.getNamespaceURI();
            epubTitle.getLocalName();
            epubTitle.getNodeName();
            Node xSection = epubTitle.getNextSibling().getNextSibling();
            xSection.getNamespaceURI();
            xSection.getLocalName();
            xSection.getNodeName();
            Node svg = xSection.getNextSibling().getNextSibling();
            svg.getNamespaceURI();
            svg.getLocalName();
            svg.getNodeName();
            Node path = svg.getChildNodes().item(1);
            path.getNamespaceURI();
            path.getLocalName();
            path.getNodeName();
            Node clip = path.getChildNodes().item(1);
            clip.getNamespaceURI();
            clip.getLocalName();
            clip.getNodeName();
            clip.getTextContent();
            Node picture = svg.getNextSibling().getNextSibling();
            picture.getNamespaceURI();
            picture.getLocalName();
            picture.getNodeName();
            Node img = picture.getFirstChild();
            img.getNamespaceURI();
            img.getLocalName();
            img.getNodeName();
            org.junit.Assert.fail("namespacePreservation_literalMutationString461 should have thrown FileNotFoundException");
        } catch (FileNotFoundException expected) {
            Assert.assertEquals("/tmp/dspot-experiments/dataset/april-2019/jsoup_parent/target/test-classes/org/jsoup/integration (Is a directory)", expected.getMessage());
        }
    }
}
