package com.veve.flowreader.uitest

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import com.veve.flowreader.MD5
import com.veve.flowreader.R
import com.veve.flowreader.Utils
import com.veve.flowreader.dao.BookRecord
import com.veve.flowreader.model.BooksCollection
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

class NameTest {

    private lateinit var bookFile: File
    private var bookFileId = -1L
    private lateinit var booksCollection: BooksCollection
    private lateinit var appContext: Context

    fun prepareSample(resourceId: Int, fileName: String, dirName: String) {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        booksCollection = BooksCollection.getInstance(appContext)
        val parentDirectory = File(appContext.getExternalFilesDir(null), dirName)
        parentDirectory.mkdirs()
        bookFile = File(parentDirectory, fileName)
        if (!bookFile.exists()) assertTrue(bookFile.createNewFile())
        val inputStream = appContext.resources.openRawResource(resourceId)
        val outputStream = FileOutputStream(bookFile)
        Utils.copy(inputStream, outputStream)
        val oldBookRecord = booksCollection.getBookByChecksum(MD5.fileToMD5(bookFile.path))
        if (oldBookRecord != null) booksCollection.deleteBook(oldBookRecord.id!!)
    }

    @Test
    fun testDjvuFileOpen() {
        prepareSample(R.raw.noname, "Book.djvu", ".")
        val intent = Intent("android.intent.action.VIEW", Uri.fromFile(bookFile)).apply {
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            component = ComponentName.unflattenFromString("com.veve.flowreader/.views.GetBookActivity")
        }
        appContext.startActivity(intent)
        Thread.sleep(5000)
        val bookRecord = booksCollection.getBook(bookFile.path)
        bookFileId = bookRecord!!.id!!
        assertNotNull(bookRecord)
        assertEquals("Book", bookRecord.title)
    }

    @Test
    fun testNoExtFileOpen() {
        prepareSample(R.raw.noname, "Book", ".")
        val intent = Intent("android.intent.action.VIEW", Uri.fromFile(bookFile)).apply {
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            component = ComponentName.unflattenFromString("com.veve.flowreader/.views.GetBookActivity")
        }
        appContext.startActivity(intent)
        Thread.sleep(5000)
        val bookRecord = booksCollection.getBook(bookFile.path)
        bookFileId = bookRecord!!.id!!
        assertNotNull(bookRecord)
        assertEquals("Book", bookRecord.title)
    }

    @After
    fun cleanup() {
        bookFile.delete()
        booksCollection.deleteBook(bookFileId)
    }
}
