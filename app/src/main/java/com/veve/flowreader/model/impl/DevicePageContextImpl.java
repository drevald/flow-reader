package com.veve.flowreader.model.impl;

import android.graphics.Canvas;
import android.graphics.Point;
import android.util.Log;

import com.veve.flowreader.model.DevicePageContext;

/**
 * Created by ddreval on 17.01.2018.
 */

public class DevicePageContextImpl implements DevicePageContext {

    private static final float DEFAULT_ZOOM = 0.3f;

    private Point startPoint;

    private Point remotestPoint;

    private Canvas canvas;

    private float zoom;

    private int width;

    public DevicePageContextImpl() {

    }

    public DevicePageContextImpl(int width) {
        this.zoom = DEFAULT_ZOOM;
        this.width = width;
        this.startPoint = new Point(0, 0);
        this.remotestPoint = new Point(0, 0);
    }

    public DevicePageContextImpl(Canvas canvas) {
        this.canvas = canvas;
        this.width = canvas.getWidth();
        this.zoom = DEFAULT_ZOOM;
        this.startPoint = new Point(0, 0);
        this.remotestPoint = new Point(0, 0);
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

    @Override
    public int getWidth() {return this.width;}

    @Override
    public Point getRemotestPoint() {return this.remotestPoint;}

    @Override
    public void setRemotestPoint(Point remotestPoint) {
        this.remotestPoint = remotestPoint;
    }

    @Override
    public void resetPosition() {
        this.startPoint = new Point(0, 0);
        this.remotestPoint = new Point(0, 0);
        Log.i("Context", String.format("Reset as StartPoint(%d. %d) RemotePoint(%d. %d)",
                startPoint.x, startPoint.y, remotestPoint.x, remotestPoint.y));
    }
}
