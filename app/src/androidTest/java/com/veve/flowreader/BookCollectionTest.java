package com.veve.flowreader;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.dao.PageGlyphRecord;
import com.veve.flowreader.model.BooksCollection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

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
    BookRecord bookRecord;
    long bookId;

    @Before
    public void prepareCollection() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        booksCollection = BooksCollection.getInstance(appContext);
    }

    @After
    public void cleaUp() {
        booksCollection.deleteBook(bookId);
    }

    @Test
    public void checkCollection() {
        bookRecord = new BookRecord();
        bookId = booksCollection.addBook(bookRecord);
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
        List<PageGlyphRecord> glyphs = booksCollection.getPageGlyphs(1L, 1, true);
        assertNotNull(glyphs);
        assertEquals(glyphs.size(), 0);
        List<PageGlyphRecord> testGlyphs;
        testGlyphs = new ArrayList<PageGlyphRecord>();
        testGlyphs.add(new PageGlyphRecord(1, 1, 1, 1, 1, 1, 1, 1, true, false));
        testGlyphs.add(new PageGlyphRecord(1, 1, 1, 1, 1, 1, 1, 1, true, false));
        testGlyphs.add(new PageGlyphRecord(1, 1, 1, 1, 1, 1, 1, 1, true, false));
        booksCollection.addGlyphs(testGlyphs, true);

        List<PageGlyphRecord> retrievedGlyphs = booksCollection.getPageGlyphs(1L, 1, true);
        assertNotNull(retrievedGlyphs);
        assertEquals(testGlyphs.size(), retrievedGlyphs.size());
        for (int i = 0; i < testGlyphs.size(); i++) {
            assertTrue(testGlyphs.get(i).equals(retrievedGlyphs.get(i)));
        }

    }

}

