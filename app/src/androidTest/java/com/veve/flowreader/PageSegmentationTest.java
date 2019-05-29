package com.veve.flowreader;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.veve.flowreader.model.PageGlyph;
import com.veve.flowreader.model.impl.PageSegmenter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opencv.android.OpenCVLoader;

import java.nio.ByteBuffer;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class PageSegmentationTest {

    PageSegmenter pageSegmenter;
    Context appContext;

    @Before
    public void prepare() {
        pageSegmenter = new PageSegmenter();
        appContext = InstrumentationRegistry.getTargetContext();

        OpenCVLoader.initDebug();
    }

    @Test
    public void testPageSegmentor() {

        int width = 100;
        int height = 100;
        int bytesperpixel = 4;
        ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * bytesperpixel);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int index = 4 * (i + (j * width));
                byte color = (byte) 255;    //white color
                byte opacity = (byte) 255;  //opaque color
                //building two 80x30 black rectangles on white field
                if ((i > 10 && i < 40 && j > 10 && j < 90) | (i > 60 && i < 90 && j > 10 && j < 90)) {
                    color = (byte)0;        //black color
                }
                buffer.put(index + 0, color);
                buffer.put(index + 1, color);
                buffer.put(index + 2, color);
                buffer.put(index + 3, opacity);
            }
        }


        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);


        bitmap.copyPixelsFromBuffer(buffer);


        List<PageGlyph> glyphs = pageSegmenter.getGlyphs(bitmap);


        assertEquals(2, glyphs.size()); //There are two glyphs to be found

    }

}