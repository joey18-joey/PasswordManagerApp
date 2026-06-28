package com.example.passwordmanagerapp;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class CustomToast {

    public static void show(Activity activity, String message) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(
                R.layout.custom_toast,
                activity.findViewById(android.R.id.content),
                false
        );

        ImageView ivToastIcon = layout.findViewById(R.id.ivToastIcon);
        TextView tvMessage = layout.findViewById(R.id.tvToastMessage);

        tvMessage.setText(message);

        ivToastIcon.setImageResource(getIconForMessage(message));

        Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(220);
        layout.startAnimation(fadeIn);

        Toast toast = new Toast(activity.getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 160);
        toast.show();
    }

    private static int getIconForMessage(String message) {
        String msg = message.toLowerCase();

        if (msg.contains("success") || msg.contains("successful") || msg.contains("added")
                || msg.contains("updated") || msg.contains("copied") || msg.contains("generated")
                || msg.contains("confirmed")) {
            return android.R.drawable.checkbox_on_background;
        }

        if (msg.contains("incorrect") || msg.contains("wrong") || msg.contains("cannot")
                || msg.contains("required") || msg.contains("must") || msg.contains("not match")
                || msg.contains("please")) {
            return android.R.drawable.ic_dialog_alert;
        }

        return android.R.drawable.ic_dialog_info;
    }
}