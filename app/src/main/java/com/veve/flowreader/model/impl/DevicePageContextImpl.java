package com.veve.flowreader.model.impl;

import android.graphics.Canvas;
import android.graphics.Point;
import android.util.Log;

import com.veve.flowreader.model.DevicePageContext;

/**
 * Created by ddreval on 17.01.2018.
 */

public class DevicePageContextImpl implements DevicePageContext {

    private static final float DEFAULT_ZOOM = 1f;

    private static final float DEFAULT_FONT_SIZE = 24;

    private static final float DEFAULT_LEADING = 0.5f * DEFAULT_FONT_SIZE;

    private static final float DEFAULT_KERNING = 0.5f; // 0.1f * DEFAULT_FONT_SIZE;

    private static final int DEFAULT_MARGIN = 25;

    private Point startPoint;

    private Point remotestPoint;

    private Canvas canvas;

    private float zoom;

    private int width;

    private float kerning;

    private float leading;

    private int margin;

    public DevicePageContextImpl() {

    }

    public DevicePageContextImpl(int width) {
        this.zoom = DEFAULT_ZOOM;
        this.kerning = DEFAULT_KERNING;
        this.leading = DEFAULT_LEADING;
        this.width = width;
        this.margin = DEFAULT_MARGIN;
        this.startPoint = new Point(margin, 0);
        this.remotestPoint = new Point(margin, 0);
    }

    public DevicePageContextImpl(Canvas canvas) {
        this.zoom = DEFAULT_ZOOM;
        this.kerning = DEFAULT_KERNING;
        this.leading = DEFAULT_LEADING;
        this.canvas = canvas;
        this.width = canvas.getWidth();
        this.margin = DEFAULT_MARGIN;
        this.startPoint = new Point(margin, 0);
        this.remotestPoint = new Point(margin, 0);
    }

    @Override
    public int getMargin() {return margin;}

    @Override
    public void setMargin(int margin) {this.margin = margin;}

    @Override
    public Point getStartPoint() {
        return startPoint;
    }

    @Override
    public void setStartPoint(Point startPoint) {
        this.startPoint = startPoint;
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

    @Override
    public void setRemotestPoint(Point remotestPoint) {
        this.remotestPoint = remotestPoint;
    }

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
        this.startPoint = new Point(margin, 0);
        this.remotestPoint = new Point(margin, 0);
    }

}
