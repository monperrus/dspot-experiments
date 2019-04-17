/**
 * Copyright 2002-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.jmx.export.naming;


import IdentityNamingStrategy.HASH_CODE_KEY;
import IdentityNamingStrategy.TYPE_KEY;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.jmx.JmxTestBean;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;


/**
 *
 *
 * @author Rob Harrop
 */
public class IdentityNamingStrategyTests {
    @Test
    public void naming() throws MalformedObjectNameException {
        JmxTestBean bean = new JmxTestBean();
        IdentityNamingStrategy strategy = new IdentityNamingStrategy();
        ObjectName objectName = strategy.getObjectName(bean, "null");
        Assert.assertEquals("Domain is incorrect", bean.getClass().getPackage().getName(), objectName.getDomain());
        Assert.assertEquals("Type property is incorrect", ClassUtils.getShortName(bean.getClass()), objectName.getKeyProperty(TYPE_KEY));
        Assert.assertEquals("HashCode property is incorrect", ObjectUtils.getIdentityHexString(bean), objectName.getKeyProperty(HASH_CODE_KEY));
    }
}
