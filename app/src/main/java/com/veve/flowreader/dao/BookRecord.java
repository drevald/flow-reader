package com.veve.flowreader.dao;


import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by ddreval on 4/3/2018.
 */

@Entity
public class BookRecord {

    @PrimaryKey
    private Long id;
    private Integer currentPage;
    private Integer pagesCount;
    private String name;
    private String url;
    private float zoom = 1f;
    private float kerning = 0.5f;
    private float leading = 12f;
    private int margin = 25;
    private byte[] preview;

    public BookRecord() {
        this(null, 0, 0, null, null);
    }

    @Ignore
    public BookRecord(Long id, Integer currentPage, Integer pagesCount, String name, String url) {
        this.id = id;
        this.currentPage = currentPage;
        this.pagesCount = pagesCount;
        this.name = name;
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

    public int getMargin() {
        return margin;
    }

    public void setMargin(int margin) {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

}
