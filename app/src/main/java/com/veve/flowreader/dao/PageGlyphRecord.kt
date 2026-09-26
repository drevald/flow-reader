package com.veve.flowreader.dao

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
class PageGlyphRecord : Serializable {
    @PrimaryKey(autoGenerate = true) var id: Long = 0
    var bookId: Long = 0
    var position: Int = 0
    var x: Int = 0
    var y: Int = 0
    var width: Int = 0
    var height: Int = 0
    var baselineShift: Int = 0
    var averageHeight: Int = 0
    var indented: Boolean = false
    var isSpace: Boolean = false
    var isLast: Boolean = false

    @Ignore constructor()

    constructor(
        bookId: Long, position: Int, x: Int, y: Int,
        width: Int, height: Int, baselineShift: Int,
        averageHeight: Int, indented: Boolean,
        isSpace: Boolean, isLast: Boolean
    ) {
        this.bookId = bookId
        this.position = position
        this.x = x
        this.y = y
        this.width = width
        this.height = height
        this.baselineShift = baselineShift
        this.averageHeight = averageHeight
        this.indented = indented
        this.isSpace = isSpace
        this.isLast = isLast
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PageGlyphRecord) return false
        return indented == other.indented &&
                x == other.x && y == other.y &&
                width == other.width && height == other.height &&
                baselineShift == other.baselineShift &&
                averageHeight == other.averageHeight &&
                isSpace == other.isSpace && isLast == other.isLast
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + x
        result = prime * result + y
        result = prime * result + width
        result = prime * result + height
        result = prime * result + baselineShift
        result = prime * result + averageHeight
        result = prime * result + if (indented) 0 else 1
        result = prime * result + if (isSpace) 0 else 1
        result = prime * result + if (isLast) 0 else 1
        return result
    }
}
