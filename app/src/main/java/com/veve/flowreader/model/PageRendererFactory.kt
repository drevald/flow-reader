package com.veve.flowreader.model

import com.veve.flowreader.dao.BookRecord
import com.veve.flowreader.model.impl.NativePageRendererImpl
import com.veve.flowreader.model.impl.djvu.DjvuBookSource
import com.veve.flowreader.model.impl.pdf.PdfBookSource

object PageRendererFactory {
    @JvmStatic
    fun getRenderer(bookCollection: BooksCollection, bookRecord: BookRecord): PageRenderer =
        when (bookRecord.format) {
            BookRecord.DJVU_FORMAT -> NativePageRendererImpl(bookCollection, bookRecord, DjvuBookSource(bookRecord.url!!))
            BookRecord.PDF_FORMAT -> NativePageRendererImpl(bookCollection, bookRecord, PdfBookSource(bookRecord.url!!))
            else -> throw Exception("Format Undefined")
        }
}
