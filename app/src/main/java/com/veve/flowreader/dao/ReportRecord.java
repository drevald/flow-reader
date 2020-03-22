package com.veve.flowreader.dao;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ReportRecord {

    @PrimaryKey
    private Long id;
    private Long incomingId = -1L;
    private Long bookId;
    private Integer position;
    private Integer state = 0;
    private byte[] glyphs;
    private byte[] originalPage;
    private byte[] overturnedPage;
    private long uploadDate;
    private long fixDate;
    private String fixVersion;
    private String response;

    public ReportRecord( byte[] glyphs, byte[] originalPage, byte[] overturnedPage) {
        this.glyphs = glyphs;
        this.originalPage = originalPage;
        this.overturnedPage = overturnedPage;
    }

    public long getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(long uploadDate) {
        this.uploadDate = uploadDate;
    }

    public long getFixDate() {
        return fixDate;
    }

    public void setFixDate(long fixDate) {
        this.fixDate = fixDate;
    }

    public String getFixVersion() {
        return fixVersion;
    }

    public void setFixVersion(String fixVersion) {
        this.fixVersion = fixVersion;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIncomingId() {
        return incomingId;
    }

    public void setIncomingId(Long incomingId) {
        this.incomingId = incomingId;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public  byte[] getGlyphs() {
        return glyphs;
    }

    public void setGlyphs( byte[] glyphs) {
        this.glyphs = glyphs;
    }

    public byte[] getOriginalPage() {
        return originalPage;
    }

    public void setOriginalPage(byte[] originalPage) {
        this.originalPage = originalPage;
    }

    public byte[] getOverturnedPage() {
        return overturnedPage;
    }

    public void setOverturnedPage(byte[] overturnedPage) {
        this.overturnedPage = overturnedPage;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

}
