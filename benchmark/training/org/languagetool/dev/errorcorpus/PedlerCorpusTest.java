/**
 * LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.errorcorpus;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;


public class PedlerCorpusTest {
    @Test
    public void testCorpusAccess() throws IOException {
        URL errors = PedlerCorpusTest.class.getResource("/org/languagetool/dev/eval");
        PedlerCorpus corpus = new PedlerCorpus(new File(errors.getFile()));
        Iterator<ErrorSentence> iterator = corpus.iterator();
        Assert.assertTrue(iterator.hasNext());
        ErrorSentence sentence1 = iterator.next();
        Assert.assertThat(sentence1.getAnnotatedText().getPlainText(), CoreMatchers.is("But also please not that grammar checkers aren't perfect."));
        Assert.assertThat(sentence1.getMarkupText(), CoreMatchers.is("But <ERR targ=foo>also</ERR> please <ERR targ=note>not</ERR> that grammar checkers aren't perfect."));
        ErrorSentence sentence2 = iterator.next();
        Assert.assertThat(sentence2.getAnnotatedText().getPlainText(), CoreMatchers.is("But also also please note note that grammar checkers aren't perfect."));
        Assert.assertThat(sentence2.getMarkupText(), CoreMatchers.is("But <ERR targ=bad suggestion>also also</ERR> please <ERR targ=note>note note</ERR> that grammar checkers aren't perfect."));
        Assert.assertTrue(iterator.hasNext());
        iterator.next();
        Assert.assertFalse(iterator.hasNext());
    }
}
