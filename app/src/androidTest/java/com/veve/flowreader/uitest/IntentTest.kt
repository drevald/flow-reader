package com.veve.flowreader.uitest

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.StrictMode
import androidx.test.platform.app.InstrumentationRegistry
import com.veve.flowreader.MD5
import com.veve.flowreader.R
import com.veve.flowreader.Utils
import com.veve.flowreader.dao.BookRecord
import com.veve.flowreader.model.BooksCollection
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

class IntentTest {

    val pdfFileName = "pdf_sample.pdf"
    val djvuFileName = "djvu_sample.djvu"
    val sampleDirectory = "."
    val djvuFileRes = R.raw.djvu_sample
    val pdfFileRes = R.raw.pdf_sample
    private lateinit var pdfBookFile: File
    private lateinit var djvuBookFile: File
    private var pdfBookFileId = -1L
    private var djvuBookFileId = -1L
    private lateinit var booksCollection: BooksCollection
    private lateinit var appContext: Context

    @Before
    fun preparePdfSample() {
        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().build())
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        booksCollection = BooksCollection.getInstance(appContext)
        val parentDirectory = File(appContext.getExternalFilesDir(null), sampleDirectory)
        pdfBookFile = File(parentDirectory, pdfFileName)
        pdfBookFile.createNewFile()
        val inputStream = appContext.resources.openRawResource(R.raw.pdf_sample)
        val outputStream = FileOutputStream(pdfBookFile)
        Utils.copy(inputStream, outputStream)
        val oldBookRecord = booksCollection.getBookByChecksum(MD5.fileToMD5(pdfBookFile.path))
        if (oldBookRecord != null) booksCollection.deleteBook(oldBookRecord.id!!)
    }

    @Before
    fun prepareDjvuSample() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        booksCollection = BooksCollection.getInstance(appContext)
        djvuBookFile = File(appContext.getExternalFilesDir(null), djvuFileName)
        if (!djvuBookFile.exists()) assertTrue(djvuBookFile.createNewFile())
        val oldBookRecord = booksCollection.getBook(djvuBookFile.path)
        if (oldBookRecord != null) booksCollection.deleteBook(oldBookRecord.id!!)
        val inputStream = appContext.resources.openRawResource(R.raw.djvu_sample)
        val outputStream = FileOutputStream(djvuBookFile)
        val buffer = ByteArray(100)
        while (inputStream.read(buffer) != -1) outputStream.write(buffer)
        outputStream.close()
        inputStream.close()
    }

    @Test
    fun testPdfFileOpen() {
        val intent = Intent("android.intent.action.VIEW", Uri.fromFile(pdfBookFile)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            component = ComponentName.unflattenFromString("com.veve.flowreader/.views.GetBookActivity")
        }
        appContext.startActivity(intent)
        Thread.sleep(5000)
        val bookRecord = booksCollection.getBook(pdfBookFile.path)
        pdfBookFileId = bookRecord!!.id!!
        assertNotNull(bookRecord)
    }

    @Test
    fun testDjvuFileOpen() {
        val intent = Intent("android.intent.action.VIEW", Uri.fromFile(djvuBookFile)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            component = ComponentName.unflattenFromString("com.veve.flowreader/.views.GetBookActivity")
        }
        appContext.startActivity(intent)
        Thread.sleep(5000)
        val bookRecord = booksCollection.getBook(djvuBookFile.path)
        djvuBookFileId = bookRecord!!.id!!
        assertNotNull(bookRecord)
    }

    @Ignore("Xiaomi-specific content URI — will fail on other devices")
    @Test
    fun testDjvuContentOpen() {
        val intent = Intent("android.intent.action.VIEW",
            Uri.parse("content://com.mi.android.globalFileexplorer.myprovider/external_files" +
                    "/Android/data/com.veve.flowreader/files/djvu_sample.djvu")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            component = ComponentName.unflattenFromString("com.veve.flowreader/.views.GetBookActivity")
        }
        appContext.startActivity(intent)
        Thread.sleep(5000)
        val bookRecord = booksCollection.getBook(djvuBookFile.path)
        djvuBookFileId = bookRecord!!.id!!
        assertNotNull(bookRecord)
    }

    @Ignore("Xiaomi-specific content URI — will fail on other devices")
    @Test
    fun testPdfContentOpen() {
        val intent = Intent("android.intent.action.VIEW",
            Uri.parse("content://com.mi.android.globalFileexplorer.myprovider/external_files" +
                    pdfBookFile.path.substring("/storage/emulated/0".length))).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            component = ComponentName.unflattenFromString("com.veve.flowreader/.views.GetBookActivity")
        }
        appContext.startActivity(intent)
        Thread.sleep(5000)
        val bookRecord = booksCollection.getBook(pdfBookFile.path)
        djvuBookFileId = bookRecord!!.id!!
        assertNotNull(bookRecord)
    }

    @After
    fun cleanup() {
        djvuBookFile.delete()
        pdfBookFile.delete()
        booksCollection.deleteBook(pdfBookFileId)
        booksCollection.deleteBook(djvuBookFileId)
    }
}
