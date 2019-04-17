package com.vaadin.server;


import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.Assert;
import org.junit.Test;


/**
 * Tests for {@link UIProvider} class.
 *
 * @author Vaadin Ltd
 */
public class UIProviderTest {
    @Test
    public void getAnnotationFor_widgetsetAnnotationForSubclass_annotationFound() {
        Assert.assertNotNull("Widgetset annotation is not found for subclass", UIProvider.getAnnotationFor(UIProviderTest.TestClass.class, Widgetset.class));
    }

    @Test
    public void getAnnotationFor_themeAnnotationForSubclass_annotationFound() {
        Assert.assertNotNull("Theme annotation is not found for subclass", UIProvider.getAnnotationFor(UIProviderTest.TestClass.class, Theme.class));
    }

    @Test
    public void getAnnotationFor_themeAnnotationForSubclass_annotationOverridden() {
        Assert.assertEquals("Theme annotation is not overridden correctly in subclass", "c", UIProvider.getAnnotationFor(UIProviderTest.TestClass.class, Theme.class).value());
    }

    @Test
    public void getAnnotationFor_notInheritedAnnotationForSubclass_annotationFound() {
        Assert.assertNotNull("TestAnnotation annotation is not found for subclass", UIProvider.getAnnotationFor(UIProviderTest.TestClass.class, UIProviderTest.TestAnnotation.class));
    }

    @Test
    public void getAnnotationFor_directAnnotationForSubclass_annotationFound() {
        Assert.assertNotNull("TestAnnotation1 annotation is not found for subclass", UIProvider.getAnnotationFor(UIProviderTest.TestClass.class, UIProviderTest.TestAnnotation1.class));
    }

    @Test
    public void getAnnotationFor_annotationInheritedFromInterface_annotationFound() {
        Assert.assertNotNull("Theme annotation is not inherited from interface", UIProvider.getAnnotationFor(UIProviderTest.ClassImplementingInterface.class, Theme.class));
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface TestAnnotation {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface TestAnnotation1 {}

    @Widgetset("a")
    @Theme("b")
    @UIProviderTest.TestAnnotation
    public static class TestSuperClass {}

    @UIProviderTest.TestAnnotation1
    @Theme("c")
    public static class TestClass extends UIProviderTest.TestSuperClass {}

    @Theme("d")
    public interface InterfaceWithAnnotation {}

    public static class ClassImplementingInterface implements UIProviderTest.InterfaceWithAnnotation {}
}
