package com.veve.flowreader.model.impl.djvu;

import android.util.Log;

import com.artifex.mupdf.fitz.Page;

import org.opencv.core.Rect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                    return Double.compare(r1.getRect().y,
                            r2.getRect().y);
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

        return insertSpaces(sortedRegions);
        //return sortedRegions;


    }

    private static List<PageRegion> insertSpaces(List<PageRegion> sortedRegions) {
        List<PageRegion> list = new ArrayList<>();
        if (sortedRegions.size() > 1) {
            for (int i=0;i<sortedRegions.size()-1;i++){

                PageRegion pageRegion = sortedRegions.get(i);

                list.add(pageRegion);
                PageRegion l = sortedRegions.get(i);
                PageRegion r = sortedRegions.get(i+1);
                int diffx = r.getRect().x - (l.getRect().x + l.getRect().width);
                int diffy = r.getRect().y + r.getRect().height  - l.getRect().y;

                if (diffx > 0 && diffy > 0) {
                    Rect rg = new Rect();
                    rg.x = l.getRect().x + l.getRect().width;
                    rg.y = l.getRect().y;
                    rg.width = diffx;
                    rg.height = r.getRect().y + r.getRect().height - l.getRect().y;
                    PageRegion pr = new PageRegion(rg);
                    list.add(pr);
                }
            }
            list.add(sortedRegions.get(sortedRegions.size()-1));

        } else {
            list = sortedRegions;
        }
        return list;
    }
}
