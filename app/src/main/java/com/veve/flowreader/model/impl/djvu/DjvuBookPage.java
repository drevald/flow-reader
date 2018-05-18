package com.veve.flowreader.model.impl.djvu;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.veve.flowreader.model.BookPage;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageGlyph;
import com.veve.flowreader.model.PageSource;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by sergey on 10.03.18.
 */

public class DjvuBookPage implements BookPage, PageSource {

    private int pageNumber;
    private long bookId;

    public DjvuBookPage(long  bookId, int pageNumber) {
        this.bookId = bookId;
        this.pageNumber = pageNumber;
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
        return getAsBitmap();
    }

    public Bitmap getAsBitmap() {

        byte[] imageBytes= getBytes(bookId, pageNumber);
        int width = getWidth();
        int height = getHeight();
        Bitmap.Config bitmapConfig = Bitmap.Config.ARGB_8888;
        Mat mat = new Mat(height, width ,CvType.CV_8UC3);
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
            Rect rectangle = new Rect(rectangleInfo[0], rectangleInfo[1], rectangleInfo[2], rectangleInfo[3]);
            PageRegion reg = new PageRegion(rectangle);
            regions.add(reg);
            //Imgproc.rectangle(mat, new Point(rectangleInfo[0],rectangleInfo[1]),
            //        new Point(rectangleInfo[0]+rectangleInfo[2],rectangleInfo[1]+rectangleInfo[3]),
            //        new Scalar(255,0,0));
        }

        regions = PageUtil.sortRegions(regions);


        //for (int i=0;i<regions.size();i++) {
        //    PageRegion reg = regions.get(i);
        //    Imgproc.putText(mat,String.valueOf(i), new Point(reg.getRect().x,reg.getRect().y), 0, 1, new Scalar(255,0,0));
        //}

        // Free memory
        rectComponents.release();
        centComponents.release();

        Bitmap bitmap = Bitmap.createBitmap(width, height, bitmapConfig);
        Utils.matToBitmap(mat, bitmap);
        return bitmap;
    }

    public List<android.graphics.Rect> getGlyphs() {

            List<android.graphics.Rect> list = new ArrayList<android.graphics.Rect>();

        byte[] imageBytes= getBytes(bookId, pageNumber);
            int width = getWidth();
            int height = getHeight();
            Bitmap.Config bitmapConfig = Bitmap.Config.ARGB_8888;
            Mat mat = new Mat(height, width ,CvType.CV_8UC3);
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
                Rect rectangle = new Rect(rectangleInfo[0], rectangleInfo[1], rectangleInfo[2], rectangleInfo[3]);
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
                Rect rect = reg.getRect();
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
        return getNativeWidth(bookId, pageNumber);
    }

    @Override
    public int getHeight() {
        return getNativeHeight(bookId, pageNumber);
    }

    private static native byte[] getBytes(long bookId, int pageNumber);

    private static native int getNativeWidth(long bookId, int pageNumber);
    private static native int getNativeHeight(long bookId, int pageNumber);

}
