package com.trasier.client.util;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ExceptionUtilsTest {

    @Test
    public void testStackTrace() {
        RuntimeException oops = new RuntimeException("oops");

        // when
        String result = ExceptionUtils.getString(oops);

        // then
        assertTrue(result.startsWith("java.lang.RuntimeException: oops"));
    }

}