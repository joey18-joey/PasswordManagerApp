package com.example.passwordmanagerapp;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PasswordAdapter extends RecyclerView.Adapter<PasswordAdapter.VH> {

    public interface OnItemClickListener {
        void onClick(Password p);
    }

    private final List<Password> list = new ArrayList<>();
    private final List<Password> fullList = new ArrayList<>();
    private OnItemClickListener clickListener;

    public void setOnItemClickListener(OnItemClickListener l) {
        clickListener = l;
    }

    public void setData(List<Password> data) {
        list.clear();
        fullList.clear();
        if (data != null) {
            list.addAll(data);
            fullList.addAll(data);
        }
        notifyDataSetChanged();
    }

    public void clearData() {
        list.clear();
        notifyDataSetChanged();
    }

    public void addItem(Password password) {
        if (password != null) {
            list.add(password);
            notifyDataSetChanged();
        }
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public Password getItemAtPosition(int position) {
        if (position >= 0 && position < list.size()) {
            return list.get(position);
        }
        return null;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_password, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Password p = list.get(position);

        h.tvSite.setText(p.getSiteName());
        h.tvUsername.setText(p.getUsername());
        h.tvPasswordMasked.setText("••••••••");
        h.tvViewHint.setText("Tap to view details");
        h.isPasswordVisible = false;
        h.btnToggle.setImageResource(android.R.drawable.presence_invisible);

        String siteName = p.getSiteName();
        String logoLetter = (siteName != null && !siteName.trim().isEmpty())
                ? siteName.substring(0, 1).toUpperCase()
                : "?";
        h.tvLogo.setText(logoLetter);
        applyLogoTint(h.tvLogo, getLogoColor(siteName));

        String category = getDisplayCategory(p);
        h.tvCategory.setText(category);
        applyBadgeColor(h.tvCategory, getCategoryColor(category));
        h.tvCategory.setTextColor(getCategoryTextColor(category));

        String decryptedPassword = EncryptionHelper.decrypt(p.getPassword());
        int strengthType = getPasswordStrengthType(decryptedPassword);

        if (strengthType == 2) {
            h.tvHealth.setText("Strong");
            applyBadgeColor(h.tvHealth, Color.parseColor("#E8F5E9"));
            h.tvHealth.setTextColor(Color.parseColor("#1B5E20"));
            h.tvRiskWarning.setText("");
            h.tvRiskWarning.setVisibility(View.GONE);

        } else if (strengthType == 1) {
            h.tvHealth.setText("Medium");
            applyBadgeColor(h.tvHealth, Color.parseColor("#FFF3E0"));
            h.tvHealth.setTextColor(Color.parseColor("#EF6C00"));
            h.tvRiskWarning.setText("⚠ Could be stronger");
            h.tvRiskWarning.setTextColor(Color.parseColor("#EF6C00"));
            h.tvRiskWarning.setVisibility(View.VISIBLE);

        } else {
            h.tvHealth.setText("Weak");
            applyBadgeColor(h.tvHealth, Color.parseColor("#FFEBEE"));
            h.tvHealth.setTextColor(Color.parseColor("#B71C1C"));
            h.tvRiskWarning.setText("⚠ Improve this password");
            h.tvRiskWarning.setTextColor(Color.parseColor("#D32F2F"));
            h.tvRiskWarning.setVisibility(View.VISIBLE);
        }

        String extraNote = p.getExtraNote();
        if (extraNote != null && !extraNote.trim().isEmpty()) {
            h.tvExtraNote.setText("📝 " + extraNote);
            h.tvExtraNote.setVisibility(View.VISIBLE);
        } else {
            h.tvExtraNote.setText("");
            h.tvExtraNote.setVisibility(View.GONE);
        }

        h.btnToggle.setOnClickListener(v -> {
            if (h.isPasswordVisible) {
                h.tvPasswordMasked.setText("••••••••");
                h.btnToggle.setImageResource(android.R.drawable.presence_invisible);
                h.isPasswordVisible = false;
            } else {
                h.tvPasswordMasked.setText(decryptedPassword);
                h.btnToggle.setImageResource(android.R.drawable.ic_menu_view);
                h.isPasswordVisible = true;
            }
        });

        h.btnCopy.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("password", decryptedPassword);
            clipboard.setPrimaryClip(clip);

            if (v.getContext() instanceof Activity) {
                CustomToast.show((Activity) v.getContext(), "Password copied");
            }
        });

        h.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onClick(p);
        });
    }

    private String getDisplayCategory(Password p) {
        if (p == null) return "General";

        String savedCategory = p.getCategory();
        if (savedCategory != null && !savedCategory.trim().isEmpty()) {
            return savedCategory;
        }

        return getCategoryForSite(p.getSiteName());
    }

    private void applyLogoTint(TextView textView, int color) {
        Drawable drawable = ContextCompat.getDrawable(textView.getContext(), R.drawable.logo_bg_circle);
        if (drawable != null) {
            Drawable wrapped = DrawableCompat.wrap(drawable.mutate());
            DrawableCompat.setTint(wrapped, color);
            textView.setBackground(wrapped);
            textView.setTextColor(Color.WHITE);
        }
    }

    private void applyBadgeColor(TextView view, int color) {
        Drawable drawable = ContextCompat.getDrawable(view.getContext(), R.drawable.badge_bg);
        if (drawable != null) {
            Drawable wrapped = DrawableCompat.wrap(drawable.mutate());
            DrawableCompat.setTint(wrapped, color);
            view.setBackground(wrapped);
        }
    }

    private int getLogoColor(String siteName) {
        String site = siteName == null ? "" : siteName.toLowerCase().trim();

        if (site.contains("google") || site.contains("gmail")) return Color.parseColor("#4285F4");
        if (site.contains("instagram")) return Color.parseColor("#9C27B0");
        if (site.contains("shopee")) return Color.parseColor("#FF6F00");
        if (site.contains("tiktok")) return Color.parseColor("#212121");
        if (site.contains("bank")) return Color.parseColor("#2E7D32");
        if (site.contains("utar") || site.contains("portal")) return Color.parseColor("#C62828");
        if (site.contains("facebook")) return Color.parseColor("#3B5998");
        if (site.contains("lazada")) return Color.parseColor("#FF6F00");
        if (site.contains("maybank")) return Color.parseColor("#2E7D32");
        if (site.contains("public bank")) return Color.parseColor("#2E7D32");

        return generateColorFromText(siteName);
    }

    private int generateColorFromText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Color.parseColor("#6B7280");
        }

        int hash = text.trim().toLowerCase().hashCode();

        int r = (hash >> 16) & 0xFF;
        int g = (hash >> 8) & 0xFF;
        int b = hash & 0xFF;

        r = (r + 160) / 2;
        g = (g + 160) / 2;
        b = (b + 160) / 2;

        return Color.rgb(r, g, b);
    }

    private int getPasswordStrengthType(String password) {
        if (password == null || password.isEmpty()) return 0;

        int score = 0;
        if (password.length() >= 8) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*[0-9].*")) score++;
        if (password.matches(".*[!@#$%^&*].*")) score++;

        if (score <= 2) return 0;
        if (score <= 4) return 1;
        return 2;
    }

    private String getCategoryForSite(String siteName) {
        String site = siteName == null ? "" : siteName.toLowerCase();

        if (site.contains("instagram") || site.contains("facebook") || site.contains("tiktok")) {
            return "Social";
        } else if (site.contains("shopee") || site.contains("lazada")) {
            return "Shopping";
        } else if (site.contains("bank") || site.contains("public bank") || site.contains("maybank")) {
            return "Finance";
        } else if (site.contains("utar") || site.contains("portal") || site.contains("student")) {
            return "Study";
        } else if (site.contains("google") || site.contains("gmail")) {
            return "Email";
        }

        return "General";
    }

    private int getCategoryColor(String category) {
        switch (category) {
            case "Social":
                return Color.parseColor("#EDE7F6");
            case "Shopping":
                return Color.parseColor("#FFF3E0");
            case "Finance":
                return Color.parseColor("#E8F5E9");
            case "Study":
                return Color.parseColor("#FFEBEE");
            case "Email":
                return Color.parseColor("#E3F2FD");
            default:
                return Color.parseColor("#F3E5F5");
        }
    }

    private int getCategoryTextColor(String category) {
        switch (category) {
            case "Social":
                return Color.parseColor("#5E35B1");
            case "Shopping":
                return Color.parseColor("#EF6C00");
            case "Finance":
                return Color.parseColor("#2E7D32");
            case "Study":
                return Color.parseColor("#C62828");
            case "Email":
                return Color.parseColor("#1565C0");
            default:
                return Color.parseColor("#6A1B9A");
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvLogo, tvSite, tvUsername, tvCategory, tvHealth, tvPasswordMasked, tvExtraNote, tvRiskWarning, tvViewHint;
        ImageButton btnCopy, btnToggle;
        boolean isPasswordVisible = false;

        VH(@NonNull View itemView) {
            super(itemView);
            tvLogo = itemView.findViewById(R.id.tvLogo);
            tvSite = itemView.findViewById(R.id.tvSite);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvHealth = itemView.findViewById(R.id.tvHealth);
            tvPasswordMasked = itemView.findViewById(R.id.tvPasswordMasked);
            tvExtraNote = itemView.findViewById(R.id.tvExtraNote);
            tvRiskWarning = itemView.findViewById(R.id.tvRiskWarning);
            tvViewHint = itemView.findViewById(R.id.tvViewHint);
            btnCopy = itemView.findViewById(R.id.btnCopy);
            btnToggle = itemView.findViewById(R.id.btnToggle);
        }
    }
}