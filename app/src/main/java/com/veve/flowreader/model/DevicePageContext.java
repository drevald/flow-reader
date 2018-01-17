package com.veve.flowreader.model;

import android.graphics.Canvas;
import android.graphics.Point;

/**
 * Created by ddreval on 16.01.2018.
 */

public interface DevicePageContext {

    public Point getStartPoint();

    public void setStartPoint(Point point);

    public Canvas getCanvas();

    public void setCanvas(Canvas canvas);

    public float getZoom();

    public void setZoom(float zoom);

}
