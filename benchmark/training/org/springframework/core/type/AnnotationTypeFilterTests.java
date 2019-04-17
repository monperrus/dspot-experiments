/**
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.core.type;


import java.lang.annotation.Inherited;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;


/**
 *
 *
 * @author Ramnivas Laddad
 * @author Juergen Hoeller
 * @author Oliver Gierke
 */
public class AnnotationTypeFilterTests {
    @Test
    public void testDirectAnnotationMatch() throws Exception {
        MetadataReaderFactory metadataReaderFactory = new SimpleMetadataReaderFactory();
        String classUnderTest = "org.springframework.core.type.AnnotationTypeFilterTests$SomeComponent";
        MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(classUnderTest);
        AnnotationTypeFilter filter = new AnnotationTypeFilter(AnnotationTypeFilterTests.InheritedAnnotation.class);
        Assert.assertTrue(filter.match(metadataReader, metadataReaderFactory));
        ClassloadingAssertions.assertClassNotLoaded(classUnderTest);
    }

    @Test
    public void testInheritedAnnotationFromInterfaceDoesNotMatch() throws Exception {
        MetadataReaderFactory metadataReaderFactory = new SimpleMetadataReaderFactory();
        String classUnderTest = "org.springframework.core.type.AnnotationTypeFilterTests$SomeClassWithSomeComponentInterface";
        MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(classUnderTest);
        AnnotationTypeFilter filter = new AnnotationTypeFilter(AnnotationTypeFilterTests.InheritedAnnotation.class);
        // Must fail as annotation on interfaces should not be considered a match
        Assert.assertFalse(filter.match(metadataReader, metadataReaderFactory));
        ClassloadingAssertions.assertClassNotLoaded(classUnderTest);
    }

    @Test
    public void testInheritedAnnotationFromBaseClassDoesMatch() throws Exception {
        MetadataReaderFactory metadataReaderFactory = new SimpleMetadataReaderFactory();
        String classUnderTest = "org.springframework.core.type.AnnotationTypeFilterTests$SomeSubclassOfSomeComponent";
        MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(classUnderTest);
        AnnotationTypeFilter filter = new AnnotationTypeFilter(AnnotationTypeFilterTests.InheritedAnnotation.class);
        Assert.assertTrue(filter.match(metadataReader, metadataReaderFactory));
        ClassloadingAssertions.assertClassNotLoaded(classUnderTest);
    }

    @Test
    public void testNonInheritedAnnotationDoesNotMatch() throws Exception {
        MetadataReaderFactory metadataReaderFactory = new SimpleMetadataReaderFactory();
        String classUnderTest = "org.springframework.core.type.AnnotationTypeFilterTests$SomeSubclassOfSomeClassMarkedWithNonInheritedAnnotation";
        MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(classUnderTest);
        AnnotationTypeFilter filter = new AnnotationTypeFilter(AnnotationTypeFilterTests.NonInheritedAnnotation.class);
        // Must fail as annotation isn't inherited
        Assert.assertFalse(filter.match(metadataReader, metadataReaderFactory));
        ClassloadingAssertions.assertClassNotLoaded(classUnderTest);
    }

    @Test
    public void testNonAnnotatedClassDoesntMatch() throws Exception {
        MetadataReaderFactory metadataReaderFactory = new SimpleMetadataReaderFactory();
        String classUnderTest = "org.springframework.core.type.AnnotationTypeFilterTests$SomeNonCandidateClass";
        MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(classUnderTest);
        AnnotationTypeFilter filter = new AnnotationTypeFilter(Component.class);
        Assert.assertFalse(filter.match(metadataReader, metadataReaderFactory));
        ClassloadingAssertions.assertClassNotLoaded(classUnderTest);
    }

    @Test
    public void testMatchesInterfacesIfConfigured() throws Exception {
        MetadataReaderFactory metadataReaderFactory = new SimpleMetadataReaderFactory();
        String classUnderTest = "org.springframework.core.type.AnnotationTypeFilterTests$SomeClassWithSomeComponentInterface";
        MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(classUnderTest);
        AnnotationTypeFilter filter = new AnnotationTypeFilter(AnnotationTypeFilterTests.InheritedAnnotation.class, false, true);
        Assert.assertTrue(filter.match(metadataReader, metadataReaderFactory));
        ClassloadingAssertions.assertClassNotLoaded(classUnderTest);
    }

    // We must use a standalone set of types to ensure that no one else is loading them
    // and interfering with ClassloadingAssertions.assertClassNotLoaded()
    @Inherited
    private @interface InheritedAnnotation {}

    @AnnotationTypeFilterTests.InheritedAnnotation
    private static class SomeComponent {}

    @AnnotationTypeFilterTests.InheritedAnnotation
    private interface SomeComponentInterface {}

    @SuppressWarnings("unused")
    private static class SomeClassWithSomeComponentInterface implements Cloneable , AnnotationTypeFilterTests.SomeComponentInterface {}

    @SuppressWarnings("unused")
    private static class SomeSubclassOfSomeComponent extends AnnotationTypeFilterTests.SomeComponent {}

    private @interface NonInheritedAnnotation {}

    @AnnotationTypeFilterTests.NonInheritedAnnotation
    private static class SomeClassMarkedWithNonInheritedAnnotation {}

    @SuppressWarnings("unused")
    private static class SomeSubclassOfSomeClassMarkedWithNonInheritedAnnotation extends AnnotationTypeFilterTests.SomeClassMarkedWithNonInheritedAnnotation {}

    @SuppressWarnings("unused")
    private static class SomeNonCandidateClass {}
}
