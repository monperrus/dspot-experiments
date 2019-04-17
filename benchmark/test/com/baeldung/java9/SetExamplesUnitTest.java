package com.baeldung.java9;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;


public class SetExamplesUnitTest {
    @Test
    public void testUnmutableSet() {
        Set<String> strKeySet = Set.of(Set, "key1", "key2", "key3");
        try {
            strKeySet.add("newKey");
        } catch (UnsupportedOperationException uoe) {
        }
        Assert.assertEquals(strKeySet.size(), 3);
    }

    @Test
    public void testArrayToSet() {
        Integer[] intArray = new Integer[]{ 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 };
        Set<Integer> intSet = Set.of(Set, intArray);
        Assert.assertEquals(intSet.size(), intArray.length);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnmodifiableSet() {
        Set<String> set = new HashSet<>();
        set.add("Canada");
        set.add("USA");
        Set<String> unmodifiableSet = Collections.unmodifiableSet(set);
        unmodifiableSet.add("Costa Rica");
    }
}
