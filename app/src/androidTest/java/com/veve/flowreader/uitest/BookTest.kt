package com.veve.flowreader.uitest

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.test.platform.app.InstrumentationRegistry
import com.veve.flowreader.R
import com.veve.flowreader.Utils
import com.veve.flowreader.dao.BookRecord
import com.veve.flowreader.model.BookFactory
import com.veve.flowreader.model.BooksCollection
import org.junit.Before
import java.io.File
import java.io.FileOutputStream

open class BookTest {

    protected var testBookId: Long = 0
    lateinit var bookRecord: BookRecord
    lateinit var appContext: Context
    lateinit var booksCollection: BooksCollection

    @Before
    open fun prepareCollection() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        booksCollection = BooksCollection.getInstance(appContext)
        val file = File(appContext.getExternalFilesDir(null), "pdf_sample.pdf")
        file.createNewFile()
        val oldBookRecord = booksCollection.getBook(file.path)
        if (oldBookRecord != null) {
            booksCollection.deleteBook(oldBookRecord.id!!)
        }
        val inputStream = appContext.resources.openRawResource(R.raw.pdf_sample)
        val outputStream = FileOutputStream(file)
        Utils.copy(inputStream, outputStream)
        bookRecord = BookFactory.getInstance().createBook(file)
        testBookId = booksCollection.addBook(bookRecord)
        bookRecord = booksCollection.getBook(testBookId)!!
    }

    companion object {
        fun areDrawablesIdentical(drawableA: Drawable, drawableB: Drawable): Boolean {
            val stateA = drawableA.constantState
            val stateB = drawableB.constantState
            return (stateA != null && stateB != null && stateA == stateB)
                    || getBitmap(drawableA).sameAs(getBitmap(drawableB))
        }

        fun getBitmap(drawable: Drawable): Bitmap {
            if (drawable is BitmapDrawable) return drawable.bitmap
            var width = drawable.intrinsicWidth
            var height = drawable.intrinsicHeight
            if (width <= 0) width = 1
            if (height <= 0) height = 1
            val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(result)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return result
        }
    }
}
