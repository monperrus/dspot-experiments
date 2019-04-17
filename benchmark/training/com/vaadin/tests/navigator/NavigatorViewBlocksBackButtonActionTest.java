package com.vaadin.tests.navigator;


import NavigatorViewBlocksBackButtonAction.LABEL_MAINVIEW_ID;
import NavigatorViewBlocksBackButtonAction.LABEL_PROMPTEDVIEW_ID;
import com.vaadin.testbench.elements.ButtonElement;
import com.vaadin.tests.tb3.MultiBrowserTest;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;


public class NavigatorViewBlocksBackButtonActionTest extends MultiBrowserTest {
    @Test
    public void testIfConfirmBack() {
        openTestURL();
        // keep URL of main view
        final String initialUrl = driver.getCurrentUrl();
        // do it 2 times to verify that login is not broken after first time
        for (int i = 0; i < 2; i++) {
            // go to prompted view
            WebElement button = $(ButtonElement.class).first();
            button.click();
            // click back button
            driver.navigate().back();
            // confirm "go back by clicking confirm button
            WebElement buttonConfirmView = $(ButtonElement.class).first();
            buttonConfirmView.click();
            // verify we are in main view and url is correct
            waitForElementPresent(By.id(LABEL_MAINVIEW_ID));
            String currentUrl = driver.getCurrentUrl();
            Assert.assertEquals("Current URL should be equal to initial main view URL", initialUrl, currentUrl);
        }
    }

    @Test
    public void testIfCancelBack() {
        openTestURL();
        // go to prompted view
        WebElement button = $(ButtonElement.class).first();
        button.click();
        // keep URL of prompted view
        final String initialPromptedUrl = driver.getCurrentUrl();
        // click back button
        driver.navigate().back();
        // verify url is correct (is not changed)
        waitForElementPresent(By.id(LABEL_PROMPTEDVIEW_ID));
        String currentUrl = driver.getCurrentUrl();
        Assert.assertEquals("Current URL should be equal to initial prompted view URL", initialPromptedUrl, currentUrl);
        WebElement cancelButton = driver.findElement(By.className("v-window-closebox"));
        // click cancel button
        cancelButton.click();
        // verify we leave in prompted view and url is correct
        waitForElementPresent(By.id(LABEL_PROMPTEDVIEW_ID));
        currentUrl = driver.getCurrentUrl();
        Assert.assertEquals("Current URL should be equal to initial prompted view URL", initialPromptedUrl, currentUrl);
    }
}
