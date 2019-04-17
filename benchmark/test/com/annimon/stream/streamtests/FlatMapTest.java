package com.annimon.stream.streamtests;


import com.annimon.stream.Functions;
import com.annimon.stream.Stream;
import org.hamcrest.Matchers;
import org.junit.Test;


public final class FlatMapTest {
    @Test
    public void testFlatMap() {
        Stream.rangeClosed(2, 4).flatMap(new com.annimon.stream.function.Function<Integer, Stream<String>>() {
            @Override
            public Stream<String> apply(final Integer i) {
                return Stream.rangeClosed(2, 4).filter(Functions.remainder(2)).map(new com.annimon.stream.function.Function<Integer, String>() {
                    @Override
                    public String apply(Integer p) {
                        return String.format("%d * %d = %d", i, p, (i * p));
                    }
                });
            }
        }).custom(assertElements(Matchers.contains("2 * 2 = 4", "2 * 4 = 8", "3 * 2 = 6", "3 * 4 = 12", "4 * 2 = 8", "4 * 4 = 16")));
    }
}
