package com.veve.flowreader.model.impl;

import android.graphics.Canvas;
import android.graphics.Point;

import com.veve.flowreader.model.DevicePageContext;

/**
 * Created by ddreval on 17.01.2018.
 */

public class DevicePageContextImpl extends DevicePageContext {

    private static final float DEFAULT_ZOOM = 1f;

    private static final float DEFAULT_FONT_SIZE = 24;

    private static final float DEFAULT_LEADING = 0.5f * DEFAULT_FONT_SIZE;

    private static final float DEFAULT_KERNING = 0.5f; // 0.1f * DEFAULT_FONT_SIZE;

    private static final int DEFAULT_MARGIN = 25;

    private static final int DEFAULT_DISPLAY_DPI = 72;

    private Point startPoint;

    private Point remotestPoint;

    private Canvas canvas;

    private float zoom;

    private int width;

    private int lineHeight;

    private float kerning;

    private float leading;

    private float margin;

    private int displayDpi;

    private int currentBaseLine;

    public DevicePageContextImpl(int width) {
        this.zoom = DEFAULT_ZOOM;
        this.kerning = DEFAULT_KERNING;
        this.leading = DEFAULT_LEADING;
        this.width = width;
        this.margin = 1.0f;
        this.startPoint = new Point((int)margin*DEFAULT_MARGIN, 0);
        this.remotestPoint = new Point((int)margin*DEFAULT_MARGIN, 0);
        this.displayDpi = DEFAULT_DISPLAY_DPI;
        this.currentBaseLine = 0;
    }

    @Override
    public void setCurrentBaseLine(int baseLine) {
        this.currentBaseLine = baseLine;
    }

    @Override
    public int getCurrentBaseLine() {
        return currentBaseLine;
    }

    @Override
    public int getLineHeight() {
        return lineHeight;
    }

    @Override
    public void setLineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
    }

    @Override
    public float getMargin() {return margin;}

    @Override
    public void setMargin(float margin) {this.margin = margin;}

    @Override
    public Point getStartPoint() {
        return startPoint;
    }

    @Override
    public Canvas getCanvas() {
        return canvas;
    }

    @Override
    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    @Override
    public float getZoom() {
        return zoom;
    }

    @Override
    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    @Override
    public int getWidth() {return this.width;}

    @Override
    public Point getRemotestPoint() {return this.remotestPoint;}

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

    @Override
    public void resetPosition() {
        this.startPoint = new Point((int)margin*DEFAULT_MARGIN, 0);
        this.remotestPoint = new Point((int)margin*DEFAULT_MARGIN, 0);
    }

}
