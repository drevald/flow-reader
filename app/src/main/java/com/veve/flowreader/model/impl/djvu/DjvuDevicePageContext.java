package com.veve.flowreader.model.impl.djvu;

import android.graphics.Canvas;
import android.graphics.Point;

import com.veve.flowreader.model.DevicePageContext;

/**
 * Created by sergey on 10.03.18.
 */

public class DjvuDevicePageContext implements DevicePageContext {
    @Override
    public Point getStartPoint() {
        return null;
    }

    @Override
    public void setStartPoint(Point point) {

    }

    @Override
    public Canvas getCanvas() {
        return null;
    }

    @Override
    public void setCanvas(Canvas canvas) {

    }

    @Override
    public float getZoom() {
        return 0;
    }

    @Override
    public void setZoom(float zoom) {

    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public Point getRemotestPoint() {
        return null;
    }

    @Override
    public void setRemotestPoint(Point point) {

    }

    @Override
    public void resetPosition() {

    }

    @Override
    public float getKerning() {
        return 0;
    }

    @Override
    public void setKerning(float kerning) {

    }

    @Override
    public float getLeading() {
        return 0;
    }

    @Override
    public void setLeading(float leading) {

    }
}