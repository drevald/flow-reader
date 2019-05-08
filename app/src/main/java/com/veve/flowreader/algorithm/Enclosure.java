package com.veve.flowreader.algorithm;

import org.opencv.core.Rect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Enclosure {

    private List<NumberTuple> points;

    private Set<NumberTuple> all_;

    private Map<Integer, Map<Integer, Integer>> elementsMap;

    private Map<Integer, List<Integer>> mapNumberToPos;

    public Enclosure(Iterable<Rect> rects) {
        points = new ArrayList<>();
        all_= new HashSet<>();
        elementsMap = new HashMap<>();
        mapNumberToPos = new HashMap<>();

        for (Rect rect : rects) {
            points.add(new NumberTuple(new int[] {-rect.x, -rect.y, rect.x+rect.width, rect.y + rect.height}));
        }
        all_.addAll(points);
        normalize(this.points);
    }

    /**
     * @return Set of enclosing rectangles
     */
    public Set<Rect> find() {
        Set<NumberTuple> solution = solve();
        Set<Rect> returnValue = new HashSet<>();

        for (NumberTuple t : solution) {
            int x = -t.get(0);
            int y = -t.get(1);
            int width = t.get(2) - x;
            int height = t.get(3) - y;
            returnValue.add(new Rect(x,y,width,height));
        }

        return returnValue;
    }

    private static Comparator<NumberTuple> thirdElementComparator = new Comparator<NumberTuple>() {
        @Override
        public int compare(NumberTuple t1, NumberTuple t2) {
            return Integer.compare(t1.get(2), t2.get(2));
        }
    };

    private static Comparator<Tuple<NumberTuple,Character>> thirdElementComparatorOfPair = new Comparator<Tuple<NumberTuple,Character>>() {
        @Override
        public int compare(Tuple<NumberTuple,Character> t1, Tuple<NumberTuple,Character> t2) {
            return -Integer.compare(t1.getFirst().get(2), t2.getFirst().get(2));
        }
    };


    private static Comparator<NumberTuple> fourthElementComparator = new Comparator<NumberTuple>() {
        @Override
        public int compare(NumberTuple t1, NumberTuple t2) {
            return Integer.compare(t1.get(3), t2.get(3));
        }
    };

    private static Comparator<NumberTuple> firstElementComparator = new Comparator<NumberTuple>() {
        @Override
        public int compare(NumberTuple t1, NumberTuple t2) {
            return Integer.compare(t1.get(0), t2.get(0));
        }
    };

    private static Comparator<NumberTuple> secondElementComparator = new Comparator<NumberTuple>() {
        @Override
        public int compare(NumberTuple t1, NumberTuple t2) {
            return Integer.compare(t1.get(1), t2.get(1));
        }
    };

    private List<Tuple<NumberTuple,NumberTuple>> merge(List<NumberTuple> s1, List<NumberTuple> s2) {

        List<Tuple<NumberTuple,NumberTuple>> returnValue = new ArrayList<>();

        Collections.sort(s1, thirdElementComparator);
        Collections.sort(s2, thirdElementComparator);

        List<Tuple<NumberTuple,Character>> newS1 = new ArrayList<>();
        List<Tuple<NumberTuple,Character>> newS2 = new ArrayList<>();

        for (int i=0;i<s1.size();i++) {
            newS1.add(new Tuple<>(s1.get(i), 'r'));
        }

        for (int i=0;i<s2.size();i++) {
            newS2.add(new Tuple<>(s2.get(i), 'b'));
        }

        List<Tuple<NumberTuple,Character>> allS = new ArrayList<>();
        allS.addAll(newS1);
        allS.addAll(newS2);

        Collections.sort(allS, thirdElementComparatorOfPair);

        List<NumberTuple> heap = new ArrayList<>();

        for (int i = 0; i < allS.size(); i++) {
            Tuple<NumberTuple, Character> tuple = allS.get(i);
            if (tuple.getSecond().equals('b')) {
                heap.add(tuple.getFirst());
            } else {
                NumberTuple se = tuple.getFirst();
                for (int j=0; j<heap.size(); j++) {
                    NumberTuple e = heap.get(j);
                    if (e.get(0) >= se.get(0) && e.get(1) >= se.get(1)) {
                        returnValue.add(new Tuple<> (se,e));
                    }
                }
            }
        }

        return returnValue;
    }


    private List<Tuple<NumberTuple,NumberTuple>> report(List<NumberTuple> points) {
        int size = points.size();
        if (size <= 1) {
            return new ArrayList<>();
        } else if (size == 2) {
            List<Tuple<NumberTuple,NumberTuple>> v = new ArrayList<>();
            NumberTuple a = points.get(0);
            NumberTuple b = points.get(1);
            if (a.get(0) < b.get(0) && a.get(1) < b.get(1) && a.get(2) < b.get(2) && a.get(3) < b.get(3)) {
                v.add(new Tuple(a,b));
                return v;
            } else if (a.get(0) >= b.get(0) && a.get(1) >= b.get(1) && a.get(2) >= b.get(2) &&
                    a.get(3) >= b.get(3)) {
                v.add(new Tuple(b,a));
                return v;
            } else {
                return v;
            }
        } else if (size ==3) {
            List<Tuple<NumberTuple,NumberTuple>> v = new ArrayList<>();
            NumberTuple a = points.get(0);
            NumberTuple b = points.get(1);
            NumberTuple c = points.get(2);

            if (a.get(0) < b.get(0) && a.get(1) < b.get(1) && a.get(2) < b.get(2) && a.get(3) < b.get(3)) {
                v.add(new Tuple(a,b));
            } else if (a.get(0) >= b.get(0) && a.get(1) >= b.get(1) && a.get(2) >= b.get(2) &&
                    a.get(3) >= b.get(3)) {
                v.add(new Tuple(b,a));
            }

            if (a.get(0) < c.get(0) && a.get(1) < c.get(1) && a.get(2) < c.get(2) && a.get(3) < c.get(3)) {
                v.add(new Tuple(a,c));
            } else if (a.get(0) >= c.get(0) && a.get(1) >= c.get(1) && a.get(2) >= c.get(2) &&
                    a.get(3) >= c.get(3)) {
                v.add(new Tuple(c,a));
            }

            if (b.get(0) < c.get(0) && b.get(1) < c.get(1) && b.get(2) < c.get(2) && b.get(3) < c.get(3)) {
                v.add(new Tuple(b,c));
            } else if (b.get(0) >= c.get(0) && b.get(1) >= c.get(1) && b.get(2) >= c.get(2) &&
                    b.get(3) >= c.get(3)) {
                v.add(new Tuple(c,b));
            }

            return v;
            
        } else {
            double m = getMedian(points);
            List<NumberTuple> s1 = new ArrayList<>();
            List<NumberTuple> s2 = new ArrayList<>();

            for (int i=0;i<points.size();i++) {
                if (points.get(i).get(3) <= m) {
                    s1.add(points.get(i));
                } else {
                    s2.add(points.get(i));
                }
            }

            List<Tuple<NumberTuple, NumberTuple>> x = report(s1);
            List<Tuple<NumberTuple, NumberTuple>> y = report(s2);
            List<Tuple<NumberTuple, NumberTuple>> merged = merge(s1, s2);
            List<Tuple<NumberTuple,NumberTuple>> returnValue = new ArrayList<>();
            returnValue.addAll(x);
            returnValue.addAll(y);
            returnValue.addAll(merged);

            return returnValue;

        }

    }

    Set<NumberTuple> solve() {
        List<Tuple<NumberTuple, NumberTuple>> out = report(points);
        Set<NumberTuple> big = new HashSet<>();
        Set<NumberTuple> small = new HashSet<>();

        for (int i=0;i<out.size();i++) {
            NumberTuple b = out.get(i).getSecond();
            NumberTuple s = out.get(i).getFirst();
            int v1 = elementsMap.get(0).get(b.get(0));
            int v2 = elementsMap.get(1).get(b.get(1));
            int v3 = elementsMap.get(2).get(b.get(2));
            int v4 = elementsMap.get(3).get(b.get(3));

            int[]v = new int[]  {v1,v2,v3,v4};
            b = new NumberTuple(v);

            v1 = elementsMap.get(0).get(s.get(0));
            v2 = elementsMap.get(1).get(s.get(1));
            v3 = elementsMap.get(2).get(s.get(2));
            v4 = elementsMap.get(3).get(s.get(3));

            v = new int[]  {v1,v2,v3,v4};
            s = new NumberTuple(v);

            big.add(b);
            small.add(s);

        }

        Set<NumberTuple> bigUnionSmall = new HashSet<>();
        bigUnionSmall.addAll(big);
        bigUnionSmall.addAll(small);

        all_.removeAll(bigUnionSmall);

        big.removeAll(small);

        Set<NumberTuple> returnValue = new HashSet<>();

        returnValue.addAll(all_);
        returnValue.addAll(big);
        return returnValue;


    }

    private double getMedian(List<NumberTuple> points) {

        Collections.sort(points, fourthElementComparator);

        double med = 0.0;
        int size = points.size();
        int ind = size / 2;
        if (size % 2 == 0) {
            med = (points.get(ind - 1).get(3) + points.get(ind).get(3)) / 2.0;
        } else {
            med = points.get(ind).get(3);
        }
        return med;
    }

    private void normalize(List<NumberTuple> points) {

        List<Comparator<NumberTuple>> orders = new ArrayList<>();
        orders.add(firstElementComparator);
        orders.add(secondElementComparator);
        orders.add(thirdElementComparator);
        orders.add(fourthElementComparator);

        int size = points.size();

        List<NumberTuple> newList = new ArrayList<>();
        newList.addAll(points);

        for (int i = 0; i < size; i++) {

            List<Integer> integers0 = mapNumberToPos.get(0);
            List<Integer> integers1 = mapNumberToPos.get(1);
            List<Integer> integers2 = mapNumberToPos.get(2);
            List<Integer> integers3 = mapNumberToPos.get(3);

            if (integers0 == null) {
                integers0 = new ArrayList<>();
                mapNumberToPos.put(0,integers0);
            }

            if (integers1 == null) {
                integers1 = new ArrayList<>();
                mapNumberToPos.put(1,integers1);
            }

            if (integers2 == null) {
                integers2 = new ArrayList<>();
                mapNumberToPos.put(2,integers2);
            }

            if (integers3 == null) {
                integers3 = new ArrayList<>();
                mapNumberToPos.put(3,integers3);
            }

            integers0.add(this.points.get(i).get(0));
            integers1.add(this.points.get(i).get(1));
            integers2.add(this.points.get(i).get(2));
            integers3.add(this.points.get(i).get(3));

            mapNumberToPos.put( 0,  integers0);
            mapNumberToPos.put( 1,  integers1);
            mapNumberToPos.put( 2,  integers2);
            mapNumberToPos.put( 3,  integers3);
        }

        for (int j = 0; j < 4; j++) {
            Collections.sort(mapNumberToPos.get(j));
            Map<Integer, Integer> lm = new HashMap<>();
            elementsMap.put(j, lm);
        }

        for (int j = 0; j < 4; j++) {
            int sz = mapNumberToPos.get(j).size();
            Collections.sort(this.points, orders.get(j));

            int[] v = new int[4];
            for (int k = 0; k < size; k++) {
                Integer n = mapNumberToPos.get(j).get(k);

                Map<Integer, Integer> integerIntegerMap = elementsMap.get(j);
                if (integerIntegerMap == null) {
                    integerIntegerMap = new HashMap<>();
                }
                integerIntegerMap.put(k, n);
                v[j] = k;
                NumberTuple numberTuple = points.get(k);
                NumberTuple newNumberTuple = numberTuple.change(j, k);
                points.set(k, newNumberTuple);

            }

        }

    }


}
