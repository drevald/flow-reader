package com.veve.flowreader.model.impl.djvu

import com.veve.flowreader.model.Book
import com.veve.flowreader.model.BookPage

class DjvuBook @Throws(Exception::class) constructor(private val path: String) : Book {

    companion object {
        init { System.loadLibrary("native-lib") }
        @JvmStatic private external fun getNativeTitle(bookId: Long): String?
        @JvmStatic private external fun getNativeAuthor(bookId: Long): String?
        @JvmStatic private external fun close(bookId: Long): Int
        private fun errorMessage(code: Int) = when (code) {
            1 -> "File not found or cannot be opened"
            2 -> "No permission to read this file"
            3 -> "Invalid or corrupted DjVu file"
            else -> "Failed to open DjVu file (error $code)"
        }
    }

    private val bookId: Long
    private val name: String = path
    private var preprocessing: Boolean = false
    init {
        val id = openBook(path)
        if (id < 0) throw Exception(errorMessage((-id).toInt()))
        bookId = id
    }

    private external fun openBook(path: String): Long
    private external fun getNumberOfPages(bookId: Long): Int

    override fun getPage(pageNumber: Int): BookPage = DjvuBookPage(bookId, pageNumber)
    override fun getPagesCount(): Int = getNumberOfPages(bookId)
    override fun getName(): String = name
    override fun getTitle(): String? = getNativeTitle(bookId)
    override fun getAuthor(): String? = getNativeAuthor(bookId)
    override fun getPreprocessing(): Boolean = preprocessing
    override fun setPreprocessing(preprocessing: Boolean) { this.preprocessing = preprocessing }
    override fun close() { close(bookId) }
    override fun getId(): Long = bookId
    override fun getPath(): String = path
}
