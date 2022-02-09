package com.veve.flowreader.model.impl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import com.veve.flowreader.Utils;
import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.dao.PageGlyphRecord;
import com.veve.flowreader.model.BookSource;
import com.veve.flowreader.model.BooksCollection;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageGlyph;
import com.veve.flowreader.model.PageGlyphInfo;
import com.veve.flowreader.model.PageLayoutParser;
import com.veve.flowreader.model.PageRenderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.graphics.Bitmap.Config.ARGB_8888;


public class NativePageRendererImpl implements PageRenderer {


    private BookSource bookSource;

    private Bitmap originalBitmap;

    private int currentOriginalPage;

    private BooksCollection booksCollection;

    private BookRecord bookRecord;

    private List<PageGlyphRecord> memGlyphs;

    public NativePageRendererImpl(BooksCollection booksCollection, BookRecord bookRecord, BookSource bookSource) {
        this.bookSource = bookSource;
        this.booksCollection = booksCollection;
        this.bookRecord = bookRecord;
    }

    private List<PageGlyph> getGlyphs(BookSource bookSource, int position) {
        // if page changed or glyphs are not stored or glyphs absent - retrieve new ones
        return Collections.emptyList();
    }

    private Bitmap getOriginalPageBitmap(int position) {
        Log.v("NULLBOOK", "Getting natively the original page " + position);
        if (position != currentOriginalPage || originalBitmap == null) {
            Log.d("NULLBOOK", "Page not cached current = " + currentOriginalPage + " requested = " + position);
            currentOriginalPage = position;
            long start = System.currentTimeMillis();
            originalBitmap = bookSource.getPageBytes(position);
            Log.d(getClass().getName(), String.format("NULLBOOK Getting page #%d took #%d milliseconds",
                    position, System.currentTimeMillis() - start));
        } else {
            Log.d(getClass().getName(), "NULLBOOK Page cached current = " + currentOriginalPage + " requested = " + position);
        }
        return originalBitmap;
    }

    public List<Bitmap> getReflownPageBitmap(int position, DevicePageContext context) {

        long start = System.currentTimeMillis();
        if (context.isInvalidateCache()) {
            booksCollection.deleteGlyphs(bookRecord.getId(), position);
        }
        Log.v("PERF", String.format("\tbooksCollection.deleteGlyphs(%d, %d) took %d ms", bookRecord.getId(), position, System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        List<PageGlyphRecord> storedGlyphs;

        if (currentOriginalPage == position) {
            storedGlyphs = memGlyphs;
        } else {
            storedGlyphs = booksCollection.getPageGlyphs(bookRecord.getId(), position, false);
            memGlyphs = storedGlyphs;
            currentOriginalPage = position;
        }

        Log.v("PERF", String.format("\tbooksCollection.getPageGlyphs(%d, %d, false) took %d ms", bookRecord.getId(), position, System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        if (storedGlyphs == null || storedGlyphs.isEmpty()) {
            List<PageGlyphInfo> glyphs = new ArrayList<>();
            List<Bitmap> reflownPageBytes = bookSource.getReflownPageBytes(position, context, glyphs);
            List<PageGlyphRecord> glyphsToStore = new ArrayList<PageGlyphRecord>();
            for (PageGlyphInfo glyph : glyphs) {
                glyphsToStore.add(new PageGlyphRecord(
                        bookRecord.getId(),
                        position,
                        glyph.getX(),
                                glyph.getY(),
                        glyph.getWidth(),
                        glyph.getHeight(),
                        glyph.getBaselineShift(),
                        glyph.getAverageHeight(),
                        glyph.isIndented(),
                        glyph.isSpace(),
                        glyph.isLast()
                ));
            }
            booksCollection.addGlyphs(glyphsToStore, false);
            Log.v("PERF", String.format("\tbookSource.getReflownPageBytes(%d, ...) took %d ms", position, System.currentTimeMillis() - start));
            Log.v(getClass().getName(), String.format("new reflownPageBytes.size()=%d for page %d", reflownPageBytes.size(), position));
            return reflownPageBytes;
        } else {
           List<PageGlyphInfo> glyphs = new ArrayList<>();
           for (PageGlyphRecord record : storedGlyphs) {
               glyphs.add(new PageGlyphInfo(
                       record.isIndented(),
                       record.getX(),
                       record.getY(),
                       record.getWidth(),
                       record.getHeight(),
                       record.getAverageHeight(),
                       record.getBaselineShift(),
                       record.isSpace(),
                       record.isLast()
               ));
           }
           List<Bitmap> reflownPageBytes = bookSource.getReflownPageBytes(position, context, glyphs);
           Log.v("PERF", String.format("\tbookSource.getReflownPageBytes(%d, ...) took %d ms", position, System.currentTimeMillis() - start));
           Log.v(getClass().getName(), String.format("reflownPageBytes.size()=%d for page %d", reflownPageBytes.size(), position));
           return reflownPageBytes;
        }

    }

    @Override
    public List<Bitmap> renderPage(DevicePageContext context, int position) {

        long start = System.currentTimeMillis();
        List<Bitmap> bitmaps = getReflownPageBitmap(position, context);
        Log.v("PERF", String.format("getReflownPageBitmap(%d, context) took %d ms", position, System.currentTimeMillis() - start));

        /*
        Bitmap bmp = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), ARGB_8888);

        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);

        Rect srcRect = new Rect(0,0,bitmap.getWidth(), bitmap.getHeight());
        Rect dstRect = new Rect(0,0,bitmap.getWidth(), bitmap.getHeight());

        Paint fillPaint = new Paint();
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(Color.WHITE);

        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), fillPaint);
        canvas.drawBitmap(bitmap, srcRect,dstRect, paint);
         */
        List<Bitmap> retVal = new ArrayList<>();
        Bitmap bm;
        for (Bitmap b : bitmaps) {
            bm = Bitmap.createScaledBitmap(b, (context.getWidth()),
                    ((context.getWidth() * b.getHeight())/b.getWidth()),
                    false);
            Utils.storeBitmap(b, "before_at_" + System.currentTimeMillis());
            Utils.storeBitmap(bm, "after_at_" + System.currentTimeMillis());
            if (!b.isRecycled()) {
                b.recycle();
            }

            retVal.add(bm);
        }
        return  retVal;
    }

    @Override
    public Bitmap renderOriginalPage(DevicePageContext context, int position) {
        Bitmap bitmap = getOriginalPageBitmap(position);
        return Bitmap.createScaledBitmap(bitmap,
                (int)(context.getZoomOriginal()*context.getWidth()),
                (int)(context.getZoomOriginal()*(context.getWidth()
                        * bitmap.getHeight())/bitmap.getWidth()),
                false);
    }

    @Override
    public Bitmap renderOriginalPage(int position) {
        return getOriginalPageBitmap(position);
    }

    public void setPageLayoutParser(PageLayoutParser parser) {
      // do nothing
    }

    @Override
    public void closeBook() {
        bookSource.closeBook();
    }

}
