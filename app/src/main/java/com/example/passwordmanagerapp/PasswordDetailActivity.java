package com.example.passwordmanagerapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PasswordDetailActivity extends AppCompatActivity {

    private ViewHolder views;

    private boolean isPasswordVisible = false;
    private int passwordId;
    private String site;
    private String username;
    private String encryptedPassword;
    private String extraNote;
    private String category;
    private long createdAt;
    private long updatedAt;

    private PasswordDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        database = PasswordDatabase.getInstance(this);

        views = new ViewHolder();
        views.tvLogo = findViewById(R.id.tvLogo);
        views.tvSite = findViewById(R.id.tvSite);
        views.tvUsername = findViewById(R.id.tvUsername);
        views.tvCategory = findViewById(R.id.tvCategory);
        views.tvHealth = findViewById(R.id.tvHealth);
        views.tvPasswordView = findViewById(R.id.tvPasswordView);
        views.btnTogglePassword = findViewById(R.id.btnTogglePassword);
        views.btnCopyPassword = findViewById(R.id.btnCopyPassword);
        views.tvUsernameValue = findViewById(R.id.tvUsernameValue);
        views.btnCopyUsername = findViewById(R.id.btnCopyUsername);
        views.tvExtraNote = findViewById(R.id.tvExtraNote);
        views.tvCreatedAt = findViewById(R.id.tvCreatedAt);
        views.tvUpdatedAt = findViewById(R.id.tvUpdatedAt);
        views.btnEdit = findViewById(R.id.btnEdit);
        views.btnDelete = findViewById(R.id.btnDelete);

        passwordId = getIntent().getIntExtra("id", -1);
        site = getIntent().getStringExtra("site");
        username = getIntent().getStringExtra("username");
        encryptedPassword = getIntent().getStringExtra("password");
        extraNote = getIntent().getStringExtra("extraNote");

        Password currentPassword = findPasswordById(passwordId);
        if (currentPassword != null) {
            createdAt = currentPassword.getCreatedAt();
            updatedAt = currentPassword.getUpdatedAt();
            category = currentPassword.getCategory();
        }

        loadDataIntoUI();

        views.btnTogglePassword.setOnClickListener(v -> togglePasswordVisibility());

        views.btnCopyPassword.setOnClickListener(v -> {
            String decryptedPassword = EncryptionHelper.decrypt(encryptedPassword);
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("password", decryptedPassword);
            clipboard.setPrimaryClip(clip);
            CustomToast.show(this, "Password copied");
        });

        views.btnCopyUsername.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("username", username == null ? "" : username);
            clipboard.setPrimaryClip(clip);
            CustomToast.show(this, "Username copied");
        });

        views.btnEdit.setOnClickListener(v -> {
            Intent i = new Intent(PasswordDetailActivity.this, AddEditActivity.class);
            i.putExtra("id", passwordId);
            i.putExtra("site", site);
            i.putExtra("username", username);
            i.putExtra("password", encryptedPassword);
            i.putExtra("extraNote", extraNote);
            startActivity(i);
        });

        views.btnDelete.setOnClickListener(v -> {
            DialogHelper.showTwoButtonDialog(
                    PasswordDetailActivity.this,
                    DialogHelper.TYPE_WARNING,
                    "Delete Password",
                    "Are you sure you want to delete this record?\n\nThis action cannot be undone.",
                    "Delete",
                    "Cancel",
                    () -> {
                        String deleteCategory = category;
                        if (deleteCategory == null || deleteCategory.trim().isEmpty()) {
                            deleteCategory = getCategoryForSite(site);
                        }

                        Password passwordToDelete = new Password(site, username, encryptedPassword, extraNote, deleteCategory);
                        passwordToDelete.setId(passwordId);
                        passwordToDelete.setCreatedAt(createdAt);
                        passwordToDelete.setUpdatedAt(updatedAt);
                        database.passwordDao().delete(passwordToDelete);
                        CustomToast.show(PasswordDetailActivity.this, "Password deleted");
                        finish();
                    },
                    null
            );
        });
    }

    private void loadDataIntoUI() {
        views.tvSite.setText(site);
        views.tvUsername.setText(username);
        views.tvUsernameValue.setText(username == null ? "" : username);

        String logoLetter = (site != null && !site.trim().isEmpty())
                ? site.substring(0, 1).toUpperCase()
                : "?";
        views.tvLogo.setText(logoLetter);
        applyLogoTint(views.tvLogo, getLogoColor(site));

        String displayCategory = category;
        if (displayCategory == null || displayCategory.trim().isEmpty()) {
            displayCategory = getCategoryForSite(site);
        }

        views.tvCategory.setText(displayCategory);
        applyBadgeColor(views.tvCategory, getCategoryColor(displayCategory));
        views.tvCategory.setTextColor(getCategoryTextColor(displayCategory));

        String decryptedPassword = EncryptionHelper.decrypt(encryptedPassword);
        int strengthType = getPasswordStrengthType(decryptedPassword);

        if (strengthType == 2) {
            views.tvHealth.setText("Strong");
            applyBadgeColor(views.tvHealth, Color.parseColor("#DCFCE7"));
            views.tvHealth.setTextColor(Color.parseColor("#166534"));
        } else if (strengthType == 1) {
            views.tvHealth.setText("Medium");
            applyBadgeColor(views.tvHealth, Color.parseColor("#FEF3C7"));
            views.tvHealth.setTextColor(Color.parseColor("#B45309"));
        } else {
            views.tvHealth.setText("Weak");
            applyBadgeColor(views.tvHealth, Color.parseColor("#FEE2E2"));
            views.tvHealth.setTextColor(Color.parseColor("#B91C1C"));
        }

        views.tvPasswordView.setText(decryptedPassword);
        views.tvPasswordView.setTransformationMethod(PasswordTransformationMethod.getInstance());
        views.btnTogglePassword.setImageResource(android.R.drawable.presence_invisible);

        if (extraNote != null && !extraNote.trim().isEmpty()) {
            views.tvExtraNote.setText(extraNote);
        } else {
            views.tvExtraNote.setText("No extra note added");
        }

        views.tvCreatedAt.setText(formatFullTimestamp(createdAt) + "  •  " + formatRelativeTime(createdAt));

        if (isNeverUpdated()) {
            views.tvUpdatedAt.setText("Not updated yet");
        } else {
            views.tvUpdatedAt.setText(formatFullTimestamp(updatedAt) + "  •  " + formatRelativeTime(updatedAt));
        }
    }

    private boolean isNeverUpdated() {
        return createdAt > 0 && updatedAt > 0 && createdAt == updatedAt;
    }

    private Password findPasswordById(int id) {
        for (Password p : database.passwordDao().getAllPasswords()) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            views.tvPasswordView.setTransformationMethod(PasswordTransformationMethod.getInstance());
            views.btnTogglePassword.setImageResource(android.R.drawable.presence_invisible);
            isPasswordVisible = false;
        } else {
            views.tvPasswordView.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            views.btnTogglePassword.setImageResource(android.R.drawable.ic_menu_view);
            isPasswordVisible = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (passwordId != -1) {
            Password p = findPasswordById(passwordId);
            if (p != null) {
                site = p.getSiteName();
                username = p.getUsername();
                encryptedPassword = p.getPassword();
                extraNote = p.getExtraNote();
                category = p.getCategory();
                createdAt = p.getCreatedAt();
                updatedAt = p.getUpdatedAt();

                loadDataIntoUI();

                if (isPasswordVisible) {
                    views.tvPasswordView.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    views.btnTogglePassword.setImageResource(android.R.drawable.ic_menu_view);
                } else {
                    views.tvPasswordView.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    views.btnTogglePassword.setImageResource(android.R.drawable.presence_invisible);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        if (item.getItemId() == R.id.menu_logout) {
            DialogHelper.showTwoButtonDialog(
                    PasswordDetailActivity.this,
                    DialogHelper.TYPE_WARNING,
                    "Logout",
                    "Are you sure you want to logout from your account?",
                    "Logout",
                    "Cancel",
                    () -> {
                        Intent i = new Intent(PasswordDetailActivity.this, LoginActivity.class);
                        i.putExtra("logout_success", true);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                        finish();
                    },
                    null
            );
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private String formatFullTimestamp(long timestamp) {
        if (timestamp <= 0) return "Not available";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy, hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private String formatRelativeTime(long timestamp) {
        if (timestamp <= 0) return "Not available";

        long diff = System.currentTimeMillis() - timestamp;

        long minute = 60 * 1000L;
        long hour = 60 * minute;
        long day = 24 * hour;

        if (diff < minute) return "just now";
        if (diff < hour) return (diff / minute) + " min ago";
        if (diff < day) return (diff / hour) + " hr ago";
        if (diff < 2 * day) return "yesterday";
        return (diff / day) + " days ago";
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
        String siteLower = siteName == null ? "" : siteName.toLowerCase().trim();

        if (siteLower.contains("google") || siteLower.contains("gmail")) return Color.parseColor("#4285F4");
        if (siteLower.contains("instagram")) return Color.parseColor("#9333EA");
        if (siteLower.contains("shopee")) return Color.parseColor("#F97316");
        if (siteLower.contains("tiktok")) return Color.parseColor("#111827");
        if (siteLower.contains("bank")) return Color.parseColor("#16A34A");
        if (siteLower.contains("utar") || siteLower.contains("portal")) return Color.parseColor("#DC2626");
        if (siteLower.contains("facebook")) return Color.parseColor("#2563EB");
        if (siteLower.contains("lazada")) return Color.parseColor("#F97316");
        if (siteLower.contains("maybank")) return Color.parseColor("#16A34A");
        if (siteLower.contains("public bank")) return Color.parseColor("#16A34A");

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
        String siteLower = siteName == null ? "" : siteName.toLowerCase();

        if (siteLower.contains("instagram") || siteLower.contains("facebook") || siteLower.contains("tiktok")) {
            return "Social";
        } else if (siteLower.contains("shopee") || siteLower.contains("lazada")) {
            return "Shopping";
        } else if (siteLower.contains("bank") || siteLower.contains("public bank") || siteLower.contains("maybank")) {
            return "Finance";
        } else if (siteLower.contains("utar") || siteLower.contains("portal") || siteLower.contains("student")) {
            return "Study";
        } else if (siteLower.contains("google") || siteLower.contains("gmail")) {
            return "Email";
        }

        return "General";
    }

    private int getCategoryColor(String category) {
        switch (category) {
            case "Social":
                return Color.parseColor("#EDE9FE");
            case "Shopping":
                return Color.parseColor("#FFEDD5");
            case "Finance":
                return Color.parseColor("#DCFCE7");
            case "Study":
                return Color.parseColor("#FEE2E2");
            case "Email":
                return Color.parseColor("#DBEAFE");
            default:
                return Color.parseColor("#F3E8FF");
        }
    }

    private int getCategoryTextColor(String category) {
        switch (category) {
            case "Social":
                return Color.parseColor("#6D28D9");
            case "Shopping":
                return Color.parseColor("#C2410C");
            case "Finance":
                return Color.parseColor("#15803D");
            case "Study":
                return Color.parseColor("#B91C1C");
            case "Email":
                return Color.parseColor("#1D4ED8");
            default:
                return Color.parseColor("#7C3AED");
        }
    }

    private static class ViewHolder {
        TextView tvLogo, tvSite, tvUsername, tvCategory, tvHealth, tvPasswordView, tvUsernameValue, tvExtraNote, tvCreatedAt, tvUpdatedAt;
        ImageButton btnTogglePassword, btnCopyPassword, btnCopyUsername;
        TextView btnEdit, btnDelete;
    }
}