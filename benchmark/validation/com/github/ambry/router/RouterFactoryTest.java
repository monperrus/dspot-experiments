/**
 * Copyright 2016 LinkedIn Corp. All rights reserved.
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
 */
package com.github.ambry.router;


import com.github.ambry.account.InMemAccountService;
import com.github.ambry.clustermap.MockClusterMap;
import com.github.ambry.commons.LoggingNotificationSystem;
import com.github.ambry.config.VerifiableProperties;
import com.github.ambry.utils.Utils;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Assert;
import org.junit.Test;


public class RouterFactoryTest {
    /**
     * (routerFactoryString, routerString) pair class.
     */
    class FactoryAndRouter {
        String factoryStr;

        String routerStr;

        FactoryAndRouter(String factoryStr, String routerStr) {
            this.factoryStr = factoryStr;
            this.routerStr = routerStr;
        }
    }

    /**
     * Tests the instantiation of a {@link Router} implementation through its {@link RouterFactory} implementation
     *
     * @throws IOException
     * 		
     */
    @Test
    public void testRouterFactory() throws Exception {
        VerifiableProperties verifiableProperties = getVerifiableProperties();
        InMemAccountService accountService = new InMemAccountService(false, true);
        List<RouterFactoryTest.FactoryAndRouter> factoryAndRouters = new ArrayList<RouterFactoryTest.FactoryAndRouter>();
        factoryAndRouters.add(new RouterFactoryTest.FactoryAndRouter("com.github.ambry.router.NonBlockingRouterFactory", "com.github.ambry.router.NonBlockingRouter"));
        for (RouterFactoryTest.FactoryAndRouter factoryAndRouter : factoryAndRouters) {
            RouterFactory routerFactory = Utils.getObj(factoryAndRouter.factoryStr, verifiableProperties, new MockClusterMap(), new LoggingNotificationSystem(), null, accountService);
            Router router = routerFactory.getRouter();
            Assert.assertEquals("Did not receive expected Router instance", factoryAndRouter.routerStr, router.getClass().getCanonicalName());
            router.close();
        }
    }
}
