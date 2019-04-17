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
package org.kie.scanner;


import KieServices.Factory;
import org.drools.core.kie.impl.MessageImpl;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message.Level.ERROR;
import org.kie.api.builder.ReleaseId;
import org.kie.internal.builder.IncrementalResults;


public class KieModuleIncrementalCompilationTest extends AbstractKieCiTest {
    @Test
    public void testCheckMetaDataAfterIncrementalDelete() throws Exception {
        String drl1 = "package org.kie.scanner\n" + ((("rule R1 when\n" + "   String()\n") + "then\n") + "end\n");
        String drl2 = "package org.kie.scanner\n" + ((("rule R2_2 when\n" + "   String( )\n") + "then\n") + "end\n");
        KieServices ks = Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem().write("src/main/resources/r1.drl", drl1).write("src/main/resources/r2.drl", drl2);
        KieBuilder kieBuilder = ks.newKieBuilder(kfs).buildAll();
        Assert.assertEquals(2, getRuleNames(kieBuilder).get("org.kie.scanner").size());
        kfs.delete("src/main/resources/r2.drl");
        IncrementalResults addResults = createFileSet("src/main/resources/r2.drl").build();
        Assert.assertEquals(1, getRuleNames(kieBuilder).get("org.kie.scanner").size());
    }

    @Test
    public void testIncrementalCompilationFirstBuildHasErrors() throws Exception {
        KieServices ks = Factory.get();
        // Malformed POM - No Version information
        ReleaseId releaseId = ks.newReleaseId("org.kie", "incremental-test-with-invalid pom", "");
        KieFileSystem kfs = createKieFileSystemWithKProject(ks);
        kfs.writePomXML(getPom(releaseId));
        // Valid
        String drl1 = "rule R1 when\n" + (("   $s : String()\n" + "then\n") + "end\n");
        // Invalid
        String drl2 = "rule R2 when\n" + (("   $s : Strin( )\n" + "then\n") + "end\n");
        // Write Rule 1 - No DRL errors, but POM is in error
        kfs.write("src/main/resources/KBase1/r1.drl", drl1);
        KieBuilder kieBuilder = ks.newKieBuilder(kfs).buildAll();
        Assert.assertEquals(1, kieBuilder.getResults().getMessages(ERROR).size());
        // Add file with error - expect 1 "added" error message
        kfs.write("src/main/resources/KBase1/r2.drl", drl2);
        IncrementalResults addResults = createFileSet("src/main/resources/KBase1/r2.drl").build();
        Assert.assertEquals(1, addResults.getAddedMessages().size());
        Assert.assertEquals(0, addResults.getRemovedMessages().size());
    }

    @Test
    public void checkIncrementalCompilationWithRuleFunctionRule() throws Exception {
        String rule_1 = "package org.kie.scanner\n" + ((("rule R1 when\n" + "   String()\n") + "then\n") + "end\n");
        String rule_2 = "package org.kie.scanner\n" + (((("rule R1 when\n" + "   String()\n") + "then\n") + "   System.out.println(MyFunction());\n") + "end\n");
        String function = "package org.kie.scanner\n" + (("function int MyFunction() {\n" + "   return 1;\n") + "}\n");
        KieServices ks = Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();
        kfs.write("src/main/resources/org/kie/scanner/rule.drl", rule_1);
        KieBuilder kieBuilder = ks.newKieBuilder(kfs).buildAll();
        Assert.assertEquals(0, kieBuilder.getResults().getMessages(ERROR).size());
        kfs.write("src/main/resources/org/kie/scanner/function.drl", function);
        IncrementalResults addResults1 = createFileSet("src/main/resources/org/kie/scanner/function.drl").build();
        Assert.assertEquals(0, addResults1.getAddedMessages().size());
        Assert.assertEquals(0, addResults1.getRemovedMessages().size());
        kfs.write("src/main/resources/org/kie/scanner/rule.drl", rule_2);
        IncrementalResults addResults2 = createFileSet("src/main/resources/org/kie/scanner/rule.drl").build();
        Assert.assertEquals(0, addResults2.getAddedMessages().size());
        Assert.assertEquals(0, addResults2.getRemovedMessages().size());
    }

    @Test
    public void checkIncrementalCompilationWithRuleThenFunction() throws Exception {
        String rule = "package org.kie.scanner\n" + (((("rule R1 when\n" + "   String()\n") + "then\n") + "   System.out.println(MyFunction());\n") + "end\n");
        String function = "package org.kie.scanner\n" + (("function int MyFunction() {\n" + "   return 1;\n") + "}\n");
        KieServices ks = Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();
        kfs.write("src/main/resources/org/kie/scanner/rule.drl", rule);
        KieBuilder kieBuilder = ks.newKieBuilder(kfs).buildAll();
        Assert.assertEquals(1, kieBuilder.getResults().getMessages(ERROR).size());
        kfs.write("src/main/resources/org/kie/scanner/function.drl", function);
        IncrementalResults addResults1 = createFileSet("src/main/resources/org/kie/scanner/function.drl").build();
        Assert.assertEquals(0, addResults1.getAddedMessages().size());
        Assert.assertEquals(1, addResults1.getRemovedMessages().size());
    }

    @Test
    public void checkIncrementalCompilationWithFunctionThenRule() throws Exception {
        String rule = "package org.kie.scanner\n" + (((("rule R1 when\n" + "   String()\n") + "then\n") + "   System.out.println(MyFunction());\n") + "end\n");
        String function = "package org.kie.scanner\n" + (("function int MyFunction() {\n" + "   return 1;\n") + "}\n");
        KieServices ks = Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();
        kfs.write("src/main/resources/org/kie/scanner/function.drl", function);
        KieBuilder kieBuilder = ks.newKieBuilder(kfs).buildAll();
        Assert.assertEquals(0, kieBuilder.getResults().getMessages(ERROR).size());
        kfs.write("src/main/resources/org/kie/scanner/rule.drl", rule);
        IncrementalResults addResults = createFileSet("src/main/resources/org/kie/scanner/rule.drl").build();
        Assert.assertEquals(0, addResults.getAddedMessages().size());
        Assert.assertEquals(0, addResults.getRemovedMessages().size());
    }

    @Test
    public void checkIncrementalCompilationWithMultipleKieBases() throws Exception {
        String rule = "package org.kie.scanner\n" + (("rule R1 when\n" + "then\n") + "end\n");
        String invalidRule = "package org.kie.scanner\n" + ((("rule R2 when\n" + "   Cheese()\n")// missing import
         + "then\n") + "end\n");
        KieServices ks = Factory.get();
        KieFileSystem kfs = createKieFileSystemWithTwoKBases(ks);
        kfs.write("src/main/resources/org/kie/scanner/rule.drl", rule);
        KieBuilder kieBuilder = ks.newKieBuilder(kfs).buildAll();
        Assert.assertEquals(0, kieBuilder.getResults().getMessages().size());
        kfs.write("src/main/resources/org/kie/scanner/invalidRule.drl", invalidRule);
        IncrementalResults addResults = createFileSet("src/main/resources/org/kie/scanner/invalidRule.drl").build();
        Assert.assertEquals(2, addResults.getAddedMessages().size());
        addResults.getAddedMessages().stream().map(( m) -> ((MessageImpl) (m))).forEach(( m) -> assertNotNull(m.getKieBaseName()));
    }
}
