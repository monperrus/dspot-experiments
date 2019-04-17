package org.jabref.logic.layout.format;


import org.jabref.logic.layout.LayoutFormatter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 * Is the save as the AuthorLastFirstAbbreviator.
 */
public class AuthorAbbreviatorTest {
    @Test
    public void testFormat() {
        LayoutFormatter a = new AuthorLastFirstAbbreviator();
        LayoutFormatter b = new AuthorAbbreviator();
        Assertions.assertEquals(b.format(""), a.format(""));
        Assertions.assertEquals(b.format("Someone, Van Something"), a.format("Someone, Van Something"));
        Assertions.assertEquals(b.format("Smith, John"), a.format("Smith, John"));
        Assertions.assertEquals(b.format("von Neumann, John and Smith, John and Black Brown, Peter"), a.format("von Neumann, John and Smith, John and Black Brown, Peter"));
    }
}
