package com.veve.flowreader.dao

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.veve.flowreader.Constants

@Entity
class BookRecord {
    companion object {
        const val PDF_FORMAT = 1
        const val DJVU_FORMAT = 2
    }

    @PrimaryKey var id: Long? = null
    var currentPage: Int? = 0
    var pagesCount: Int? = 0
    var title: String? = null
    var originalTitle: String? = null
    var author: String? = null
    var url: String? = null
    var zoom: Float = 2.0f
    var zoomOriginal: Float = 1f
    var kerning: Float = 0.5f
    var leading: Float = 12f
    var margin: Float = 1.0f
    var mode: Int = Constants.VIEW_MODE_PHONE
    var scrollOffset: Int = 0
    var preview: ByteArray? = null
    var md5: String? = null
    var preprocessing: Boolean = false

    @ColumnInfo(name = "justify", defaultValue = "1")
    var justify: Boolean = true

    @ColumnInfo(name = "breakOnSpace", defaultValue = "0")
    var breakWords: Boolean = false

    @ColumnInfo(name = "medianGlyphBaseHeight", defaultValue = "0")
    var medianGlyphBaseHeight: Float = 0f
    var format: Int = 0
    var size: Long = 0
    constructor()

    @Ignore
    constructor(id: Long?, currentPage: Int?, pagesCount: Int?, name: String?, url: String?) {
        this.id = id
        this.currentPage = currentPage
        this.pagesCount = pagesCount
        this.title = name
        this.url = url
    }
}
