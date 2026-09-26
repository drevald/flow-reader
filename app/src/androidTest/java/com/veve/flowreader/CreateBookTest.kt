package com.veve.flowreader

import android.content.Context
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import com.veve.flowreader.dao.BookRecord
import com.veve.flowreader.model.BookFactory
import com.veve.flowreader.model.BooksCollection
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

class CreateBookTest {

    private lateinit var pdfBookFile: File
    private var pdfBookFileId = -1L
    private lateinit var booksCollection: BooksCollection
    private lateinit var appContext: Context

    companion object {
        private const val BOOK_MD5 = "B5407E667B7BAA38FF6F10C3A8E1B8AD"
        private const val BOOK_URI = "/storage/emulated/0/Android/data/com.veve.flowreader/files/pdf_sample.pdf"
        private const val BOOK_SIZE = 30921L
    }

    @Before
    fun preparePdfSample() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        booksCollection = BooksCollection.getInstance(appContext)
        pdfBookFile = File(appContext.getExternalFilesDir(null), "pdf_sample.pdf")
        pdfBookFile.createNewFile()
        val oldBookRecord = booksCollection.getBook(pdfBookFile.path)
        if (oldBookRecord != null) {
            booksCollection.deleteBook(oldBookRecord.id!!)
            Log.v("BOOK", "Deleting ${oldBookRecord.url} with id ${oldBookRecord.id}")
        } else {
            Log.v("BOOK", "Book with id ${pdfBookFile.path} is missing")
        }
        val inputStream = appContext.resources.openRawResource(R.raw.pdf_sample)
        val outputStream = FileOutputStream(pdfBookFile)
        Utils.copy(inputStream, outputStream)
    }

    @Test
    fun createPdfBook() {
        val bookRecord: BookRecord = BookFactory.getInstance().createBook(pdfBookFile)
        pdfBookFileId = booksCollection.addBook(bookRecord)
        assertEquals(BOOK_SIZE, bookRecord.size)
        assertEquals(BOOK_MD5, bookRecord.md5)
        assertEquals(BOOK_URI, bookRecord.url)
    }

    @After
    fun cleanup() {
        pdfBookFile.delete()
        booksCollection.deleteBook(pdfBookFileId)
    }
}
