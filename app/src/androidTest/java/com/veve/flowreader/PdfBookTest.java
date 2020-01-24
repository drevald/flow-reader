package com.veve.flowreader;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.veve.flowreader.model.Book;
import com.veve.flowreader.model.PageGlyph;
import com.veve.flowreader.model.impl.pdf.PdfBook;
import com.veve.flowreader.model.impl.pdf.PdfBookSource;
import com.veve.flowreader.uitest.BookTest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class PdfBookTest {

    private String filePath;
    Context context;

    @Before
    public void storeBook() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        File file = new File(context.getExternalFilesDir(null), "sample.pdf");
        filePath = file.getPath();
        InputStream is = context.getResources().openRawResource(R.raw.sample);
        Utils.copy(is, new FileOutputStream(file));
    }

    @Test
    public void testGlyphCount() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        PdfBookSource source = new PdfBookSource(filePath);
        List<PageGlyph> pageGlyphs = source.getPageGlyphs(0);
        System.out.println(pageGlyphs.size());
        Assert.assertEquals(3309, pageGlyphs.size());
    }

    @Test
    public void testMetaData() {
        Book book = new PdfBook(filePath);
        Assert.assertEquals("", book.getAuthor() );
        Assert.assertEquals("Sample Document", book.getTitle() );
    }

}
