package com.example.passwordmanagerapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class GuideOverlayView extends FrameLayout {

    public interface OnFinishListener {
        void onFinish();
    }

    public static class Step {
        public View targetView;
        public String title;
        public String message;

        public Step(View targetView, String title, String message) {
            this.targetView = targetView;
            this.title = title;
            this.message = message;
        }
    }

    private final List<Step> steps;
    private final OnFinishListener onFinishListener;

    private int currentIndex = 0;

    private final Paint dimPaint = new Paint();
    private final Paint clearPaint = new Paint();
    private final Paint borderPaint = new Paint();

    private final RectF highlightRect = new RectF();

    private LinearLayout infoCard;
    private TextView tvStepCount;
    private TextView tvTitle;
    private TextView tvMessage;
    private TextView btnSkip;
    private TextView btnNext;

    public GuideOverlayView(Context context, List<Step> steps, OnFinishListener onFinishListener) {
        super(context);
        this.steps = steps;
        this.onFinishListener = onFinishListener;

        setWillNotDraw(false);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        setClickable(true);

        dimPaint.setColor(Color.parseColor("#CC0F172A"));

        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        borderPaint.setColor(Color.parseColor("#E0FFFFFF"));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(dp(2));

        buildInfoCard();
        post(this::refreshStep);
    }

    private void buildInfoCard() {
        infoCard = new LinearLayout(getContext());
        infoCard.setOrientation(LinearLayout.VERTICAL);
        infoCard.setPadding(dp(18), dp(18), dp(18), dp(18));

        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Color.parseColor("#F7FFFFFF"));
        cardBg.setCornerRadius(dp(20));
        cardBg.setStroke(dp(1), Color.parseColor("#33FFFFFF"));
        infoCard.setBackground(cardBg);

        LayoutParams cardParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.leftMargin = dp(20);
        cardParams.rightMargin = dp(20);
        infoCard.setLayoutParams(cardParams);

        tvStepCount = new TextView(getContext());
        tvStepCount.setTextColor(Color.parseColor("#7C3AED"));
        tvStepCount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tvStepCount.setTypeface(null, Typeface.BOLD);

        tvTitle = new TextView(getContext());
        tvTitle.setTextColor(Color.parseColor("#111827"));
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setPadding(0, dp(6), 0, 0);

        tvMessage = new TextView(getContext());
        tvMessage.setTextColor(Color.parseColor("#4B5563"));
        tvMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        tvMessage.setPadding(0, dp(10), 0, dp(16));

        LinearLayout buttonRow = new LinearLayout(getContext());
        buttonRow.setOrientation(LinearLayout.HORIZONTAL);
        buttonRow.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);

        btnSkip = new TextView(getContext());
        btnSkip.setText("Skip");
        btnSkip.setTextColor(Color.parseColor("#6B7280"));
        btnSkip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        btnSkip.setTypeface(null, Typeface.BOLD);
        btnSkip.setPadding(dp(12), dp(10), dp(12), dp(10));

        btnNext = new TextView(getContext());
        btnNext.setText("Next");
        btnNext.setTextColor(Color.WHITE);
        btnNext.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        btnNext.setTypeface(null, Typeface.BOLD);
        btnNext.setPadding(dp(18), dp(10), dp(18), dp(10));

        GradientDrawable nextBg = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{
                        Color.parseColor("#8B5CF6"),
                        Color.parseColor("#6D28D9")
                }
        );
        nextBg.setCornerRadius(dp(14));
        btnNext.setBackground(nextBg);

        buttonRow.addView(btnSkip);
        buttonRow.addView(btnNext);

        infoCard.addView(tvStepCount);
        infoCard.addView(tvTitle);
        infoCard.addView(tvMessage);
        infoCard.addView(buttonRow);

        addView(infoCard);

        btnSkip.setOnClickListener(v -> finishGuide());

        btnNext.setOnClickListener(v -> {
            currentIndex++;
            if (currentIndex >= steps.size()) {
                finishGuide();
            } else {
                refreshStep();
            }
        });
    }

    private void refreshStep() {
        if (steps == null || steps.isEmpty() || currentIndex >= steps.size()) {
            finishGuide();
            return;
        }

        Step step = steps.get(currentIndex);

        tvStepCount.setText("Step " + (currentIndex + 1) + " of " + steps.size());
        tvTitle.setText(step.title);
        tvMessage.setText(step.message);

        if (currentIndex == steps.size() - 1) {
            btnNext.setText("Finish");
        } else {
            btnNext.setText("Next");
        }

        View target = step.targetView;
        if (target == null || target.getWidth() == 0 || target.getHeight() == 0) return;

        int[] overlayLoc = new int[2];
        int[] targetLoc = new int[2];

        getLocationOnScreen(overlayLoc);
        target.getLocationOnScreen(targetLoc);

        float left = targetLoc[0] - overlayLoc[0] - dp(8);
        float top = targetLoc[1] - overlayLoc[1] - dp(8);
        float right = left + target.getWidth() + dp(16);
        float bottom = top + target.getHeight() + dp(16);

        highlightRect.set(left, top, right, bottom);

        post(this::positionInfoCard);
        invalidate();
    }

    private void positionInfoCard() {
        LayoutParams params = (LayoutParams) infoCard.getLayoutParams();

        int cardHeight = infoCard.getMeasuredHeight();
        if (cardHeight == 0) {
            infoCard.measure(
                    MeasureSpec.makeMeasureSpec(getWidth() - dp(40), MeasureSpec.AT_MOST),
                    MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST)
            );
            cardHeight = infoCard.getMeasuredHeight();
        }

        int preferredTop = (int) highlightRect.bottom + dp(18);

        if (preferredTop + cardHeight > getHeight() - dp(24)) {
            preferredTop = (int) highlightRect.top - cardHeight - dp(18);
        }

        if (preferredTop < dp(24)) {
            preferredTop = dp(24);
        }

        params.topMargin = preferredTop;
        infoCard.setLayoutParams(params);
    }

    private void finishGuide() {
        if (onFinishListener != null) {
            onFinishListener.onFinish();
        }

        ViewGroup parent = (ViewGroup) getParent();
        if (parent != null) {
            parent.removeView(this);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRect(0, 0, getWidth(), getHeight(), dimPaint);
        canvas.drawRoundRect(highlightRect, dp(20), dp(20), clearPaint);
        canvas.drawRoundRect(highlightRect, dp(20), dp(20), borderPaint);
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics()
        );
    }
}