package com.veve.flowreader.model.impl.djvu;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import android.graphics.Region;
import android.util.Log;
import android.widget.ImageView;

import org.opencv.core.*;

import com.veve.flowreader.R;
import com.veve.flowreader.model.BookPage;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageGlyph;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by sergey on 10.03.18.
 */

public class DjvuBookPage implements BookPage {



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

    private int[] tranformBytes(byte[] imageBytes, int width, int height) {

        int[] bitmapPixels = new int[width * height];

        class PageJob implements Runnable {

            int from;
            int to;
            PageJob(int from, int to) {
                this.from = from;
                this.to = to;
            }

            @Override
            public void run() {
                for (int i = from; i < to; ++i) {
                    bitmapPixels[i] = Color.rgb(imageBytes[3*i], imageBytes[3*i+1],imageBytes[3*i+2]);
                }
            }
        }

        int cores = Runtime.getRuntime().availableProcessors();

        ExecutorService executor = Executors.newFixedThreadPool(cores);
        int size = bitmapPixels.length;

        int end = 0;
        for (int i=0;i<=cores;i++) {
            int start = end;
            end = (i*size)/cores;
            Runnable job = new PageJob(start, end);
            executor.execute(job);
        }

        executor.shutdown();

        while (!executor.isTerminated()) {
        }

        return bitmapPixels;
    }

    @Override
    public Bitmap getAsBitmap(DevicePageContext context) {

        byte[] imageBytes= getBytes(bookId, pageNumber);
        int width = getWidth();
        int height = getHeight();
        Bitmap.Config bitmapConfig = Bitmap.Config.ARGB_8888;
        Mat mat = new Mat(height, width ,CvType.CV_8UC3);
        mat.put(0,0,imageBytes, 0, imageBytes.length);


        Mat dst = new Mat();

        Imgproc.cvtColor(mat, dst, Imgproc.COLOR_BGR2GRAY);

        Imgproc.adaptiveThreshold(dst, dst, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C,
                Imgproc.THRESH_BINARY, 15, 40);

        Mat lines = new Mat();
        Mat kernel = Mat.ones(5,100, CvType.CV_8UC3);
        Imgproc.blur(dst, lines, new Size(100,5));

        final List<MatOfPoint> points = new ArrayList<>();
        final Mat hierarchy = new Mat();
        Imgproc.findContours(lines, points, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);


        Iterator<MatOfPoint> iterator = points.iterator();
        int j = -1;
        while (iterator.hasNext()){
            j++;

            MatOfPoint contour = iterator.next();
            RotatedRect rect = Imgproc.minAreaRect(new MatOfPoint2f(contour.toArray()));
            Log.d("TEST1", rect.toString());
            //Imgproc.rectangle(mat, new Point(rect.boundingRect().x,rect.boundingRect().y),
            //        new Point(rect.boundingRect().x+rect.boundingRect().width,rect.boundingRect().y+rect.boundingRect().height),
            //        new Scalar(0,255,0));
            Imgproc.drawContours(mat,points,j,new Scalar(0,255,0));

        }

        Imgproc.blur(dst, dst, new Size(5,1));

        Core.bitwise_not(dst,dst);

        Core.compare(dst,new Scalar(3), dst, Core.CMP_GT);
        Mat labeled = new Mat(dst.size(), dst.type());

        // Extract components
        Mat rectComponents = Mat.zeros(new Size(0, 0), 0);
        Mat centComponents = Mat.zeros(new Size(0, 0), 0);
        Imgproc.connectedComponentsWithStats(dst, labeled, rectComponents, centComponents);

        // Collect regions info
        int[] rectangleInfo = new int[5];
        double[] centroidInfo = new double[2];
        List<Rect> regions = new ArrayList<>();

        for(int i = 1; i < rectComponents.rows(); i++) {

            // Extract bounding box
            rectComponents.row(i).get(0, 0, rectangleInfo);
            Rect rectangle = new Rect(rectangleInfo[0], rectangleInfo[1], rectangleInfo[2], rectangleInfo[3]);
            regions.add(rectangle);
            Imgproc.rectangle(mat, new Point(rectangleInfo[0],rectangleInfo[1]),
                    new Point(rectangleInfo[0]+rectangleInfo[2],rectangleInfo[1]+rectangleInfo[3]),
                    new Scalar(255,0,0));
            //Imgproc.putText(mat,String.valueOf(i), new Point(rectangleInfo[0],rectangleInfo[1]), 0, 1, new Scalar(255,0,0));
        }





        //for (int i=0;i<regions.size();i++) {
         //   Rect rect = regions.get(i);
          //  Imgproc.putText(mat,String.valueOf(i), new Point(rect.x,rect.y), 0, 1, new Scalar(255,0,0));
        //}

        // Free memory
        rectComponents.release();
        centComponents.release();

        Bitmap bitmap = Bitmap.createBitmap(width, height, bitmapConfig);
        Utils.matToBitmap(mat, bitmap);
        return bitmap;
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

    public static class Region {
        private Rect bounding;
        private Point centroid;

        public Region(Rect bounding, Point centroid) {
            this.bounding = bounding;
            this.centroid = centroid;
        }

        public Rect getBounding() {
            return bounding;
        }

        public Point getCentroid() {
            return centroid;
        }
    }
}
