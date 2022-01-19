package com.backbase.ct.bbfuel.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInRelativeOrder;
import static org.junit.Assert.assertEquals;

import java.util.List;
import org.junit.Test;

public class GlobalPropertiesTest {

    @Test
    public void testGetList() {
        // given
        GlobalProperties properties = GlobalProperties.getInstance();

        // when
        List<String> actual = properties.getList("list.property");

        // then
        assertEquals(3, actual.size());
        assertThat(actual,
            containsInRelativeOrder("value1", "value2", "value3"));
    }

    @Test
    public void testGetList_SingleValue() {
        // given
        GlobalProperties properties = GlobalProperties.getInstance();

        // when
        List<String> actual = properties.getList("single.property");

        // then
        assertEquals(1, actual.size());
        assertEquals("value1", actual.get(0));
    }

    @Test
    public void testGetList_allPropertiesCombined() {
        // given
        GlobalProperties properties = GlobalProperties.getInstance();

        // when
        List<String> actual = properties.getList("list.property", true);

        // then
        assertEquals(6, actual.size());
        assertThat(actual,
            containsInRelativeOrder("value1", "value2", "value3", "value4", "value5", "value6"));
    }

    @Test
    public void testGetList_SingleValue_allPropertiesCombined() {
        // given
        GlobalProperties properties = GlobalProperties.getInstance();

        // when
        List<String> actual = properties.getList("single.property", true);

        // then
        assertEquals(2, actual.size());
        assertEquals("value1", actual.get(0));

        assertThat(actual, containsInRelativeOrder("value1", "value2"));
    }
}
