package com.veve.flowreader.model

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.util.Log
import com.veve.flowreader.MD5
import com.veve.flowreader.dao.BookRecord
import com.veve.flowreader.model.impl.djvu.DjvuBook
import com.veve.flowreader.model.impl.pdf.PdfBook
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.nio.charset.StandardCharsets

class BookFactory private constructor() {

    companion object {
        private val ourInstance = BookFactory()
        @JvmStatic fun getInstance(): BookFactory = ourInstance
    }

    @Throws(Exception::class)
    fun createBook(file: File): BookRecord {
        Log.v("TEST", "Making book from file ${file.absolutePath}")
        val bookRecord = BookRecord()
        var book: Book? = null

        when {
            file.name.lowercase().endsWith("djvu") -> {
                book = DjvuBook(file.path)
                bookRecord.format = BookRecord.DJVU_FORMAT
            }
            file.name.lowercase().endsWith("pdf") -> {
                book = PdfBook(file.path)
                Log.v(javaClass.name, "Book.getPath = ${book.getPath()}")
                Log.v(javaClass.name, "Book.id = ${book.getId()}")
                bookRecord.format = BookRecord.PDF_FORMAT
            }
            else -> {
                val header = getHeader(file)
                when {
                    header != null && header.startsWith("%PDF") -> {
                        book = PdfBook(file.path)
                        bookRecord.format = BookRecord.PDF_FORMAT
                    }
                    header != null && header.startsWith("AT&T") -> {
                        book = DjvuBook(file.path)
                        bookRecord.format = BookRecord.DJVU_FORMAT
                    }
                }
            }
        }

        if (book == null) throw Exception("Unsupported file format")

        val pagesCount = book.getPagesCount()
        if (pagesCount == 0) throw Exception("PDF has no readable pages")
        bookRecord.pagesCount = pagesCount
        bookRecord.title = book.getName()

        val bitmap = book.getPage(0).getAsBitmap(DevicePageContext(100))
            ?: throw Exception("Failed to render page — file may be corrupted")
        val thumbnail = Bitmap.createScaledBitmap(bitmap, 100, 150, true)
        val grayThumbnail = Bitmap.createBitmap(thumbnail.width, thumbnail.height, Bitmap.Config.ARGB_8888)
        val contrast = 1.3f
        val translate = (1f - contrast) / 2f * 255f
        val cm = ColorMatrix().apply {
            setSaturation(0f)
            postConcat(ColorMatrix(floatArrayOf(
                contrast, 0f, 0f, 0f, translate,
                0f, contrast, 0f, 0f, translate,
                0f, 0f, contrast, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            )))
        }
        Canvas(grayThumbnail).drawBitmap(thumbnail, 0f, 0f, Paint().apply { colorFilter = ColorMatrixColorFilter(cm) })
        val byteArrayOutputStream = ByteArrayOutputStream()
        grayThumbnail.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        bookRecord.preview = byteArrayOutputStream.toByteArray()

        val bookName: String
        if (book.getTitle() != null) {
            bookName = book.getTitle()!! + (book.getAuthor()?.let { " $it" } ?: "")
            bookRecord.originalTitle = book.getTitle()
            book.getAuthor()?.let { bookRecord.author = it }
        } else {
            var name = file.name.replace("_", " ")
            val index = name.lastIndexOf('.')
            if (index > 0) name = name.substring(0, index)
            bookName = name
        }

        bookRecord.md5 = MD5.fileToMD5(file.path)
        bookRecord.size = file.length()
        bookRecord.title = bookName
        bookRecord.url = file.path
        Log.v("TEST", "New book stored id:${bookRecord.id}, url:${bookRecord.url}, md5:${bookRecord.md5}, title:${bookRecord.title}")
        return bookRecord
    }

    private fun getHeader(file: File): String? =
        try {
            FileInputStream(file).use { String(it.readNBytes(16), StandardCharsets.US_ASCII) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
}
