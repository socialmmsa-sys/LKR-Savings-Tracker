package com.savindu.savingstracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

import java.util.Locale;

public class ProgressCircleView extends View {

    private int max = 100000;
    private int progress = 0;

    private int trackColor = Color.rgb(232,237,245);
    private int progressColor = Color.rgb(239,79,123);
    private int titleColor = Color.rgb(11,45,92);
    private int subtitleColor = Color.rgb(115,128,153);
    private int amountColor = Color.rgb(31,157,90);

    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bigTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint smallTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint moneyTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF arcRect = new RectF();

    public ProgressCircleView(Context context) {
        super(context);

        bgPaint.setStyle(Paint.Style.STROKE);
        bgPaint.setStrokeCap(Paint.Cap.ROUND);

        fgPaint.setStyle(Paint.Style.STROKE);
        fgPaint.setStrokeCap(Paint.Cap.ROUND);

        bigTextPaint.setTextAlign(Paint.Align.CENTER);
        bigTextPaint.setFakeBoldText(true);

        smallTextPaint.setTextAlign(Paint.Align.CENTER);

        moneyTextPaint.setTextAlign(Paint.Align.CENTER);
        moneyTextPaint.setFakeBoldText(true);

        refreshPaint();
    }

    public void setMax(int max) {
        this.max = Math.max(1, max);
        invalidate();
    }

    public void setProgress(int progress) {
        this.progress = Math.max(0, Math.min(progress, max));
        invalidate();
    }

    public void setThemeColors(int track, int progress, int title, int subtitle, int amount) {
        this.trackColor = track;
        this.progressColor = progress;
        this.titleColor = title;
        this.subtitleColor = subtitle;
        this.amountColor = amount;
        refreshPaint();
        invalidate();
    }

    private void refreshPaint() {
        bgPaint.setColor(trackColor);
        fgPaint.setColor(progressColor);
        bgPaint.setStrokeWidth(dp(14));
        fgPaint.setStrokeWidth(dp(14));

        bigTextPaint.setColor(titleColor);
        bigTextPaint.setTextSize(dp(17));

        smallTextPaint.setColor(subtitleColor);
        smallTextPaint.setTextSize(dp(6));

        moneyTextPaint.setColor(amountColor);
        moneyTextPaint.setTextSize(dp(8));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desired = dp(210);
        int width = resolveSize(desired, widthMeasureSpec);
        int height = resolveSize(desired, heightMeasureSpec);
        int size = Math.min(width, height);
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int size = Math.min(getWidth(), getHeight());
        int pad = dp(20);
        arcRect.set(pad, pad, size - pad, size - pad);

        canvas.drawArc(arcRect, 0, 360, false, bgPaint);
        float sweep = (progress * 360f) / max;
        canvas.drawArc(arcRect, -90, sweep, false, fgPaint);

        float cx = size / 2f;
        float cy = size / 2f;
        float pct = (progress * 100f) / max;

        canvas.drawText(String.format(Locale.US, "%.0f%%", pct), cx, cy - dp(6), bigTextPaint);
        canvas.drawText("complete", cx, cy + dp(9), smallTextPaint);
        canvas.drawText("Rs. " + String.format(Locale.US, "%,d", progress), cx, cy + dp(28), moneyTextPaint);
    }

    private int dp(int v) {
        return (int)(v * getResources().getDisplayMetrics().density + 0.5f);
    }
}
