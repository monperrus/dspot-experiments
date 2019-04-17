package com.vaadin.tests.accessibility;


import com.vaadin.testbench.elements.ButtonElement;
import com.vaadin.tests.tb3.MultiBrowserTest;
import org.junit.Assert;
import org.junit.Test;


/**
 * Test to see if regular and alert windows get the correct wai-aria roles
 *
 * @author Vaadin Ltd
 */
public class WindowWaiAriaRolesTest extends MultiBrowserTest {
    @Test
    public void testRegularWindowRole() {
        openTestURL();
        $(ButtonElement.class).caption("Regular").first().click();
        String role = getWindowRole();
        Assert.assertTrue((("Dialog has incorrect role '" + role) + "', expected 'dialog'"), "dialog".equals(role));
    }

    @Test
    public void testAlertWindowRole() {
        openTestURL();
        $(ButtonElement.class).caption("Alert").first().click();
        String role = getWindowRole();
        Assert.assertTrue((("Dialog has incorrect role '" + role) + "', expected 'alertdialog'"), "alertdialog".equals(role));
    }
}
