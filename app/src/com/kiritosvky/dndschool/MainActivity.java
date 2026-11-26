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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.time.LocalTime;
import java.util.Locale;

import static com.kiritosvky.dndschool.Fmt.clock;
import static com.kiritosvky.dndschool.Fmt.contrast;
import static com.kiritosvky.dndschool.Fmt.hms;
import static com.kiritosvky.dndschool.Fmt.mix;
import static com.kiritosvky.dndschool.Fmt.parseColor;

public class MainActivity extends Activity {

    static final String DEF_SCH = "15:20, 16:10, 17:00, 17:45, 18:50";

    final Handler h = new Handler(Looper.getMainLooper());
    float d;
    int bg = 0xFF000000, fg = 0xFFFFFFFF, ac = 0xFF4F8CFF;
    float base = 15f;
    boolean bold = true;
    int tab;

    TextView tDnd, tSchool, cDnd, sDnd, cBreak, sBreak, bAccess;
    EditText eH, eM, eS;
    View pDnd, pSchool;

    final Runnable tick = new Runnable() {
        @Override public void run() {
            long now = System.currentTimeMillis();
            if (tab == 0) updDnd(now);
            else updSchool();
            h.postDelayed(this, 1000 - now % 1000);
        }
    };

    SharedPreferences p() {
        return getSharedPreferences(Dnd.P, 0);
    }

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        d = getResources().getDisplayMetrics().density;
        bg = p().getInt("bg", bg);
        fg = p().getInt("fg", fg);
        ac = p().getInt("ac", ac);
        base = p().getFloat("size", base);
        bold = p().getBoolean("bold", bold);
        build();
        setTab(p().getInt("tab", 0));
    }

    @Override protected void onResume() {
        super.onResume();
        long end = p().getLong("end", 0);
        if (end > System.currentTimeMillis()) Dnd.apply(this);
        else if (end != 0) Dnd.off(this);
        setTab(tab);
    }

    @Override protected void onPause() {
        super.onPause();
        h.removeCallbacks(tick);
    }

    int dp(float v) {
        return Math.round(v * d);
    }

    GradientDrawable pill(int c) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(c);
        g.setCornerRadius(dp(12));
        return g;
    }

    Typeface face() {
        return getResources().getFont(bold ? R.font.inter_bold : R.font.inter_regular);
    }

    TextView tv(String s, float sp) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(fg);
        t.setTextSize(sp);
        t.setTypeface(face());
        return t;
    }

    TextView action(String label, int fill, int text) {
        TextView t = tv(label, base);
        t.setBackground(pill(fill));
        t.setTextColor(text);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(14), dp(12), dp(14), dp(12));
        return t;
    }

    EditText numField(String hint, float sp) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setHintTextColor(mix(fg, bg, 0.55f));
        e.setTextColor(fg);
        e.setTypeface(face());
        e.setTextSize(sp);
        e.setGravity(Gravity.CENTER);
        e.setInputType(InputType.TYPE_CLASS_NUMBER);
        e.setSingleLine(true);
        e.setBackground(pill(mix(bg, fg, 0.1f)));
        e.setPadding(dp(6), dp(10), dp(6), dp(10));
        return e;
    }

    TextView tabView(String label) {
        TextView t = tv(label, base);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(6), dp(12), dp(6), dp(12));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(4);
        t.setLayoutParams(lp);
        return t;
    }

    void build() {
        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setBackgroundColor(bg);

        LinearLayout rail = new LinearLayout(this);
        rail.setOrientation(LinearLayout.VERTICAL);
        rail.setPadding(dp(6), dp(10), dp(6), dp(10));
        rail.setBackgroundColor(mix(bg, fg, 0.07f));
        root.addView(rail, new LinearLayout.LayoutParams(dp(86), ViewGroup.LayoutParams.MATCH_PARENT));

        tDnd = tabView("DND");
        tDnd.setOnClickListener(v -> setTab(0));
        tSchool = tabView("Школа");
        tSchool.setOnClickListener(v -> setTab(1));
        rail.addView(tDnd);
        rail.addView(tSchool);
        rail.addView(new View(this), new LinearLayout.LayoutParams(0, 0, 1f));
        TextView gear = tabView("\u2699");
        gear.setTextSize(base * 1.4f);
        gear.setOnClickListener(v -> settings());
        rail.addView(gear);

        FrameLayout content = new FrameLayout(this);
        root.addView(content, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        pDnd = paneDnd();
        pSchool = paneSchool();
        content.addView(pDnd, match());
        content.addView(pSchool, match());
        setContentView(root);
        root.setOnApplyWindowInsetsListener((v, insets) -> {
            v.setPadding(0, insets.getSystemWindowInsets().top, 0, insets.getSystemWindowInsets().bottom);
            return insets;
        });
    }

    LinearLayout.LayoutParams match() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    View paneDnd() {
        LinearLayout p = new LinearLayout(this);
        p.setOrientation(LinearLayout.VERTICAL);
        p.setPadding(dp(18), dp(26), dp(18), dp(18));

        cDnd = tv("0:00:00", base * 2.7f);
        cDnd.setGravity(Gravity.CENTER);
        p.addView(cDnd);

        sDnd = tv("", base);
        sDnd.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sp.topMargin = dp(8);
        sp.bottomMargin = dp(18);
        p.addView(sDnd, sp);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        eH = numField("ч", base * 1.5f);
        eM = numField("мин", base * 1.5f);
        eS = numField("сек", base * 1.5f);
        addField(row, eH);
        addField(row, eM);
        addField(row, eS);
        p.addView(row);

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        blp.rightMargin = dp(8);
        TextView start = action("Включить", ac, contrast(ac));
        start.setOnClickListener(v -> startTimer());
        row2.addView(start, blp);
        TextView stop = action("Выключить", mix(bg, fg, 0.12f), fg);
        stop.setOnClickListener(v -> {
            Dnd.off(this);
            updDnd(System.currentTimeMillis());
        });
        row2.addView(stop, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rlp.topMargin = dp(12);
        p.addView(row2, rlp);

        bAccess = action("Разрешить «Не беспокоить»", mix(bg, fg, 0.12f), fg);
        bAccess.setOnClickListener(v -> startActivity(
                new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)));
        LinearLayout.LayoutParams alp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        alp.topMargin = dp(12);
        p.addView(bAccess, alp);
        return p;
    }

    void addField(LinearLayout row, EditText e) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp.rightMargin = dp(6);
        row.addView(e, lp);
    }

    View paneSchool() {
        LinearLayout p = new LinearLayout(this);
        p.setOrientation(LinearLayout.VERTICAL);
        p.setPadding(dp(18), dp(26), dp(18), dp(18));

        cBreak = tv("0:00:00", base * 2.7f);
        cBreak.setGravity(Gravity.CENTER);
        p.addView(cBreak);

        sBreak = tv("", base);
        sBreak.setGravity(Gravity.CENTER);
        sBreak.setTextColor(mix(fg, bg, 0.35f));
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sp.topMargin = dp(8);
        p.addView(sBreak, sp);

        p.addView(new View(this), new LinearLayout.LayoutParams(0, 0, 1f));

        TextView edit = action("Изменить расписание", ac, contrast(ac));
        edit.setOnClickListener(v -> editSchedule());
        p.addView(edit, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return p;
    }

    void setTab(int i) {
        tab = i;
        p().edit().putInt("tab", i).apply();
        pDnd.setVisibility(i == 0 ? View.VISIBLE : View.GONE);
        pSchool.setVisibility(i == 1 ? View.VISIBLE : View.GONE);
        tDnd.setBackground(i == 0 ? pill(ac) : null);
        tDnd.setTextColor(i == 0 ? contrast(ac) : fg);
        tSchool.setBackground(i == 1 ? pill(ac) : null);
        tSchool.setTextColor(i == 1 ? contrast(ac) : fg);
        h.removeCallbacks(tick);
        h.post(tick);
    }

    int val(EditText e) {
        int v = School.num(e.getText().toString().trim());
        return v < 0 ? 0 : v;
    }

    void startTimer() {
        long sec = val(eH) * 3600L + val(eM) * 60L + val(eS);
        if (sec <= 0) return;
        Dnd.on(this, System.currentTimeMillis() + sec * 1000);
        if (!Dnd.access(this)) startActivity(
                new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS));
        updDnd(System.currentTimeMillis());
    }

    void updDnd(long now) {
        long end = p().getLong("end", 0);
        if (end > now) {
            cDnd.setText(hms((end - now + 999) / 1000));
            sDnd.setText("Без звука и вибрации до " + clock(end));
            sDnd.setTextColor(ac);
        } else {
            if (end != 0) Dnd.off(this);
            cDnd.setText("0:00:00");
            sDnd.setText(Dnd.access(this) ? "Режим выключен" : "Нужен доступ к «Не беспокоить»");
            sDnd.setTextColor(mix(fg, bg, 0.35f));
        }
        bAccess.setVisibility(Dnd.access(this) ? View.GONE : View.VISIBLE);
    }

    void updSchool() {
        LocalTime t = LocalTime.now();
        int[] mins = School.parse(p().getString("sch", DEF_SCH));
        int nowMin = t.getHour() * 60 + t.getMinute();
        long r = School.remain(mins, nowMin, t.getSecond());
        if (r < 0) {
            cBreak.setText("Школа закончилась");
            cBreak.setTextSize(base * 2.2f);
            sBreak.setText("Перемен больше нет");
        } else {
            cBreak.setTextSize(base * 2.7f);
            cBreak.setText(hms(r));
            long at = nowMin * 60L + t.getSecond() + r;
            sBreak.setText(String.format(Locale.US, "Перемена в %02d:%02d", at / 3600 % 24, at / 60 % 60));
        }
    }

    void editSchedule() {
        EditText e = numField("", base * 1.2f);
        e.setInputType(InputType.TYPE_CLASS_TEXT);
        e.setSingleLine(false);
        e.setGravity(Gravity.START);
        e.setText(School.format(School.parse(p().getString("sch", DEF_SCH))));
        TextView hint = tv("Время через запятую, ЧЧ:ММ", base);
        hint.setTextColor(mix(fg, bg, 0.4f));
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(18), dp(16), dp(18), 0);
        box.addView(hint);
        box.addView(e, match());
        AlertDialog dlg = new AlertDialog.Builder(this)
                .setView(box)
                .setPositiveButton("Сохранить", (x, y) -> {
                    p().edit().putString("sch", e.getText().toString()).apply();
                    updSchool();
                })
                .setNegativeButton("Отмена", null)
                .create();
        dlg.setOnShowListener(x -> {
            dlg.getWindow().setBackgroundDrawable(pill(bg));
            dlg.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ac);
            dlg.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(fg);
        });
        dlg.show();
    }

    void settings() {
        LinearLayout v = new LinearLayout(this);
        v.setOrientation(LinearLayout.VERTICAL);
        v.setPadding(dp(20), dp(18), dp(20), dp(8));

        TextView head = tv("Оформление", base * 1.4f);
        v.addView(head);

        EditText c1 = hexField(bg);
        EditText c2 = hexField(fg);
        EditText c3 = hexField(ac);
        addRow(v, "Фон", c1);
        addRow(v, "Текст", c2);
        addRow(v, "Акцент", c3);
        TextView sizeLabel = tv("Размер текста", base);
        sizeLabel.setPadding(0, dp(14), 0, 0);
        v.addView(sizeLabel);
        SeekBar sb = new SeekBar(this);
        sb.setMax(52);
        sb.setProgress(Math.round(base) - 10);
        v.addView(sb);

        android.widget.CheckBox cb = new android.widget.CheckBox(this);
        cb.setText("Жирный шрифт");
        cb.setTextColor(fg);
        cb.setTypeface(face());
        cb.setChecked(bold);
        v.addView(cb);

        TextView reset = action("Сброс", mix(bg, fg, 0.12f), fg);
        v.addView(reset);

        ScrollView sc = new ScrollView(this);
        sc.addView(v);

        AlertDialog dlg = new AlertDialog.Builder(this)
                .setView(sc)
                .setPositiveButton("Готово", (x, y) -> {
                    p().edit()
                            .putInt("bg", parseColor(c1.getText().toString(), bg))
                            .putInt("fg", parseColor(c2.getText().toString(), fg))
                            .putInt("ac", parseColor(c3.getText().toString(), ac))
                            .putFloat("size", sb.getProgress() + 10)
                            .putBoolean("bold", cb.isChecked())
                            .apply();
                    recreate();
                })
                .setNegativeButton("Отмена", null)
                .create();
        reset.setOnClickListener(x -> {
            p().edit().remove("bg").remove("fg").remove("ac").remove("size").remove("bold").apply();
            dlg.dismiss();
            recreate();
        });
        dlg.setOnShowListener(x -> {
            dlg.getWindow().setBackgroundDrawable(pill(bg));
            dlg.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ac);
            dlg.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(fg);
        });
        dlg.show();
    }

    EditText hexField(int color) {
        EditText e = numField("", base);
        e.setInputType(InputType.TYPE_CLASS_TEXT);
        e.setGravity(Gravity.START);
        e.setText(String.format("#%08X", color));
        return e;
    }

    void addRow(LinearLayout parent, String label, EditText field) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        TextView t = tv(label, base);
        row.addView(t, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.42f));
        row.addView(field, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.58f));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(8);
        parent.addView(row, lp);
    }
}
