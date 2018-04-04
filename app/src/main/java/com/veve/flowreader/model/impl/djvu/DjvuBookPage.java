package com.veve.flowreader.model.impl.djvu;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.widget.ImageView;

import com.veve.flowreader.R;
import com.veve.flowreader.model.BookPage;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageGlyph;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by sergey on 10.03.18.
 */

public class DjvuBookPage implements BookPage {



    private int pageNumber;
    private long bookId;

    public DjvuBookPage(long  bookId, int pageNumber) {
        this.bookId = bookId;
        this.pageNumber = pageNumber;
    }

    @Override
    public PageGlyph getNextGlyph() {
        return null;
    }

    @Override
    public void reset() {

    }

    private int[] tranformBytes(byte[] imageBytes, int width, int height) {

        int[] bitmapPixels = new int[width * height];

        class PageJob implements Runnable {

            int from;
            int to;
            PageJob(int from, int to) {
                this.from = from;
                this.to = to;
            }

            @Override
            public void run() {
                for (int i = from; i < to; ++i) {
                    bitmapPixels[i] = Color.rgb(imageBytes[3*i], imageBytes[3*i+1],imageBytes[3*i+2]);
                }
            }
        }

        int cores = Runtime.getRuntime().availableProcessors();

        ExecutorService executor = Executors.newFixedThreadPool(cores);
        int size = bitmapPixels.length;

        int end = 0;
        for (int i=0;i<=cores;i++) {
            int start = end;
            end = (i*size)/cores;
            Runnable job = new PageJob(start, end);
            executor.execute(job);
        }

        executor.shutdown();

        while (!executor.isTerminated()) {
        }

        return bitmapPixels;
    }

    @Override
    public Bitmap getAsBitmap(DevicePageContext context) {

        byte[] imageBytes= getBytes(bookId, pageNumber);
        int width = getWidth();
        int height = getHeight();
        Bitmap.Config bitmapConfig = Bitmap.Config.ARGB_8888;
        Mat mat = new Mat(height, width ,CvType.CV_8UC3);
        mat.put(0,0,imageBytes, 0, imageBytes.length);
        Bitmap bitmap = Bitmap.createBitmap(width, height, bitmapConfig);
        Utils.matToBitmap(mat, bitmap);
        return bitmap;
    }

    @Override
    public int getWidth() {
        return getNativeWidth(bookId, pageNumber);
    }

    @Override
    public int getHeight() {
        return getNativeHeight(bookId, pageNumber);
    }

    private static native byte[] getBytes(long bookId, int pageNumber);

    private static native int getNativeWidth(long bookId, int pageNumber);
    private static native int getNativeHeight(long bookId, int pageNumber);
}
