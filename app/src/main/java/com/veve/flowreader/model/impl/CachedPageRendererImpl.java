package com.veve.flowreader.model.impl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

import com.veve.flowreader.Constants;
import com.veve.flowreader.model.BookSource;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageGlyph;
import com.veve.flowreader.model.PageLayoutParser;
import com.veve.flowreader.model.PageRenderer;

import java.util.List;

import static android.graphics.Bitmap.Config.ARGB_8888;

public class CachedPageRendererImpl implements PageRenderer {

    private PageLayoutParser pageLayoutParser;

    private BookSource bookSource;

    private Bitmap originalBitmap;

    private int currentPage;

    private int currentOriginalPage;

    private List<PageGlyph> glyphs;

    public CachedPageRendererImpl(BookSource bookSource) {
        pageLayoutParser = OpenCVPageLayoutParser.getInstance();
        this.bookSource = bookSource;
    }

    private List<PageGlyph> getGlyphs(BookSource bookSource, int position) {
        if (position != currentPage || glyphs == null || glyphs.size() == 0) {
            Log.v(getClass().getName(), "Glyphs not cached current = " + currentPage + " requested = " + position);
            currentPage = position;
            long start = System.currentTimeMillis();
            glyphs = pageLayoutParser.getGlyphs(bookSource, position);
            Log.v(getClass().getName(),
                    String.format("Getting glyphs for page #%d took #%d milliseconds",
                    position, System.currentTimeMillis() - start));
        } else {
            Log.v(getClass().getName(), "Glyphs cached current = " + currentPage + " requested = " + position);
        }
        return glyphs;
    }

    private Bitmap getOriginalPageBitmap(int position) {
        if (position != currentOriginalPage || originalBitmap == null) {
            Log.v(getClass().getName(), "Page not cached current = " + currentOriginalPage + " requested = " + position);
            currentOriginalPage = position;
            long start = System.currentTimeMillis();
            originalBitmap = bookSource.getPageBytes(position);
            Log.v(getClass().getName(), String.format("Getting page #%d took #%d milliseconds",
                    position, System.currentTimeMillis() - start));
        } else {
            Log.v(getClass().getName(), "Page cached current = " + currentOriginalPage + " requested = " + position);
        }
        return originalBitmap;
    }

    @Override
    public Bitmap renderPage(DevicePageContext context, int position) {
        Log.i(getClass().getName(), String.format("position=%d", position));
        List<PageGlyph> pageGlyphList = getGlyphs(bookSource, position);

        if (pageGlyphList.size() <= 1) {
            return renderOriginalPage(context, position);
        } else {
            for(PageGlyph pageGlyph : pageGlyphList) {
                pageGlyph.draw(context, false);
            }
            context.setCurrentBaseLine(0);
            Point remotestPoint = context.getRemotestPoint();
            Bitmap bitmap = Bitmap.createBitmap(context.getWidth(), remotestPoint.y + (int)context.getLeading() , ARGB_8888);

            Canvas canvas = new Canvas(bitmap);
            context.resetPosition();
            context.setCanvas(canvas);

            Paint paint1 = new Paint();
            paint1.setStyle(Paint.Style.STROKE);
            paint1.setColor(Color.RED);
            paint1.setStrokeWidth(2);

            if (Constants.DEBUG)
                canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint1);

            for(PageGlyph pageGlyph : pageGlyphList) {
                pageGlyph.draw(context, true);
            }

            Log.d(getClass().getName(), "Drawing: Remotest point is X:" + context.getRemotestPoint().x + " Y" + context.getRemotestPoint().y + " Baseline: " + context.getCurrentBaseLine() ) ;

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
                (int) (context.getZoom() * bitmap.getWidth()),
                (int) (context.getZoom() * bitmap.getHeight()),
                false);
    }

    public void setPageLayoutParser(PageLayoutParser pageLayoutParser) {
        this.pageLayoutParser = pageLayoutParser;
    }




}
