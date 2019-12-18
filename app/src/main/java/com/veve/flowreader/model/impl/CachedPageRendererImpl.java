package com.veve.flowreader.model.impl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import androidx.annotation.NonNull;
import android.util.Log;

import com.veve.flowreader.Constants;
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
import java.util.List;

import static android.graphics.Bitmap.Config.ARGB_8888;

public class CachedPageRendererImpl implements PageRenderer {

    private PageLayoutParser pageLayoutParser;

    private BookSource bookSource;

    private Bitmap originalBitmap;

    private int currentPage;

    private int currentOriginalPage;

    private List<PageGlyph> glyphs;

    private BooksCollection booksCollection;

    private BookRecord bookRecord;

    public CachedPageRendererImpl(BooksCollection booksCollection, BookRecord bookRecord, BookSource bookSource) {
        pageLayoutParser = OpenCVPageLayoutParser.getInstance();
        this.bookSource = bookSource;
        this.booksCollection = booksCollection;
        this.bookRecord = bookRecord;
    }

    private List<PageGlyph> getGlyphs(BookSource bookSource, int position) {
        // if page changed or glyphs are not stored or glyphs absent - retrieve new ones
        if (position != currentPage || glyphs == null || glyphs.size() == 0) {
            Log.d(getClass().getName(), "Glyphs not cached current = " + currentPage + " requested = " + position);
            currentPage = position;
            long start = System.currentTimeMillis();
            List<PageGlyphRecord> storedGlyphs = booksCollection.getPageGlyphs(bookRecord.getId(), position, false);
            if (storedGlyphs == null || storedGlyphs.size() == 0) {
                glyphs = pageLayoutParser.getGlyphs(bookSource, position);
                List<PageGlyphRecord> glyphsToStore = new ArrayList<PageGlyphRecord>();
                for (PageGlyph glyph : glyphs) {
                    glyphsToStore.add(new PageGlyphRecord(
                            bookRecord.getId(),
                            position,
                            ((PageGlyphImpl)glyph).getX(),
                            ((PageGlyphImpl)glyph).getY(),
                            ((PageGlyphImpl)glyph).getWidth(),
                            ((PageGlyphImpl)glyph).getHeight(),
                            ((PageGlyphImpl)glyph).getBaseLineShift(),
                            ((PageGlyphImpl)glyph).getAverageHeight(),
                            ((PageGlyphImpl)glyph).isIndented(),
                            ((PageGlyphImpl)glyph).isSpace(),
                            ((PageGlyphImpl)glyph).isLast()
                    ));
                }
                booksCollection.addGlyphs(glyphsToStore, false);
                Log.d(getClass().getName(),
                        String.format("Getting glyphs for page #%d took #%d milliseconds",
                                position, System.currentTimeMillis() - start));
            } else {
                List<PageGlyph> glyphsRestored = new ArrayList<PageGlyph>();
                long timeBuildingGlyphs = 0;
                Bitmap bitmap = bookSource.getPageGrayscaleBytes(position);
                for (PageGlyphRecord storedGlyph : storedGlyphs) {
                    PageGlyphInfo pageGlyphInfo = new PageGlyphInfo(
                            storedGlyph.isIndented(),
                            storedGlyph.getX(),
                            storedGlyph.getY(),
                            storedGlyph.getWidth(),
                            storedGlyph.getHeight(),
                            storedGlyph.getAverageHeight(),
                            storedGlyph.getBaselineShift(),
                            storedGlyph.isSpace(),
                            storedGlyph.isLast()
                            );
                    long startBuildingGlyph = System.currentTimeMillis();
                    Bitmap glyphBitmap = Bitmap.createBitmap(
                            bitmap,
                            storedGlyph.getX(),
                            storedGlyph.getY(),
                            storedGlyph.getWidth(),
                            storedGlyph.getHeight());
                    timeBuildingGlyphs += (System.currentTimeMillis() - startBuildingGlyph);
                    PageGlyphImpl pageGlyph = new PageGlyphImpl(glyphBitmap, pageGlyphInfo);
                    glyphsRestored.add(pageGlyph);
                }
                Log.v(getClass().getName(), "Bitmap creation took " + timeBuildingGlyphs + " milliseconds");
                glyphs = glyphsRestored;
                Log.d(getClass().getName(),
                        String.format("Getting stored glyphs for page #%d took %d milliseconds",
                                position, System.currentTimeMillis() - start));
            }
        } else {
            Log.d(getClass().getName(), "Glyphs cached current = " + currentPage + " requested = " + position);
        }
        return glyphs;
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


    @Override
    public Bitmap renderPage(DevicePageContext context, int position) {
        Log.v(getClass().getName(), String.format("position=%d", position));
        List<PageGlyph> pageGlyphList = getGlyphs(bookSource, position);
        long start = System.currentTimeMillis();
        if (pageGlyphList.size() <= 1) {
            Bitmap bitmap = renderOriginalPage(context, position);
            Log.v(getClass().getName(), "Reflowed page #" + position + " rendering took "
                    + (System.currentTimeMillis() - start) + " milliseconds") ;
            return bitmap;
        } else {
            for(PageGlyph pageGlyph : pageGlyphList) {
                pageGlyph.draw(context, false);
            }
            context.setCurrentBaseLine(0);
            Point remotestPoint = context.getRemotestPoint();
            Log.v("BITMAP_SIZE", "Creating " + context.getWidth() + "x" + (remotestPoint.y + (int)context.getLeading()) + " bitmap");
            Bitmap bitmap = Bitmap.createBitmap(context.getWidth(), remotestPoint.y + (int)context.getLeading() , ARGB_8888);

            Canvas canvas = new Canvas(bitmap);
            context.resetPosition();
            context.setCanvas(canvas);

            Paint strokePaint = new Paint();
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setColor(Color.RED);
            strokePaint.setStrokeWidth(2);

            Paint fillPaint = new Paint();
            fillPaint.setStyle(Paint.Style.FILL);
            fillPaint.setColor(Color.WHITE);

            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), fillPaint);

            if (Constants.DEBUG)
                canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), strokePaint);

            for(PageGlyph pageGlyph : pageGlyphList) {
                pageGlyph.draw(context, true);
            }

            Log.d(getClass().getName(), "Reflowed page #" + position + " rendering took"
                    + (System.currentTimeMillis() - start) + " milliseconds") ;

            context.resetPosition();
            context.setCurrentBaseLine(0);
            context.setCanvas(canvas);
            return bitmap;
        }

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

    public void setPageLayoutParser(PageLayoutParser pageLayoutParser) {
        this.pageLayoutParser = pageLayoutParser;
    }

    @Override
    public Bitmap renderOriginalPage(int position) {
        return getOriginalPageBitmap(position);
    }

}
