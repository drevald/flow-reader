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

    public BookRecord() {
    }

    @Ignore
    public BookRecord(Long id, Integer currentPage, Integer pagesCount, String name, String url) {
        this.id = id;
        this.currentPage = currentPage;
        this.pagesCount = pagesCount;
        this.name = name;
        this.url = url;
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
