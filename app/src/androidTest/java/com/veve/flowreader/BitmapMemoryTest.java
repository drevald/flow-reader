package com.veve.flowreader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import android.util.Log;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class BitmapMemoryTest {

    @Test
    public void testMemory() {
        Bitmap.Config bitmapConfig = Bitmap.Config.ARGB_8888;
        int width = 1000;
        Bitmap bm;
        for (int height = 1000; height < 25000; height += 1000) {
            Log.d(getClass().getName(), String.format("Building bitimap %d x %d", width, height));
            bm = Bitmap.createBitmap(width, height, bitmapConfig);
            bm = null;
            System.gc();
        }
    }

    // please commit when test is working
    @Ignore
    public void testConversion() {

        byte[] bitmapData = {-119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 20, 0, 0, 0, 31, 8, 2, 0, 0, 0, 104, 44, 122, -103, 0, 0, 0, 4, 103, 65, 77, 65, 0, 0, -81, -56, 55, 5, -118, -23, 0, 0, 0, 25, 116, 69, 88, 116, 83, 111, 102, 116, 119, 97, 114, 101, 0, 65, 100, 111, 98, 101, 32, 73, 109, 97, 103, 101, 82, 101, 97, 100, 121, 113, -55, 101, 60, 0, 0, 2, 35, 73, 68, 65, 84, 120, -38, 98, -4, -1, -1, 63, 3, -71, 0, 32, -128, -104, 24, 40, 0, 0, 1, -60, 34, 37, 37, -59, -63, -63, 1, 100, 49, 51, 51, -77, -79, -79, 1, 25, -84, -84, -84, 16, 6, -112, -28, 0, -125, 127, -1, -2, 113, -126, 1, 80, -112, -101, -101, -101, -99, -99, 29, -56, 120, -7, -14, 37, 64, 0, 49, 2, -99, 45, 36, 36, 4, 84, 1, 119, 63, 80, -23, -73, 111, -33, -128, -116, -1, 48, 0, 17, -121, 104, -122, 112, -65, 127, -1, -34, -43, -43, 5, 16, 64, -116, 16, -114, -120, -120, 8, -60, 54, -120, -123, 15, 30, 60, 64, 115, -31, -117, 23, 47, -108, -107, -107, 5, 4, 4, -128, -22, 89, 88, 88, -42, -81, 95, 111, 108, 108, 12, 16, 64, 88, -4, -52, -56, -56, -120, 41, 40, 33, 33, 113, -6, -12, -23, 63, 127, -2, 64, 60, 8, -44, 9, 100, 0, 4, 16, 19, -36, -87, 112, -58, -113, 31, 63, -80, 6, -113, -106, -106, -42, -33, -65, 127, -127, 54, -61, 77, 7, 8, 32, -88, 102, -120, -29, -127, 36, -48, 97, -49, -97, 63, -57, 21, -68, 16, 59, -128, -50, -122, 112, 1, 2, 8, -35, -39, 64, 39, 17, -116, 33, 120, -24, 0, 4, 16, -70, -51, -8, 53, 67, 2, -97, -119, 9, -86, 11, 32, -128, -48, 53, 67, -30, 28, -65, 102, -72, -97, 1, 2, -120, -100, 20, 6, -73, 0, 32, -128, -48, 109, -122, 7, 6, 46, -101, 65, 122, 96, -50, 6, 8, 32, 114, -100, 13, 15, 23, -128, 0, 98, 66, -109, 19, 22, 22, -58, -17, 102, -96, 26, 46, 46, 46, 8, 27, 32, -128, 80, 52, 3, -77, -60, -47, -93, 71, 11, 11, 11, 9, -28, 68, -104, -77, 1, 2, -120, 9, 45, 97, -2, -4, -7, -13, -47, -93, 71, -8, -99, 13, 15, 23, -128, 0, 98, -62, -108, -26, -27, -27, -59, -86, -13, -3, -5, -9, 16, 5, -64, 92, 9, 17, 1, 8, 32, 18, -94, 10, -104, -80, -47, -100, 13, 16, 64, 40, -95, 13, 97, 72, 74, 74, 98, -43, 12, -52, -74, 16, 103, 3, 115, 24, 68, 4, 32, -128, 24, -33, -67, 123, -89, -92, -92, 4, -116, 33, 72, -70, 1, -54, 1, 83, -1, -118, 21, 43, 28, 29, 29, -47, 52, -21, -24, -24, 0, 75, 15, -96, -75, -4, -4, -4, -73, 110, -35, 2, -118, 0, 4, 16, 3, 15, 15, 15, 48, 122, -128, -90, 10, -126, 1, 80, 2, -24, 103, -96, -32, -12, -23, -45, -31, 37, 9, 48, -109, 2, -43, 0, -29, -126, 23, 12, -128, -59, 16, -112, 11, 44, 48, 0, 2, -120, -27, -13, -25, -49, 4, 125, 11, 84, 125, -31, -62, 5, -96, -47, -65, 126, -3, 2, -70, -12, -51, -101, 55, -25, -49, -97, -25, -29, -29, 3, 8, 32, 70, 74, -118, 94, -128, 0, -94, -88, -24, 5, 8, 32, -118, 52, 3, 4, 16, 69, -102, 1, 2, -120, 34, -51, 0, 1, 68, -111, 102, -128, 0, -94, 72, 51, 64, 0, 81, -92, 25, 32, -64, 0, 48, 13, -21, 122, -77, 1, -127, -106, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126};

        Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);

        // Create a byte array from ByteArrayOutputStream
        byte[] byteArray = stream.toByteArray();


        ByteBuffer byteBuffer = ByteBuffer.allocate(bitmap.getByteCount());

        try {
            bitmap.copyPixelsToBuffer(byteBuffer);
        } catch (Exception e) {
            e.printStackTrace();
        }


        //byteBuffer.rewind();
        byte[] convertedBytes = byteBuffer.array();

        assertEquals(bitmapData, byteBuffer.array());

        Bitmap convertedBitmap = BitmapFactory.decodeByteArray(convertedBytes, 0, convertedBytes.length);

        assertEquals(bitmap, convertedBitmap);

    }


}
