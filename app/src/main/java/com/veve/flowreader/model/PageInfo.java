package com.veve.flowreader.model;

public class PageInfo {

    private int pageWidth;
    private int pageHeight;
    private int resolution;

    public PageInfo() {
        pageWidth = 0;
        pageHeight = 0;
    }

    public PageInfo(int width, int height) {
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

    public int getResolution() {
        return resolution;
    }

    public void setResolution(int resolution) {
        this.resolution = resolution;
    }
}
