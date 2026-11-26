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

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;

final class Dnd {

    static final String P = "s";
    static final int[] STREAMS = {1, 2, 3, 4, 5, 8};

    static SharedPreferences p(Context c) {
        return c.getSharedPreferences(P, 0);
    }

    static NotificationManager nm(Context c) {
        return (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    static AudioManager am(Context c) {
        return (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
    }

    static boolean access(Context c) {
        return nm(c).isNotificationPolicyAccessGranted();
    }

    static void on(Context c, long end) {
        SharedPreferences s = p(c);
        if (s.getLong("end", 0) == 0) {
            StringBuilder v = new StringBuilder();
            for (int st : STREAMS) v.append(am(c).getStreamVolume(st)).append(',');
            s.edit()
                    .putInt("f", nm(c).getCurrentInterruptionFilter())
                    .putString("v", v.toString())
                    .putLong("end", end)
                    .apply();
        } else {
            s.edit().putLong("end", end).apply();
        }
        apply(c);
        alarm(c, end);
    }

    static void apply(Context c) {
        if (access(c)) nm(c).setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
        StringBuilder r = new StringBuilder("apply filter=" + nm(c).getCurrentInterruptionFilter());
        for (int st : STREAMS) {
            try {
                am(c).setStreamVolume(st, 0, 0);
            } catch (SecurityException e) {
                r.append(" volume-denied:").append(st);
            }
        }
        try {
            am(c).setRingerMode(AudioManager.RINGER_MODE_SILENT);
        } catch (SecurityException e) {
            r.append(" ringer-denied");
        }
        r.append(" ringer=").append(am(c).getRingerMode());
        for (int st : STREAMS) r.append(' ').append(st).append('=').append(am(c).getStreamVolume(st));
        Dbg.log(r.toString());
    }

    static void off(Context c) {
        SharedPreferences s = p(c);
        String[] v = s.getString("v", "").split(",");        for (int i = 0; i < STREAMS.length && i < v.length; i++) {
            try {
                am(c).setStreamVolume(STREAMS[i], Integer.parseInt(v[i].trim()), 0);
            } catch (Exception ignored) {
            }
        }
        if (s.contains("f") && access(c)) {
            nm(c).setInterruptionFilter(s.getInt("f", NotificationManager.INTERRUPTION_FILTER_ALL));
        }
        s.edit().remove("end").remove("f").remove("v").apply();
        cancel(c);
        Dbg.log("off ringer=" + am(c).getRingerMode() + " filter=" + nm(c).getCurrentInterruptionFilter());
    }

    static PendingIntent pi(Context c) {
        Intent i = new Intent(c, DndReceiver.class);
        return PendingIntent.getBroadcast(c, 1, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    static void alarm(Context c, long at) {
        AlarmManager a = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT < 31 || a.canScheduleExactAlarms()) {
            a.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi(c));
        } else {
            a.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi(c));
        }
    }

    static void cancel(Context c) {
        AlarmManager a = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        a.cancel(pi(c));
    }
}
