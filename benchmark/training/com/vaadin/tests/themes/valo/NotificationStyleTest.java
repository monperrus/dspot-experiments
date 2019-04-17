package com.vaadin.tests.themes.valo;


import ValoTheme.LABEL_H1;
import com.vaadin.testbench.elements.ButtonElement;
import com.vaadin.tests.tb3.MultiBrowserTest;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;


/**
 * Test for H1 and P elements styles in Notifications.
 *
 * @author Vaadin Ltd
 */
public class NotificationStyleTest extends MultiBrowserTest {
    @Test
    public void testNotificationH1Style() {
        openTestURL();
        $(ButtonElement.class).first().click();
        moveByOffset(10, 10).perform();
        waitUntil(notificationPresentCondition(), 2);
        WebElement notification = findElement(By.className("v-Notification"));
        List<WebElement> headers = notification.findElements(By.tagName(LABEL_H1));
        String textAlign = headers.get(0).getCssValue("text-align");
        String textAlignInnerHeader = headers.get(1).getCssValue("text-align");
        Assert.assertNotEquals(("Styles for notification defined h1 tag " + "and custom HTML tag are the same"), textAlign, textAlignInnerHeader);
    }

    @Test
    public void testNotificationPStyle() {
        openTestURL();
        $(ButtonElement.class).get(1).click();
        moveByOffset(10, 10).perform();
        waitUntil(notificationPresentCondition(), 2);
        WebElement notification = findElement(By.className("v-Notification"));
        WebElement description = notification.findElement(By.className("v-Notification-description"));
        String display = description.getCssValue("display");
        String displayP2 = notification.findElement(By.className("tested-p")).getCssValue("display");
        Assert.assertNotEquals(("Styles for notification defined 'p' tag " + "and custom HTML tag are the same"), display, displayP2);
    }
}
