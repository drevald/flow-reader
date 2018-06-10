package com.veve.flowreader.dao;

/**
 * Created by ddreval on 4/3/2018.
 */

public class BookRecord {

    public BookRecord(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public BookRecord(String url) {
        this.url = url;
    }

    public Long getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getPagesCount() {
        return pagesCount;
    }

    public void setPagesCount(Integer pagesCount) {
        this.pagesCount = pagesCount;
    }

    Integer pagesCount;

    String name;

    String url;

    Long id;

}
