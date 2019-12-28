package com.veve.flowreader;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.util.Log;

import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageRenderer;
import com.veve.flowreader.model.impl.DevicePageContextImpl;

import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;


public class PageTailor {

    PageRenderer pageRenderer;
    PageGetterTask pageGetterTask;

    List<PagesSet> pages;
    DevicePageContext context;
    int height;
    Bitmap bitmap;
    Stack<Integer> singlePages;
    Bitmap bitmapSuffix;
    Bitmap tailoredBitmap;
    Bitmap cutBitmap;
    Stack<Bitmap> bitmapBuffer;
    Canvas canvas;

    static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;

    public PageTailor(PageRenderer pageRenderer, List<PagesSet> pages, DevicePageContext context, int height) {
        this.pages = pages;
        this.context = context;
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
                Log.v(getClass().getName(), "no data buffered and nothing retrieved, returning null");
                return null;
            } else if (tailoredBitmap == null) {
                Log.v(getClass().getName(), "no data buffered, buffering retrieved");
                tailoredBitmap = nextBitmap;
            } else if (nextBitmap == null) {
                Log.v(getClass().getName(), "no data retrieved, returning buffered");
                result = Bitmap.createBitmap(tailoredBitmap);
                tailoredBitmap = null;
                return result;
            } else {
                Log.v(getClass().getName(), "appending retrieved data");
                Bitmap concatenatedBitmap = Bitmap.createBitmap(context.getWidth(), tailoredBitmap.getHeight() + nextBitmap.getHeight(), BITMAP_CONFIG);
                Canvas canvas = new Canvas(concatenatedBitmap);
                canvas.drawBitmap(tailoredBitmap, 0, 0, null);
                canvas.drawBitmap(nextBitmap, 0, tailoredBitmap.getHeight(), null);
                tailoredBitmap = concatenatedBitmap;
                Log.v(getClass().getName(), concatenatedBitmap.getHeight() + " data buffered now");
            }
        }

        result = Bitmap.createBitmap(tailoredBitmap, 0, tailoredBitmap.getHeight() - height, context.getWidth(), height);
        tailoredBitmap = Bitmap.createBitmap(tailoredBitmap, 0, 0, context.getWidth(), tailoredBitmap.getHeight() - height);

        return result;
    }

    private Bitmap getNextBitmap() {
        if (bitmapBuffer.isEmpty()) {
            if(!singlePages.isEmpty()) {
                int pageNum = singlePages.pop();
                pageGetterTask = new PageGetterTask();
                pageGetterTask.execute(context, pageNum);
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
        SortedSet<Integer> sortedPages = new TreeSet<Integer>((x, y) -> {
            return - Integer.compare(x, y);
        });
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

    class PageGetterTask extends AsyncTask<Object, Void, List<Bitmap>> {

        @Override
        protected List<Bitmap> doInBackground(Object... objects) {
            DevicePageContext context = (DevicePageContext) objects[0];
            int pageNum = (Integer)objects[1];
            Log.v(getClass().getName(), "Getting new portion of bitmaps for page " + pageNum);
            List<Bitmap> bitmaps = pageRenderer.renderPage(context, pageNum);
            Log.v(getClass().getName(), bitmaps.size() + " bitmaps retrieved for page " + pageNum);
            return bitmaps;
        }

    }

}
