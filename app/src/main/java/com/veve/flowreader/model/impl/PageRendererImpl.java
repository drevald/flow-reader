package com.veve.flowreader.model.impl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.Log;

import com.veve.flowreader.model.BookSource;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageGlyph;
import com.veve.flowreader.model.PageLayoutParser;
import com.veve.flowreader.model.PageRenderer;

import java.util.List;

import static android.graphics.Bitmap.Config.ARGB_8888;

public class PageRendererImpl implements PageRenderer {

    PageLayoutParser pageLayoutParser;

    BookSource bookSource;

    public PageRendererImpl(BookSource bookSource) {
        pageLayoutParser = OpenCvPageLayoutParserImpl.getInstance();
        this.bookSource = bookSource;
    }

    @Override
    public Bitmap renderPage(DevicePageContext context, int position) {
        Log.d(getClass().getName(), "1");

        Log.i(getClass().getName(), String.format("position=%d", position));
        List<PageGlyph> pageGlyphList = pageLayoutParser.getGlyphs(bookSource.getPageBytes(position));

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

        for(PageGlyph pageGlyph : pageGlyphList) {
            pageGlyph.draw(context, true);
        }

        context.resetPosition();
        context.setCanvas(canvas);
        return bitmap;

    }

    @Override
    public Bitmap renderOriginalPage(DevicePageContext context, int position) {
        return bookSource.getPageBytes(position);
    }

    public PageLayoutParser getPageLayoutParser() {
        return pageLayoutParser;
    }

    public void setPageLayoutParser(PageLayoutParser pageLayoutParser) {
        this.pageLayoutParser = pageLayoutParser;
    }

}
