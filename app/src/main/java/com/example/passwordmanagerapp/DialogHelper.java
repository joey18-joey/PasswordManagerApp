package com.example.passwordmanagerapp;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DialogHelper {

    public static final int TYPE_WARNING = 1;
    public static final int TYPE_SUCCESS = 2;
    public static final int TYPE_ERROR = 3;

    public static void showSingleButtonDialog(
            Context context,
            int type,
            String title,
            String message,
            String buttonText,
            Runnable onPositive
    ) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_status, null);
        dialog.setContentView(view);
        dialog.setCancelable(true);

        setupDialogWindow(dialog);
        bindDialog(view, dialog, type, title, message);

        LinearLayout layoutTwoButtons = view.findViewById(R.id.layoutTwoButtons);
        Button btnSingle = view.findViewById(R.id.btnSingle);
        ImageView btnClose = view.findViewById(R.id.btnCloseDialog);

        layoutTwoButtons.setVisibility(View.GONE);
        btnSingle.setVisibility(View.VISIBLE);
        btnSingle.setText(buttonText);
        btnSingle.setBackgroundResource(getPositiveButtonBackground(type));

        btnSingle.setOnClickListener(v -> {
            dialog.dismiss();
            if (onPositive != null) onPositive.run();
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());

        animateDialog(view);
        dialog.show();
    }

    public static void showTwoButtonDialog(
            Context context,
            int type,
            String title,
            String message,
            String positiveText,
            String negativeText,
            Runnable onPositive,
            Runnable onNegative
    ) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_status, null);
        dialog.setContentView(view);
        dialog.setCancelable(true);

        setupDialogWindow(dialog);
        bindDialog(view, dialog, type, title, message);

        LinearLayout layoutTwoButtons = view.findViewById(R.id.layoutTwoButtons);
        Button btnSingle = view.findViewById(R.id.btnSingle);
        Button btnPositive = view.findViewById(R.id.btnPositive);
        Button btnNegative = view.findViewById(R.id.btnNegative);
        ImageView btnClose = view.findViewById(R.id.btnCloseDialog);

        layoutTwoButtons.setVisibility(View.VISIBLE);
        btnSingle.setVisibility(View.GONE);

        btnPositive.setText(positiveText);
        btnNegative.setText(negativeText);

        btnPositive.setBackgroundResource(getPositiveButtonBackground(type));
        btnNegative.setBackgroundResource(getNegativeButtonBackground(type));

        btnPositive.setOnClickListener(v -> {
            dialog.dismiss();
            if (onPositive != null) onPositive.run();
        });

        btnNegative.setOnClickListener(v -> {
            dialog.dismiss();
            if (onNegative != null) onNegative.run();
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());

        animateDialog(view);
        dialog.show();
    }

    private static void bindDialog(View view, Dialog dialog, int type, String title, String message) {
        TextView tvIcon = view.findViewById(R.id.tvDialogIcon);
        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        TextView tvMessage = view.findViewById(R.id.tvDialogMessage);

        tvTitle.setText(title);
        tvMessage.setText(message);

        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(getCircleColor(type));
        circle.setStroke(6, Color.WHITE);

        tvIcon.setBackground(circle);
        tvIcon.setText(getSymbol(type));
    }

    private static void setupDialogWindow(Dialog dialog) {
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private static void animateDialog(View view) {
        ScaleAnimation anim = new ScaleAnimation(
                0.85f, 1f,
                0.85f, 1f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f
        );
        anim.setDuration(260);
        anim.setInterpolator(new OvershootInterpolator());
        view.startAnimation(anim);
    }

    private static int getCircleColor(int type) {
        if (type == TYPE_SUCCESS) return Color.parseColor("#14D777");
        if (type == TYPE_ERROR) return Color.parseColor("#F44336");
        return Color.parseColor("#FFC107");
    }

    private static String getSymbol(int type) {
        if (type == TYPE_SUCCESS) return "✓";
        if (type == TYPE_ERROR) return "✕";
        return "!";
    }

    private static int getPositiveButtonBackground(int type) {
        if (type == TYPE_ERROR) return R.drawable.bg_dialog_button_red;
        if (type == TYPE_WARNING) return R.drawable.bg_dialog_button_green;
        return R.drawable.bg_dialog_button_green;
    }

    private static int getNegativeButtonBackground(int type) {
        if (type == TYPE_WARNING) return R.drawable.bg_dialog_button_red;
        if (type == TYPE_ERROR) return R.drawable.bg_dialog_button_red;
        return R.drawable.bg_dialog_button_red;
    }
}