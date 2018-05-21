package com.veve.flowreader.model.impl.djvu;

import org.opencv.core.Rect;

public class PageRegion {

    private Rect rect;
    private int x;
    private int y;

    public PageRegion(Rect rect) {
       this.rect = rect;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Rect getRect() {
        return rect;
    }

    @Override
    public String toString() {
        return "PageRegion{" +
                "rect=" + rect +
                ", x=" + x +
                ", y=" + y +
                '}';
    }
}
