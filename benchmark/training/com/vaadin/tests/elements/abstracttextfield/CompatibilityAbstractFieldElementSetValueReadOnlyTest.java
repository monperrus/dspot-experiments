package com.vaadin.tests.elements.abstracttextfield;


import com.vaadin.testbench.elements.AbstractComponentElement.ReadOnlyException;
import com.vaadin.testbench.elements.CheckBoxElement;
import com.vaadin.testbench.elements.ComboBoxElement;
import com.vaadin.testbench.elements.DateFieldElement;
import com.vaadin.testbench.elements.ListSelectElement;
import com.vaadin.testbench.elements.NativeSelectElement;
import com.vaadin.testbench.elements.OptionGroupElement;
import com.vaadin.testbench.elements.PasswordFieldElement;
import com.vaadin.testbench.elements.TextAreaElement;
import com.vaadin.testbench.elements.TextFieldElement;
import com.vaadin.testbench.elements.TwinColSelectElement;
import com.vaadin.tests.tb3.MultiBrowserTest;
import org.junit.Test;


public class CompatibilityAbstractFieldElementSetValueReadOnlyTest extends MultiBrowserTest {
    @Test(expected = ReadOnlyException.class)
    public void testNativeSelect() {
        NativeSelectElement elem = $(NativeSelectElement.class).first();
        elem.setValue("");
    }

    @Test(expected = ReadOnlyException.class)
    public void testOptionGroup() {
        OptionGroupElement elem = $(OptionGroupElement.class).first();
        elem.setValue("");
    }

    @Test(expected = ReadOnlyException.class)
    public void testTextField() {
        TextFieldElement elem = $(TextFieldElement.class).first();
        elem.setValue("");
    }

    @Test(expected = ReadOnlyException.class)
    public void testTextArea() {
        TextAreaElement elem = $(TextAreaElement.class).first();
        elem.setValue("");
    }

    @Test(expected = ReadOnlyException.class)
    public void testPasswordField() {
        PasswordFieldElement elem = $(PasswordFieldElement.class).first();
        elem.setValue("");
    }

    @Test(expected = ReadOnlyException.class)
    public void testDateField() {
        DateFieldElement elem = $(DateFieldElement.class).first();
        elem.setValue("");
    }

    @Test(expected = ReadOnlyException.class)
    public void testCheckBox() {
        CheckBoxElement elem = $(CheckBoxElement.class).first();
        elem.click();
    }

    @Test(expected = ReadOnlyException.class)
    public void testListSelect() {
        ListSelectElement elem = $(ListSelectElement.class).first();
        elem.selectByText("foo");
    }

    @Test(expected = ReadOnlyException.class)
    public void testListSelectDeselect() {
        ListSelectElement elem = $(ListSelectElement.class).first();
        elem.deselectByText("foo");
    }

    @Test(expected = ReadOnlyException.class)
    public void testTwinColSelect() {
        TwinColSelectElement elem = $(TwinColSelectElement.class).first();
        elem.selectByText("foo");
    }

    @Test(expected = ReadOnlyException.class)
    public void testTwinColSelectDeselect() {
        TwinColSelectElement elem = $(TwinColSelectElement.class).first();
        elem.deselectByText("foo");
    }

    @Test(expected = ReadOnlyException.class)
    public void testComboBox() {
        ComboBoxElement elem = $(ComboBoxElement.class).first();
        elem.selectByText("foo");
    }
}
