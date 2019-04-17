/**
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.pmml.pmml_4_2;


import KieServices.Factory;
import Message.Level.ERROR;
import ResourceType.DRL;
import ResourceType.PMML;
import Variable.v;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Results;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.internal.io.ResourceFactory;


@Ignore
public class PMMLUsageDemoTest extends DroolsAbstractPMMLTest {
    private static final String pmmlSource = "org/kie/pmml/pmml_4_2/mock_cold_simple.xml";

    @Test
    public void invokePmmlWithRawData() {
        KieSession kSession = getModelSession(PMMLUsageDemoTest.pmmlSource, false);
        // One entry-point per input field
        // field name "xyz" => entry point name "in_Xyz"
        kSession.getEntryPoint("in_Temp").insert(22.0);
        kSession.fireAllRules();
        // Query results
        // output field name   --> query name
        // model name          --> first arg
        // value               --> second arg ( Variable.v for output, any value for testing )
        QueryResults qrs = kSession.getQueryResults("Cold", "MockCold", v);
        Assert.assertTrue(qrs.iterator().hasNext());
        Object val = qrs.iterator().next().get("$result");
        Assert.assertEquals(0.56, val);
        QueryResults qrs2 = kSession.getQueryResults("Cold", "MockCold", 0.56);
        Assert.assertTrue(qrs2.iterator().hasNext());
        QueryResults qrs3 = kSession.getQueryResults("Cold", "MockCold", 0.99);
        Assert.assertFalse(qrs3.iterator().hasNext());
    }

    @Test
    public void invokePmmlWithTrait() {
        String extraDrl = "package org.kie.pmml.pmml_4_2.test;" + (((((((((((((((((((("" + "import org.drools.core.factmodel.traits.Entity;") + "") + "rule \"Init\" ") + "when ") + "   $s : String( this == \"trigger\" ) ") + "then ") + "   System.out.println( \"Trig\" ); ") + "   Entity o = new Entity(); ") + "   insert( o ); \n") + "") + // don an object with the default input trait ( modelName + "Input" )
        // both soft and hard fields will be used to feed data into the model
        "") + "   MockColdTrait input = don( o, MockColdTrait.class ); ") + "   modify( input ) { ") + "       setTemp( 22.0 );") + "   } ") + "end ") + "") + "") + "rule Log when $x : MockColdTrait() then System.out.println( \"IN \" + $x ); end ") + "rule Log2 when $x : Cold() then System.out.println( \"OUT \" + $x ); end ");
        KieServices ks = Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();
        kfs.write(ResourceFactory.newClassPathResource(PMMLUsageDemoTest.pmmlSource).setResourceType(ResourceType.PMML));
        kfs.write(ResourceFactory.newByteArrayResource(extraDrl.getBytes()).setSourcePath("rules.drl").setResourceType(DRL));
        Results res = ks.newKieBuilder(kfs).buildAll().getResults();
        if (res.hasMessages(ERROR)) {
            System.out.println(res.getMessages(ERROR));
        }
        Assert.assertEquals(0, res.getMessages(ERROR).size());
        KieSession kSession = ks.newKieContainer(ks.getRepository().getDefaultReleaseId()).newKieSession();
        kSession.insert("trigger");
        kSession.fireAllRules();
        System.out.println(reportWMObjects(kSession));
        QueryResults qrs = kSession.getQueryResults("Cold", "MockCold", v);
        Assert.assertTrue(qrs.iterator().hasNext());
        Object val = qrs.iterator().next().get("$result");
        Assert.assertEquals(0.56, val);
    }

    @Test
    public void invokePmmlWithTraitMissing() {
        KieServices ks = Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();
        kfs.write(ResourceFactory.newClassPathResource(PMMLUsageDemoTest.pmmlSource).setResourceType(ResourceType.PMML));
        Results res = ks.newKieBuilder(kfs).buildAll().getResults();
        if (res.hasMessages(ERROR)) {
            System.out.println(res.getMessages(ERROR));
        }
        Assert.assertEquals(0, res.getMessages(ERROR).size());
        KieSession kSession = ks.newKieContainer(ks.getRepository().getDefaultReleaseId()).newKieSession();
        kSession.insert("trigger");
        kSession.fireAllRules();
        System.out.println(reportWMObjects(kSession));
    }
}
