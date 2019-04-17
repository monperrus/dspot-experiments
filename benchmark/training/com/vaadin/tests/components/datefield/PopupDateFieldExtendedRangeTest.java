package com.vaadin.tests.components.datefield;


import Keys.ARROW_DOWN;
import Keys.ARROW_LEFT;
import Keys.SHIFT;
import com.vaadin.testbench.By;
import com.vaadin.testbench.elements.ButtonElement;
import com.vaadin.testbench.elements.DateFieldElement;
import com.vaadin.testbench.parallel.BrowserUtil;
import com.vaadin.tests.tb3.MultiBrowserTest;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.WebElement;


/**
 * Tests contents and functionality of PopupDateField's popup.
 *
 * @author Vaadin Ltd
 */
public class PopupDateFieldExtendedRangeTest extends MultiBrowserTest {
    @Test
    public void testFirstDateField() {
        List<DateFieldElement> dateFields = $(DateFieldElement.class).all();
        Assert.assertEquals("unexpected amount of datefields", 3, dateFields.size());
        DateFieldElement dateField = dateFields.get(0);
        // open the popup
        dateField.findElement(By.tagName("button")).click();
        Assert.assertTrue("popup not found when there should be one", isElementPresent(By.className("v-datefield-popup")));
        // verify contents
        WebElement popup = findElement(By.className("v-datefield-popup"));
        Assert.assertEquals("unexpected month", "tammikuu 2011", popup.findElements(By.className("v-datefield-calendarpanel-month")).get(1).getText());
        List<WebElement> headerElements = popup.findElement(By.className("v-datefield-calendarpanel-weekdays")).findElements(By.tagName("td"));
        List<WebElement> weekdays = new ArrayList<>();
        for (WebElement headerElement : headerElements) {
            if ("columnheader".equals(headerElement.getAttribute("role"))) {
                weekdays.add(headerElement);
            }
        }
        Assert.assertEquals("unexpected weekday count", 7, weekdays.size());
        Assert.assertEquals("unexpected first day of week", "MA", weekdays.get(0).getText());
        Assert.assertEquals("unexpected weeknumber count", 0, popup.findElements(By.className("v-datefield-calendarpanel-weeknumber")).size());
        Assert.assertEquals("unexpected selection", "1", popup.findElement(By.className("v-datefield-calendarpanel-day-selected")).getText());
        Assert.assertEquals("unexpected focus", "1", popup.findElement(By.className("v-datefield-calendarpanel-day-focused")).getText());
        List<WebElement> days = popup.findElements(By.className("v-datefield-calendarpanel-day"));
        Assert.assertEquals("unexpected day count", 42, days.size());
        Assert.assertEquals("unexpected day content", "27", days.get(0).getText());
        Assert.assertEquals("unexpected day content", "4", days.get(8).getText());
        Assert.assertEquals("unexpected day content", "21", days.get(25).getText());
        Assert.assertEquals("unexpected day content", "6", days.get(41).getText());
        // move to the previous month
        popup.findElement(By.className("v-datefield-calendarpanel-prevmonth")).findElement(By.tagName("button")).click();
        // verify contents
        Assert.assertEquals("unexpected month", "joulukuu 2010", popup.findElements(By.className("v-datefield-calendarpanel-month")).get(1).getText());
        Assert.assertEquals("unexpected selection", "1", popup.findElement(By.className("v-datefield-calendarpanel-day-selected")).getText());
        Assert.assertEquals("unexpected focus", 0, popup.findElements(By.className("v-datefield-calendarpanel-day-focused")).size());
        days = popup.findElements(By.className("v-datefield-calendarpanel-day"));
        Assert.assertEquals("unexpected day count", 42, days.size());
        Assert.assertEquals("unexpected day content", "29", days.get(0).getText());
        Assert.assertEquals("unexpected day content", "7", days.get(8).getText());
        Assert.assertEquals("unexpected day content", "24", days.get(25).getText());
        Assert.assertEquals("unexpected day content", "9", days.get(41).getText());
        // move to the previous year
        popup.findElement(By.className("v-datefield-calendarpanel-prevyear")).findElement(By.tagName("button")).click();
        // verify contents
        Assert.assertEquals("unexpected month", "joulukuu 2009", popup.findElements(By.className("v-datefield-calendarpanel-month")).get(1).getText());
        Assert.assertEquals("unexpected selection", 0, popup.findElements(By.className("v-datefield-calendarpanel-day-selected")).size());
        Assert.assertEquals("unexpected focus", 0, popup.findElements(By.className("v-datefield-calendarpanel-day-focused")).size());
        days = popup.findElements(By.className("v-datefield-calendarpanel-day"));
        Assert.assertEquals("unexpected day count", 42, days.size());
        Assert.assertEquals("unexpected day content", "30", days.get(0).getText());
        Assert.assertEquals("unexpected day content", "8", days.get(8).getText());
        Assert.assertEquals("unexpected day content", "25", days.get(25).getText());
        Assert.assertEquals("unexpected day content", "10", days.get(41).getText());
        // close the popup by clicking the button again
        dateField.findElement(By.tagName("button")).click();
        Assert.assertFalse("popup found when there should be none", isElementPresent(By.className("v-datefield-popup")));
    }

    @Test
    public void testSecondDateField() throws InterruptedException {
        DateFieldElement dateField = $(DateFieldElement.class).all().get(1);
        ButtonElement button = $(ButtonElement.class).first();
        // change the date
        button.click();
        sleep(100);
        // open the popup
        dateField.findElement(By.tagName("button")).click();
        Assert.assertTrue("popup not found when there should be one", isElementPresent(By.className("v-datefield-popup")));
        // verify contents
        WebElement popup = findElement(By.className("v-datefield-popup"));
        Assert.assertEquals("unexpected month", "February 2010", popup.findElements(By.className("v-datefield-calendarpanel-month")).get(1).getText());
        List<WebElement> headerElements = popup.findElement(By.className("v-datefield-calendarpanel-weekdays")).findElements(By.tagName("td"));
        List<WebElement> weekdays = new ArrayList<>();
        for (WebElement headerElement : headerElements) {
            if ("columnheader".equals(headerElement.getAttribute("role"))) {
                weekdays.add(headerElement);
            }
        }
        Assert.assertEquals("unexpected weekday count", 7, weekdays.size());
        Assert.assertEquals("unexpected first day of week", "SUN", weekdays.get(0).getText());
        Assert.assertEquals("unexpected weeknumber count", 0, popup.findElements(By.className("v-datefield-calendarpanel-weeknumber")).size());
        Assert.assertEquals("unexpected selection", "16", popup.findElement(By.className("v-datefield-calendarpanel-day-selected")).getText());
        Assert.assertEquals("unexpected focus", "16", popup.findElement(By.className("v-datefield-calendarpanel-day-focused")).getText());
        List<WebElement> days = popup.findElements(By.className("v-datefield-calendarpanel-day"));
        Assert.assertEquals("unexpected day count", 42, days.size());
        Assert.assertEquals("unexpected day content", "31", days.get(0).getText());
        Assert.assertEquals("unexpected day content", "8", days.get(8).getText());
        Assert.assertEquals("unexpected day content", "25", days.get(25).getText());
        Assert.assertEquals("unexpected day content", "13", days.get(41).getText());
        // navigate down
        WebElement popupBody = popup.findElement(By.className("v-datefield-calendarpanel"));
        popupBody.sendKeys(ARROW_DOWN);
        // ensure the focus changed
        Assert.assertEquals("unexpected focus", "23", popup.findElement(By.className("v-datefield-calendarpanel-day-focused")).getText());
        // navigate down
        popupBody.sendKeys(ARROW_DOWN);
        // verify contents
        Assert.assertEquals("unexpected month", "March 2010", popup.findElements(By.className("v-datefield-calendarpanel-month")).get(1).getText());
        Assert.assertEquals("unexpected selection", 0, popup.findElements(By.className("v-datefield-calendarpanel-day-selected")).size());
        Assert.assertEquals("unexpected focus", "2", popup.findElement(By.className("v-datefield-calendarpanel-day-focused")).getText());
        days = popup.findElements(By.className("v-datefield-calendarpanel-day"));
        Assert.assertEquals("unexpected day count", 42, days.size());
        Assert.assertEquals("unexpected day content", "28", days.get(0).getText());
        Assert.assertEquals("unexpected day content", "8", days.get(8).getText());
        Assert.assertEquals("unexpected day content", "25", days.get(25).getText());
        Assert.assertEquals("unexpected day content", "10", days.get(41).getText());
        // navigate left
        popupBody = popup.findElement(By.className("v-datefield-calendarpanel"));
        popupBody.sendKeys(ARROW_LEFT);
        // ensure the focus changed
        Assert.assertEquals("unexpected focus", "1", popup.findElement(By.className("v-datefield-calendarpanel-day-focused")).getText());
        // navigate left
        popupBody.sendKeys(ARROW_LEFT);
        // verify contents
        Assert.assertEquals("unexpected month", "February 2010", popup.findElements(By.className("v-datefield-calendarpanel-month")).get(1).getText());
        Assert.assertEquals("unexpected selection", "16", popup.findElement(By.className("v-datefield-calendarpanel-day-selected")).getText());
        Assert.assertEquals("unexpected focus", "28", popup.findElement(By.className("v-datefield-calendarpanel-day-focused")).getText());
        days = popup.findElements(By.className("v-datefield-calendarpanel-day"));
        Assert.assertEquals("unexpected day count", 42, days.size());
        Assert.assertEquals("unexpected day content", "31", days.get(0).getText());
        Assert.assertEquals("unexpected day content", "8", days.get(8).getText());
        Assert.assertEquals("unexpected day content", "25", days.get(25).getText());
        Assert.assertEquals("unexpected day content", "13", days.get(41).getText());
        // close the popup by clicking the input field
        dateField.findElement(By.tagName("input")).click();
        Assert.assertFalse("popup found when there should be none", isElementPresent(By.className("v-datefield-popup")));
    }

    @Test
    public void testThirdDateField() throws InterruptedException {
        DateFieldElement dateField = $(DateFieldElement.class).all().get(2);
        ButtonElement button = $(ButtonElement.class).first();
        // change the date
        button.click();
        sleep(100);
        // open the popup
        dateField.findElement(By.tagName("button")).click();
        Assert.assertTrue("popup not found when there should be one", isElementPresent(By.className("v-datefield-popup")));
        // verify contents
        WebElement popup = findElement(By.className("v-datefield-popup"));
        Assert.assertEquals("unexpected month", "helmikuu 2010", popup.findElements(By.className("v-datefield-calendarpanel-month")).get(1).getText());
        List<WebElement> headerElements = popup.findElement(By.className("v-datefield-calendarpanel-weekdays")).findElements(By.tagName("td"));
        List<WebElement> weekdays = new ArrayList<>();
        for (WebElement headerElement : headerElements) {
            if ("columnheader".equals(headerElement.getAttribute("role"))) {
                weekdays.add(headerElement);
            }
        }
        Assert.assertEquals("unexpected weekday count", 7, weekdays.size());
        Assert.assertEquals("unexpected first day of week", "MA", weekdays.get(0).getText());
        List<WebElement> weeknumbers = popup.findElements(By.className("v-datefield-calendarpanel-weeknumber"));
        Assert.assertEquals("unexpected weeknumber count", 6, weeknumbers.size());
        Assert.assertEquals("unexpected weeknumber content", "5", weeknumbers.get(0).getText());
        Assert.assertEquals("unexpected weeknumber content", "10", weeknumbers.get(5).getText());
        Assert.assertEquals("unexpected selection", "16", popup.findElement(By.className("v-datefield-calendarpanel-day-selected")).getText());
        Assert.assertEquals("unexpected focus", "16", popup.findElement(By.className("v-datefield-calendarpanel-day-focused")).getText());
        List<WebElement> days = popup.findElements(By.className("v-datefield-calendarpanel-day"));
        Assert.assertEquals("unexpected day count", 42, days.size());
        Assert.assertEquals("unexpected day content", "1", days.get(0).getText());
        Assert.assertEquals("unexpected day content", "9", days.get(8).getText());
        Assert.assertEquals("unexpected day content", "26", days.get(25).getText());
        Assert.assertEquals("unexpected day content", "14", days.get(41).getText());
        // navigate to previous month
        WebElement popupBody = popup.findElement(By.className("v-datefield-calendarpanel"));
        new org.openqa.selenium.interactions.Actions(driver).keyDown(SHIFT).perform();
        popupBody.sendKeys(ARROW_LEFT);
        new org.openqa.selenium.interactions.Actions(driver).keyUp(SHIFT).perform();
        // TODO: remove this once #14406 has been fixed
        if ((BrowserUtil.isIE(getDesiredCapabilities())) || (BrowserUtil.isFirefox(getDesiredCapabilities()))) {
            popup.findElement(By.className("v-datefield-calendarpanel-prevmonth")).findElement(By.tagName("button")).click();
        }
        // verify contents
        Assert.assertEquals("unexpected month", "tammikuu 2010", popup.findElements(By.className("v-datefield-calendarpanel-month")).get(1).getText());
        weeknumbers = popup.findElements(By.className("v-datefield-calendarpanel-weeknumber"));
        Assert.assertEquals("unexpected weeknumber count", 6, weeknumbers.size());
        Assert.assertEquals("unexpected weeknumber content", "53", weeknumbers.get(0).getText());
        Assert.assertEquals("unexpected weeknumber content", "5", weeknumbers.get(5).getText());
        Assert.assertEquals("unexpected selection", 0, popup.findElements(By.className("v-datefield-calendarpanel-day-selected")).size());
        // TODO: remove this check once #14406 has been fixed -- clicking the
        // button instead of navigating with arrow keys steals the focus
        if ((!(BrowserUtil.isIE(getDesiredCapabilities()))) && (!(BrowserUtil.isFirefox(getDesiredCapabilities())))) {
            Assert.assertEquals("unexpected focus", "16", popup.findElement(By.className("v-datefield-calendarpanel-day-focused")).getText());
        }
        days = popup.findElements(By.className("v-datefield-calendarpanel-day"));
        Assert.assertEquals("unexpected day count", 42, days.size());
        Assert.assertEquals("unexpected day content", "28", days.get(0).getText());
        Assert.assertEquals("unexpected day content", "5", days.get(8).getText());
        Assert.assertEquals("unexpected day content", "22", days.get(25).getText());
        Assert.assertEquals("unexpected day content", "7", days.get(41).getText());
        // navigate to previous year
        new org.openqa.selenium.interactions.Actions(driver).keyDown(SHIFT).perform();
        popupBody.sendKeys(ARROW_DOWN);
        new org.openqa.selenium.interactions.Actions(driver).keyUp(SHIFT).perform();
        // TODO: remove this once #14406 has been fixed
        popup.findElement(By.className("v-datefield-calendarpanel-prevyear")).findElement(By.tagName("button")).click();
        // verify contents
        Assert.assertEquals("unexpected month", "tammikuu 2009", popup.findElements(By.className("v-datefield-calendarpanel-month")).get(1).getText());
        weeknumbers = popup.findElements(By.className("v-datefield-calendarpanel-weeknumber"));
        Assert.assertEquals("unexpected weeknumber count", 6, weeknumbers.size());
        Assert.assertEquals("unexpected weeknumber content", "1", weeknumbers.get(0).getText());
        Assert.assertEquals("unexpected weeknumber content", "6", weeknumbers.get(5).getText());
        Assert.assertEquals("unexpected selection", 0, popup.findElements(By.className("v-datefield-calendarpanel-day-selected")).size());
        // TODO: remove this check once #14406 has been fixed -- clicking the
        // button instead of navigating with arrow keys steals the focus
        if (false) {
            Assert.assertEquals("unexpected focus", "16", popup.findElement(By.className("v-datefield-calendarpanel-day-focused")).getText());
        }
        days = popup.findElements(By.className("v-datefield-calendarpanel-day"));
        Assert.assertEquals("unexpected day count", 42, days.size());
        Assert.assertEquals("unexpected day content", "29", days.get(0).getText());
        Assert.assertEquals("unexpected day content", "6", days.get(8).getText());
        Assert.assertEquals("unexpected day content", "23", days.get(25).getText());
        Assert.assertEquals("unexpected day content", "8", days.get(41).getText());
        // close the popup by clicking an unrelated element
        button.click();
        Assert.assertFalse("popup found when there should be none", isElementPresent(By.className("v-datefield-popup")));
    }
}
