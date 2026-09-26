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
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

class WeirdNameTest {

    val djvuFileName = "S P\tACE(1).djvu"
    val sampleDirName = "/1/2/3/4/5/6/7/8/9/10/"
    val djvuFileRes = R.raw.noname
    private lateinit var djvuBookFile: File
    private var djvuBookFileId = -1L
    private lateinit var booksCollection: BooksCollection
    private lateinit var appContext: Context

    @Before
    fun prepareDjvuSample() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        booksCollection = BooksCollection.getInstance(appContext)
        val parentDirectory = File(appContext.getExternalFilesDir(null), sampleDirName)
        parentDirectory.mkdirs()
        djvuBookFile = File(parentDirectory, djvuFileName)
        if (!djvuBookFile.exists()) assertTrue(djvuBookFile.createNewFile())
        val inputStream = appContext.resources.openRawResource(djvuFileRes)
        val outputStream = FileOutputStream(djvuBookFile)
        Utils.copy(inputStream, outputStream)
        val oldBookRecord = booksCollection.getBookByChecksum(MD5.fileToMD5(djvuBookFile.path))
        if (oldBookRecord != null) booksCollection.deleteBook(oldBookRecord.id!!)
    }

    @Test
    fun testDjvuFileOpen() {
        val intent = Intent("android.intent.action.VIEW", Uri.fromFile(djvuBookFile)).apply {
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            component = ComponentName.unflattenFromString("com.veve.flowreader/.views.GetBookActivity")
        }
        appContext.startActivity(intent)
        Thread.sleep(5000)
        val bookRecord = booksCollection.getBook(djvuBookFile.path)
        djvuBookFileId = bookRecord!!.id!!
        assertNotNull(bookRecord)
        assertEquals(djvuFileName.substring(0, djvuFileName.lastIndexOf(".")), bookRecord.title)
    }

    @Ignore("Xiaomi-specific content URI — will fail on other devices")
    @Test
    fun testDjvuContentOpen() {
        val intent = Intent("android.intent.action.VIEW",
            Uri.parse("content://com.mi.android.globalFileexplorer.myprovider/external_files" +
                    djvuBookFile.path.substring("/storage/emulated/0".length))).apply {
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            component = ComponentName.unflattenFromString("com.veve.flowreader/.views.GetBookActivity")
        }
        appContext.startActivity(intent)
        Thread.sleep(5000)
        val bookRecord = booksCollection.getBook(djvuBookFile.path)
        djvuBookFileId = bookRecord!!.id!!
        assertNotNull(bookRecord)
        assertEquals(djvuFileName.substring(0, djvuFileName.indexOf(".")), bookRecord.title)
    }

    @After
    fun cleanup() {
        djvuBookFile.delete()
        booksCollection.deleteBook(djvuBookFileId)
    }
}
