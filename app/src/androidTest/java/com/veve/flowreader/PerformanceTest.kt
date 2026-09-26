package com.veve.flowreader

import android.content.Context
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.veve.flowreader.dao.BookRecord
import com.veve.flowreader.model.BooksCollection
import com.veve.flowreader.model.DevicePageContext
import com.veve.flowreader.model.impl.NativePageRendererImpl
import com.veve.flowreader.model.impl.djvu.DjvuBook
import com.veve.flowreader.model.impl.djvu.DjvuBookSource
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class PerformanceTest {

    lateinit var djvuFile: File
    lateinit var booksCollection: BooksCollection
    lateinit var appContext: Context
    var bookRecordId: Long = 0

    @Before
    fun getBookFile() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        booksCollection = BooksCollection.getInstance(appContext)
        val inputStream = appContext.resources.openRawResource(R.raw.djvu_sample)
        djvuFile = File.createTempFile("djvu", null)
        djvuFile.deleteOnExit()
        Utils.copy(inputStream, FileOutputStream(djvuFile))
    }

    @Test
    fun testDjvu() {
        println("System.getProperty(\"TRAVIS\")=${System.getProperty("TRAVIS")}")
        println("System.getenv(\"TRAVIS\")=${System.getenv("TRAVIS")}")

        if ("true" == System.getProperty("TRAVIS")) throw Exception()
        if ("true" == System.getenv("TRAVIS")) throw Exception()

        val djvuBook = DjvuBook(djvuFile.path)
        Log.d(javaClass.name, "Book creation took ${System.currentTimeMillis()} milliseconds")
        djvuBook.id
        val djvuBookSource = DjvuBookSource(djvuFile.path)
        val bookRecord = BookRecord(djvuBook.id, 0, djvuBook.pagesCount, djvuBook.getName(), djvuBook.getPath())
        bookRecordId = booksCollection.addBook(bookRecord)

        val pageRenderer = NativePageRendererImpl(booksCollection, bookRecord, djvuBookSource)
        val context = DevicePageContext(800)
        var start = System.currentTimeMillis()
        pageRenderer.renderOriginalPage(context, 0)
        var end = System.currentTimeMillis()
        Log.d(javaClass.name, "First time opening original page took ${end - start} milliseconds")
        assertTrue("First time opening original page took ${end - start} milliseconds which is more than 10000", end - start < 10000)

        start = System.currentTimeMillis()
        pageRenderer.renderOriginalPage(context, 0)
        end = System.currentTimeMillis()
        Log.d(javaClass.name, "Second time opening original page took ${end - start} milliseconds")
        assertTrue("Second time opening original page took ${end - start} milliseconds which is more than 2500", end - start < 2500)

        start = System.currentTimeMillis()
        pageRenderer.renderPage(context, 0)
        end = System.currentTimeMillis()
        Log.d(javaClass.name, "First time opening page took ${end - start} milliseconds")
        assertTrue("First time opening page took ${end - start} milliseconds which is more than 15000", end - start < 15000)

        start = System.currentTimeMillis()
        pageRenderer.renderPage(context, 0)
        end = System.currentTimeMillis()
        Log.d(javaClass.name, "Second time opening page took ${end - start} milliseconds")
        assertTrue("Second time opening page took ${end - start} milliseconds which is more than 5000", end - start < 5000)

        start = System.currentTimeMillis()
        pageRenderer.renderPage(context, 1)
        end = System.currentTimeMillis()
        Log.d(javaClass.name, "First time opening next page took ${end - start} milliseconds")
        assertTrue("First time opening next page took ${end - start} milliseconds which is more than 15000", end - start < 15000)

        start = System.currentTimeMillis()
        pageRenderer.renderPage(context, 0)
        end = System.currentTimeMillis()
        Log.d(javaClass.name, "Third time opening page took ${end - start} milliseconds")
        assertTrue("Third time opening page took ${end - start} milliseconds which is more than 5000", end - start < 5000)
    }

    @After
    fun cleanup() {
        booksCollection.deleteBook(bookRecordId)
    }
}
