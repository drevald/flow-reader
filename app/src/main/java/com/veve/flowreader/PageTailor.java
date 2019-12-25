package com.veve.flowreader;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.Log;

import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageRenderer;
import com.veve.flowreader.model.impl.DevicePageContextImpl;
import com.veve.flowreader.views.PrintActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;

public class PageTailor {

    PageRenderer pageRenderer;
    PageGetterTask pageGetterTask;

    List<PagesSet> pages;
    int width;
    int height;
    Bitmap bitmap;
    Stack<Integer> singlePages;
    Bitmap bitmapSuffix;
    Bitmap tailoredBitmap;
    Bitmap cutBitmap;
    Stack<Bitmap> bitmapBuffer;
    Canvas canvas;

    public PageTailor(PageRenderer pageRenderer, List<PagesSet> pages, int width, int height) {
        this.pages = pages;
        this.width = width;
        this.height = height;
        this.pageRenderer = pageRenderer;
        bitmapBuffer = new Stack<Bitmap>();
        initPages();
    }

    public Bitmap read() {

        Bitmap result = null;

        while (tailoredBitmap == null || tailoredBitmap.getHeight() <= height) {
            Bitmap nextBitmap = getNextBitmap();
            if (tailoredBitmap == null && nextBitmap == null) {
                return null;
            } else if (tailoredBitmap == null) {
                tailoredBitmap = nextBitmap;
            } else if (nextBitmap == null) {
                result = Bitmap.createBitmap(tailoredBitmap);
                tailoredBitmap = null;
                return result;
            } else {
                Bitmap concatenatedBitmap = Bitmap.createBitmap(width, tailoredBitmap.getHeight() + nextBitmap.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(concatenatedBitmap);
                canvas.drawBitmap(tailoredBitmap, 0, 0, null);
                canvas.drawBitmap(nextBitmap, 0, tailoredBitmap.getHeight(), null);
                tailoredBitmap = concatenatedBitmap;
            }
        }

        result = Bitmap.createBitmap(tailoredBitmap, 0, tailoredBitmap.getHeight() - height, width, height);
        tailoredBitmap = Bitmap.createBitmap(tailoredBitmap, 0, 0, width, tailoredBitmap.getHeight() - height);

        return result;
    }

    private Bitmap getNextBitmap() {
        if (bitmapBuffer.isEmpty()) {
            if(!singlePages.isEmpty()) {
                int pageNum = singlePages.pop();
                pageGetterTask = new PageGetterTask();
                pageGetterTask.execute(pageNum, width);
                try {
                    bitmapBuffer.addAll(pageGetterTask.get());
                } catch (Exception e) {
                    Log.e(getClass().getName(), e.getLocalizedMessage());
                    return null;
                }
            }
        }

        return bitmapBuffer.isEmpty() ? null : bitmapBuffer.pop();

    }

    private void initPages() {
        SortedSet<Integer> sortedPages = new TreeSet<Integer>();
        for (PagesSet pagesSet : pages) {
            for (int i=pagesSet.getStart(); i<=pagesSet.getEnd(); i++) {
                sortedPages.add(i);
            }
        }
        singlePages = new Stack<Integer>();
        for(Integer page : sortedPages) {
            singlePages.push(page);
        }
    }

    class PageGetterTask extends AsyncTask<Integer, Void, List<Bitmap>> {

        @Override
        protected List<Bitmap> doInBackground(Integer... integers) {
            DevicePageContext context = new DevicePageContextImpl(integers[1]);
            return pageRenderer.renderPage(new DevicePageContextImpl(width), integers[1]);
        }

    }

}
