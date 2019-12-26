package com.veve.flowreader.uitest;

import android.graphics.Bitmap;

import com.veve.flowreader.PageTailor;
import com.veve.flowreader.PagesSet;
import com.veve.flowreader.model.BookSource;
import com.veve.flowreader.model.BooksCollection;
import com.veve.flowreader.model.PageRenderer;
import com.veve.flowreader.model.impl.CachedPageRendererImpl;
import com.veve.flowreader.model.impl.DevicePageContextImpl;
import com.veve.flowreader.model.impl.NativePageRendererImpl;
import com.veve.flowreader.model.impl.djvu.DjvuBookSource;
import com.veve.flowreader.uitest.BookTest;

import org.junit.Before;
import org.junit.Test;

import java.lang.annotation.Native;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RendererTailorTest extends BookTest {

    @Test
    public void testTailoringNativeRenderer() {
        BookSource bookSource = new DjvuBookSource(bookRecord.getUrl());
        PageRenderer pageRenderer = new NativePageRendererImpl(booksCollection, bookRecord, bookSource);
        List<PagesSet> pagesSet = PagesSet.getPagesSet("1-3");
        PageTailor pageTailor = new PageTailor(pageRenderer, pagesSet,50, 100);
        Bitmap pageBitmap;
        while ((pageBitmap = pageTailor.read()) != null) {
            assertEquals(50, pageBitmap.getWidth());
            assertEquals(100, pageBitmap.getHeight());
        }
    }

    @Test
    public void testTailoringCachedRenderer() {
        BookSource bookSource = new DjvuBookSource(bookRecord.getUrl());
        PageRenderer pageRenderer = new CachedPageRendererImpl(booksCollection, bookRecord, bookSource);
        List<PagesSet> pagesSet = PagesSet.getPagesSet("1-3");
        PageTailor pageTailor = new PageTailor(pageRenderer, pagesSet,50, 100);
        Bitmap pageBitmap;
        while ((pageBitmap = pageTailor.read()) != null) {
            assertEquals(50, pageBitmap.getWidth());
            assertEquals(100, pageBitmap.getHeight());
        }
    }

    @Test
    public void testCachedRenderer() {
        BookSource bookSource = new DjvuBookSource(bookRecord.getUrl());
        PageRenderer pageRenderer = new CachedPageRendererImpl(booksCollection, bookRecord, bookSource);
        List<Bitmap> bitmaps = pageRenderer.renderPage(new DevicePageContextImpl(50), 1);
        assertNotNull(bitmaps);
    }

}
