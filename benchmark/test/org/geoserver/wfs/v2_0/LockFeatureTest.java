/**
 * (c) 2014 - 2016 Open Source Geospatial Foundation - all rights reserved
 * (c) 2001 - 2013 OpenPlans
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wfs.v2_0;


import FES.NAMESPACE;
import StoredQuery.DEFAULT;
import java.io.ByteArrayInputStream;
import org.custommonkey.xmlunit.XMLUnit;
import org.geoserver.config.GeoServer;
import org.geoserver.wfs.WFSInfo;
import org.geotools.wfs.v2_0.WFS;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.w3c.dom.Document;


public class LockFeatureTest extends WFS20TestSupport {
    @Test
    public void testLock() throws Exception {
        String xml = ((((((("<wfs:LockFeature xmlns:sf=\"http://cite.opengeospatial.org/gmlsf\" " + "   xmlns:wfs='") + (WFS.NAMESPACE)) + "\' expiry=\"5\" handle=\"LockFeature-tc1\" ") + " lockAction=\"ALL\" ") + " service=\"WFS\" ") + " version=\"2.0.0\">") + "<wfs:Query handle=\"lock-1\" typeNames=\"sf:PrimitiveGeoFeature\"/>") + "</wfs:LockFeature>";
        Document dom = postAsDOM("wfs", xml);
        Assert.assertEquals("wfs:LockFeatureResponse", dom.getDocumentElement().getNodeName());
        Assert.assertEquals(5, dom.getElementsByTagNameNS(NAMESPACE, "ResourceId").getLength());
        // release the lock
        // print(dom);
        String lockId = dom.getDocumentElement().getAttribute("lockId");
        get(("wfs?request=ReleaseLock&version=2.0&lockId=" + lockId));
    }

    @Test
    public void testSOAP() throws Exception {
        String xml = ((((((((("<soap:Envelope xmlns:soap='http://www.w3.org/2003/05/soap-envelope'> " + (((" <soap:Header/> " + " <soap:Body>") + "<wfs:LockFeature xmlns:sf=\"http://cite.opengeospatial.org/gmlsf\" ") + "   xmlns:wfs='")) + (WFS.NAMESPACE)) + "\' expiry=\"5\" handle=\"LockFeature-tc1\" ") + " lockAction=\"ALL\" ") + " service=\"WFS\" ") + " version=\"2.0.0\">") + "<wfs:Query handle=\"lock-1\" typeNames=\"sf:PrimitiveGeoFeature\"/>") + "</wfs:LockFeature>") + " </soap:Body> ") + "</soap:Envelope> ";
        MockHttpServletResponse resp = postAsServletResponse("wfs", xml, "application/soap+xml");
        Assert.assertEquals("application/soap+xml", resp.getContentType());
        Document dom = dom(new ByteArrayInputStream(resp.getContentAsString().getBytes()));
        Assert.assertEquals("soap:Envelope", dom.getDocumentElement().getNodeName());
        Assert.assertEquals(1, dom.getElementsByTagName("wfs:LockFeatureResponse").getLength());
        // release the lock
        String lockId = XMLUnit.newXpathEngine().evaluate("//wfs:LockFeatureResponse/@lockId", dom);
        get(("wfs?request=ReleaseLock&version=2.0&lockId=" + lockId));
    }

    @Test
    public void testRenewLockFail() throws Exception {
        GeoServer gs = getGeoServer();
        WFSInfo wfs = gs.getService(WFSInfo.class);
        wfs.setCiteCompliant(true);
        gs.save(wfs);
        try {
            String xml = ((((((("<wfs:LockFeature xmlns:sf=\"http://cite.opengeospatial.org/gmlsf\" " + "   xmlns:wfs='") + (WFS.NAMESPACE)) + "\' expiry=\"1\" handle=\"LockFeature-tc1\" ") + " lockAction=\"ALL\" ") + " service=\"WFS\" ") + " version=\"2.0.0\">") + "<wfs:Query handle=\"lock-1\" typeNames=\"sf:PrimitiveGeoFeature\"/>") + "</wfs:LockFeature>";
            Document dom = postAsDOM("wfs", xml);
            print(dom);
            Assert.assertEquals("wfs:LockFeatureResponse", dom.getDocumentElement().getNodeName());
            Assert.assertEquals(5, dom.getElementsByTagNameNS(NAMESPACE, "ResourceId").getLength());
            String lockId = XMLUnit.newXpathEngine().evaluate("//wfs:LockFeatureResponse/@lockId", dom);
            // wait a couple of seconds, the lock was acquired only for one
            Thread.sleep((2 * 1000));
            // try to reset the expired lock, it should fail
            xml = (((((((("<wfs:LockFeature xmlns:sf=\"http://cite.opengeospatial.org/gmlsf\" " + "   xmlns:wfs='") + (WFS.NAMESPACE)) + "\' expiry=\"1\" handle=\"LockFeature-tc1\" ") + " lockAction=\"ALL\" ") + " service=\"WFS\" ") + " version=\"2.0.0\"") + " lockId=\"") + lockId) + "\"/>";
            MockHttpServletResponse response = postAsServletResponse("wfs", xml);
            Assert.assertEquals(403, response.getStatus());
            dom = dom(new ByteArrayInputStream(response.getContentAsByteArray()));
            checkOws11Exception(dom, "2.0.0", WFSException.LOCK_HAS_EXPIRED, "lockId");
        } finally {
            wfs.setCiteCompliant(false);
            gs.save(wfs);
        }
    }

    @Test
    public void testRenewUnknownLock() throws Exception {
        // try to reset an unknown lock instead
        String xml = (((((("<wfs:LockFeature xmlns:sf=\"http://cite.opengeospatial.org/gmlsf\" " + "   xmlns:wfs='") + (WFS.NAMESPACE)) + "\' expiry=\"1\" handle=\"LockFeature-tc1\" ") + " lockAction=\"ALL\" ") + " service=\"WFS\" ") + " version=\"2.0.0\"") + " lockId=\"abcd\"/>";
        MockHttpServletResponse response = postAsServletResponse("wfs", xml);
        Assert.assertEquals(400, response.getStatus());
        Document dom = dom(new ByteArrayInputStream(response.getContentAsByteArray()));
        checkOws11Exception(dom, "2.0.0", WFSException.INVALID_LOCK_ID, "lockId");
    }

    @Test
    public void testLockWithStoredQuery() throws Exception {
        lockWithStoredQuery("wfs");
    }

    @Test
    public void testLockWithStoredQueryWorkspaceSpecific() throws Exception {
        lockWithStoredQuery("sf/wfs");
    }

    @Test
    public void testLockGet() throws Exception {
        Document dom = getAsDOM("wfs?service=WFS&version=2.0.0&request=LockFeature&typenames=sf:GenericEntity", 200);
        // print(dom);
        Assert.assertEquals("wfs:LockFeatureResponse", dom.getDocumentElement().getNodeName());
        Assert.assertEquals(3, dom.getElementsByTagNameNS(NAMESPACE, "ResourceId").getLength());
        // release the lock
        String lockId = dom.getDocumentElement().getAttribute("lockId");
        get(("wfs?request=ReleaseLock&version=2.0&lockId=" + lockId));
    }

    @Test
    public void testLockWithNamespacesGet() throws Exception {
        Document dom = getAsDOM(("wfs?service=WFS&version=2.0.0&request=LockFeature&typenames=ns53:GenericEntity" + "&namespaces=xmlns(ns53,http://cite.opengeospatial.org/gmlsf)"), 200);
        // print(dom);
        Assert.assertEquals("wfs:LockFeatureResponse", dom.getDocumentElement().getNodeName());
        Assert.assertEquals(3, dom.getElementsByTagNameNS(NAMESPACE, "ResourceId").getLength());
        // release the lock
        String lockId = dom.getDocumentElement().getAttribute("lockId");
        get(("wfs?request=ReleaseLock&version=2.0&lockId=" + lockId));
    }

    @Test
    public void testLockWithStoredQueryGet() throws Exception {
        Document dom = getAsDOM((("wfs?service=WFS&version=2.0.0&request=LockFeature&storedQueryId=" + (DEFAULT.getName())) + "&ID=PrimitiveGeoFeature.f001"), 200);
        // print(dom);
        Assert.assertEquals("wfs:LockFeatureResponse", dom.getDocumentElement().getNodeName());
        Assert.assertEquals(1, dom.getElementsByTagNameNS(NAMESPACE, "ResourceId").getLength());
        // release the lock
        String lockId = dom.getDocumentElement().getAttribute("lockId");
        get(("wfs?request=ReleaseLock&version=2.0&lockId=" + lockId));
    }

    @Test
    public void testLockByBBOX() throws Exception {
        Document dom = getAsDOM(("wfs?service=WFS&version=2.0.0&request=LockFeature&typeName=sf:PrimitiveGeoFeature" + "&BBOX=57.0,-4.5,62.0,1.0,EPSG:4326"), 200);
        // print(dom);
        Assert.assertEquals("wfs:LockFeatureResponse", dom.getDocumentElement().getNodeName());
        Assert.assertEquals(1, dom.getElementsByTagNameNS(NAMESPACE, "ResourceId").getLength());
        // release the lock
        String lockId = dom.getDocumentElement().getAttribute("lockId");
        get(("wfs?request=ReleaseLock&version=2.0&lockId=" + lockId));
    }

    @Test
    public void testFailLockAll() throws Exception {
        // lock one
        Document dom = getAsDOM((("wfs?service=WFS&version=2.0.0&request=LockFeature&storedQueryId=" + (DEFAULT.getName())) + "&ID=PrimitiveGeoFeature.f001"), 200);
        // print(dom);
        Assert.assertEquals("wfs:LockFeatureResponse", dom.getDocumentElement().getNodeName());
        Assert.assertEquals(1, dom.getElementsByTagNameNS(NAMESPACE, "ResourceId").getLength());
        String lockId = dom.getDocumentElement().getAttribute("lockId");
        // try to lock all now
        dom = getAsDOM("wfs?service=WFS&version=2.0.0&request=LockFeature&typeNames=sf:PrimitiveGeoFeature", 400);
        checkOws11Exception(dom, "2.0.0", WFSException.CANNOT_LOCK_ALL_FEATURES, "GeoServer");
        get(("wfs?request=ReleaseLock&version=2.0&lockId=" + lockId));
    }
}
