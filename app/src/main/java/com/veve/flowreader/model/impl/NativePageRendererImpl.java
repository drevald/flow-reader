package com.veve.flowreader.model.impl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

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
        if (position != currentOriginalPage || originalBitmap == null) {
            Log.d(getClass().getName(), "Page not cached current = " + currentOriginalPage + " requested = " + position);
            currentOriginalPage = position;
            long start = System.currentTimeMillis();
            originalBitmap = bookSource.getPageBytes(position);
            Log.d(getClass().getName(), String.format("Getting page #%d took #%d milliseconds",
                    position, System.currentTimeMillis() - start));
        } else {
            Log.d(getClass().getName(), "Page cached current = " + currentOriginalPage + " requested = " + position);
        }
        return originalBitmap;
    }

    private Bitmap getReflownPageBitmap(int position, DevicePageContext context) {

        if (context.isInvalidateCache()) {
            booksCollection.deleteGlyphs(bookRecord.getId(), position);
        }

        List<PageGlyphRecord> storedGlyphs = booksCollection.getPageGlyphs(bookRecord.getId(), position, false);


        if (storedGlyphs == null || storedGlyphs.isEmpty()) {
            List<PageGlyphInfo> glyphs = new ArrayList<>();
            Bitmap reflownPageBytes = bookSource.getReflownPageBytes(position, context, glyphs);
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
           Bitmap reflownPageBytes = bookSource.getReflownPageBytes(position, context, glyphs);
           return reflownPageBytes;
        }

    }

    @Override
    public Bitmap renderPage(DevicePageContext context, int position) {

        Bitmap bitmap = getReflownPageBitmap(position, context);
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
        return Bitmap.createScaledBitmap(bitmap, (context.getWidth()),
                ((context.getWidth() * bitmap.getHeight())/bitmap.getWidth()),
                false);
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

}
