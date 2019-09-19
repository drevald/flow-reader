package com.veve.flowreader.dao;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class PageGlyphRecord {

    public PageGlyphRecord(long bookId, int position, int x, int y, int width, int height, int baselineShift, int averageHeight, boolean indented) {
        this.bookId = bookId;
        this.position = position;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.baselineShift = baselineShift;
        this.averageHeight = averageHeight;
        this.indented = indented;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getBookId() {
        return bookId;
    }

    public void setBookId(long bookId) {
        this.bookId = bookId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getBaselineShift() {
        return baselineShift;
    }

    public void setBaselineShift(int baselineShift) {
        this.baselineShift = baselineShift;
    }

    public int getAverageHeight() {
        return averageHeight;
    }

    public void setAverageHeight(int averageHeight) {
        this.averageHeight = averageHeight;
    }

    @PrimaryKey
    private long id;
    private long bookId;
    private int position;
    private int x;
    private int y;
    private int width;
    private int height;
    private int baselineShift;
    private int averageHeight;
    private boolean indented;

    public boolean isIndented() {
        return indented;
    }

    public void setIndented(boolean indented) {
        this.indented = indented;
    }

    //    private static Paint paint = new Paint();
//
//    private static Paint paint_debug = new Paint();
//
//    private Bitmap bitmap;
//
//    private int baseLineShift;
//
//    private int averageHeight;
//
//    private int x, y;
//
//    private boolean indented;

}
