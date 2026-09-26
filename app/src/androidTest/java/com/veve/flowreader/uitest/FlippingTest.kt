package com.veve.flowreader.uitest

import android.content.Context
import android.content.Intent
import android.view.View.VISIBLE
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.veve.flowreader.Constants
import com.veve.flowreader.R
import com.veve.flowreader.Utils
import com.veve.flowreader.dao.BookRecord
import com.veve.flowreader.model.BookFactory
import com.veve.flowreader.model.BooksCollection
import com.veve.flowreader.views.PageActivity
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

class FlippingTest {

    lateinit var appContext: Context
    lateinit var booksCollection: BooksCollection
    lateinit var bookRecord: BookRecord
    var bookRecordId: Long? = null
    lateinit var bookFile: File
    var scenario: ActivityScenario<PageActivity>? = null

    @Before
    fun setup() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        appContext.deleteDatabase("db")
        bookFile = File(appContext.getExternalFilesDir(null), "cyberiada.pdf")
        bookFile.createNewFile()
        val inputStream = appContext.resources.openRawResource(R.raw.sample)
        val outputStream = FileOutputStream(bookFile)
        Utils.copy(inputStream, outputStream)
        bookRecord = BookFactory.getInstance().createBook(bookFile)
        booksCollection = BooksCollection.getInstance(appContext)
        bookRecordId = booksCollection.addBook(bookRecord)
        val intent = Intent(appContext, PageActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(Constants.BOOK_ID, bookRecordId)
            putExtra(Constants.POSITION, 1)
        }
        scenario = ActivityScenario.launch(intent)
    }

    @Test
    fun testFlipping() {
        waitForPageLoad()
        for (i in 1 until bookRecord.pagesCount) {
            InstrumentationRegistry.getInstrumentation().runOnMainSync {
                scenario!!.onActivity { a -> a.setPageNumber(i) }
            }
            waitForPageLoad()
        }
    }

    private fun waitForPageLoad() {
        val deadline = System.currentTimeMillis() + 30_000
        while (System.currentTimeMillis() < deadline) {
            val loading = booleanArrayOf(false)
            scenario!!.onActivity { a ->
                loading[0] = a.findViewById<android.view.View>(R.id.progress).visibility == VISIBLE
            }
            if (!loading[0]) return
        }
    }

    @After
    fun clearUp() {
        scenario?.close()
        bookRecordId?.let { booksCollection.deleteBook(it) }
        bookFile.delete()
    }
}
