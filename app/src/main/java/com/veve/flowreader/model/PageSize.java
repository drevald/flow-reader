package com.veve.flowreader.model;

public class PageSize {

    private int pageWidth;
    private int pageHeight;

    public PageSize() {
        pageWidth = 0;
        pageHeight = 0;
    }

    public PageSize(int width, int height) {
        pageHeight = height;
        pageWidth = width;
    }

    public int getPageWidth() {
        return pageWidth;
    }

    public int getPageHeight() {
        return pageHeight;
    }

    public void setPageWidth(int pageWidth) {
        this.pageWidth = pageWidth;
    }

    public void setPageHeight(int pageHeight) {
        this.pageHeight = pageHeight;
    }

}