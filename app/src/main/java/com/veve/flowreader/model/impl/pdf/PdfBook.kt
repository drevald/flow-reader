package com.veve.flowreader.model.impl.pdf

import com.veve.flowreader.model.Book
import com.veve.flowreader.model.BookPage

class PdfBook @Throws(Exception::class) constructor(private val path: String) : Book {

    companion object {
        init { System.loadLibrary("native-lib") }
        @JvmStatic private external fun close(bookId: Long): Int
        @JvmStatic private external fun getNativeTitle(bookId: Long): String?
        @JvmStatic private external fun getNativeAuthor(bookId: Long): String?
        private fun errorMessage(code: Int) = when (code) {
            2 -> "File not found or cannot be opened"
            3 -> "Invalid or corrupted PDF file"
            4 -> "Password-protected PDF is not supported"
            5 -> "Unsupported PDF security scheme"
            6 -> "PDF content error"
            else -> "Failed to open PDF (error $code)"
        }
    }

    private val bookId: Long
    private var preprocessing: Boolean = false
    init {
        val id = openBook(path)
        // On 64-bit Android, allocator pointer-tagging sets the high byte of the handle,
        // making valid FPDF_DOCUMENT pointers negative as jlong. Real FPDF error codes
        // are 1-6 (negated: -1 to -6). Tagged valid pointers are << -100.
        if (id < 0 && id >= -100) throw Exception(errorMessage((-id).toInt()))
        bookId = id
    }

    private external fun openBook(path: String): Long
    private external fun getNumberOfPages(bookId: Long): Int

    override fun getPage(pageNumber: Int): BookPage = PdfBookPage(bookId, pageNumber)
    override fun getPagesCount(): Int = getNumberOfPages(bookId)
    override fun getName(): String = path
    override fun getPath(): String = path
    override fun getTitle(): String? = getNativeTitle(bookId)
    override fun getAuthor(): String? = getNativeAuthor(bookId)
    override fun getPreprocessing(): Boolean = preprocessing
    override fun setPreprocessing(preprocessing: Boolean) { this.preprocessing = preprocessing }
    override fun close() { close(bookId) }
    override fun getId(): Long = bookId
}
