package com.backbase.ct.bbfuel.input;

import com.backbase.dbs.pocket.tailor.client.v2.model.PocketPostRequest;
import java.util.List;
import junit.framework.TestCase;

public class PocketsReaderTest extends TestCase {

    private PocketsReader pocketsReader;

    public void setUp() throws Exception {
        super.setUp();
        pocketsReader = new PocketsReader();
    }

    public void testLoad() {
        List<PocketPostRequest> list = pocketsReader.load();
        assertEquals(2, list.size());
        assertEquals("Asian kitchen knives", list.get(1).getName());
        assertEquals("2021-06-01", list.get(1).getGoal().getDeadline().toString());
    }
}