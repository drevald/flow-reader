package com.veve.flowreader.com.veve.flowreader.algorithm;

import com.veve.flowreader.algorithm.Enclosure;

import org.junit.Test;
import org.opencv.core.Rect;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class EnclosureTest {



//               +------------------------+
//               |                        |
//               |       +--------+       |
//               |       |        |       |
//               |       |        |       |
//               |       |        |       |
//               |       |        |       |
//               |       +--------+       |
//               |                        |
//               |                        |
//               +------------------------+
//
//
//
//               +-------------------------------+
//               |                               |
//               |                               |
//               |                +--------+     |
//               |                |        |     |
//               |                |        |     |
//               |                |        |     |
//               |                |        |     |
//               |      +------------------+     |
//               |      |         |              |
//               |      |         |              |
//               |      |         |              |
//               |      |         |              |
//               |      +---------+              |
//               |                               |
//               |                               |
//               |                               |
//               +-------------------------------+

// There are 2 big rectanges

    @Test
    public void testSolve() {
        List<Rect> rects = new ArrayList<>();
        rects.add(new Rect(0,0,4,4)); // big
        rects.add(new Rect(1,1,1,1)); // small
        rects.add(new Rect(2,2,1,1)); // small

        rects.add(new Rect(1,5,3,3)); // big
        rects.add(new Rect(3,6,1,1)); // small

        Enclosure enc = new Enclosure(rects);
        Set<Rect> rects1 = enc.find();

        System.out.println(rects1);

        assertEquals(2, rects1.size());

    }

}