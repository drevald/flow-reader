package com.veve.flowreader.model;

public class PageGlyphInfo {

    private boolean indented;
    private int x;
    private int y;
    private int width;
    private int height;
    private int averageHeight;
    private int baselineShift;
    private boolean space;
    private boolean last;
    private boolean picture;

    public boolean isSpace() {
        return space;
    }

    public PageGlyphInfo(boolean indented, int x, int y, int width, int height,
                         int averageHeight, int baselineShift, boolean space, boolean last, boolean picture) {
        this.indented = indented;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.averageHeight = averageHeight;
        this.baselineShift = baselineShift;
        this.space = space;
        this.last = last;
        this.picture = picture;
    }


    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getAverageHeight() {
        return averageHeight;
    }

    public int getBaselineShift() {
        return baselineShift;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isIndented() {
        return indented;
    }

    public boolean isLast() {return last;}

    public boolean isPicture() {
        return picture;
    }
}