package com.veve.flowreader.model.impl.djvu;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.widget.ImageView;

import com.veve.flowreader.R;
import com.veve.flowreader.model.BookPage;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageGlyph;

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
        Bitmap bitmap = Bitmap.createBitmap(width, height, bitmapConfig);
        int[] bitmapPixels = tranformBytes(imageBytes, width, height);
        bitmap.setPixels(bitmapPixels, 0, width, 0, 0, width, height);
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
