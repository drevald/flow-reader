package com.veve.flowreader.model.impl;

import android.graphics.Bitmap;

import com.veve.flowreader.model.PageGlyph;
import com.veve.flowreader.model.PageLayoutParser;

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

class OpenCvPageLayoutParserImpl implements PageLayoutParser {

    @Override
    public List<PageGlyph> getGlyphs(Bitmap bitmap) {

        List<PageGlyph> list = new ArrayList<PageGlyph>();

        int iBytes = bitmap.getWidth() * bitmap.getHeight() * 4;
        ByteBuffer buffer = ByteBuffer.allocate(iBytes);
        bitmap.copyPixelsToBuffer(buffer);

        byte[] imageBytes= buffer.array();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap.Config bitmapConfig = Bitmap.Config.ARGB_8888;
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
            org.opencv.core.Rect rectangle = new org.opencv.core.Rect(rectangleInfo[0],
                    rectangleInfo[1], rectangleInfo[2], rectangleInfo[3]);
            PageRegion reg = new PageRegion(rectangle);
            regions.add(reg);
        }

        regions = PageUtil.sortRegions(regions);

        for (int i=0;i<regions.size();i++) {
            PageRegion reg = regions.get(i);
            org.opencv.core.Rect rect = reg.getRect();
            //Bitmap newBitmap = Bitmap.createBitmap(bitmap, rect.x, rect.y, rect.x+rect.width,rect.y+rect.height);
            Bitmap newBitmap = Bitmap.createBitmap(bitmap, rect.x, rect.y, rect.width,rect.height);
            list.add(new PageGlyphImpl(newBitmap));
        }

        // Free memory
        rectComponents.release();
        centComponents.release();

        return list;

    }

}
