package com.veve.flowreader;

import android.os.PatternMatcher;

import org.junit.Assert;
import org.junit.Test;

public class MD5Test {


    static boolean matchPattern(String pattern, String match, int type) {

        final int NP = pattern.length();
        if (NP <= 0) {
            return match.length() <= 0;
        }
        final int NM = match.length();
        int ip = 0, im = 0;
        char nextChar = pattern.charAt(0);
        while ((ip<NP) && (im<NM)) {
            char c = nextChar;
            ip++;
            nextChar = ip < NP ? pattern.charAt(ip) : 0;
            final boolean escaped = (c == '\\');
            if (escaped) {
                c = nextChar;
                ip++;
                nextChar = ip < NP ? pattern.charAt(ip) : 0;
            }
            if (nextChar == '*') {
                if (!escaped && c == '.') {
                    if (ip >= (NP-1)) {
                        // at the end with a pattern match, so
                        // all is good without checking!
                        return true;
                    }
                    ip++;
                    nextChar = pattern.charAt(ip);
                    // Consume everything until the next character in the
                    // pattern is found.
                    if (nextChar == '\\') {
                        ip++;
                        nextChar = ip < NP ? pattern.charAt(ip) : 0;
                    }
                    do {
                        if (match.charAt(im) == nextChar) {
                            break;
                        }
                        im++;
                    } while (im < NM);
                    if (im == NM) {
                        // Whoops, the next character in the pattern didn't
                        // exist in the match.
                        return false;
                    }
                    ip++;
                    nextChar = ip < NP ? pattern.charAt(ip) : 0;
                    im++;
                } else {
                    // Consume only characters matching the one before '*'.
                    do {
                        if (match.charAt(im) != c) {
                            break;
                        }
                        im++;
                    } while (im < NM);
                    ip++;
                    nextChar = ip < NP ? pattern.charAt(ip) : 0;
                }
            } else {
                if (c != '.' && match.charAt(im) != c) return false;
                im++;
            }
        }

        if (ip >= NP && im >= NM) {
            // Reached the end of both strings, all is good!
            return true;
        }

        // One last check: we may have finished the match string, but still
        // have a '.*' at the end of the pattern, which should still count
        // as a match.
        return ip == NP - 2 && pattern.charAt(ip) == '.'
                && pattern.charAt(ip + 1) == '*';
    }


    @Test
    public void testMD5() {

        String matchPattern = ".*\\.djvu";

        String s = "//storage/emulated/0/Download/dvurog (2).djvu";
        //String s = "/storage/emulated/0/Download/dvurog (2).djvu";

        boolean b = matchPattern(matchPattern, s, 3);
        System.out.println(b);



    }
}
