/*
MIT License

Copyright (c) 2026 Kiritosvky

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package com.kiritosvky.dndschool;

import java.util.Arrays;

final class School {

    static int num(String s) {
        if (s == null || s.isEmpty() || s.length() > 2) return -1;
        int v = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') return -1;
            v = v * 10 + (c - '0');
        }
        return v;
    }

    static int[] parse(String text) {
        int[] tmp = new int[32];
        int n = 0;
        String src = text == null ? "" : text;
        for (String part : src.split("[,;\\s]+")) {
            int c = part.indexOf(':');
            if (c < 1) continue;
            int h = num(part.substring(0, c));
            int m = num(part.substring(c + 1));
            if (h < 0 || m < 0 || h > 23 || m > 59) continue;
            tmp[n++] = h * 60 + m;
            if (n == tmp.length) break;
        }
        int[] out = Arrays.copyOf(tmp, n);
        Arrays.sort(out);
        return out;
    }

    static String format(int[] mins) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mins.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(String.format(java.util.Locale.US, "%02d:%02d", mins[i] / 60, mins[i] % 60));
        }
        return sb.toString();
    }

    static long remain(int[] mins, int nowMin, int nowSec) {
        long now = nowMin * 60L + nowSec;
        for (int m : mins) {
            long at = m * 60L;
            if (at > now) return at - now;
        }
        return -1;
    }
}
