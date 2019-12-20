package com.veve.flowreader;

import java.util.ArrayList;
import java.util.List;

public class PagesSet {

    int start;

    int end;

    public PagesSet (String str) {
        if (str.indexOf('-') == -1) {
            this.start = this.end = Integer.parseInt(str.trim());
        } else {
            this.start = Integer.parseInt(str.substring(0, str.lastIndexOf('-')).trim());
            this.end = Integer.parseInt(str.substring(1 + str.lastIndexOf('-')).trim());
        }
    }

    public PagesSet(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public static List<PagesSet> getPagesSet(String str) {
        String[] parts = str.split("[\\s\\,\\;\\:]+");
        List<PagesSet> sets = new ArrayList<PagesSet>();
        for (String part : parts) {
            if (part.trim() != "") {
                sets.add(new PagesSet(part.trim()));
            };
        }
        return sets;
    }

}