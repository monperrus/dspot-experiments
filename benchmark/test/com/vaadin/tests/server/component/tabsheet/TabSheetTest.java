package com.vaadin.tests.server.component.tabsheet;


import com.vaadin.shared.ui.tabsheet.TabsheetServerRpc;
import com.vaadin.shared.ui.tabsheet.TabsheetState;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentTest;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.TabSheet.Tab;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Assert;
import org.junit.Test;


public class TabSheetTest {
    @Test
    public void addExistingComponent() {
        Component c = new Label("abc");
        TabSheet tabSheet = new TabSheet();
        tabSheet.addComponent(c);
        tabSheet.addComponent(c);
        Iterator<Component> iter = tabSheet.getComponentIterator();
        Assert.assertEquals(c, iter.next());
        Assert.assertFalse(iter.hasNext());
        Assert.assertNotNull(tabSheet.getTab(c));
    }

    @Test
    public void getComponentFromTab() {
        Component c = new Label("abc");
        TabSheet tabSheet = new TabSheet();
        Tab tab = tabSheet.addTab(c);
        Assert.assertEquals(c, tab.getComponent());
    }

    @Test
    public void addTabWithComponentOnly() {
        TabSheet tabSheet = new TabSheet();
        Tab tab1 = tabSheet.addTab(new Label("aaa"));
        Tab tab2 = tabSheet.addTab(new Label("bbb"));
        Tab tab3 = tabSheet.addTab(new Label("ccc"));
        // Check right order of tabs
        Assert.assertEquals(0, tabSheet.getTabPosition(tab1));
        Assert.assertEquals(1, tabSheet.getTabPosition(tab2));
        Assert.assertEquals(2, tabSheet.getTabPosition(tab3));
        // Calling addTab with existing component does not move tab
        tabSheet.addTab(tab1.getComponent());
        // Check right order of tabs
        Assert.assertEquals(0, tabSheet.getTabPosition(tab1));
        Assert.assertEquals(1, tabSheet.getTabPosition(tab2));
        Assert.assertEquals(2, tabSheet.getTabPosition(tab3));
    }

    @Test
    public void addTabWithComponentAndIndex() {
        TabSheet tabSheet = new TabSheet();
        Tab tab1 = tabSheet.addTab(new Label("aaa"));
        Tab tab2 = tabSheet.addTab(new Label("bbb"));
        Tab tab3 = tabSheet.addTab(new Label("ccc"));
        Tab tab4 = tabSheet.addTab(new Label("ddd"), 1);
        Tab tab5 = tabSheet.addTab(new Label("eee"), 3);
        Assert.assertEquals(0, tabSheet.getTabPosition(tab1));
        Assert.assertEquals(1, tabSheet.getTabPosition(tab4));
        Assert.assertEquals(2, tabSheet.getTabPosition(tab2));
        Assert.assertEquals(3, tabSheet.getTabPosition(tab5));
        Assert.assertEquals(4, tabSheet.getTabPosition(tab3));
        // Calling addTab with existing component does not move tab
        tabSheet.addTab(tab1.getComponent(), 3);
        Assert.assertEquals(0, tabSheet.getTabPosition(tab1));
        Assert.assertEquals(1, tabSheet.getTabPosition(tab4));
        Assert.assertEquals(2, tabSheet.getTabPosition(tab2));
        Assert.assertEquals(3, tabSheet.getTabPosition(tab5));
        Assert.assertEquals(4, tabSheet.getTabPosition(tab3));
    }

    @Test
    public void addTabWithAllParameters() {
        TabSheet tabSheet = new TabSheet();
        Tab tab1 = tabSheet.addTab(new Label("aaa"));
        Tab tab2 = tabSheet.addTab(new Label("bbb"));
        Tab tab3 = tabSheet.addTab(new Label("ccc"));
        Tab tab4 = tabSheet.addTab(new Label("ddd"), "ddd", null, 1);
        Tab tab5 = tabSheet.addTab(new Label("eee"), "eee", null, 3);
        Assert.assertEquals(0, tabSheet.getTabPosition(tab1));
        Assert.assertEquals(1, tabSheet.getTabPosition(tab4));
        Assert.assertEquals(2, tabSheet.getTabPosition(tab2));
        Assert.assertEquals(3, tabSheet.getTabPosition(tab5));
        Assert.assertEquals(4, tabSheet.getTabPosition(tab3));
        // Calling addTab with existing component does not move tab
        tabSheet.addTab(tab1.getComponent(), "xxx", null, 3);
        Assert.assertEquals(0, tabSheet.getTabPosition(tab1));
        Assert.assertEquals(1, tabSheet.getTabPosition(tab4));
        Assert.assertEquals(2, tabSheet.getTabPosition(tab2));
        Assert.assertEquals(3, tabSheet.getTabPosition(tab5));
        Assert.assertEquals(4, tabSheet.getTabPosition(tab3));
    }

    @Test
    public void getTabByPosition() {
        TabSheet tabSheet = new TabSheet();
        Tab tab1 = tabSheet.addTab(new Label("aaa"));
        Tab tab2 = tabSheet.addTab(new Label("bbb"));
        Tab tab3 = tabSheet.addTab(new Label("ccc"));
        Assert.assertEquals(tab1, tabSheet.getTab(0));
        Assert.assertEquals(tab2, tabSheet.getTab(1));
        Assert.assertEquals(tab3, tabSheet.getTab(2));
        Assert.assertEquals(null, tabSheet.getTab(3));
    }

    @Test
    public void selectTab() {
        TabSheet tabSheet = new TabSheet();
        Tab tab1 = tabSheet.addTab(new Label("aaa"));
        Tab tab2 = tabSheet.addTab(new Label("bbb"));
        Tab tab3 = tabSheet.addTab(new Label("ccc"));
        Label componentNotInSheet = new Label("ddd");
        Tab tabNotInSheet = new TabSheet().addTab(new Label("eee"));
        Assert.assertEquals(tab1.getComponent(), tabSheet.getSelectedTab());
        // Select tab by component...
        tabSheet.setSelectedTab(tab2.getComponent());
        Assert.assertEquals(tab2.getComponent(), tabSheet.getSelectedTab());
        // by tab instance
        tabSheet.setSelectedTab(tab3);
        Assert.assertEquals(tab3.getComponent(), tabSheet.getSelectedTab());
        // by index
        tabSheet.setSelectedTab(0);
        Assert.assertEquals(tab1.getComponent(), tabSheet.getSelectedTab());
        // Should be no-op...
        tabSheet.setSelectedTab(componentNotInSheet);
        Assert.assertEquals(tab1.getComponent(), tabSheet.getSelectedTab());
        // this as well
        tabSheet.setSelectedTab(tabNotInSheet);
        Assert.assertEquals(tab1.getComponent(), tabSheet.getSelectedTab());
        // and this
        tabSheet.setSelectedTab(123);
        Assert.assertEquals(tab1.getComponent(), tabSheet.getSelectedTab());
    }

    @Test
    public void replaceComponent() {
        TabSheet tabSheet = new TabSheet();
        Label lbl1 = new Label("aaa");
        Label lbl2 = new Label("bbb");
        Label lbl3 = new Label("ccc");
        Label lbl4 = new Label("ddd");
        Tab tab1 = tabSheet.addTab(lbl1);
        tab1.setCaption("tab1");
        tab1.setClosable(true);
        Tab tab2 = tabSheet.addTab(lbl2);
        tab2.setDescription("description");
        tab2.setEnabled(false);
        // Replace component not in tabsheet with one already in tabsheet -
        // should be no-op
        tabSheet.replaceComponent(lbl3, lbl2);
        Assert.assertEquals(2, tabSheet.getComponentCount());
        Assert.assertSame(tab1, tabSheet.getTab(lbl1));
        Assert.assertSame(tab2, tabSheet.getTab(lbl2));
        Assert.assertNull(tabSheet.getTab(lbl3));
        // Replace component not in tabsheet with one not in tabsheet either
        // should add lbl4 as last tab
        tabSheet.replaceComponent(lbl3, lbl4);
        Assert.assertEquals(3, tabSheet.getComponentCount());
        Assert.assertSame(tab1, tabSheet.getTab(lbl1));
        Assert.assertSame(tab2, tabSheet.getTab(lbl2));
        Assert.assertEquals(2, tabSheet.getTabPosition(tabSheet.getTab(lbl4)));
        // Replace component in tabsheet with another
        // should swap places, tab association should stay the same but tabs
        // should swap metadata
        tabSheet.replaceComponent(lbl1, lbl2);
        Assert.assertSame(tab1, tabSheet.getTab(lbl1));
        Assert.assertSame(tab2, tabSheet.getTab(lbl2));
        Assert.assertFalse(tab1.isClosable());
        Assert.assertTrue(tab2.isClosable());
        Assert.assertFalse(tab1.isEnabled());
        Assert.assertTrue(tab2.isEnabled());
        Assert.assertEquals("description", tab1.getDescription());
        Assert.assertEquals(null, tab2.getDescription());
        Assert.assertEquals(3, tabSheet.getComponentCount());
        Assert.assertEquals(1, tabSheet.getTabPosition(tabSheet.getTab(lbl1)));
        Assert.assertEquals(0, tabSheet.getTabPosition(tabSheet.getTab(lbl2)));
        // Replace component in tabsheet with one not in tabsheet
        // should create a new tab instance for the new component, old tab
        // instance should become unattached
        // tab metadata should be copied from old to new
        tabSheet.replaceComponent(lbl1, lbl3);
        Assert.assertEquals(3, tabSheet.getComponentCount());
        Assert.assertNull(tabSheet.getTab(lbl1));
        Assert.assertNull(tab1.getComponent());
        Assert.assertNotNull(tabSheet.getTab(lbl3));
        Assert.assertFalse(tabSheet.getTab(lbl3).isEnabled());
        Assert.assertEquals("description", tab1.getDescription());
        Assert.assertEquals(1, tabSheet.getTabPosition(tabSheet.getTab(lbl3)));
    }

    @Test
    public void testSelectedTabChangeEvent_whenComponentReplaced() {
        // given
        final class SelectedTabExpectedComponentListener implements SelectedTabChangeListener {
            private Component actualComponent;

            @Override
            public void selectedTabChange(SelectedTabChangeEvent event) {
                actualComponent = event.getTabSheet().getSelectedTab();
            }

            public void assertActualComponentIs(Component expectedComponent) {
                Assert.assertEquals(expectedComponent, actualComponent);
                actualComponent = null;
            }
        }
        TabSheet tabSheet = new TabSheet();
        final Label lbl1 = new Label("aaa");
        final Label lbl2 = new Label("bbb");
        final Label lbl3 = new Label("ccc");
        final Label lbl4 = new Label("ddd");
        tabSheet.addComponent(lbl1);
        tabSheet.addComponent(lbl2);
        tabSheet.addComponent(lbl3);
        tabSheet.setSelectedTab(lbl2);
        SelectedTabExpectedComponentListener listener = new SelectedTabExpectedComponentListener();
        tabSheet.addSelectedTabChangeListener(listener);
        // when selected tab is replaced with new Component
        tabSheet.replaceComponent(lbl2, lbl4);
        // then
        listener.assertActualComponentIs(lbl4);
        Assert.assertEquals(lbl4, tabSheet.getSelectedTab());
        // when not selected tab is replaced with new Component
        tabSheet.replaceComponent(lbl1, lbl2);
        // then
        Assert.assertEquals(lbl4, tabSheet.getSelectedTab());
        // when not selected tab is replaced with existing Component
        tabSheet.replaceComponent(lbl2, lbl3);
        // then
        Assert.assertEquals(lbl4, tabSheet.getSelectedTab());
        // when selected tab is replaced with existing Component (locations are
        // just swapped)
        tabSheet.replaceComponent(lbl4, lbl3);
        // then
        listener.assertActualComponentIs(lbl3);
        Assert.assertEquals(lbl3, tabSheet.getSelectedTab());
    }

    public static class TestTabsheet extends TabSheet {
        public TestTabsheet(Component... components) {
            super(components);
        }

        public String getKey(Component c) {
            return keyMapper.key(c);
        }

        @Override
        public TabsheetState getState() {
            return super.getState();
        }
    }

    @Test
    public void userOriginatedForSelectionEvent() {
        AtomicBoolean userOriginated = new AtomicBoolean(false);
        AtomicReference<Component> selected = new AtomicReference<>();
        Button b1 = new Button("b1");
        Button b2 = new Button("b2");
        Button b3 = new Button("b3");
        Button b4 = new Button("b4");
        TabSheetTest.TestTabsheet tabsheet = new TabSheetTest.TestTabsheet(b1, b2, b3, b4);
        addSelectedTabChangeListener(( event) -> {
            userOriginated.set(event.isUserOriginated());
            selected.set(event.getTabSheet().getSelectedTab());
        });
        tabsheet.setSelectedTab(b2);
        Assert.assertFalse(userOriginated.get());
        Assert.assertEquals(b2, selected.get());
        TabsheetServerRpc rpc = ComponentTest.getRpcProxy(tabsheet, TabsheetServerRpc.class);
        rpc.setSelected(tabsheet.getKey(b1));
        Assert.assertTrue(userOriginated.get());
        Assert.assertEquals(b1, selected.get());
        tabsheet.setSelectedTab(tabsheet.getTab(b4));
        Assert.assertFalse(userOriginated.get());
        Assert.assertEquals(b4, selected.get());
    }
}
