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

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Locale;

final class Fmt {

    static String hms(long s) {
        return String.format(Locale.US, "%d:%02d:%02d", s / 3600, s % 3600 / 60, s % 60);
    }

    static String clock(long ms) {
        LocalTime t = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalTime();
        return String.format(Locale.US, "%02d:%02d", t.getHour(), t.getMinute());
    }

    static int mix(int a, int b, float t) {
        return 0xFF000000
                | Math.round(((a >> 16) & 0xFF) + (((b >> 16) & 0xFF) - ((a >> 16) & 0xFF)) * t) << 16
                | Math.round(((a >> 8) & 0xFF) + (((b >> 8) & 0xFF) - ((a >> 8) & 0xFF)) * t) << 8
                | Math.round((a & 0xFF) + ((b & 0xFF) - (a & 0xFF)) * t);
    }

    static int contrast(int c) {
        double l = 0.2126 * ((c >> 16) & 0xFF) + 0.7152 * ((c >> 8) & 0xFF) + 0.0722 * (c & 0xFF);
        return l > 140 ? 0xFF000000 : 0xFFFFFFFF;
    }

    static int parseColor(String s, int def) {
        String t = s.trim();
        if (t.startsWith("#")) t = t.substring(1);
        if (!t.matches("[0-9a-fA-F]{6}") && !t.matches("[0-9a-fA-F]{8}")) return def;
        long v = Long.parseLong(t, 16);
        return t.length() == 6 ? (int) (0xFF000000L | v) : (int) v;
    }
}
