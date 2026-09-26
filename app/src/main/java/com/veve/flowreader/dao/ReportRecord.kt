package com.veve.flowreader.dao

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class ReportRecord(
    var glyphs: ByteArray?,
    var originalPage: ByteArray?,
    var overturnedPage: ByteArray?
) {
    @PrimaryKey var id: Long? = null
    var incomingId: Long? = -1L
    var bookId: Long? = null
    var position: Int? = null
    var state: Int? = 0
    var uploadDate: Long = 0
    var fixDate: Long = 0
    var fixVersion: String? = null
    var response: String? = null
}
