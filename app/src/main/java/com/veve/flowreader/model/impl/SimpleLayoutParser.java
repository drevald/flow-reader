package com.veve.flowreader.model.impl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import com.veve.flowreader.model.BookSource;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageGlyph;
import com.veve.flowreader.model.PageLayoutParser;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class SimpleLayoutParser implements PageLayoutParser {

    private static PageLayoutParser parser;

    private static final int STATE_BLANK = 0;
    private static final int STATE_ROW = 1;
    private static final int STATE_GLYPH_BLANK = 0;
    private static final int STATE_GLYPH_STARTED = 1;

    private static final float ROW_THRESHOLD = 0.05f;
    private static final float CHAR_THRESHOLD = 0.1f;


    private SimpleLayoutParser() {

    }

    @Override
    public List<PageGlyph> getGlyphs(BookSource bookSource, int position) {

        Bitmap bitmap = bookSource.getPageBytes(position);

        Log.d(getClass().getName(), "getGlyphs started");

        List<Row> rows = getRows(bitmap);

        List<PageGlyph> glyphs = new ArrayList<PageGlyph>();

        for (Row row : rows) {
            glyphs.addAll(getGlyphs(row, bitmap));
        }

        Log.d(getClass().getName(), "getGlyphs ended");

        return glyphs;

    }

    private Collection<? extends PageGlyph> getGlyphs(Row row, Bitmap bitmap) {

        int width = bitmap.getWidth();
        int glyphState = STATE_GLYPH_BLANK;
        int glyphStart = 0;
        List<Glyph> glyphs = new ArrayList<Glyph>();
        double sum;
        double greyscale;

        int rowHeight = row.end - row.start;
        for (int i=0; i<width; i++) {
            sum = 0;
            for (int j=row.start; j<row.end; j++) {
                int rgb = bitmap.getPixel(i, j);
                greyscale = getGrayscale(rgb);
                sum += greyscale;
            }
            Log.v(getClass().getName(),i + "\t" + (rowHeight - sum) / rowHeight + "\n");
            if (glyphState == STATE_GLYPH_BLANK) {
                if ((rowHeight-sum)/rowHeight > CHAR_THRESHOLD) {
                    glyphState = STATE_GLYPH_STARTED;
                    glyphStart = i;
                }
            } else if (glyphState == STATE_GLYPH_STARTED) {
                if ((rowHeight-sum)/rowHeight < CHAR_THRESHOLD) {
                    glyphState = STATE_GLYPH_BLANK;
                    Bitmap glyphBitmap = Bitmap
                            .createBitmap(bitmap, glyphStart,row.start,
                                    i-glyphStart, rowHeight);
                    glyphs.add(new Glyph(glyphBitmap));
                }
            }
        }

        return glyphs;

    }

    private List<Row> getRows(Bitmap bitmap) {

        int currentState = STATE_BLANK;
        double sum;
        double greyscale;
        int width = bitmap.getWidth();
        List<Row> rows = new ArrayList<Row>();
        int rowStart = 0;

        for (int i=0; i<bitmap.getHeight(); i++ ) {
            sum = 0;
            for (int j=0; j<width; j++) {
                try {
                    int rgb = bitmap.getPixel(j, i);
                    greyscale = getGrayscale(rgb);
                    sum += greyscale;
                } catch (Exception e) {
                    Log.e(getClass().getName(),"i:"+i+",j:"+j);
                }
            }

            if (currentState == STATE_BLANK) {
                if ((width-sum)/width > ROW_THRESHOLD) {
                    currentState = STATE_ROW;
                    rowStart = i;
                }
            } else if (currentState == STATE_ROW) {
                if ((width-sum)/width < ROW_THRESHOLD) {
                    currentState = STATE_BLANK;
                    rows.add(new Row(rowStart, i));
                }
            }
        }

        return rows;

    }

    private double getGrayscale(int rgb) {
        int red = (rgb >> 16) & 0x000000FF;
        int green = (rgb >>8 ) & 0x000000FF;
        int blue = (rgb) & 0x000000FF;
        return 0.2126 * (red/255) + 0.7152 * (green/255) + 0.0722 * (blue/255);
    }

    public static PageLayoutParser getInstance() {
        if (parser == null) {
            parser = new SimpleLayoutParser();
        }
        return parser;
    }

}

class Row {

    Row(int start, int end) {
        this.start = start;
        this.end = end;
    }

    int start;

    int end;

    public String toString() {
        return String.format(Locale.CANADA, "%d->%d", start, end);
    }

}

class Glyph implements PageGlyph {

    private static Paint paint = new Paint();
    private int top, bottom, left, right;
    private Bitmap bitmap;

    Glyph(int top, int bottom, int left, int right) {
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
    }

    Glyph(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String toString() {
        return String.format(Locale.KOREA, "top:%d,bottom:%d,left:%d,right:%d", top, bottom, left, right);
    }

    @Override
    public void draw(DevicePageContext context, boolean show) {

        context.getCanvas();
        Point startPoint = context.getStartPoint();
        Canvas canvas = context.getCanvas();
        int __height = bitmap.getHeight();
        int __width = bitmap.getWidth();
        if(__width * context.getZoom() + startPoint.x > context.getWidth() - context.getMargin()) {
            startPoint.set(context.getMargin(), startPoint.y + (int)(__height * context.getZoom())
                    + (int)(context.getLeading()* context.getZoom()));
        }
        Rect __srcRect = new Rect(0, 0, __width, __height);
        Rect __dstRect = new Rect(startPoint.x , startPoint.y,
                startPoint.x +(int)(__width * context.getZoom()),
                startPoint.y + (int)(__height * context.getZoom()));
        if(show) {
            canvas.drawBitmap(bitmap, __srcRect, __dstRect, paint);
        }
        context.getStartPoint().set(startPoint.x + __dstRect.width()
                + (int)(context.getKerning()* context.getZoom()), startPoint.y);
        context.getRemotestPoint().set(startPoint.x + __dstRect.width()
                + (int)(context.getKerning()* context.getZoom()), startPoint.y + __dstRect.height());

    }

}