package com.trasier.client.utils;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;


public class PreconditionTest {

    @Test(expected = IllegalArgumentException.class)
    public void throwsOnNullArgument() {
        Precondition.notNull(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsOnBlankArgument() {
        Precondition.notBlank(" ", null);
    }

    @Test
    public void shouldReturnValidObject() {
        assertEquals("", Precondition.notNull("", "emptyField"));
        assertEquals(BigDecimal.ONE, Precondition.notNull(BigDecimal.ONE, "number"));
        assertEquals("abc", Precondition.notBlank("abc", "alphabet"));
    }

}