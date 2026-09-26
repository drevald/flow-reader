package com.veve.flowreader

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.veve.flowreader.dao.BookRecord
import com.veve.flowreader.dao.PageGlyphRecord
import com.veve.flowreader.model.BooksCollection
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookCollectionTest {

    lateinit var booksCollection: BooksCollection
    var bookRecord: BookRecord? = null
    var bookId: Long = 0

    @Before
    fun prepareCollection() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        booksCollection = BooksCollection.getInstance(appContext)
    }

    @After
    fun cleanUp() {
        booksCollection.deleteBook(bookId)
    }

    @Test
    fun checkCollection() {
        bookRecord = BookRecord()
        bookId = booksCollection.addBook(bookRecord!!)
        bookRecord = booksCollection.getBook(bookId)
        assertNotEquals(-1, bookId)
    }

    @Test
    fun checkGlyphQueries() {
        booksCollection.deleteBook(1)
        val glyphs = booksCollection.getPageGlyphs(1L, 1, true)
        assertNotNull(glyphs)
        assertEquals(0, glyphs!!.size)
        val testGlyphs = mutableListOf(
            PageGlyphRecord(1, 1, 1, 1, 1, 1, 1, 1, true, false, false),
            PageGlyphRecord(1, 1, 1, 1, 1, 1, 1, 1, true, false, false),
            PageGlyphRecord(1, 1, 1, 1, 1, 1, 1, 1, true, false, false)
        )
        booksCollection.addGlyphs(testGlyphs, true)
        val retrievedGlyphs = booksCollection.getPageGlyphs(1L, 1, true)
        assertNotNull(retrievedGlyphs)
        assertEquals(testGlyphs.size, retrievedGlyphs!!.size)
        for (i in testGlyphs.indices) {
            assertTrue(testGlyphs[i] == retrievedGlyphs[i])
        }
    }
}
