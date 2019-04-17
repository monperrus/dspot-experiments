package com.annimon.stream.streamtests;


import com.annimon.stream.IntPair;
import com.annimon.stream.Stream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;


public final class ForEachIndexedTest {
    @Test
    @SuppressWarnings("unchecked")
    public void testForEachIndexed() {
        final List<IntPair<String>> result = new ArrayList<IntPair<String>>();
        Stream.of("a", "b", "c").forEachIndexed(new com.annimon.stream.function.IndexedConsumer<String>() {
            @Override
            public void accept(int index, String t) {
                result.add(new IntPair<String>(index, t));
            }
        });
        Assert.assertThat(result, Matchers.is(Arrays.asList(new IntPair<String>(0, "a"), new IntPair<String>(1, "b"), new IntPair<String>(2, "c"))));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testForEachIndexedWithStartAndStep() {
        final List<IntPair<String>> result = new ArrayList<IntPair<String>>();
        Stream.of("a", "b", "c").forEachIndexed(50, (-10), new com.annimon.stream.function.IndexedConsumer<String>() {
            @Override
            public void accept(int index, String t) {
                result.add(new IntPair<String>(index, t));
            }
        });
        Assert.assertThat(result, Matchers.is(Arrays.asList(new IntPair<String>(50, "a"), new IntPair<String>(40, "b"), new IntPair<String>(30, "c"))));
    }
}
