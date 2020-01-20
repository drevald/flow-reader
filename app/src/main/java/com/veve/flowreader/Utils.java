package com.veve.flowreader;

import android.graphics.BitmapFactory;

import java.io.InputStream;
import java.io.OutputStream;

public class Utils {

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public synchronized static void copy(InputStream is, OutputStream os) throws Exception {
        byte[] buffer = new byte[100];
        while (is.read(buffer) != -1) {
            os.write(buffer);
        }
        os.close();
        is.close();
    }


}