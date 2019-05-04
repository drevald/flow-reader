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

        assertEquals(2, rects1.size());

    }

    @Test
    public void testSolveManyRectangles() {
        List<Rect> rects = new ArrayList<>();

        for (int i=0; i<1000; i++) {
            rects.add(new Rect(2*i,2*i,1,1)); // big
        }

        Enclosure enc = new Enclosure(rects);
        Set<Rect> rects1 = enc.find();

        assertEquals(1000, rects1.size());

    }

    @Test
    public void testNothingToExclude() {
        List<Rect> rects = new ArrayList<>();
        rects.add(new Rect(472, 1748, 168, 31));
        rects.add(new Rect(1236, 1715, 99, 21));
        rects.add(new Rect(464, 1102, 16, 1));
        rects.add(new Rect(994, 2069, 26, 16));
        rects.add(new Rect(1100, 1647, 29, 17));
        rects.add(new Rect(712, 1349, 44, 22));
        rects.add(new Rect(1147, 1501, 189, 22));
        rects.add(new Rect(948, 1303, 31, 23));
        rects.add(new Rect(1181, 1337, 108, 33));
        rects.add(new Rect(269, 1238, 177, 48));
        rects.add(new Rect(842, 1752, 216, 18));
        rects.add(new Rect(861, 1340, 93, 38));
        rects.add(new Rect(1121, 1957, 140, 24));
        rects.add(new Rect(1163, 1610, 172, 26));
        rects.add(new Rect(1060, 1342, 103, 34));
        rects.add(new Rect(887, 1682, 127, 18));
        rects.add(new Rect(279, 258, 51, 32));
        rects.add(new Rect(703, 1611, 204, 26));
        rects.add(new Rect(344, 1435, 109, 32));
        rects.add(new Rect(991, 2013, 183, 23));
        rects.add(new Rect(1188, 2015, 123, 20));
        rects.add(new Rect(514, 1334, 155, 55));
        rects.add(new Rect(523, 1502, 234, 28));
        rects.add(new Rect(322, 205, 54, 32));
        rects.add(new Rect(272, 1381, 75, 34));
        rects.add(new Rect(425, 1611, 215, 21));
        rects.add(new Rect(743, 1646, 129, 19));
        rects.add(new Rect(837, 1574, 190, 19));
        rects.add(new Rect(922, 1608, 233, 27));
        rects.add(new Rect(466, 1435, 287, 32));
        rects.add(new Rect(267, 1745, 158, 29));
        rects.add(new Rect(160, 1293, 96, 33));
        rects.add(new Rect(614, 1717, 152, 26));
        rects.add(new Rect(687, 1339, 34, 32));
        rects.add(new Rect(947, 1929, 40, 26));
        rects.add(new Rect(706, 1647, 27, 18));
        rects.add(new Rect(851, 1502, 288, 26));
        rects.add(new Rect(159, 1821, 404, 29));
        rects.add(new Rect(879, 1643, 212, 26));
        rects.add(new Rect(877, 1569, 20, 3));
        rects.add(new Rect(700, 2145, 15, 2));
        rects.add(new Rect(289, 1929, 268, 34));
        rects.add(new Rect(1021, 2065, 201, 21));
        rects.add(new Rect(163, 254, 111, 40));
        rects.add(new Rect(778, 1714, 153, 24));
        rects.add(new Rect(995, 1926, 195, 32));
        rects.add(new Rect(163, 149, 103, 36));
        rects.add(new Rect(271, 1646, 146, 25));
        rects.add(new Rect(603, 1680, 269, 21));
        rects.add(new Rect(374, 1339, 119, 32));
        rects.add(new Rect(992, 1539, 48, 18));
        rects.add(new Rect(848, 1641, 22, 5));
        rects.add(new Rect(1259, 1824, 73, 27));
        rects.add(new Rect(650, 1745, 183, 27));
        rects.add(new Rect(1232, 1530, 103, 32));
        rects.add(new Rect(903, 1540, 73, 17));
        rects.add(new Rect(1102, 1288, 116, 36));
        rects.add(new Rect(657, 1611, 27, 17));
        rects.add(new Rect(1230, 2064, 75, 21));
        rects.add(new Rect(771, 1503, 68, 28));
        rects.add(new Rect(425, 1393, 67, 23));
        rects.add(new Rect(434, 1754, 29, 18));
        rects.add(new Rect(995, 1955, 124, 21));
        rects.add(new Rect(342, 1496, 169, 29));
        rects.add(new Rect(1051, 1539, 161, 19));
        rects.add(new Rect(947, 1346, 103, 23));
        rects.add(new Rect(268, 1603, 144, 36));
        rects.add(new Rect(619, 147, 728, 494));
        rects.add(new Rect(1137, 1646, 201, 54));
        rects.add(new Rect(356, 1394, 55, 21));
        rects.add(new Rect(1100, 1682, 72, 21));
        rects.add(new Rect(1195, 1924, 138, 39));
        rects.add(new Rect(244, 1842, 183, 32));
        rects.add(new Rect(995, 2036, 249, 25));
        rects.add(new Rect(1281, 1354, 56, 5));
        rects.add(new Rect(450, 1242, 82, 38));
        rects.add(new Rect(339, 1709, 81, 30));
        rects.add(new Rect(271, 1349, 110, 30));
        rects.add(new Rect(716, 1540, 175, 25));
        rects.add(new Rect(999, 1300, 110, 25));
        rects.add(new Rect(195, 1797, 274, 26));
        rects.add(new Rect(279, 151, 85, 32));
        rects.add(new Rect(680, 1304, 123, 23));
        rects.add(new Rect(1030, 1675, 59, 24));
        rects.add(new Rect(830, 1287, 106, 54));
        rects.add(new Rect(830, 1533, 22, 5));
        rects.add(new Rect(1285, 1573, 52, 21));
        rects.add(new Rect(429, 1716, 171, 26));
        rects.add(new Rect(993, 1982, 343, 25));
        rects.add(new Rect(392, 1393, 19, 7));
        rects.add(new Rect(581, 1541, 119, 16));
        rects.add(new Rect(270, 1569, 347, 33));
        rects.add(new Rect(1247, 2036, 87, 26));
        rects.add(new Rect(269, 1679, 134, 25));
        rects.add(new Rect(765, 1346, 85, 34));
        rects.add(new Rect(536, 1541, 27, 16));
        rects.add(new Rect(340, 1294, 322, 35));
        rects.add(new Rect(421, 1640, 129, 25));
        rects.add(new Rect(554, 1647, 142, 18));
        rects.add(new Rect(269, 1540, 28, 18));
        rects.add(new Rect(1040, 1573, 231, 24));
        rects.add(new Rect(944, 1717, 286, 27));
        rects.add(new Rect(418, 1682, 28, 17));
        rects.add(new Rect(160, 1930, 116, 34));
        rects.add(new Rect(278, 205, 45, 32));
        rects.add(new Rect(635, 1575, 152, 22));
        rects.add(new Rect(797, 1576, 25, 16));
        rects.add(new Rect(463, 1682, 123, 25));
        rects.add(new Rect(1231, 1297, 106, 37));
        rects.add(new Rect(1169, 1822, 82, 29));
        rects.add(new Rect(476, 1350, 18, 5));
        rects.add(new Rect(305, 1534, 216, 24));

        Enclosure enc = new Enclosure(rects);
        Set<Rect> rects1 = enc.find();

        assertEquals(112, rects1.size());

    }



}