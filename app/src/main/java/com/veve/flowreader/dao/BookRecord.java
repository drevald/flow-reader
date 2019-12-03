package com.veve.flowreader.dao;


import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Created by ddreval on 4/3/2018.
 */

@Entity
public class BookRecord {

    @PrimaryKey
    private Long id;
    private Integer currentPage;
    private Integer pagesCount;
    private String title;
    private String originalTitle;
    private String author;
    private String url;
    private float zoom = 1f;
    private float zoomOriginal = 1f;
    private float kerning = 0.5f;
    private float leading = 12f;
    private float margin = 1.0f;
    private int mode = 2;
    private int scrollOffset = 0;
    private byte[] preview;
    private String md5;
    private boolean preprocessing = false;

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    private long size;

    public BookRecord() {
        this(null, 0, 0, null, null);
    }

    @Ignore
    public BookRecord(Long id, Integer currentPage, Integer pagesCount, String name, String url) {
        this.id = id;
        this.currentPage = currentPage;
        this.pagesCount = pagesCount;
        this.title = name;
        this.url = url;
    }

    public float getZoom() {
        return zoom;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    public float getKerning() {
        return kerning;
    }

    public void setKerning(float kerning) {
        this.kerning = kerning;
    }

    public float getLeading() {
        return leading;
    }

    public void setLeading(float leading) {
        this.leading = leading;
    }

    public float getMargin() {
        return margin;
    }

    public void setMargin(float margin) {
        this.margin = margin;
    }

    public byte[] getPreview() {
        return preview;
    }

    public void setPreview(byte[] preview) {
        this.preview = preview;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public Integer getPagesCount() {
        return pagesCount;
    }

    public void setPagesCount(Integer pagesCount) {
        this.pagesCount = pagesCount;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getMode() { return mode; }

    public void setMode(int mode) { this.mode = mode;}

    public int getScrollOffset() {return scrollOffset; }

    public void setScrollOffset(int scrollOffset) {this.scrollOffset = scrollOffset; }

    public void setPreprocessing(boolean preprocessing) {
        this.preprocessing = preprocessing;
    }

    public boolean getPreprocessing() {
        return this.preprocessing;
    }
    public float getZoomOriginal() { return zoomOriginal; }

    public void setZoomOriginal(float zoomOriginal) {
        this.zoomOriginal = zoomOriginal;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

}
