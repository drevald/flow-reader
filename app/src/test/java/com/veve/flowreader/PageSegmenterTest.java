package com.veve.flowreader;

import android.graphics.Bitmap;

import com.veve.flowreader.model.PageGlyph;
import com.veve.flowreader.model.impl.PageSegmenter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opencv.android.Utils;
import org.opencv.core.Core;


import java.nio.ByteBuffer;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PageSegmenterTest {

    PageSegmenter pageSegmenter;

    @Before
    public void prepare() {
        pageSegmenter = new PageSegmenter();
    }

    @Test
    public void testPageSegmentor() {

        int width = 100;
        int height = 100;
        int bytesperpixel = 4;
        ByteBuffer buffer = ByteBuffer.allocate(width * height * bytesperpixel);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int index = 4 * (i + (j * width));
                byte color = (byte) 255;    //white color
                byte opacity = (byte) 255;  //opaque color
                //building 40x40 black square on white field
                if (i > 30 && i < 70 && j > 30 && j < 70) {
                    color = (byte)0;        //black color
                }
                buffer.put(index + 0, color);
                buffer.put(index + 1, color);
                buffer.put(index + 2, color);
                buffer.put(index + 3, opacity);
            }
        }

        //List<PageGlyph> glyphs = pageSegmenter.getGlyphs(width,height, buffer.array());
        //assertEquals(glyphs.size(), 1); //Only one glyph to be found

    }

}
