package com.savindu.savingstracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends Activity {

    private static final int TARGET = 100000;
    private static final String PREFS = "lkr_savings_tracker_premium";
    private static final String KEY_DONE = "done_boxes";
    private static final String KEY_DARK = "dark_mode";
    private static final String KEY_SINHALA = "sinhala_mode";

    private SharedPreferences prefs;
    private Set<String> done = new HashSet<>();

    private FrameLayout contentFrame;
    private LinearLayout bottomNav;
    private Button navHome;
    private Button navTracker;
    private Button navHistory;

    private int currentScreen = 0; // 0 home, 1 tracker, 2 history
    private boolean darkMode;
    private boolean sinhalaMode;

    private final int NAVY = Color.rgb(11,45,92);
    private final int PINK = Color.rgb(239,79,123);
    private final int BLUE = Color.rgb(79,143,232);
    private final int GREEN = Color.rgb(61,155,99);
    private final int PURPLE = Color.rgb(128,87,200);
    private final int GOLD = Color.rgb(233,173,53);
    private final int ORANGE = Color.rgb(245,132,59);
    private final int DONE = Color.rgb(31,157,90);

    private int pageBg() { return darkMode ? Color.rgb(20,25,35) : Color.rgb(246,248,252); }
    private int cardBg() { return darkMode ? Color.rgb(30,37,50) : Color.WHITE; }
    private int textPrimary() { return darkMode ? Color.rgb(244,247,252) : Color.rgb(23,34,56); }
    private int textMuted() { return darkMode ? Color.rgb(166,176,194) : Color.rgb(115,128,153); }
    private int border() { return darkMode ? Color.rgb(52,62,80) : Color.rgb(230,234,242); }
    private int softBg() { return darkMode ? Color.rgb(37,45,60) : Color.rgb(244,246,250); }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        done = new HashSet<>(prefs.getStringSet(KEY_DONE, new HashSet<>()));
        darkMode = prefs.getBoolean(KEY_DARK, false);
        sinhalaMode = prefs.getBoolean(KEY_SINHALA, false);

        applySystemBars();
        setContentView(buildAppShell());
        showScreen(0);
    }

    private void applySystemBars() {
        getWindow().setStatusBarColor(darkMode ? Color.rgb(14,18,26) : NAVY);
        getWindow().setNavigationBarColor(darkMode ? Color.rgb(22,27,38) : Color.WHITE);
    }

    private View buildAppShell() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(pageBg());

        contentFrame = new FrameLayout(this);
        LinearLayout.LayoutParams contentLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        root.addView(contentFrame, contentLp);

        bottomNav = new LinearLayout(this);
        bottomNav.setOrientation(LinearLayout.HORIZONTAL);
        bottomNav.setPadding(dp(10), dp(8), dp(10), dp(10));
        bottomNav.setGravity(Gravity.CENTER);
        bottomNav.setBackground(roundRect(cardBg(), border(), 0, 1));

        navHome = navButton("⌂\n" + t("Home", "මුල් පිටුව"), 0);
        navTracker = navButton("▦\n" + t("Tracker", "ට්‍රැකර්"), 1);
        navHistory = navButton("◷\n" + t("History", "ඉතිහාසය"), 2);

        bottomNav.addView(navHome, navLp());
        bottomNav.addView(navTracker, navLp());
        bottomNav.addView(navHistory, navLp());

        root.addView(bottomNav, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(74)));

        return root;
    }

    private LinearLayout.LayoutParams navLp() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        lp.setMargins(dp(4), 0, dp(4), 0);
        return lp;
    }

    private Button navButton(String label, int screen) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(12);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setPadding(dp(6), dp(4), dp(6), dp(4));
        b.setOnClickListener(v -> showScreen(screen));
        return b;
    }

    private void styleNav() {
        styleOneNav(navHome, currentScreen == 0);
        styleOneNav(navTracker, currentScreen == 1);
        styleOneNav(navHistory, currentScreen == 2);
    }

    private void styleOneNav(Button b, boolean active) {
        if (active) {
            b.setTextColor(Color.WHITE);
            b.setBackground(roundRect(PINK, PINK, 16, 0));
        } else {
            b.setTextColor(textMuted());
            b.setBackground(roundRect(cardBg(), Color.TRANSPARENT, 16, 0));
        }
    }

    private void showScreen(int screen) {
        currentScreen = screen;
        contentFrame.removeAllViews();
        View screenView;
        if (screen == 1) screenView = buildTrackerScreen();
        else if (screen == 2) screenView = buildHistoryScreen();
        else screenView = buildHomeScreen();
        contentFrame.addView(screenView);
        styleNav();
    }

    private View buildHomeScreen() {
        ScrollView scroll = baseScroll();
        LinearLayout root = baseContent();
        scroll.addView(root);

        root.addView(buildTopHeader(), matchWrap());
        root.addView(buildHeroCard(), topMargin(14));
        root.addView(buildQuickStats(), topMargin(14));
        root.addView(buildBadges(), topMargin(14));
        root.addView(buildSettingsCard(), topMargin(14));

        return scroll;
    }

    private View buildTrackerScreen() {
        ScrollView scroll = baseScroll();
        LinearLayout root = baseContent();
        scroll.addView(root);

        TextView title = text(t("Savings Tracker", "ඉතිරි කිරීමේ ට්‍රැකර්"), 27, textPrimary(), true);
        root.addView(title);
        TextView sub = text(t("Tap a box when you save that amount.", "එම මුදල ඉතිරි කළ විට කොටුවක් තට්ටු කරන්න."), 13, textMuted(), false);
        LinearLayout.LayoutParams subLp = matchWrap();
        subLp.topMargin = dp(5);
        root.addView(sub, subLp);

        root.addView(buildTrackerSummary(), topMargin(14));
        root.addView(section(500, 100, BLUE, t("100 boxes = Rs. 50,000", "කොටු 100 = රු. 50,000")), topMargin(14));
        root.addView(section(200, 150, GREEN, t("150 boxes = Rs. 30,000", "කොටු 150 = රු. 30,000")), topMargin(14));
        root.addView(section(100, 100, PURPLE, t("100 boxes = Rs. 10,000", "කොටු 100 = රු. 10,000")), topMargin(14));
        root.addView(section(50, 200, ORANGE, t("200 boxes = Rs. 10,000", "කොටු 200 = රු. 10,000")), topMargin(14));

        return scroll;
    }

    private View buildHistoryScreen() {
        ScrollView scroll = baseScroll();
        LinearLayout root = baseContent();
        scroll.addView(root);

        TextView title = text(t("Saving History", "ඉතිරි කිරීමේ ඉතිහාසය"), 27, textPrimary(), true);
        root.addView(title);
        TextView sub = text(t("See your recent daily savings.", "ඔබගේ දිනපතා ඉතිරි කිරීම් බලන්න."), 13, textMuted(), false);
        LinearLayout.LayoutParams subLp = matchWrap();
        subLp.topMargin = dp(5);
        root.addView(sub, subLp);

        root.addView(buildHistorySummary(), topMargin(14));
        root.addView(buildHistoryList(14), topMargin(14));

        Button reset = new Button(this);
        reset.setText(t("Reset All Progress", "සියලු ප්‍රගතිය නැවත සකසන්න"));
        reset.setAllCaps(false);
        reset.setTextSize(15);
        reset.setTypeface(Typeface.DEFAULT_BOLD);
        reset.setTextColor(Color.rgb(190,58,84));
        reset.setBackground(roundRect(
                darkMode ? Color.rgb(65,34,43) : Color.rgb(255,240,243),
                darkMode ? Color.rgb(117,58,73) : Color.rgb(243,201,211),
                18, 1));
        reset.setOnClickListener(v -> confirmReset());
        LinearLayout.LayoutParams resetLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(54));
        resetLp.topMargin = dp(16);
        root.addView(reset, resetLp);

        return scroll;
    }

    private LinearLayout buildTopHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView pig = text("🐷", 32, textPrimary(), false);
        pig.setGravity(Gravity.CENTER);
        pig.setBackground(roundRect(
                darkMode ? Color.rgb(62,39,50) : Color.rgb(255,242,246),
                darkMode ? Color.rgb(93,54,70) : Color.rgb(248,213,224),
                22, 1));
        header.addView(pig, new LinearLayout.LayoutParams(dp(62), dp(62)));

        LinearLayout textWrap = new LinearLayout(this);
        textWrap.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        textLp.leftMargin = dp(12);
        header.addView(textWrap, textLp);

        textWrap.addView(text(t("100,000 LKR Savings", "රු. 100,000 ඉතිරි කිරීම"), 24, textPrimary(), true));
        TextView sub = text(t("Your goal. Your pace. Your progress.", "ඔබේ ඉලක්කය. ඔබේ වේගය. ඔබේ ප්‍රගතිය."), 12, textMuted(), false);
        LinearLayout.LayoutParams subLp = matchWrap();
        subLp.topMargin = dp(3);
        textWrap.addView(sub, subLp);

        return header;
    }

    private LinearLayout buildHeroCard() {
        LinearLayout card = card();
        card.setPadding(dp(16), dp(16), dp(16), dp(16));

        ProgressCircleView circle = new ProgressCircleView(this);
        circle.setMax(TARGET);
        circle.setProgress(totalSaved());
        circle.setThemeColors(
                darkMode ? Color.rgb(55,65,84) : Color.rgb(232,237,245),
                PINK,
                textPrimary(),
                textMuted(),
                DONE
        );
        card.addView(circle, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(230)));

        LinearLayout stats = new LinearLayout(this);
        stats.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams statsLp = matchWrap();
        statsLp.topMargin = dp(4);
        card.addView(stats, statsLp);

        statCard(stats, t("Saved", "ඉතිරි කළ"), "Rs. " + fmt(totalSaved()));
        statCard(stats, t("Remaining", "ඉතිරි"), "Rs. " + fmt(TARGET - totalSaved()));

        return card;
    }

    private LinearLayout buildQuickStats() {
        LinearLayout card = card();
        card.setPadding(dp(16), dp(16), dp(16), dp(16));

        TextView title = text(t("Quick Overview", "ඉක්මන් සාරාංශය"), 20, textPrimary(), true);
        card.addView(title);

        int completed = totalCompletedBoxes();
        double pct = totalSaved() * 100.0 / TARGET;

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        LinearLayout.LayoutParams lp = matchWrap();
        lp.topMargin = dp(12);
        card.addView(grid, lp);

        addInfoTile(grid, "🎯", t("Target", "ඉලක්කය"), "Rs. 100,000", PINK);
        addInfoTile(grid, "✓", t("Boxes", "කොටු"), completed + " / 550", DONE);
        addInfoTile(grid, "📈", t("Progress", "ප්‍රගතිය"), String.format(Locale.US, "%.1f%%", pct), BLUE);
        addInfoTile(grid, "🔥", t("Today", "අද"), "Rs. " + fmt(todaySaved()), ORANGE);

        return card;
    }

    private LinearLayout buildBadges() {
        LinearLayout card = card();
        card.setPadding(dp(16), dp(16), dp(16), dp(16));

        card.addView(text(t("Achievement Badges", "ජයග්‍රහණ ලාංඡන"), 20, textPrimary(), true));
        TextView sub = text(t("Unlock milestones as you keep saving.", "ඉතිරි කරමින් ඉලක්ක සපුරා ලාංඡන විවෘත කරන්න."), 12, textMuted(), false);
        LinearLayout.LayoutParams subLp = matchWrap();
        subLp.topMargin = dp(4);
        card.addView(sub, subLp);

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        LinearLayout.LayoutParams gridLp = matchWrap();
        gridLp.topMargin = dp(12);
        card.addView(grid, gridLp);

        String[] en = {"Starter","Steady Saver","Halfway Hero","Goal Hunter","100K Champion"};
        String[] si = {"ආරම්භකයා","ස්ථිර ඉතිරිකරු","අඩක් සම්පූර්ණයි","ඉලක්ක හඹායන්නා","100K ශූරයා"};
        String[] icons = {"🌱","💪","⭐","🏆","👑"};
        int[] goals = {10000,25000,50000,75000,100000};
        int saved = totalSaved();

        for (int i = 0; i < goals.length; i++) {
            boolean unlocked = saved >= goals[i];
            TextView badge = text(
                    icons[i] + "\n" + (sinhalaMode ? si[i] : en[i]) + "\nRs. " + fmt(goals[i]),
                    13,
                    unlocked ? (i == 4 ? GOLD : DONE) : textMuted(),
                    true
            );
            badge.setGravity(Gravity.CENTER);
            badge.setPadding(dp(10), dp(14), dp(10), dp(14));
            badge.setMinHeight(dp(96));
            badge.setBackground(roundRect(
                    unlocked
                            ? (darkMode ? Color.rgb(40,56,47) : Color.rgb(238,249,242))
                            : softBg(),
                    unlocked ? (i == 4 ? GOLD : DONE) : border(),
                    18, 1));

            GridLayout.LayoutParams badgeLp = new GridLayout.LayoutParams();
            badgeLp.width = 0;
            badgeLp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            badgeLp.setMargins(dp(4), dp(4), dp(4), dp(4));
            grid.addView(badge, badgeLp);
        }

        return card;
    }

    private LinearLayout buildSettingsCard() {
        LinearLayout card = card();
        card.setPadding(dp(16), dp(16), dp(16), dp(16));

        card.addView(text(t("App Settings", "යෙදුම් සැකසුම්"), 20, textPrimary(), true));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowLp = matchWrap();
        rowLp.topMargin = dp(12);
        card.addView(row, rowLp);

        Button theme = compactButton(darkMode ? t("☀ Light Mode", "☀ ආලෝක මාදිලිය") : t("🌙 Dark Mode", "🌙 අඳුරු මාදිලිය"));
        theme.setOnClickListener(v -> {
            darkMode = !darkMode;
            prefs.edit().putBoolean(KEY_DARK, darkMode).apply();
            recreate();
        });
        row.addView(theme, new LinearLayout.LayoutParams(0, dp(52), 1f));

        Button lang = compactButton(sinhalaMode ? "EN English" : "සිංහල");
        lang.setOnClickListener(v -> {
            sinhalaMode = !sinhalaMode;
            prefs.edit().putBoolean(KEY_SINHALA, sinhalaMode).apply();
            recreate();
        });
        LinearLayout.LayoutParams langLp = new LinearLayout.LayoutParams(0, dp(52), 1f);
        langLp.leftMargin = dp(8);
        row.addView(lang, langLp);

        return card;
    }

    private LinearLayout buildTrackerSummary() {
        LinearLayout card = card();
        card.setPadding(dp(14), dp(14), dp(14), dp(14));

        ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(TARGET);
        bar.setProgress(totalSaved());
        bar.setProgressTintList(ColorStateList.valueOf(PINK));
        bar.setProgressBackgroundTintList(ColorStateList.valueOf(darkMode ? Color.rgb(57,67,85) : Color.rgb(231,236,245)));
        card.addView(bar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(12)));

        LinearLayout labels = new LinearLayout(this);
        labels.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams labelsLp = matchWrap();
        labelsLp.topMargin = dp(8);
        card.addView(labels, labelsLp);

        TextView left = text("Rs. " + fmt(totalSaved()) + " " + t("saved", "ඉතිරි කර ඇත"), 13, DONE, true);
        TextView right = text(String.format(Locale.US, "%.1f%%", totalSaved()*100.0/TARGET), 13, PINK, true);

        labels.addView(left, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        right.setGravity(Gravity.END);
        labels.addView(right, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        return card;
    }

    private LinearLayout buildHistorySummary() {
        LinearLayout card = card();
        card.setPadding(dp(16), dp(16), dp(16), dp(16));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        card.addView(row, matchWrap());

        statCard(row, t("Today", "අද"), "Rs. " + fmt(todaySaved()));
        statCard(row, t("Last 7 Days", "පසුගිය දින 7"), "Rs. " + fmt(lastNDaysSaved(7)));

        return card;
    }

    private LinearLayout buildHistoryList(int days) {
        LinearLayout card = card();
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.addView(text(t("Daily Activity", "දිනපතා ක්‍රියාකාරකම්"), 20, textPrimary(), true));

        for (int i = 0; i < days; i++) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -i);
            Date d = cal.getTime();
            String key = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(d);
            int amount = prefs.getInt(historyPrefKey(key), 0);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(dp(12), dp(12), dp(12), dp(12));
            row.setBackground(roundRect(softBg(), border(), 14, 1));

            TextView day = text(historyDayLabel(i, d), 13, textPrimary(), true);
            row.addView(day, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            TextView val = text("Rs. " + fmt(amount), 13, amount > 0 ? DONE : textMuted(), true);
            row.addView(val);

            LinearLayout.LayoutParams lp = matchWrap();
            lp.topMargin = dp(i == 0 ? 12 : 8);
            card.addView(row, lp);
        }

        return card;
    }

    private LinearLayout section(int amount, int count, int accent, String subtitle) {
        LinearLayout outer = card();
        outer.setPadding(dp(12), dp(14), dp(12), dp(16));

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout textArea = new LinearLayout(this);
        textArea.setOrientation(LinearLayout.VERTICAL);
        textArea.addView(text((sinhalaMode ? "රු. " : "Rs. ") + amount, 21, textPrimary(), true));
        textArea.addView(text(subtitle, 12, textMuted(), false));

        head.addView(textArea, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        int doneCount = countAmount(amount, count);
        TextView counter = text(doneCount + " / " + count, 12, accent, true);
        counter.setGravity(Gravity.CENTER);
        counter.setPadding(dp(10), dp(7), dp(10), dp(7));
        counter.setBackground(roundRect(lightenForTheme(accent), accent, 18, 0));
        head.addView(counter);

        outer.addView(head, matchWrap());

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(5);
        LinearLayout.LayoutParams gridLp = matchWrap();
        gridLp.topMargin = dp(12);
        outer.addView(grid, gridLp);

        for (int i = 0; i < count; i++) {
            String key = amount + "_" + i;
            boolean checked = done.contains(key);

            Button b = new Button(this);
            b.setTag(String.valueOf(amount));
            b.setText(checked ? "✓" : String.valueOf(amount));
            b.setAllCaps(false);
            b.setTextSize(14);
            b.setTypeface(Typeface.DEFAULT_BOLD);
            b.setPadding(0,0,0,0);

            if (checked) {
                b.setTextColor(Color.WHITE);
                b.setBackground(roundRect(DONE, DONE, 12, 1));
            } else {
                b.setTextColor(accent);
                b.setBackground(roundRect(cardBg(), accent, 12, 1));
            }

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = 0;
            lp.height = dp(50);
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            lp.setMargins(dp(3), dp(4), dp(3), dp(4));
            grid.addView(b, lp);

            b.setOnClickListener(v -> {
                if (done.contains(key)) {
                    done.remove(key);
                    String originalDate = getBoxDate(key);
                    if (originalDate != null) adjustHistory(originalDate, -amount);
                    prefs.edit().remove(boxDatePrefKey(key)).apply();
                } else {
                    done.add(key);
                    String today = todayKey();
                    prefs.edit().putString(boxDatePrefKey(key), today).apply();
                    adjustHistory(today, amount);
                }
                saveDone();
                showScreen(1);
                if (totalSaved() == TARGET) {
                    Toast.makeText(this,
                            t("🎉 Congratulations! You reached Rs. 100,000!",
                              "🎉 සුභ පැතුම්! ඔබ රු. 100,000 ඉලක්කය සපුරා ඇත!"),
                            Toast.LENGTH_LONG).show();
                }
            });
        }

        return outer;
    }

    private void addInfoTile(GridLayout grid, String icon, String label, String value, int accent) {
        LinearLayout tile = new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setPadding(dp(12), dp(12), dp(12), dp(12));
        tile.setBackground(roundRect(softBg(), border(), 17, 1));

        tile.addView(text(icon, 22, textPrimary(), false));
        TextView l = text(label, 12, textMuted(), false);
        LinearLayout.LayoutParams lLp = matchWrap();
        lLp.topMargin = dp(5);
        tile.addView(l, lLp);

        TextView v = text(value, 17, accent, true);
        LinearLayout.LayoutParams vLp = matchWrap();
        vLp.topMargin = dp(2);
        tile.addView(v, vLp);

        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0;
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        lp.setMargins(dp(4), dp(4), dp(4), dp(4));
        grid.addView(tile, lp);
    }

    private TextView statCard(LinearLayout parent, String label, String valueText) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(12), dp(10), dp(12), dp(10));
        box.setBackground(roundRect(softBg(), border(), 16, 1));

        box.addView(text(label, 12, textMuted(), false));
        box.addView(text(valueText, 19, textPrimary(), true));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        if (parent.getChildCount() > 0) lp.leftMargin = dp(8);
        parent.addView(box, lp);
        return null;
    }

    private Button compactButton(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(13);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setTextColor(textPrimary());
        b.setBackground(roundRect(softBg(), border(), 16, 1));
        return b;
    }

    private ScrollView baseScroll() {
        ScrollView s = new ScrollView(this);
        s.setFillViewport(true);
        s.setBackgroundColor(pageBg());
        return s;
    }

    private LinearLayout baseContent() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(14), dp(16), dp(14), dp(24));
        return root;
    }

    private LinearLayout card() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setBackground(roundRect(cardBg(), border(), 22, 1));
        l.setElevation(dp(2));
        return l;
    }

    private TextView text(String s, int sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    private String t(String en, String si) {
        return sinhalaMode ? si : en;
    }

    private int totalSaved() {
        return countAmount(500,100) * 500
                + countAmount(200,150) * 200
                + countAmount(100,100) * 100
                + countAmount(50,200) * 50;
    }

    private int totalCompletedBoxes() {
        return countAmount(500,100)
                + countAmount(200,150)
                + countAmount(100,100)
                + countAmount(50,200);
    }

    private int countAmount(int amount, int count) {
        int n = 0;
        for (int i = 0; i < count; i++) {
            if (done.contains(amount + "_" + i)) n++;
        }
        return n;
    }

    private void saveDone() {
        prefs.edit().putStringSet(KEY_DONE, new HashSet<>(done)).apply();
    }

    private String todayKey() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }

    private String historyPrefKey(String date) {
        return "history_" + date;
    }

    private String boxDatePrefKey(String boxKey) {
        return "box_date_" + boxKey;
    }

    private String getBoxDate(String boxKey) {
        return prefs.getString(boxDatePrefKey(boxKey), null);
    }

    private void adjustHistory(String date, int delta) {
        int current = prefs.getInt(historyPrefKey(date), 0);
        int next = Math.max(0, current + delta);
        SharedPreferences.Editor editor = prefs.edit();
        if (next == 0) editor.remove(historyPrefKey(date));
        else editor.putInt(historyPrefKey(date), next);
        editor.apply();
    }

    private int todaySaved() {
        return prefs.getInt(historyPrefKey(todayKey()), 0);
    }

    private int lastNDaysSaved(int days) {
        int total = 0;
        for (int i = 0; i < days; i++) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -i);
            String key = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.getTime());
            total += prefs.getInt(historyPrefKey(key), 0);
        }
        return total;
    }

    private String historyDayLabel(int offset, Date date) {
        if (offset == 0) return t("Today", "අද");
        if (offset == 1) return t("Yesterday", "ඊයේ");
        return new SimpleDateFormat("dd MMM yyyy", Locale.US).format(date);
    }

    private void confirmReset() {
        new AlertDialog.Builder(this)
                .setTitle(t("Reset tracker?", "ට්‍රැකර් නැවත සකසන්නද?"))
                .setMessage(t(
                        "This removes all saved progress and history.",
                        "මෙය සියලු ඉතිරි කළ ප්‍රගතිය සහ ඉතිහාසය මකා දමයි."
                ))
                .setNegativeButton(t("Cancel", "අවලංගු"), null)
                .setPositiveButton(t("Reset", "නැවත සකසන්න"), (dialog, which) -> {
                    boolean keepDark = darkMode;
                    boolean keepSinhala = sinhalaMode;
                    prefs.edit().clear()
                            .putBoolean(KEY_DARK, keepDark)
                            .putBoolean(KEY_SINHALA, keepSinhala)
                            .apply();
                    done.clear();
                    showScreen(2);
                })
                .show();
    }

    private GradientDrawable roundRect(int fill, int stroke, int radiusDp, int strokeDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fill);
        g.setCornerRadius(dp(radiusDp));
        if (strokeDp > 0) g.setStroke(dp(strokeDp), stroke);
        return g;
    }

    private int lightenForTheme(int color) {
        if (darkMode) {
            int r = Math.max(0, Color.red(color) / 3);
            int g = Math.max(0, Color.green(color) / 3);
            int b = Math.max(0, Color.blue(color) / 3);
            return Color.rgb(r + 20, g + 20, b + 20);
        }
        int r = Color.red(color), g = Color.green(color), b = Color.blue(color);
        r = (int)(r + (255-r)*0.88);
        g = (int)(g + (255-g)*0.88);
        b = (int)(b + (255-b)*0.88);
        return Color.rgb(r,g,b);
    }

    private String fmt(int value) {
        return String.format(Locale.US, "%,d", Math.max(0, value));
    }

    private int dp(int v) {
        return (int)(v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    private LinearLayout.LayoutParams topMargin(int marginDp) {
        LinearLayout.LayoutParams lp = matchWrap();
        lp.topMargin = dp(marginDp);
        return lp;
    }
}
