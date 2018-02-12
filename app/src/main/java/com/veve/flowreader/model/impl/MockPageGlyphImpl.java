package com.veve.flowreader.model.impl;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageGlyph;

import java.util.Random;

/**
 * Created by ddreval on 15.01.2018.
 */

public class MockPageGlyphImpl implements PageGlyph {

    String str;
    public static Rect rect = new Rect();
    public static Paint paint = new Paint();
    private static Paint paint1 = new Paint();

    public MockPageGlyphImpl(String aStr) {
        str = aStr;
        paint.setTextSize(48);
        paint.setFakeBoldText(true);
        paint1.setStyle(Paint.Style.STROKE);
    }

    @Override
    public void draw(DevicePageContext context) {
        Point startPoint = context.getStartPoint();
        Canvas canvas = context.getCanvas();

        //canvas.drawRect(0, 100, 400, 200, new Paint(Color.RED));

        if(getWidth(context) + startPoint.x > canvas.getWidth()) {
            startPoint.set(0, startPoint.y + getHeight(context));
            Log.i("MockGlyph.Draw","New line with y=" + startPoint.y + " + after " + str);
        }
        canvas.drawText(str, startPoint.x, startPoint.y + getHeight(context), paint);
        canvas.drawRect(startPoint.x, startPoint.y, startPoint.x+getWidth(context), startPoint.y+ getHeight(context), paint1);
        canvas.drawText("O",0, 0, paint);
//        Log.i("tag", "canvas.drawText("+str+", "+startPoint.x+", "+startPoint.y+", "+paint+");");
        context.getStartPoint().set(startPoint.x + getWidth(context), startPoint.y);
        context.getRemotestPoint().set(startPoint.x + getWidth(context), startPoint.y+getHeight(context));
    }

    @Override
    public void virtualDraw(DevicePageContext context) {
        Point startPoint = context.getStartPoint();
        Log.i("CR","y=" + startPoint.y );
        if(getWidth(context) + startPoint.x > context.getWidth()) {
            startPoint.set(0, startPoint.y + getHeight(context));
            Log.i("MockGlyph.Virt","New line with y=" + startPoint.y + " + after " + str);
        }
        context.getStartPoint().set(startPoint.x + getWidth(context), startPoint.y);
        context.getRemotestPoint().set(startPoint.x + getWidth(context), startPoint.y+getHeight(context));
    }

    protected int getWidth(DevicePageContext context) {
        paint.getTextBounds(str, 0, 1, rect);
//        if(context.getCanvas() != null)
//            context.getCanvas().drawRect(
//                    context.getStartPoint().x,
//                    context.getStartPoint().y,
//                    context.getStartPoint().x+rect.width()*context.getZoom(),
//                    context.getStartPoint().y+rect.height()*context.getZoom(),
//                    paint1);
        return (int)(rect.width() * context.getZoom());
    }

    protected int getHeight(DevicePageContext context) {
        paint.getTextBounds(str, 0, 1, rect);
        //Log.i("GLYPH","getHeight = " + rect.height() * context.getZoom());
        return (int)(rect.height() * context.getZoom());
    }

}
