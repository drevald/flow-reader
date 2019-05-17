package com.veve.flowreader;

import android.graphics.Bitmap;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Arrays;

@RunWith(AndroidJUnit4.class)
public class ParserTest {

    @Test
    public void testPageSegmentor() {
        int width = 100;
        int height = 100;
        int bytesperpixel = 4;
        ByteBuffer buffer = ByteBuffer.allocate(width * height * bytesperpixel);
        byte[] data = buffer.array();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int index = 4 * (i + (j * width));
                byte color = (byte) 255;
                byte opacity = (byte) 255;
                if (i > 3 && i < 6 && j > 3 && j < 6) {
                    color = (byte)0;
                }
                buffer.put(index + 0, (byte) color);
                buffer.put(index + 1, (byte) color);
                buffer.put(index + 2, (byte) color);
                buffer.put(index + 3, (byte) opacity);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
    }

}
