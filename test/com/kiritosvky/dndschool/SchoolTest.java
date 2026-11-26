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

public class SchoolTest {

    static void check(boolean ok, String what) {
        if (!ok) throw new AssertionError(what);
    }

    public static void main(String[] args) {
        int[] m = School.parse("18:50, 15:20,16:10 , 17:00;17:45");
        check(m.length == 5, "count");
        check(m[0] == 15 * 60 + 20, "first");
        check(m[4] == 18 * 60 + 50, "last");

        check(School.remain(m, 15 * 60 + 19, 0) == 60, "one minute left");
        check(School.remain(m, 15 * 60 + 20, 0) == 50 * 60, "at break -> next");
        check(School.remain(m, 0, 0) == (15 * 60 + 20) * 60L, "morning");
        check(School.remain(m, 18 * 60 + 50, 0) == -1, "over");
        check(School.remain(m, 23 * 60, 59) == -1, "late night");

        check(School.parse("bad, 25:00, 12:70, , 8:5").length == 1, "garbage");
        check(School.parse("").length == 0, "empty");
        check(School.parse(null).length == 0, "null");

        check(School.format(m).equals("15:20, 16:10, 17:00, 17:45, 18:50"), "format");

        check(School.num("09") == 9, "num");
        check(School.num("9x") == -1, "num bad");
        check(School.num("") == -1, "num empty");

        check(Fmt.hms(3725).equals("1:02:05"), "hms");
        check(Fmt.parseColor("#FF112233", 0) == 0xFF112233, "hex");
        check(Fmt.parseColor("112233", 0) == 0xFF112233, "hex no hash");
        check(Fmt.parseColor("zzz", 7) == 7, "hex bad");
        check(Fmt.contrast(0xFFFFFFFF) == 0xFF000000, "contrast white");
        check(Fmt.contrast(0xFF000000) == 0xFFFFFFFF, "contrast black");
        check(Fmt.mix(0xFF000000, 0xFFFFFFFF, 0.5f) == 0xFF808080, "mix");

        System.out.println("SchoolTest OK");
    }
}
