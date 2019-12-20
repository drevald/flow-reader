package com.veve.flowreader;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class PagesSetsTest {

    @Test
    public void testParsing() {
        List<PagesSet> sets = PagesSet.getPagesSet("1-2;3-6:7-8");
        Assert.assertNotNull(sets);
    }

}
