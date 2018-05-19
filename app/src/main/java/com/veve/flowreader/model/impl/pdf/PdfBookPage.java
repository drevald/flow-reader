package com.veve.flowreader.model.impl.pdf;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

import com.artifex.mupdf.fitz.Page;
import com.artifex.mupdf.fitz.android.AndroidDrawDevice;
import com.veve.flowreader.model.BookPage;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageGlyph;
import com.veve.flowreader.model.PageSource;
import com.veve.flowreader.model.impl.DevicePageContextImpl;
import com.veve.flowreader.model.impl.djvu.PageRegion;
import com.veve.flowreader.model.impl.djvu.PageUtil;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PdfBookPage implements BookPage, PageSource {

    private Page page;

    private int dpi;

    public PdfBookPage(Page page){
        this.page = page;
    }

    @Override
    public PageGlyph getNextGlyph() {
        return null;
    }

    @Override
    public void reset() {

    }

    @Override
    public Bitmap getAsBitmap(DevicePageContext context) {
        return AndroidDrawDevice.drawPage(page, context.getDisplayDpi());
    }

    @Override
    public Bitmap getAsBitmap() {
        DevicePageContext context = new DevicePageContextImpl();
        context.setDisplayDpi(72);
        return getAsBitmap(context);
    }

    @Override
    public List<Rect> getGlyphs() {

//        List<Rect> list = new ArrayList<Rect>();
//        Bitmap bitmap = getAsBitmap();
//        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
//        list.add(rect);
//        return list;

        List<android.graphics.Rect> list = new ArrayList<android.graphics.Rect>();

        Bitmap bitmap = getAsBitmap();
        int iBytes = bitmap.getWidth() * bitmap.getHeight() * 4;
        ByteBuffer buffer = ByteBuffer.allocate(iBytes);
        bitmap.copyPixelsToBuffer(buffer);


        byte[] imageBytes= buffer.array();
        int width = getWidth();
        int height = getHeight();
        Bitmap.Config bitmapConfig = Bitmap.Config.ARGB_8888;
        //Mat mat = new Mat(height, width , CvType.CV_8UC3);
        Mat mat = new Mat(height, width , CvType.CV_8UC4);
        mat.put(0,0,imageBytes, 0, imageBytes.length);
        int[] rectangleInfo = new int[5];

        Mat dst = new Mat();
        Imgproc.cvtColor(mat, dst, Imgproc.COLOR_BGR2GRAY);

        Imgproc.adaptiveThreshold(dst, dst, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C,
                Imgproc.THRESH_BINARY, 15, 40);

        Imgproc.blur(dst, dst, new Size(5,3));
        Core.bitwise_not(dst,dst);

        Core.compare(dst,new Scalar(3), dst, Core.CMP_GT);
        Mat labeled = new Mat(dst.size(), dst.type());

        // Extract components
        Mat rectComponents = Mat.zeros(new Size(0, 0), 0);
        Mat centComponents = Mat.zeros(new Size(0, 0), 0);
        Imgproc.connectedComponentsWithStats(dst, labeled, rectComponents, centComponents);

        // Collect regions info

        List<PageRegion> regions = new ArrayList<>();

        Map<Integer,List<PageRegion>> map = new HashMap<>();

        for(int i = 1; i < rectComponents.rows(); i++) {

            // Extract bounding box
            rectComponents.row(i).get(0, 0, rectangleInfo);
            org.opencv.core.Rect rectangle = new org.opencv.core.Rect(rectangleInfo[0], rectangleInfo[1], rectangleInfo[2], rectangleInfo[3]);
            PageRegion reg = new PageRegion(rectangle);
            regions.add(reg);
            // Imgproc.rectangle(mat, new Point(rectangleInfo[0],rectangleInfo[1]),
            //         new Point(rectangleInfo[0]+rectangleInfo[2],rectangleInfo[1]+rectangleInfo[3]),
            //         new Scalar(255,0,0));
        }

        regions = PageUtil.sortRegions(regions);


        for (int i=0;i<regions.size();i++) {
            PageRegion reg = regions.get(i);
            //Imgproc.putText(mat,String.valueOf(i), new Point(reg.getRect().x,reg.getRect().y), 0, 1, new Scalar(255,0,0));
            org.opencv.core.Rect rect = reg.getRect();
            list.add(new android.graphics.Rect(rect.x, rect.y, rect.x+rect.width, rect.y+rect.height));
        }

        // Free memory
        rectComponents.release();
        centComponents.release();

        return list;

    }


    @Override
    public Bitmap getAsOriginalBitmap(DevicePageContext context) {
        return getAsBitmap();
    }

    @Override
    public int getWidth() {
        return (int)(page.getBounds().x1 - page.getBounds().x0);
    }

    @Override
    public int getHeight() {
        return (int)(page.getBounds().y1 - page.getBounds().y0);
    }



}
