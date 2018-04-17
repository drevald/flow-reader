package com.veve.flowreader.dao;

/**
 * Created by ddreval on 4/3/2018.
 */

public class BookRecord {

    public BookRecord(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    String url;

}
