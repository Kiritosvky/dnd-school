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

import android.content.Context;
import android.os.Build;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class Dbg {

    static volatile String target = "";
    static final ExecutorService ex = Executors.newSingleThreadExecutor();
    static final SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);

    static void init(Context c) {
        target = c.getSharedPreferences(Dnd.P, 0).getString("log", "").trim();
        Thread.UncaughtExceptionHandler prev = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            StringBuilder sb = new StringBuilder("CRASH " + t.getName() + ": " + e);
            for (StackTraceElement el : e.getStackTrace()) sb.append("\n    at ").append(el);
            for (Throwable c2 = e.getCause(); c2 != null; c2 = c2.getCause()) {
                sb.append("\n  caused by ").append(c2);
                for (StackTraceElement el : c2.getStackTrace()) sb.append("\n    at ").append(el);
            }
            sendNow(sb.toString());
            if (prev != null) prev.uncaughtException(t, e);
        });
    }

    static void log(String s) {
        if (target.isEmpty()) return;
        String line = fmt.format(new Date()) + " " + s;
        ex.execute(() -> write(line));
    }

    static void sendNow(String s) {
        if (target.isEmpty()) return;
        write(fmt.format(new Date()) + " " + s);
    }

    static void write(String line) {
        String t = target;
        int c = t.indexOf(':');
        String host = c > 0 ? t.substring(0, c) : t;
        int port = c > 0 ? Integer.parseInt(t.substring(c + 1)) : 8765;
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress(host, port), 1500);
            OutputStream o = s.getOutputStream();
            o.write((line + "\n").getBytes("UTF-8"));
            o.flush();
        } catch (Exception ignored) {
        }
    }

    static String env() {
        return "android " + Build.VERSION.RELEASE + " sdk " + Build.VERSION.SDK_INT
                + " " + Build.MANUFACTURER + " " + Build.MODEL;
    }
}
