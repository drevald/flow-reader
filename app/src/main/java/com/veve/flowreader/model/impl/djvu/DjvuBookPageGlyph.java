package com.veve.flowreader.model.impl.djvu;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageGlyph;

/**
 * Created by ddreval on 19.04.2018.
 */

public class DjvuBookPageGlyph implements PageGlyph {

    private static Paint paint = new Paint();

    private Bitmap bitmap;

    private Rect rect;

    public DjvuBookPageGlyph(Bitmap bitmap, Rect rect) {
        this.bitmap = bitmap;
        this.rect = rect;
    }

    @Override
    public void draw(DevicePageContext context, boolean show) {
        context.getCanvas();
        Point startPoint = context.getStartPoint();
        Canvas canvas = context.getCanvas();
        int __height = bitmap.getHeight();
        int __width = bitmap.getWidth();
        if(__width * context.getZoom() + startPoint.x > context.getWidth() - context.getMargin()) {
            startPoint.set(context.getMargin(), startPoint.y + (int)(__height * context.getZoom())
                    + (int)(context.getLeading()* context.getZoom()));
        }
        Rect __srcRect = new Rect(0, 0, __width, __height);
        Rect __dstRect = new Rect(startPoint.x , startPoint.y,
                startPoint.x +(int)(__width * context.getZoom()),
                startPoint.y + (int)(__height * context.getZoom()));
        if(show) {
            canvas.drawBitmap(bitmap, __srcRect, __dstRect, paint);
        }
        context.getStartPoint().set(startPoint.x + __dstRect.width()
                + (int)(context.getKerning()* context.getZoom()), startPoint.y);
        context.getRemotestPoint().set(startPoint.x + __dstRect.width()
                + (int)(context.getKerning()* context.getZoom()), startPoint.y + __dstRect.height());
        bitmap = null;
    }

}
