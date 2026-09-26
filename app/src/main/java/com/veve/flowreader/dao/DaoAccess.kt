package com.veve.flowreader.dao

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface DaoAccess {

    @Query("DELETE FROM BookRecord WHERE md5 = :checksum")
    fun deleteByChecksum(checksum: String)

    @Query("SELECT * FROM BookRecord WHERE md5 = :checksum")
    fun fetchBookByChecksum(checksum: String): BookRecord?

    @Query("SELECT * FROM BookRecord WHERE size = :size")
    fun fetchBookBySize(size: Long): BookRecord?

    @Query("SELECT * FROM BookRecord WHERE url = :url")
    fun fetchBook(url: String): BookRecord?

    @Query("SELECT * FROM BookRecord")
    fun listBooks(): List<BookRecord>

    @Query("DELETE FROM BookRecord WHERE id = :bookId")
    fun deleteBook(bookId: Long)

    @Insert
    fun addBook(bookRecord: BookRecord): Long

    @Update
    fun updateBook(bookRecord: BookRecord)

    @Query("SELECT * FROM BookRecord WHERE id = :bookId")
    fun getBook(bookId: Long?): BookRecord?

    @Query("SELECT * FROM PageGlyphRecord WHERE bookId = :bookId AND position = :position")
    fun getPageGlyphs(bookId: Long?, position: Int?): List<PageGlyphRecord>

    @Query("SELECT * FROM PageGlyphRecord")
    fun getPageGlyphs(): List<PageGlyphRecord>

    @Insert
    fun insertGlyphs(glyphs: List<PageGlyphRecord>)

    @Query("DELETE FROM PageGlyphRecord WHERE bookId = :bookId")
    fun deleteBookGlyphs(bookId: Long)

    @Query("DELETE FROM PageGlyphRecord WHERE bookId = :bookId AND position = :position")
    fun deleteBookPageGlyphs(bookId: Long, position: Long)

    @Insert
    fun insertReport(reportRecord: ReportRecord): Long

    @Query("SELECT * from ReportRecord WHERE id = :reportId")
    fun getReport(reportId: Long): Cursor

    @Query("DELETE FROM ReportRecord WHERE id = :reportId")
    fun deleteReport(reportId: Long)

    @Query("UPDATE ReportRecord SET incomingId = :incomingId WHERE id = :id")
    fun setIncomingReportId(id: Long, incomingId: Long?)
}
