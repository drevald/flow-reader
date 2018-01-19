package com.veve.flowreader.model.impl;

import android.graphics.Canvas;
import android.graphics.Point;

import com.veve.flowreader.model.DevicePageContext;

/**
 * Created by ddreval on 17.01.2018.
 */

public class DevicePageContextImpl implements DevicePageContext {

    private Point startPoint;

    private Canvas canvas;

    private float zoom;

    public DevicePageContextImpl() {

    }

    public DevicePageContextImpl(Canvas canvas) {
        this.canvas = canvas;
        this.zoom = zoom;
        this.startPoint = new Point(0, 0);
    }

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

}
