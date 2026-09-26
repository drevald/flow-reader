package com.veve.flowreader.model

interface Book {
    fun getPage(pageNumber: Int): BookPage
    fun getPagesCount(): Int
    fun getName(): String
    fun getId(): Long
    fun getPath(): String
    fun getTitle(): String?
    fun getAuthor(): String?
    fun getPreprocessing(): Boolean
    fun setPreprocessing(preprocessing: Boolean)
    fun close()
}
