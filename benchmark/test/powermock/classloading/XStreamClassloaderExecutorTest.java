/**
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package powermock.classloading;


import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import org.junit.Assert;
import org.junit.Test;
import org.powermock.core.classloader.MockClassLoader;
import org.powermock.core.classloader.javassist.JavassistMockClassLoader;
import powermock.classloading.classes.MyArgument;
import powermock.classloading.classes.MyClass;
import powermock.classloading.classes.MyCollectionHolder;
import powermock.classloading.classes.MyEnum;
import powermock.classloading.classes.MyEnumHolder;
import powermock.classloading.classes.MyHierarchicalFieldHolder;
import powermock.classloading.classes.MyHierarchicalOverloadedFieldHolder;
import powermock.classloading.classes.MyIntegerHolder;
import powermock.classloading.classes.MyPrimitiveArrayHolder;
import powermock.classloading.classes.MyReferenceFieldHolder;
import powermock.classloading.classes.MyReturnValue;
import powermock.classloading.classes.MyStaticFinalArgumentHolder;
import powermock.classloading.classes.MyStaticFinalNumberHolder;
import powermock.classloading.classes.MyStaticFinalPrimitiveHolder;
import powermock.classloading.classes.ReflectionMethodInvoker;


public class XStreamClassloaderExecutorTest {
    @Test
    public void loadsObjectGraphInSpecifiedClassloaderAndReturnsResultInOriginalClassloader() throws Exception {
        MockClassLoader classloader = createClassloader();
        final MyReturnValue expectedConstructorValue = new MyReturnValue(new MyArgument("first value"));
        final MyClass myClass = new MyClass(expectedConstructorValue);
        final MyArgument expected = new MyArgument("A value");
        MyReturnValue[] actual = new org.powermock.classloading.SingleClassloaderExecutor(classloader).execute(new Callable<MyReturnValue[]>() {
            public MyReturnValue[] call() throws Exception {
                Assert.assertEquals(JavassistMockClassLoader.class.getName(), this.getClass().getClassLoader().getClass().getName());
                return myClass.myMethod(expected);
            }
        });
        Assert.assertFalse(JavassistMockClassLoader.class.getName().equals(this.getClass().getClassLoader().getClass().getName()));
        final MyReturnValue myReturnValue = actual[0];
        Assert.assertEquals(expectedConstructorValue.getMyArgument().getValue(), myReturnValue.getMyArgument().getValue());
        Assert.assertEquals(expected.getValue(), actual[1].getMyArgument().getValue());
    }

    @Test
    public void loadsObjectGraphThatIncludesPrimitiveValuesInSpecifiedClassloaderAndReturnsResultInOriginalClassloader() throws Exception {
        MockClassLoader classloader = createClassloader();
        final Integer expected = 42;
        final MyIntegerHolder myClass = new MyIntegerHolder(expected);
        Integer actual = new org.powermock.classloading.SingleClassloaderExecutor(classloader).execute(new Callable<Integer>() {
            public Integer call() throws Exception {
                Assert.assertEquals(JavassistMockClassLoader.class.getName(), this.getClass().getClassLoader().getClass().getName());
                final int myInteger = myClass.getMyInteger();
                Assert.assertEquals(((int) (expected)), myInteger);
                return myInteger;
            }
        });
        Assert.assertFalse(JavassistMockClassLoader.class.getName().equals(this.getClass().getClassLoader().getClass().getName()));
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void loadsObjectGraphThatIncludesEnumsInSpecifiedClassloaderAndReturnsResultInOriginalClassloader() throws Exception {
        MockClassLoader classloader = createClassloader();
        final MyEnum expected = MyEnum.MyEnum1;
        final MyEnumHolder myClass = new MyEnumHolder(expected);
        MyEnum actual = new org.powermock.classloading.SingleClassloaderExecutor(classloader).execute(new Callable<MyEnum>() {
            public MyEnum call() throws Exception {
                Assert.assertEquals(JavassistMockClassLoader.class.getName(), this.getClass().getClassLoader().getClass().getName());
                MyEnum myEnum = myClass.getMyEnum();
                Assert.assertEquals(expected, myEnum);
                return myEnum;
            }
        });
        Assert.assertFalse(JavassistMockClassLoader.class.getName().equals(this.getClass().getClassLoader().getClass().getName()));
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void clonesStaticFinalObjectFields() throws Exception {
        MockClassLoader classloader = createClassloader();
        final MyStaticFinalArgumentHolder expected = new MyStaticFinalArgumentHolder();
        MyStaticFinalArgumentHolder actual = new org.powermock.classloading.SingleClassloaderExecutor(classloader).execute(new Callable<MyStaticFinalArgumentHolder>() {
            public MyStaticFinalArgumentHolder call() throws Exception {
                Assert.assertEquals(JavassistMockClassLoader.class.getName(), this.getClass().getClassLoader().getClass().getName());
                MyStaticFinalArgumentHolder actual = new MyStaticFinalArgumentHolder();
                Assert.assertEquals(expected.getMyObject(), actual.getMyObject());
                return actual;
            }
        });
        Assert.assertFalse(JavassistMockClassLoader.class.getName().equals(this.getClass().getClassLoader().getClass().getName()));
        Assert.assertEquals(expected.getMyObject(), actual.getMyObject());
    }

    @Test
    public void clonesStaticFinalPrimitiveFields() throws Exception {
        MockClassLoader classloader = createClassloader();
        final MyStaticFinalPrimitiveHolder expected = new MyStaticFinalPrimitiveHolder();
        MyStaticFinalPrimitiveHolder actual = new org.powermock.classloading.SingleClassloaderExecutor(classloader).execute(new Callable<MyStaticFinalPrimitiveHolder>() {
            public MyStaticFinalPrimitiveHolder call() throws Exception {
                Assert.assertEquals(JavassistMockClassLoader.class.getName(), this.getClass().getClassLoader().getClass().getName());
                MyStaticFinalPrimitiveHolder actual = new MyStaticFinalPrimitiveHolder();
                Assert.assertEquals(expected.getMyInt(), actual.getMyInt());
                return actual;
            }
        });
        Assert.assertFalse(JavassistMockClassLoader.class.getName().equals(this.getClass().getClassLoader().getClass().getName()));
        Assert.assertEquals(expected.getMyInt(), actual.getMyInt());
    }

    @Test
    public void clonesStaticFinalNumberFields() throws Exception {
        MockClassLoader classloader = createClassloader();
        final MyStaticFinalNumberHolder expected = new MyStaticFinalNumberHolder();
        MyStaticFinalNumberHolder actual = new org.powermock.classloading.SingleClassloaderExecutor(classloader).execute(new Callable<MyStaticFinalNumberHolder>() {
            public MyStaticFinalNumberHolder call() throws Exception {
                Assert.assertEquals(JavassistMockClassLoader.class.getName(), this.getClass().getClassLoader().getClass().getName());
                MyStaticFinalNumberHolder actual = new MyStaticFinalNumberHolder();
                Assert.assertEquals(expected.getMyLong(), actual.getMyLong());
                return actual;
            }
        });
        Assert.assertFalse(JavassistMockClassLoader.class.getName().equals(this.getClass().getClassLoader().getClass().getName()));
        Assert.assertEquals(expected.getMyLong(), actual.getMyLong());
    }

    @Test
    public void loadsObjectGraphThatIncludesPrimitiveArraysInSpecifiedClassloaderAndReturnsResultInOriginalClassloader() throws Exception {
        MockClassLoader classloader = createClassloader();
        final int[] expected = new int[]{ 1, 2 };
        final MyPrimitiveArrayHolder myClass = new MyPrimitiveArrayHolder(expected);
        int[] actual = new org.powermock.classloading.SingleClassloaderExecutor(classloader).execute(new Callable<int[]>() {
            public int[] call() throws Exception {
                Assert.assertEquals(JavassistMockClassLoader.class.getName(), this.getClass().getClassLoader().getClass().getName());
                int[] myArray = myClass.getMyArray();
                Assert.assertArrayEquals(expected, myArray);
                return myArray;
            }
        });
        Assert.assertFalse(JavassistMockClassLoader.class.getName().equals(this.getClass().getClassLoader().getClass().getName()));
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void loadsObjectGraphThatIncludesCollectionInSpecifiedClassloaderAndReturnsResultInOriginalClassloader() throws Exception {
        final MockClassLoader classloader = createClassloader();
        final Collection<MyReturnValue> expected = new LinkedList<MyReturnValue>();
        expected.add(new MyReturnValue(new MyArgument("one")));
        expected.add(new MyReturnValue(new MyArgument("two")));
        final MyCollectionHolder myClass = new MyCollectionHolder(expected);
        Collection<?> actual = new org.powermock.classloading.SingleClassloaderExecutor(classloader).execute(new Callable<Collection<?>>() {
            public Collection<?> call() throws Exception {
                Assert.assertEquals(JavassistMockClassLoader.class.getName(), this.getClass().getClassLoader().getClass().getName());
                Collection<?> myCollection = myClass.getMyCollection();
                for (Object object : myCollection) {
                    Assert.assertEquals(JavassistMockClassLoader.class.getName(), object.getClass().getClassLoader().getClass().getName());
                }
                return myCollection;
            }
        });
        Assert.assertFalse(JavassistMockClassLoader.class.getName().equals(this.getClass().getClassLoader().getClass().getName()));
        Assert.assertEquals(2, actual.size());
        for (Object object : actual) {
            final String value = ((MyReturnValue) (object)).getMyArgument().getValue();
            Assert.assertTrue(((value.equals("one")) || (value.equals("two"))));
        }
    }

    @Test
    public void usesReferenceCloningWhenTwoFieldsPointToSameInstance() throws Exception {
        final MockClassLoader classloader = createClassloader();
        final MyReferenceFieldHolder tested = new MyReferenceFieldHolder();
        Assert.assertSame(tested.getMyArgument1(), tested.getMyArgument2());
        Assert.assertSame(tested.getMyArgument1(), MyReferenceFieldHolder.MY_ARGUMENT);
        new org.powermock.classloading.SingleClassloaderExecutor(classloader).execute(new Runnable() {
            public void run() {
                Assert.assertEquals(JavassistMockClassLoader.class.getName(), this.getClass().getClassLoader().getClass().getName());
                Assert.assertEquals(tested.getMyArgument1(), tested.getMyArgument2());
                Assert.assertEquals(tested.getMyArgument1(), MyReferenceFieldHolder.MY_ARGUMENT);
                Assert.assertSame(tested.getMyArgument1(), tested.getMyArgument2());
                // FIXME: This assertion should work:
                // assertSame(tested.getMyArgument1(), MyReferenceFieldHolder.MY_ARGUMENT);
            }
        });
    }

    @Test
    public void worksWithObjectHierarchy() throws Exception {
        final MockClassLoader classloader = createClassloader();
        final MyHierarchicalFieldHolder tested = new MyHierarchicalFieldHolder();
        Assert.assertSame(tested.getMyArgument1(), tested.getMyArgument2());
        Assert.assertEquals(tested.getMyArgument3(), tested.getMyArgument2());
        new org.powermock.classloading.SingleClassloaderExecutor(classloader).execute(new Runnable() {
            public void run() {
                Assert.assertEquals(JavassistMockClassLoader.class.getName(), this.getClass().getClassLoader().getClass().getName());
                Assert.assertSame(tested.getMyArgument1(), tested.getMyArgument2());
                Assert.assertEquals(tested.getMyArgument3(), tested.getMyArgument2());
            }
        });
    }

    @Test
    public void worksWithObjectHierarchyAndOverloadedFields() throws Exception {
        final MockClassLoader classloader = createClassloader();
        final MyHierarchicalOverloadedFieldHolder tested = new MyHierarchicalOverloadedFieldHolder();
        Assert.assertSame(tested.getMyArgument1(), tested.getMyArgument2());
        Assert.assertEquals(tested.getMyArgument1(), tested.getMyArgument3());
        Assert.assertSame(tested.getMyArgument3(), MyHierarchicalOverloadedFieldHolder.MY_ARGUMENT);
        Assert.assertNotSame(MyReferenceFieldHolder.MY_ARGUMENT, MyHierarchicalOverloadedFieldHolder.MY_ARGUMENT);
        Assert.assertEquals(MyReferenceFieldHolder.MY_ARGUMENT, MyHierarchicalOverloadedFieldHolder.MY_ARGUMENT);
        new org.powermock.classloading.SingleClassloaderExecutor(classloader).execute(new Runnable() {
            public void run() {
                Assert.assertEquals(JavassistMockClassLoader.class.getName(), this.getClass().getClassLoader().getClass().getName());
                Assert.assertSame(tested.getMyArgument1(), tested.getMyArgument2());
                Assert.assertEquals(tested.getMyArgument1(), tested.getMyArgument3());
                // Note: Cannot be same using X-Stream
                Assert.assertEquals(tested.getMyArgument3(), MyHierarchicalOverloadedFieldHolder.MY_ARGUMENT);
                Assert.assertNotSame(MyReferenceFieldHolder.MY_ARGUMENT, MyHierarchicalOverloadedFieldHolder.MY_ARGUMENT);
                Assert.assertEquals(MyReferenceFieldHolder.MY_ARGUMENT, MyHierarchicalOverloadedFieldHolder.MY_ARGUMENT);
            }
        });
    }

    @Test
    public void worksWithReflection() throws Exception {
        final MockClassLoader classloader = createClassloader();
        final MyArgument myArgument = new MyArgument("test");
        final MyReturnValue instance = new MyReturnValue(myArgument);
        Method method = instance.getClass().getMethod("getMyArgument");
        final ReflectionMethodInvoker tested = new ReflectionMethodInvoker(method, instance);
        execute(new Runnable() {
            public void run() {
                Object invoke = tested.invoke();
                Assert.assertSame(invoke, myArgument);
            }
        });
    }
}
