package com.veve.flowreader;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.veve.flowreader.model.BookPage;
import com.veve.flowreader.model.PageGlyph;
import com.veve.flowreader.model.impl.pdf.PdfBook;
import com.veve.flowreader.model.impl.pdf.PdfBookSource;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class PdfBookTest {

    @Test
    public void test() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        PdfBookSource source = new PdfBookSource("/data/local/tmp/pdf_sample.pdf");
        List<PageGlyph> pageGlyphs = source.getPageGlyphs(0);
        Assert.assertEquals(354, pageGlyphs.size());
    }

}
