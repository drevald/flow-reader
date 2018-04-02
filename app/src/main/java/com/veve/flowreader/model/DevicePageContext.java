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

    public int getWidth();

    public Point getRemotestPoint();

    public void setRemotestPoint(Point point);

    public void resetPosition();

    public float getKerning();

    public void setKerning(float kerning);

    public float getLeading();

    public void setLeading(float leading);

    public int getMargin();

    public void setMargin(int margin);

}
