package com.veve.flowreader.model.impl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

import com.veve.flowreader.model.BookSource;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageGlyph;
import com.veve.flowreader.model.PageLayoutParser;
import com.veve.flowreader.model.PageRenderer;

import java.util.List;

import static android.graphics.Bitmap.Config.ARGB_8888;

public class CachedPageRendererImpl implements PageRenderer {

    PageLayoutParser pageLayoutParser;

    BookSource bookSource;

    Bitmap originalBitmap;

    int currentPage;

    List<PageGlyph> glyphs;

    public CachedPageRendererImpl(BookSource bookSource) {
        pageLayoutParser = OpenCVPageLayoutParser.getInstance();
        this.bookSource = bookSource;
    }

    private List<PageGlyph> getGlyphs(BookSource bookSource, int position) {
        if (position != currentPage || glyphs == null || glyphs.size() == 0) {
            currentPage = position;
            glyphs = pageLayoutParser.getGlyphs(bookSource, position);
        }
        return glyphs;
    }

    private Bitmap getOriginalPageBitmap(int position) {
        if (position != currentPage || originalBitmap == null) {
            currentPage = position;
            originalBitmap = bookSource.getPageBytes(position);
        }
        return originalBitmap;
    }

    @Override
    public Bitmap renderPage(DevicePageContext context, int position) {
        Log.d(getClass().getName(), "1");

        Log.i(getClass().getName(), String.format("position=%d", position));
        List<PageGlyph> pageGlyphList = getGlyphs(bookSource, position);

        if (pageGlyphList.size() <= 1) {
            return renderOriginalPage(context, position);
        } else {
            Log.d(getClass().getName(),"2");

            for(PageGlyph pageGlyph : pageGlyphList) {
                pageGlyph.draw(context, false);
            }

            Log.d(getClass().getName(), "3");

            context.setCurrentBaseLine(0);
            Point remotestPoint = context.getRemotestPoint();
            Log.i(getClass().getName(), String.format("w=%d h=%d, position=%d", context.getWidth(), remotestPoint.y, position));
            Bitmap bitmap = Bitmap.createBitmap(context.getWidth(), remotestPoint.y + (int)context.getLeading() , ARGB_8888);

            Canvas canvas = new Canvas(bitmap);
            context.resetPosition();
            context.setCanvas(canvas);

            Paint paint1 = new Paint();
            paint1.setStyle(Paint.Style.STROKE);
            paint1.setColor(Color.BLUE);
            paint1.setStrokeWidth(25);

            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint1);

            for(PageGlyph pageGlyph : pageGlyphList) {
                pageGlyph.draw(context, true);
            }

            context.resetPosition();
            context.setCanvas(canvas);
            return bitmap;
        }

    }

    @Override
    public Bitmap renderOriginalPage(DevicePageContext context, int position) {
        Bitmap bitmap = getOriginalPageBitmap(position);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap,
                (int) (context.getZoom() * bitmap.getWidth()),
                (int) (context.getZoom() * bitmap.getHeight()),
                false);
        return scaledBitmap;
    }

    public PageLayoutParser getPageLayoutParser() {
        return pageLayoutParser;
    }

    public void setPageLayoutParser(PageLayoutParser pageLayoutParser) {
        this.pageLayoutParser = pageLayoutParser;
    }




}
