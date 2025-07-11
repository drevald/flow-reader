package com.veve.flowreader.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import android.database.Cursor;

import java.util.List;

@Dao
public interface DaoAccess {

    @Query("DELETE FROM BookRecord WHERE md5 = :checksum")
    void deleteByChecksum(String checksum);

    @Query("SELECT * FROM BookRecord WHERE md5 = :checksum")
    BookRecord fetchBookByChecksum(String checksum);

    @Query("SELECT * FROM BookRecord WHERE size = :size")
    BookRecord fetchBookBySize(long size);

    @Query("SELECT * FROM BookRecord WHERE url = :url")
    BookRecord fetchBook(String url);

    @Query("SELECT * FROM BookRecord")
    List<BookRecord> listBooks();

    @Query("DELETE FROM BookRecord WHERE id = :bookId")
    void deleteBook(long bookId);

    @Insert
    long addBook(BookRecord bookRecord);

    @Update
    void updateBook(BookRecord bookRecord);

    @Query("SELECT * FROM BookRecord WHERE id = :bookId")
    BookRecord getBook(Long bookId);

    @Query("SELECT * FROM PageGlyphRecord WHERE bookId = :bookId AND position = :position")
    List<PageGlyphRecord> getPageGlyphs(Long bookId, Integer position);

    @Query("SELECT * FROM PageGlyphRecord")
    List<PageGlyphRecord> getPageGlyphs();

    @Insert
    void insertGlyphs(List<PageGlyphRecord> glyphs);

    @Query("DELETE FROM PageGlyphRecord WHERE bookId = :bookId")
    void deleteBookGlyphs(long bookId);

    @Query("DELETE FROM PageGlyphRecord WHERE bookId = :bookId AND position = :position")
    void deleteBookPageGlyphs(long bookId, long position);

    @Insert
    long insertReport(ReportRecord reportRecord);

    @Query("SELECT * from ReportRecord WHERE id = :reportId")
    Cursor getReport(long reportId);

    @Query("DELETE FROM ReportRecord WHERE id = :reportId")
    void deleteReport(long reportId);

    @Query("UPDATE ReportRecord SET incomingId = :incomingId WHERE id = :id")
    void setIncomingReportId(long id, Long incomingId);

}