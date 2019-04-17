/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.shardingsphere.transaction.xa.manager.atomikos;


import javax.sql.XADataSource;
import org.apache.shardingsphere.transaction.xa.spi.SingleXAResource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class AtomikosXARecoverableResourceTest {
    @Mock
    private SingleXAResource singleXAResource;

    @Mock
    private XADataSource xaDataSource;

    @Test
    public void assertUseXAResource() {
        AtomikosXARecoverableResource atomikosXARecoverableResource = new AtomikosXARecoverableResource("ds1", xaDataSource);
        Assert.assertTrue(atomikosXARecoverableResource.usesXAResource(singleXAResource));
    }
}
