package com.vaadin.tests.themes.valo;


import com.vaadin.testbench.By;
import com.vaadin.testbench.elements.LabelElement;
import com.vaadin.tests.tb3.MultiBrowserTest;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;


/**
 * Test for the built-in reponsive ("RWD") styles in Valo.
 */
public class ResponsiveStylesTest extends MultiBrowserTest {
    /**
     * Use this parameter to test the collapsed menu state.
     */
    public static final String COLLAPSED_MENU_TEST_PARAM = "collapsed";

    private static final String MENU_STYLENAME = "valo-menu";

    private static final int NARROW_ELEMENT_INDEX = 0;

    private static final int NARROW_WIDTH = 112;

    private static final int WIDE_ELEMENT_INDEX = 1;

    private static final int WIDE_WIDTH = 146;

    private static final String TOGGLE_STYLENAME = "valo-menu-toggle";

    /**
     * Tests that valo-menu-responsive can be used in any element on the page,
     * not just as top-level component.
     *
     * @throws Exception
     * 		
     */
    @Test
    public void testValoMenuResponsiveParentSize() throws Exception {
        openTestURL();
        waitUntilLoadingIndicatorNotVisible();
        List<WebElement> menus = findElements(By.className(ResponsiveStylesTest.MENU_STYLENAME));
        WebElement narrowMenu = menus.get(ResponsiveStylesTest.NARROW_ELEMENT_INDEX);
        int narrowWidth = narrowMenu.getSize().width;
        MatcherAssert.assertThat(narrowWidth, Matchers.equalTo(ResponsiveStylesTest.NARROW_WIDTH));
        WebElement wideMenu = menus.get(ResponsiveStylesTest.WIDE_ELEMENT_INDEX);
        int wideWidth = wideMenu.getSize().width;
        MatcherAssert.assertThat(wideWidth, Matchers.equalTo(ResponsiveStylesTest.WIDE_WIDTH));
        sleep(200);
        compareScreen("defaultMenuWidths");
    }

    /**
     * Tests that the valo-menu-hover style makes the menu appear on mouseover.
     *
     * @throws Exception
     * 		
     */
    @Test
    public void testValoMenuResponsiveHover() throws Exception {
        openTestURL(ResponsiveStylesTest.COLLAPSED_MENU_TEST_PARAM);
        waitUntilLoadingIndicatorNotVisible();
        // Make sure mouse is not hovering the menu
        new Actions(getDriver()).moveToElement($(LabelElement.class).first()).moveByOffset(0, 300).perform();
        compareScreen("collapsedMenu");
        List<WebElement> toggles = findElements(By.className(ResponsiveStylesTest.TOGGLE_STYLENAME));
        // Only one menu in the collapsed example
        WebElement toggle = toggles.get(0);
        Actions actions = new Actions(getDriver());
        actions.moveToElement(toggle);
        actions.perform();
        sleep(200);
        compareScreen("expandedMenu");
    }
}
