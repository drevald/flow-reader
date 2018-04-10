package com.veve.flowreader.model.impl.djvu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PageUtil {

    public static List<PageRegion> sortRegions(List<PageRegion> regions) {

        List<PageRegion> sortedRegions = new ArrayList<>(regions);

        Collections.sort(sortedRegions,(PageRegion r1, PageRegion r2) -> {
            return Double.compare(r1.getRect().x + r1.getRect().width/2.0,
                    r2.getRect().x + r2.getRect().width/2.0);
        });

        PageRegion rect = sortedRegions.get(0);
        int right = rect.getRect().x + rect.getRect().width;

        int x = 0;
        for (int i=0;i<sortedRegions.size();i++) {
            rect = sortedRegions.get(i);
            if (rect.getRect().x > right) {
                x++;
                right = rect.getRect().x + rect.getRect().width;
            }
            rect.setX(x);
        }

        Collections.sort(
                sortedRegions, (PageRegion r1, PageRegion r2) -> {
                    return Double.compare(r1.getRect().y + r1.getRect().height/2.0,
                            r2.getRect().y + r2.getRect().height/2.0);
                });


        rect = sortedRegions.get(0);
        int bottom = rect.getRect().y + rect.getRect().height;
        int y = 0;

        for (int i=0;i<sortedRegions.size();i++) {
            rect = sortedRegions.get(i);
            if (rect.getRect().y > bottom) {
                y++;
                bottom = rect.getRect().y + rect.getRect().height;
            }
            rect.setY(y);
        }

        Collections.sort(sortedRegions, (PageRegion r1, PageRegion r2) -> {
            if ( r1.getY() == r2.getY() ) {
                return Integer.compare(r1.getX(), r2.getX());
            }
            return Integer.compare(r1.getY(), r2.getY());
        });

        return sortedRegions;

    }
}
