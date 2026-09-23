package com.veve.flowreader.model.impl;

import android.graphics.Bitmap;
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

/**
 * Component designed to render book page.
 */
public class NativePageRendererImpl implements PageRenderer {

    private BookSource bookSource;
    private Bitmap originalBitmap;
    private int currentOriginalPage;
    private BooksCollection booksCollection;
    private BookRecord bookRecord;
    private List<PageGlyphRecord> memGlyphs;

    public NativePageRendererImpl(BooksCollection booksCollection, BookRecord bookRecord,
                                  BookSource bookSource) {
        this.bookSource = bookSource;
        this.booksCollection = booksCollection;
        this.bookRecord = bookRecord;
    }

    private List<PageGlyph> getGlyphs(BookSource bookSource, int position) {
        // if page changed or glyphs are not stored or glyphs absent - retrieve new ones
        return Collections.emptyList();
    }

    private void updateZoomLimitsFromGlyphs(List<PageGlyphInfo> glyphs, DevicePageContext context) {
        List<Float> heights = new ArrayList<>();
        for (PageGlyphInfo g : glyphs) {
            // averageHeight is line_height from PageSegmenter (300 DPI source pixels).
            // Skip spaces (unreliable height) and anything > 200 px — that threshold
            // corresponds to ~48 pt at 300 DPI, well above any real text size; values
            // above it are embedded images whose giant height would skew the median.
            // Also skip values < 8: hairline/rule glyphs from thin separator lines have
            // line_height ≈ 2 and would corrupt the median if included.
            if (g.isSpace()) continue;
            if (g.getAverageHeight() < 8 || g.getAverageHeight() > 200) continue;
            heights.add((float) g.getAverageHeight());
        }
        if (heights.isEmpty()) return;
        Collections.sort(heights);
        float medianBaseHeight = heights.get(heights.size() / 2);
        bookRecord.setMedianGlyphBaseHeight(medianBaseHeight);
        context.setZoomLimits(medianBaseHeight, context.getResolution());
        float initialZoom = 10f * context.getZoomStep();
        context.setZoom(initialZoom);
        bookRecord.setZoom(initialZoom);
        booksCollection.updateBook(bookRecord);
        Log.d("ZOOM_LIMITS", String.format("medianBaseHeight=%.2f zoomMin=%.2f zoomMax=%.2f initialZoom=%.2f",
                medianBaseHeight, context.getZoomMin(), context.getZoomMax(), initialZoom));
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
            if (bookRecord.getMedianGlyphBaseHeight() == 0) {
                updateZoomLimitsFromGlyphs(glyphs, context);
                // First render used the uncalibrated default zoom; re-render with the correct initial zoom.
                for (Bitmap b : reflownPageBytes) {
                    if (!b.isRecycled()) b.recycle();
                }
                glyphs.clear();
                reflownPageBytes = bookSource.getReflownPageBytes(position, context, glyphs);
            }
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
            if (bookRecord.getMedianGlyphBaseHeight() == 0) {
                updateZoomLimitsFromGlyphs(glyphs, context);
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

        return new ArrayList<>(bitmaps);
    }

    @Override
    public Bitmap renderOriginalPage(DevicePageContext context, int position) {
        Bitmap bitmap = getOriginalPageBitmap(position);
        return Bitmap.createScaledBitmap(bitmap,
                (int) (context.getZoomOriginal() * context.getWidth()),
                (int) (context.getZoomOriginal() * (context.getWidth()
                        * bitmap.getHeight()) / bitmap.getWidth()),
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
