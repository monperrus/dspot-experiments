package com.vaadin.tests.components.combobox;


import com.vaadin.testbench.elements.ButtonElement;
import com.vaadin.testbench.elements.ComboBoxElement;
import com.vaadin.tests.tb3.MultiBrowserTest;
import org.junit.Assert;
import org.junit.Test;


/**
 * Tests that changing a stylename will not cause the width parameter to be
 * removed from a combobox.
 *
 * @author Vaadin Ltd
 */
public class ComboboxStyleChangeWidthTest extends MultiBrowserTest {
    @Test
    public void testWidthRetained() {
        openTestURL();
        ComboBoxElement comboBox = $(ComboBoxElement.class).first();
        String oldStyle = comboBox.getAttribute("style");
        ButtonElement button = $(ButtonElement.class).first();
        button.click();
        String newStyle = comboBox.getAttribute("style");
        Assert.assertEquals("width has changed, should remain equal", oldStyle, newStyle);
    }
}
