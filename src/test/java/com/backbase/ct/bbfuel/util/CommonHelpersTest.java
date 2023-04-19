package com.backbase.ct.bbfuel.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CommonHelpersTest {

    @Test
    public void testCreateValidRtn() {
        assertEquals("000000000", CommonHelpers.createValidRtn(0));
        assertEquals("000000990", CommonHelpers.createValidRtn(99));
        assertEquals("123456780", CommonHelpers.createValidRtn(12345678));
        assertEquals("123123123", CommonHelpers.createValidRtn(12312312));
        assertEquals("111111118", CommonHelpers.createValidRtn(11111111));
        assertEquals("474836473", CommonHelpers.createValidRtn(Integer.MAX_VALUE));
    }

    @Test
    public void testCreateRandomValidRtn() {
        assertEquals(9, CommonHelpers.createRandomValidRtn().length());
    }
}