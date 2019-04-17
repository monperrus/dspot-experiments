package edu.umd.cs.findbugs.ba.npe;


import edu.umd.cs.findbugs.ba.interproc.ParameterProperty;
import org.junit.Assert;
import org.junit.Test;


public class NonNullParamPropertyTest {
    ParameterProperty empty;

    ParameterProperty nonEmpty;

    ParameterProperty extremes;

    @Test
    public void testEmpty() {
        for (int i = 0; i < 32; ++i) {
            Assert.assertFalse(empty.hasProperty(i));
        }
    }

    @Test
    public void testIsEmpty() {
        Assert.assertTrue(empty.isEmpty());
        Assert.assertFalse(nonEmpty.isEmpty());
        Assert.assertFalse(extremes.isEmpty());
    }

    @Test
    public void testNonEmpty() {
        Assert.assertTrue(nonEmpty.hasProperty(11));
        Assert.assertTrue(nonEmpty.hasProperty(25));
        Assert.assertFalse(nonEmpty.hasProperty(5));
    }

    @Test
    public void testExtremes() {
        Assert.assertTrue(extremes.hasProperty(0));
        Assert.assertTrue(extremes.hasProperty(31));
        Assert.assertFalse(extremes.hasProperty(10));
    }

    @Test
    public void testOutOfBounds() {
        Assert.assertFalse(nonEmpty.hasProperty((-1)));
        Assert.assertFalse(nonEmpty.hasProperty(32));
    }
}
