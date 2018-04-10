package com.veve.flowreader.model.impl.djvu;

import org.junit.Assert;
import org.junit.Test;
import org.opencv.core.Rect;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class DjvuBookPageTest {

    @Test
    public void sortRegions() {

        PageRegion region1 = new PageRegion(new Rect(1,1, 2, 1));
        PageRegion region2 = new PageRegion(new Rect(4,1, 2, 2));
        PageRegion region3 = new PageRegion(new Rect(1,4, 2, 2));
        PageRegion region4 = new PageRegion(new Rect(4,4, 2, 1));

        List<PageRegion> regions = new ArrayList<PageRegion>() {{
            add(region4);
            add(region3);
            add(region2);
            add(region1);
        }};

        regions = DjvuBookPage.sortRegions(regions);
        Assert.assertEquals(region1, regions.get(0));
        Assert.assertEquals(region2, regions.get(1));
        Assert.assertEquals(region3, regions.get(2));
        Assert.assertEquals(region4, regions.get(3));

    }
}