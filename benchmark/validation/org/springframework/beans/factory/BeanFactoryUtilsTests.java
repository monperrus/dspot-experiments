/**
 * Copyright 2002-2017 the original author or authors.
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
package org.springframework.beans.factory;


import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.cglib.proxy.NoOp;
import org.springframework.core.io.Resource;
import org.springframework.tests.sample.beans.AnnotatedBean;
import org.springframework.tests.sample.beans.ITestBean;
import org.springframework.tests.sample.beans.IndexedTestBean;
import org.springframework.tests.sample.beans.TestAnnotation;
import org.springframework.tests.sample.beans.TestBean;
import org.springframework.tests.sample.beans.factory.DummyFactory;
import org.springframework.util.ObjectUtils;


/**
 *
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 04.07.2003
 */
public class BeanFactoryUtilsTests {
    private static final Class<?> CLASS = BeanFactoryUtilsTests.class;

    private static final Resource ROOT_CONTEXT = qualifiedResource(BeanFactoryUtilsTests.CLASS, "root.xml");

    private static final Resource MIDDLE_CONTEXT = qualifiedResource(BeanFactoryUtilsTests.CLASS, "middle.xml");

    private static final Resource LEAF_CONTEXT = qualifiedResource(BeanFactoryUtilsTests.CLASS, "leaf.xml");

    private static final Resource DEPENDENT_BEANS_CONTEXT = qualifiedResource(BeanFactoryUtilsTests.CLASS, "dependentBeans.xml");

    private DefaultListableBeanFactory listableBeanFactory;

    private DefaultListableBeanFactory dependentBeansFactory;

    @Test
    public void testHierarchicalCountBeansWithNonHierarchicalFactory() {
        StaticListableBeanFactory lbf = new StaticListableBeanFactory();
        lbf.addBean("t1", new TestBean());
        lbf.addBean("t2", new TestBean());
        Assert.assertTrue(((BeanFactoryUtils.countBeansIncludingAncestors(lbf)) == 2));
    }

    /**
     * Check that override doesn't count as two separate beans.
     */
    @Test
    public void testHierarchicalCountBeansWithOverride() throws Exception {
        // Leaf count
        Assert.assertTrue(((this.listableBeanFactory.getBeanDefinitionCount()) == 1));
        // Count minus duplicate
        Assert.assertTrue(("Should count 8 beans, not " + (BeanFactoryUtils.countBeansIncludingAncestors(this.listableBeanFactory))), ((BeanFactoryUtils.countBeansIncludingAncestors(this.listableBeanFactory)) == 8));
    }

    @Test
    public void testHierarchicalNamesWithNoMatch() throws Exception {
        List<String> names = Arrays.asList(BeanFactoryUtils.beanNamesForTypeIncludingAncestors(this.listableBeanFactory, NoOp.class));
        Assert.assertEquals(0, names.size());
    }

    @Test
    public void testHierarchicalNamesWithMatchOnlyInRoot() throws Exception {
        List<String> names = Arrays.asList(BeanFactoryUtils.beanNamesForTypeIncludingAncestors(this.listableBeanFactory, IndexedTestBean.class));
        Assert.assertEquals(1, names.size());
        Assert.assertTrue(names.contains("indexedBean"));
        // Distinguish from default ListableBeanFactory behavior
        Assert.assertTrue(((listableBeanFactory.getBeanNamesForType(IndexedTestBean.class).length) == 0));
    }

    @Test
    public void testGetBeanNamesForTypeWithOverride() throws Exception {
        List<String> names = Arrays.asList(BeanFactoryUtils.beanNamesForTypeIncludingAncestors(this.listableBeanFactory, ITestBean.class));
        // includes 2 TestBeans from FactoryBeans (DummyFactory definitions)
        Assert.assertEquals(4, names.size());
        Assert.assertTrue(names.contains("test"));
        Assert.assertTrue(names.contains("test3"));
        Assert.assertTrue(names.contains("testFactory1"));
        Assert.assertTrue(names.contains("testFactory2"));
    }

    @Test
    public void testNoBeansOfType() {
        StaticListableBeanFactory lbf = new StaticListableBeanFactory();
        lbf.addBean("foo", new Object());
        Map<String, ?> beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(lbf, ITestBean.class, true, false);
        Assert.assertTrue(beans.isEmpty());
    }

    @Test
    public void testFindsBeansOfTypeWithStaticFactory() {
        StaticListableBeanFactory lbf = new StaticListableBeanFactory();
        TestBean t1 = new TestBean();
        TestBean t2 = new TestBean();
        DummyFactory t3 = new DummyFactory();
        DummyFactory t4 = new DummyFactory();
        t4.setSingleton(false);
        lbf.addBean("t1", t1);
        lbf.addBean("t2", t2);
        lbf.addBean("t3", t3);
        lbf.addBean("t4", t4);
        Map<String, ?> beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(lbf, ITestBean.class, true, true);
        Assert.assertEquals(4, beans.size());
        Assert.assertEquals(t1, beans.get("t1"));
        Assert.assertEquals(t2, beans.get("t2"));
        Assert.assertEquals(t3.getObject(), beans.get("t3"));
        Assert.assertTrue(((beans.get("t4")) instanceof TestBean));
        beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(lbf, DummyFactory.class, true, true);
        Assert.assertEquals(2, beans.size());
        Assert.assertEquals(t3, beans.get("&t3"));
        Assert.assertEquals(t4, beans.get("&t4"));
        beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(lbf, FactoryBean.class, true, true);
        Assert.assertEquals(2, beans.size());
        Assert.assertEquals(t3, beans.get("&t3"));
        Assert.assertEquals(t4, beans.get("&t4"));
    }

    @Test
    public void testFindsBeansOfTypeWithDefaultFactory() {
        Object test3 = this.listableBeanFactory.getBean("test3");
        Object test = this.listableBeanFactory.getBean("test");
        TestBean t1 = new TestBean();
        TestBean t2 = new TestBean();
        DummyFactory t3 = new DummyFactory();
        DummyFactory t4 = new DummyFactory();
        t4.setSingleton(false);
        this.listableBeanFactory.registerSingleton("t1", t1);
        this.listableBeanFactory.registerSingleton("t2", t2);
        this.listableBeanFactory.registerSingleton("t3", t3);
        this.listableBeanFactory.registerSingleton("t4", t4);
        Map<String, ?> beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(this.listableBeanFactory, ITestBean.class, true, false);
        Assert.assertEquals(6, beans.size());
        Assert.assertEquals(test3, beans.get("test3"));
        Assert.assertEquals(test, beans.get("test"));
        Assert.assertEquals(t1, beans.get("t1"));
        Assert.assertEquals(t2, beans.get("t2"));
        Assert.assertEquals(t3.getObject(), beans.get("t3"));
        Assert.assertTrue(((beans.get("t4")) instanceof TestBean));
        // t3 and t4 are found here as of Spring 2.0, since they are pre-registered
        // singleton instances, while testFactory1 and testFactory are *not* found
        // because they are FactoryBean definitions that haven't been initialized yet.
        beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(this.listableBeanFactory, ITestBean.class, false, true);
        Object testFactory1 = this.listableBeanFactory.getBean("testFactory1");
        Assert.assertEquals(5, beans.size());
        Assert.assertEquals(test, beans.get("test"));
        Assert.assertEquals(testFactory1, beans.get("testFactory1"));
        Assert.assertEquals(t1, beans.get("t1"));
        Assert.assertEquals(t2, beans.get("t2"));
        Assert.assertEquals(t3.getObject(), beans.get("t3"));
        beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(this.listableBeanFactory, ITestBean.class, true, true);
        Assert.assertEquals(8, beans.size());
        Assert.assertEquals(test3, beans.get("test3"));
        Assert.assertEquals(test, beans.get("test"));
        Assert.assertEquals(testFactory1, beans.get("testFactory1"));
        Assert.assertTrue(((beans.get("testFactory2")) instanceof TestBean));
        Assert.assertEquals(t1, beans.get("t1"));
        Assert.assertEquals(t2, beans.get("t2"));
        Assert.assertEquals(t3.getObject(), beans.get("t3"));
        Assert.assertTrue(((beans.get("t4")) instanceof TestBean));
        beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(this.listableBeanFactory, DummyFactory.class, true, true);
        Assert.assertEquals(4, beans.size());
        Assert.assertEquals(this.listableBeanFactory.getBean("&testFactory1"), beans.get("&testFactory1"));
        Assert.assertEquals(this.listableBeanFactory.getBean("&testFactory2"), beans.get("&testFactory2"));
        Assert.assertEquals(t3, beans.get("&t3"));
        Assert.assertEquals(t4, beans.get("&t4"));
        beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(this.listableBeanFactory, FactoryBean.class, true, true);
        Assert.assertEquals(4, beans.size());
        Assert.assertEquals(this.listableBeanFactory.getBean("&testFactory1"), beans.get("&testFactory1"));
        Assert.assertEquals(this.listableBeanFactory.getBean("&testFactory2"), beans.get("&testFactory2"));
        Assert.assertEquals(t3, beans.get("&t3"));
        Assert.assertEquals(t4, beans.get("&t4"));
    }

    @Test
    public void testHierarchicalResolutionWithOverride() throws Exception {
        Object test3 = this.listableBeanFactory.getBean("test3");
        Object test = this.listableBeanFactory.getBean("test");
        Map<String, ?> beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(this.listableBeanFactory, ITestBean.class, true, false);
        Assert.assertEquals(2, beans.size());
        Assert.assertEquals(test3, beans.get("test3"));
        Assert.assertEquals(test, beans.get("test"));
        beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(this.listableBeanFactory, ITestBean.class, false, false);
        Assert.assertEquals(1, beans.size());
        Assert.assertEquals(test, beans.get("test"));
        beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(this.listableBeanFactory, ITestBean.class, false, true);
        Object testFactory1 = this.listableBeanFactory.getBean("testFactory1");
        Assert.assertEquals(2, beans.size());
        Assert.assertEquals(test, beans.get("test"));
        Assert.assertEquals(testFactory1, beans.get("testFactory1"));
        beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(this.listableBeanFactory, ITestBean.class, true, true);
        Assert.assertEquals(4, beans.size());
        Assert.assertEquals(test3, beans.get("test3"));
        Assert.assertEquals(test, beans.get("test"));
        Assert.assertEquals(testFactory1, beans.get("testFactory1"));
        Assert.assertTrue(((beans.get("testFactory2")) instanceof TestBean));
        beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(this.listableBeanFactory, DummyFactory.class, true, true);
        Assert.assertEquals(2, beans.size());
        Assert.assertEquals(this.listableBeanFactory.getBean("&testFactory1"), beans.get("&testFactory1"));
        Assert.assertEquals(this.listableBeanFactory.getBean("&testFactory2"), beans.get("&testFactory2"));
        beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(this.listableBeanFactory, FactoryBean.class, true, true);
        Assert.assertEquals(2, beans.size());
        Assert.assertEquals(this.listableBeanFactory.getBean("&testFactory1"), beans.get("&testFactory1"));
        Assert.assertEquals(this.listableBeanFactory.getBean("&testFactory2"), beans.get("&testFactory2"));
    }

    @Test
    public void testHierarchicalNamesForAnnotationWithNoMatch() throws Exception {
        List<String> names = Arrays.asList(BeanFactoryUtils.beanNamesForAnnotationIncludingAncestors(this.listableBeanFactory, Override.class));
        Assert.assertEquals(0, names.size());
    }

    @Test
    public void testHierarchicalNamesForAnnotationWithMatchOnlyInRoot() throws Exception {
        List<String> names = Arrays.asList(BeanFactoryUtils.beanNamesForAnnotationIncludingAncestors(this.listableBeanFactory, TestAnnotation.class));
        Assert.assertEquals(1, names.size());
        Assert.assertTrue(names.contains("annotatedBean"));
        // Distinguish from default ListableBeanFactory behavior
        Assert.assertTrue(((listableBeanFactory.getBeanNamesForAnnotation(TestAnnotation.class).length) == 0));
    }

    @Test
    public void testGetBeanNamesForAnnotationWithOverride() throws Exception {
        AnnotatedBean annotatedBean = new AnnotatedBean();
        this.listableBeanFactory.registerSingleton("anotherAnnotatedBean", annotatedBean);
        List<String> names = Arrays.asList(BeanFactoryUtils.beanNamesForAnnotationIncludingAncestors(this.listableBeanFactory, TestAnnotation.class));
        Assert.assertEquals(2, names.size());
        Assert.assertTrue(names.contains("annotatedBean"));
        Assert.assertTrue(names.contains("anotherAnnotatedBean"));
    }

    @Test
    public void testADependencies() {
        String[] deps = this.dependentBeansFactory.getDependentBeans("a");
        Assert.assertTrue(ObjectUtils.isEmpty(deps));
    }

    @Test
    public void testBDependencies() {
        String[] deps = this.dependentBeansFactory.getDependentBeans("b");
        Assert.assertTrue(Arrays.equals(new String[]{ "c" }, deps));
    }

    @Test
    public void testCDependencies() {
        String[] deps = this.dependentBeansFactory.getDependentBeans("c");
        Assert.assertTrue(Arrays.equals(new String[]{ "int", "long" }, deps));
    }

    @Test
    public void testIntDependencies() {
        String[] deps = this.dependentBeansFactory.getDependentBeans("int");
        Assert.assertTrue(Arrays.equals(new String[]{ "buffer" }, deps));
    }
}
