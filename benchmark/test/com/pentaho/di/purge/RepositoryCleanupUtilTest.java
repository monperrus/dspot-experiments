/**
 * !
 * Copyright 2010 - 2018 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pentaho.di.purge;


import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import java.util.Base64;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


@RunWith(PowerMockRunner.class)
@PrepareForTest(Client.class)
public class RepositoryCleanupUtilTest {
    @Test
    public void authenticateLoginCredentials() throws Exception {
        RepositoryCleanupUtil util = Mockito.mock(RepositoryCleanupUtil.class);
        Mockito.doCallRealMethod().when(util).authenticateLoginCredentials();
        setInternalState(util, "url", "http://localhost:8080/pentaho");
        setInternalState(util, "username", "admin");
        setInternalState(util, "password", "Encrypted 2be98afc86aa7f2e4bb18bd63c99dbdde");
        WebResource resource = Mockito.mock(WebResource.class);
        Mockito.doReturn("true").when(resource).get(String.class);
        Client client = Mockito.mock(Client.class);
        Mockito.doCallRealMethod().when(client).addFilter(ArgumentMatchers.any(HTTPBasicAuthFilter.class));
        Mockito.doCallRealMethod().when(client).getHeadHandler();
        Mockito.doReturn(resource).when(client).resource(ArgumentMatchers.anyString());
        mockStatic(Client.class);
        Mockito.when(Client.create(ArgumentMatchers.any(ClientConfig.class))).thenReturn(client);
        util.authenticateLoginCredentials();
        // the expected value is: "Basic <base64 encoded username:password>"
        Assert.assertEquals(("Basic " + (new String(Base64.getEncoder().encode("admin:password".getBytes("utf-8"))))), getInternalState(client.getHeadHandler(), "authentication"));
    }
}
