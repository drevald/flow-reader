package com.veve.flowreader;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageLayoutParser;
import com.veve.flowreader.model.PageRenderer;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class PageTailorTest {


    private DevicePageContext context;

    @Before
    public void setContext() {
        context = new DevicePageContext(50);
        context.setScreenRatio(1);
    }

    @Test
    public void testTailTinyPageIntoHugeColumn() {
        PageTailor pageTailor = new PageTailor(
                new MockPageRenderer(10, 1),
                PagesSet.getPagesSet("1-100"),
                context,
                1000);
        int counter = 0;
        Bitmap pageBitmap;
        while ((pageBitmap = pageTailor.read()) != null) {
            assertEquals(50, pageBitmap.getWidth());
            assertEquals(1000, pageBitmap.getHeight());
            counter++;
        }
        assertEquals(1, counter);
    }

    @Test
    public void testTailTallPageIntoShortColumn() {
        PageTailor pageTailor = new PageTailor(
                new MockPageRenderer(400, 1),
                PagesSet.getPagesSet("1-10"),
                context,
                200);
        int counter = 0;
        Bitmap pageBitmap;
        while ((pageBitmap = pageTailor.read()) != null) {
            assertEquals(context.getWidth(), pageBitmap.getWidth());
            assertEquals(200, pageBitmap.getHeight());
            counter++;
        }
        assertEquals(20, counter);
    }

    @Test
    public void testTailShortPageIntoTallColumn() {
        PageTailor pageTailor = new PageTailor(
                new MockPageRenderer(200, 1),
                PagesSet.getPagesSet("1-10"),
                context,
                400);
        int counter = 0;
        Bitmap pageBitmap;
        while ((pageBitmap = pageTailor.read()) != null) {
            assertEquals(context.getWidth(), pageBitmap.getWidth());
            assertEquals(400, pageBitmap.getHeight());
            counter++;
        }
        assertEquals(5, counter);
    }

    class MockPageRenderer implements PageRenderer {

        int height;

        int count;

        MockPageRenderer(int height, int count) {
            this.height = height;
            this.count = count;
        }

        @Override
        public List<Bitmap> renderPage(DevicePageContext context, int position) {
            List<Bitmap> bitmaps = new ArrayList<Bitmap>();
            for (int i=0; i<count; i++) {
                Bitmap bitmap = Bitmap.createBitmap(context.getWidth(), height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                Paint paint = new Paint();
                Random random = new Random();
                int randomInt = Math.abs(random.nextInt());
                paint.setStyle(Paint.Style.FILL);
                switch (randomInt % 3) {
                    case 0 : {
                        paint.setColor(Color.RED); break;}
                    case 1 : {
                        paint.setColor(Color.GREEN); break;}
                    case 2 : {
                        paint.setColor(Color.BLUE); break;}
                }
                canvas.drawRect(0, 0, bitmap.getWidth(), bitmap.getHeight(), paint);
                bitmaps.add(bitmap);
            }
            return bitmaps;
        }

        @Override
        public Bitmap renderOriginalPage(DevicePageContext context, int position) {return null;}

        @Override
        public Bitmap renderOriginalPage(int position) {return null;}

        @Override
        public void setPageLayoutParser(PageLayoutParser parser) {}

        @Override
        public void closeBook() {}

    }

}
