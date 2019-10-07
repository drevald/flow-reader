package com.veve.flowreader.dao;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.veve.flowreader.model.PageGlyph;
import com.veve.flowreader.model.impl.PageGlyphImpl;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;

@Entity
public class PageGlyphRecord implements Serializable {

    private static final String GLYPH_JSON = "{\"x\":\"%d\", \"y\":\"%d\", \"width\":\"%d\", \"height\":\"%d\", \"baselineShift\":\"%d\",\"averageHeight\":\"%d\",\"indented\":\"%b\"}";

    @PrimaryKey(autoGenerate = true)
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
    private boolean isSpace;

    @Ignore
    public PageGlyphRecord() {

    }

    public PageGlyphRecord(long bookId, int position, int x, int y, int width, int height, int baselineShift, int averageHeight, boolean indented, boolean isSpace) {
        this.bookId = bookId;
        this.position = position;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.baselineShift = baselineShift;
        this.averageHeight = averageHeight;
        this.indented = indented;
        this.isSpace = isSpace;
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

    public boolean isIndented() {
        return indented;
    }

    public void setIndented(boolean indented) {
        this.indented = indented;
    }

    public boolean isSpace() {
        return isSpace;
    }

    public void setSpace(boolean space) {
        isSpace = space;
    }

    public boolean equals(PageGlyphRecord record) {
        return this.indented == record.indented
                &&this.x == record.x
                &&this.y == record.y
                &&this.width == record.width
                &&this.height == record.height
                &&this.baselineShift == record.baselineShift
                &&this.averageHeight == record.averageHeight
                &&this.isSpace == record.isSpace;
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + x;
        result = prime * result + y;
        result = prime * result + width;
        result = prime * result + height;
        result = prime * result + baselineShift;
        result = prime * result + averageHeight;
        result = prime * result + (indented ? 0 : 1);
        return result;
    }

}

