/**
 * LanguageTool, a natural language style checker
 * Copyright (C) 2009 Marcin Mi?kowski (http://www.languagetool.org)
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
package org.languagetool.tools;


import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.Assert;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.language.English;
import org.xml.sax.SAXException;


public class ToolsTest {
    @Test
    public void testCorrect() throws IOException, ParserConfigurationException, SAXException {
        JLanguageTool tool = new JLanguageTool(new English());
        Assert.assertEquals("This is a test.", Tools.correctText("This is an test.", tool));
    }
}
