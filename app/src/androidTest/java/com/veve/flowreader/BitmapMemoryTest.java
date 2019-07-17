package com.veve.flowreader;

import android.graphics.Bitmap;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class BitmapMemoryTest {

    @Test
    public void testMemory() {
        Bitmap.Config bitmapConfig = Bitmap.Config.ARGB_8888;
        int width = 1000;
        Bitmap bm;
        for (int height = 1000; height < 100000; height += 1000) {
            Log.d(getClass().getName(), String.format("Building bitimaз %d x %d", width, height));
            bm = Bitmap.createBitmap(width, height, bitmapConfig);
            bm = null;
            System.gc();
        }
    }

}
