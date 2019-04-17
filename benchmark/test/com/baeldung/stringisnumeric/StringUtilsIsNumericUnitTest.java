package com.baeldung.stringisnumeric;


import org.apache.commons.lang3.StringUtils;
import org.junit.Test;


public class StringUtilsIsNumericUnitTest {
    @Test
    public void givenApacheCommons_whenUsingIsNumeric_thenTrue() {
        // Valid Numbers
        assertThat(StringUtils.isNumeric("123")).isTrue();
        assertThat(StringUtils.isNumeric("???")).isTrue();
        assertThat(StringUtils.isNumeric("???")).isTrue();
        // Invalid Numbers
        assertThat(StringUtils.isNumeric(null)).isFalse();
        assertThat(StringUtils.isNumeric("")).isFalse();
        assertThat(StringUtils.isNumeric("  ")).isFalse();
        assertThat(StringUtils.isNumeric("12 3")).isFalse();
        assertThat(StringUtils.isNumeric("ab2c")).isFalse();
        assertThat(StringUtils.isNumeric("12.3")).isFalse();
        assertThat(StringUtils.isNumeric("-123")).isFalse();
    }
}
