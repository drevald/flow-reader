package com.veve.flowreader.uitest

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import com.veve.flowreader.MD5
import com.veve.flowreader.R
import com.veve.flowreader.dao.AppDatabase
import com.veve.flowreader.dao.BookRecord
import com.veve.flowreader.model.BooksCollection
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

class DuplicateTest {

    private lateinit var firstBookFile: File
    private lateinit var secondBookFile: File
    private var firstBookRecord: BookRecord? = null
    private var secondBookRecord: BookRecord? = null
    private lateinit var booksCollection: BooksCollection
    private lateinit var appContext: Context

    @Before
    fun prepareSamples() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        booksCollection = BooksCollection.getInstance(appContext)
        val appDatabase = AppDatabase.getInstance(appContext)
        val daoAccess = appDatabase.daoAccess()
        firstBookFile = makeFile("first_sample.pdf")
        secondBookFile = makeFile("second_sample.pdf")
        val checksum = MD5.fileToMD5(secondBookFile.path)
        daoAccess.deleteByChecksum(checksum)
    }

    private fun makeFile(fileName: String): File {
        val file = File(appContext.getExternalFilesDir(null), fileName)
        if (!file.exists()) assertTrue(file.createNewFile())
        val inputStream = appContext.resources.openRawResource(R.raw.pdf_sample)
        val outputStream = FileOutputStream(file)
        val buffer = ByteArray(1)
        while (inputStream.read(buffer) != -1) {
            outputStream.write(buffer)
        }
        outputStream.close()
        inputStream.close()
        return file
    }

    @Test
    fun testDuplicate() {
        val firstIntent = Intent("android.intent.action.VIEW", Uri.fromFile(firstBookFile)).apply {
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            component = ComponentName.unflattenFromString("com.veve.flowreader/.views.GetBookActivity")
        }
        appContext.startActivity(firstIntent)
        Thread.sleep(5000)

        val secondIntent = Intent("android.intent.action.VIEW", Uri.fromFile(secondBookFile)).apply {
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            component = ComponentName.unflattenFromString("com.veve.flowreader/.views.GetBookActivity")
        }
        appContext.startActivity(secondIntent)
        Thread.sleep(5000)

        firstBookRecord = booksCollection.getBook(firstBookFile.path)
        secondBookRecord = booksCollection.getBook(secondBookFile.path)

        assertTrue((firstBookRecord == null && secondBookRecord != null)
                || (secondBookRecord == null && firstBookRecord != null))
    }

    @After
    fun cleanup() {
        assertTrue(firstBookFile.delete())
        assertTrue(secondBookFile.delete())
        firstBookRecord?.let { booksCollection.deleteBook(it.id!!) }
        secondBookRecord?.let { booksCollection.deleteBook(it.id!!) }
    }
}
