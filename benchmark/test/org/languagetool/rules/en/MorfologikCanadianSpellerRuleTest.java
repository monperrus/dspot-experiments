/**
 * LanguageTool, a natural language style checker
 * Copyright (C) 2012 Marcin Mi?kowski
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.rules.en;


import java.io.IOException;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.TestTools;
import org.languagetool.language.CanadianEnglish;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;


public class MorfologikCanadianSpellerRuleTest extends AbstractEnglishSpellerRuleTest {
    @Test
    public void testSuggestions() throws IOException {
        Language language = new CanadianEnglish();
        Rule rule = new MorfologikCanadianSpellerRule(TestTools.getMessages("en"), language, null, Collections.emptyList());
        super.testNonVariantSpecificSuggestions(rule, language);
        JLanguageTool langTool = new JLanguageTool(language);
        // suggestions from language specific spelling_en-XX.txt
        assertSuggestion(rule, langTool, "CATestWordToBeIgnore", "CATestWordToBeIgnored");
    }

    @Test
    public void testMorfologikSpeller() throws IOException {
        CanadianEnglish language = new CanadianEnglish();
        MorfologikBritishSpellerRule rule = new MorfologikBritishSpellerRule(TestTools.getMessages("en"), language, null, Collections.emptyList());
        JLanguageTool langTool = new JLanguageTool(language);
        // correct sentences:
        Assert.assertEquals(0, rule.match(langTool.getAnalyzedSentence("This is an example: we get behaviour as a dictionary word.")).length);
        Assert.assertEquals(0, rule.match(langTool.getAnalyzedSentence("Why don't we speak today.")).length);
        // with doesn't
        Assert.assertEquals(0, rule.match(langTool.getAnalyzedSentence("He doesn't know what to do.")).length);
        Assert.assertEquals(0, rule.match(langTool.getAnalyzedSentence(",")).length);
        Assert.assertEquals(0, rule.match(langTool.getAnalyzedSentence("123454")).length);
        Assert.assertEquals(0, rule.match(langTool.getAnalyzedSentence("I like my emoji (?)...")).length);
        Assert.assertEquals(0, rule.match(langTool.getAnalyzedSentence("?")).length);
        // incorrect sentences:
        RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence("arbor"));
        // check match positions:
        Assert.assertEquals(1, matches.length);
        Assert.assertEquals(0, matches[0].getFromPos());
        Assert.assertEquals(5, matches[0].getToPos());
        Assert.assertTrue(matches[0].getSuggestedReplacements().contains("arbour"));
        Assert.assertEquals(1, rule.match(langTool.getAnalyzedSentence("a?h")).length);
        Assert.assertEquals(0, rule.match(langTool.getAnalyzedSentence("a")).length);
    }
}
