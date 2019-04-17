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
package org.apache.camel.core.osgi;


import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.Endpoint;
import org.apache.camel.component.file.FileComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spi.Registry;
import org.apache.camel.support.DefaultRegistry;
import org.junit.Assert;
import org.junit.Test;


public class OsgiComponentResolverTest extends CamelOsgiTestSupport {
    @Test
    public void testOsgiResolverFindComponentTest() throws Exception {
        CamelContext camelContext = new DefaultCamelContext();
        OsgiComponentResolver resolver = new OsgiComponentResolver(getBundleContext());
        Component component = resolver.resolveComponent("file_test", camelContext);
        Assert.assertNotNull("We should find file_test component", component);
        Assert.assertTrue("We should get the file component here", (component instanceof FileComponent));
    }

    @Test
    public void testOsgiResolverFindComponentFallbackTest() throws Exception {
        Registry registry = new DefaultRegistry();
        registry.bind("allstar-component", new OsgiComponentResolverTest.SampleComponent(true));
        CamelContext camelContext = new DefaultCamelContext(registry);
        OsgiComponentResolver resolver = new OsgiComponentResolver(getBundleContext());
        Component component = resolver.resolveComponent("allstar", camelContext);
        Assert.assertNotNull("We should find the super component", component);
        Assert.assertTrue("We should get the super component here", (component instanceof OsgiComponentResolverTest.SampleComponent));
    }

    @Test
    public void testOsgiResolverFindLanguageDoubleFallbackTest() throws Exception {
        Registry registry = new DefaultRegistry();
        registry.bind("allstar", new OsgiComponentResolverTest.SampleComponent(false));
        registry.bind("allstar-component", new OsgiComponentResolverTest.SampleComponent(true));
        CamelContext camelContext = new DefaultCamelContext(registry);
        OsgiComponentResolver resolver = new OsgiComponentResolver(getBundleContext());
        Component component = resolver.resolveComponent("allstar", camelContext);
        Assert.assertNotNull("We should find the super component", component);
        Assert.assertTrue("We should get the super component here", (component instanceof OsgiComponentResolverTest.SampleComponent));
        Assert.assertFalse("We should NOT find the fallback component", ((OsgiComponentResolverTest.SampleComponent) (component)).isFallback());
    }

    private static class SampleComponent implements Component {
        private boolean fallback;

        SampleComponent(boolean fallback) {
            this.fallback = fallback;
        }

        @Override
        public void setCamelContext(CamelContext camelContext) {
            throw new UnsupportedOperationException("Should not be called");
        }

        @Override
        public CamelContext getCamelContext() {
            throw new UnsupportedOperationException("Should not be called");
        }

        @Override
        public Endpoint createEndpoint(String uri) throws Exception {
            throw new UnsupportedOperationException("Should not be called");
        }

        @Override
        public boolean useRawUri() {
            throw new UnsupportedOperationException("Should not be called");
        }

        public boolean isFallback() {
            return fallback;
        }

        public void setFallback(boolean fallback) {
            this.fallback = fallback;
        }
    }
}
