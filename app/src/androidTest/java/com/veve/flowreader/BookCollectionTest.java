package com.veve.flowreader;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.dao.PageGlyphRecord;
import com.veve.flowreader.model.BookPage;
import com.veve.flowreader.model.BooksCollection;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.impl.DevicePageContextImpl;
import com.veve.flowreader.model.impl.PageGlyphImpl;
import com.veve.flowreader.model.impl.pdf.PdfBook;
import com.veve.flowreader.model.impl.pdf.PdfBookPage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class BookCollectionTest {

    BooksCollection booksCollection;

    @Before
    public void prepareCollection() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        booksCollection = BooksCollection.getInstance(appContext);
    }

    @Test
    public void checkCollection() {
        BookRecord bookRecord = new BookRecord();
        long bookId = booksCollection.addBook(bookRecord);
        bookRecord = booksCollection.getBook(bookId);
        assertNotEquals(-1, bookId);
        /*
        int currPage = bookRecord.getCurrentPage();
        assertEquals("Initial page number is not zero but " + currPage, 0, currPage);
        bookRecord.setCurrentPage(newPage);
        booksCollection.updateBook(bookRecord);
        BookRecord updatedBookRecord = booksCollection.getBook(bookId);
        currPage = updatedBookRecord.getCurrentPage();
        assertEquals("new page number is not stored, it is " + currPage, newPage, currPage);
        */
    }

    @Test
    public void checkGlyphQueries() {

        booksCollection.deleteBook(1);
        List<PageGlyphRecord> glyphs = booksCollection.getPageGlyphs(1L, 1);
        assertNotNull(glyphs);
        assertEquals(glyphs.size(), 0);
        List<PageGlyphRecord> testGlyphs;
        testGlyphs = new ArrayList<PageGlyphRecord>();
        testGlyphs.add(new PageGlyphRecord(1, 1, 1, 1, 1, 1, 1, 1, true));
        testGlyphs.add(new PageGlyphRecord(1, 2, 1, 1, 1, 1, 1, 1, true));
        testGlyphs.add(new PageGlyphRecord(1, 3, 1, 1, 1, 1, 1, 1, true));
        booksCollection.addGlyphs(testGlyphs);

        List<PageGlyphRecord> retrievedGlyphs = booksCollection.getPageGlyphs(1L, 1);
        assertNotNull(retrievedGlyphs);
        assertEquals(testGlyphs.size(), retrievedGlyphs.size());
        for (int i = 0; i < testGlyphs.size(); i++) {
            assertTrue(testGlyphs.get(i).equals(retrievedGlyphs.get(i)));
        }

    }

}

