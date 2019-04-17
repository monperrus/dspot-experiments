package com.annimon.stream.intstreamtests;


import com.annimon.stream.IntStream;
import com.annimon.stream.function.IntSupplier;
import org.junit.Assert;
import org.junit.Test;


public final class CountTest {
    @Test
    public void testCount() {
        Assert.assertEquals(IntStream.empty().count(), 0);
        Assert.assertEquals(IntStream.of(42).count(), 1);
        Assert.assertEquals(IntStream.range(1, 7).count(), 6);
        Assert.assertEquals(IntStream.generate(new IntSupplier() {
            @Override
            public int getAsInt() {
                return 1;
            }
        }).limit(10).count(), 10);
        Assert.assertEquals(IntStream.rangeClosed(1, 7).skip(3).count(), 4);
    }
}
