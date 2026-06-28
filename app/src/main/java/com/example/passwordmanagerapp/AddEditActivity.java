package com.example.passwordmanagerapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AddEditActivity extends AppCompatActivity {

    private EditText etSite, etUsername, etPassword, etConfirmPassword, etExtraNote;
    private Button btnSave, btnGeneratePassword;
    private ImageButton btnTogglePassword, btnToggleConfirmPassword;
    private Spinner spCategory;
    private TextView tvStrength;
    private TextView tvSiteError, tvUsernameError, tvPasswordError, tvConfirmPasswordError;

    private PasswordDatabase database;
    private Password editingPassword = null;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    private boolean isEditMode = false;
    private boolean hasUserChangedAnything = false;

    private long originalCreatedAt = 0L;
    private long originalUpdatedAt = 0L;

    private final String[] categoryOptions = {
            "General",
            "Social",
            "Shopping",
            "Finance",
            "Study",
            "Email"
    };

    private final TextWatcher changeTracker = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            hasUserChangedAnything = true;
        }

        @Override
        public void afterTextChanged(Editable s) { }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        database = PasswordDatabase.getInstance(this);

        etSite = findViewById(R.id.etSite);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etExtraNote = findViewById(R.id.etExtraNote);

        btnSave = findViewById(R.id.btnSave);
        btnGeneratePassword = findViewById(R.id.btnGeneratePassword);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);
        btnToggleConfirmPassword = findViewById(R.id.btnToggleConfirmPassword);

        spCategory = findViewById(R.id.spCategory);

        tvStrength = findViewById(R.id.tvStrength);
        tvSiteError = findViewById(R.id.tvSiteError);
        tvUsernameError = findViewById(R.id.tvUsernameError);
        tvPasswordError = findViewById(R.id.tvPasswordError);
        tvConfirmPasswordError = findViewById(R.id.tvConfirmPasswordError);

        setupCategorySpinner();

        int id = getIntent().getIntExtra("id", -1);
        String site = getIntent().getStringExtra("site");
        String username = getIntent().getStringExtra("username");
        String encryptedPassword = getIntent().getStringExtra("password");
        String extraNote = getIntent().getStringExtra("extraNote");

        if (id != -1) {
            isEditMode = true;
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Edit Password");
            }

            String decryptedPassword = EncryptionHelper.decrypt(encryptedPassword);

            editingPassword = findPasswordById(id);
            if (editingPassword == null) {
                editingPassword = new Password(site, username, encryptedPassword, extraNote, "General");
                editingPassword.setId(id);
            }

            originalCreatedAt = editingPassword.getCreatedAt();
            originalUpdatedAt = editingPassword.getUpdatedAt();

            etSite.setText(site);
            etUsername.setText(username);
            etPassword.setText(decryptedPassword);
            etConfirmPassword.setText(decryptedPassword);
            etExtraNote.setText(extraNote);

            String savedCategory = editingPassword.getCategory();
            if (savedCategory == null || savedCategory.trim().isEmpty()) {
                savedCategory = "General";
            }
            setSpinnerSelection(savedCategory);

            updatePasswordStrength(decryptedPassword);
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Add Password");
            }
            setSpinnerSelection("General");
        }

        btnSave.setOnClickListener(v -> savePassword());

        btnTogglePassword.setOnClickListener(v -> togglePasswordVisibility());
        btnToggleConfirmPassword.setOnClickListener(v -> toggleConfirmPasswordVisibility());

        btnGeneratePassword.setOnClickListener(v -> {
            String generated = generateStrongPassword();
            etPassword.setText(generated);
            etConfirmPassword.setText(generated);
            updatePasswordStrength(generated);
            hasUserChangedAnything = true;
            CustomToast.show(this, "Strong password generated");
        });

        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePasswordStrength(s.toString());
                if (!s.toString().trim().isEmpty()) {
                    tvPasswordError.setVisibility(View.GONE);
                }
                hasUserChangedAnything = true;
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        addClearErrorWatcher(etSite, tvSiteError);
        addClearErrorWatcher(etUsername, tvUsernameError);
        addClearErrorWatcher(etConfirmPassword, tvConfirmPasswordError);

        etSite.addTextChangedListener(changeTracker);
        etUsername.addTextChangedListener(changeTracker);
        etConfirmPassword.addTextChangedListener(changeTracker);
        etExtraNote.addTextChangedListener(changeTracker);

        etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        etConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        btnTogglePassword.setImageResource(android.R.drawable.presence_invisible);
        btnToggleConfirmPassword.setImageResource(android.R.drawable.presence_invisible);

        hasUserChangedAnything = false;

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                attemptLeavePage();
            }
        });
    }

    private void setupCategorySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categoryOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapter);
    }

    private void setSpinnerSelection(String category) {
        for (int i = 0; i < categoryOptions.length; i++) {
            if (categoryOptions[i].equalsIgnoreCase(category)) {
                spCategory.setSelection(i);
                return;
            }
        }
        spCategory.setSelection(categoryOptions.length - 1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            attemptLeavePage();
            return true;
        }

        if (item.getItemId() == R.id.menu_logout) {
            DialogHelper.showTwoButtonDialog(
                    AddEditActivity.this,
                    DialogHelper.TYPE_WARNING,
                    "Logout",
                    "Are you sure you want to logout from your account?",
                    "Logout",
                    "Cancel",
                    () -> {
                        Intent i = new Intent(AddEditActivity.this, LoginActivity.class);
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

    private void attemptLeavePage() {
        if (!hasUserChangedAnything) {
            finish();
            return;
        }

        DialogHelper.showTwoButtonDialog(
                AddEditActivity.this,
                DialogHelper.TYPE_WARNING,
                "Discard Changes",
                "You have unsaved changes.\n\nAre you sure you want to leave this page?",
                "Leave",
                "Stay",
                this::finish,
                null
        );
    }

    private void savePassword() {
        String site = etSite.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String extraNote = etExtraNote.getText().toString().trim();
        String category = spCategory.getSelectedItem().toString();

        boolean hasError = false;

        tvSiteError.setVisibility(View.GONE);
        tvUsernameError.setVisibility(View.GONE);
        tvPasswordError.setVisibility(View.GONE);
        tvConfirmPasswordError.setVisibility(View.GONE);

        if (site.isEmpty()) {
            tvSiteError.setText("* Site name is required");
            tvSiteError.setVisibility(View.VISIBLE);
            hasError = true;
        }

        if (username.isEmpty()) {
            tvUsernameError.setText("* Username / Email is required");
            tvUsernameError.setVisibility(View.VISIBLE);
            hasError = true;
        }

        if (password.isEmpty()) {
            tvPasswordError.setText("* Password is required");
            tvPasswordError.setVisibility(View.VISIBLE);
            hasError = true;
        }

        if (confirmPassword.isEmpty()) {
            tvConfirmPasswordError.setText("* Confirm Password is required");
            tvConfirmPasswordError.setVisibility(View.VISIBLE);
            hasError = true;
        }

        if (hasError) {
            CustomToast.show(this, "Please fill all fields");
            return;
        }

        if (!password.equals(confirmPassword)) {
            tvConfirmPasswordError.setText("* Passwords do not match");
            tvConfirmPasswordError.setVisibility(View.VISIBLE);
            CustomToast.show(this, "Passwords do not match");
            return;
        }

        if (isDuplicateEntry(site, username)) {
            tvSiteError.setText("* This site and username already exist");
            tvSiteError.setVisibility(View.VISIBLE);
            tvUsernameError.setText("* Duplicate account detected");
            tvUsernameError.setVisibility(View.VISIBLE);
            CustomToast.show(this, "Duplicate site and username found");
            return;
        }

        String encryptedPassword = EncryptionHelper.encrypt(password);
        long now = System.currentTimeMillis();

        if (!isEditMode) {
            Password newPassword = new Password(site, username, encryptedPassword, extraNote, category);
            newPassword.setCreatedAt(now);
            newPassword.setUpdatedAt(now);
            database.passwordDao().insert(newPassword);
            CustomToast.show(this, "Password added");
        } else {
            Password updatedPassword = new Password(site, username, encryptedPassword, extraNote, category);
            updatedPassword.setId(getIntent().getIntExtra("id", -1));
            updatedPassword.setCreatedAt(originalCreatedAt == 0L ? now : originalCreatedAt);
            updatedPassword.setUpdatedAt(now);
            database.passwordDao().update(updatedPassword);
            CustomToast.show(this, "Password updated");
        }

        hasUserChangedAnything = false;
        finish();
    }

    private Password findPasswordById(int id) {
        List<Password> allPasswords = database.passwordDao().getAllPasswords();
        if (allPasswords == null) return null;

        for (Password p : allPasswords) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }

    private boolean isDuplicateEntry(String site, String username) {
        List<Password> allPasswords = database.passwordDao().getAllPasswords();

        if (allPasswords == null) return false;

        for (Password p : allPasswords) {
            boolean sameSite = p.getSiteName() != null && p.getSiteName().trim().equalsIgnoreCase(site);
            boolean sameUsername = p.getUsername() != null && p.getUsername().trim().equalsIgnoreCase(username);

            if (sameSite && sameUsername) {
                if (isEditMode && editingPassword != null && p.getId() == editingPassword.getId()) {
                    continue;
                }
                return true;
            }
        }
        return false;
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            btnTogglePassword.setImageResource(android.R.drawable.presence_invisible);
            isPasswordVisible = false;
        } else {
            etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            btnTogglePassword.setImageResource(android.R.drawable.ic_menu_view);
            isPasswordVisible = true;
        }
        etPassword.setSelection(etPassword.getText().length());
    }

    private void toggleConfirmPasswordVisibility() {
        if (isConfirmPasswordVisible) {
            etConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            btnToggleConfirmPassword.setImageResource(android.R.drawable.presence_invisible);
            isConfirmPasswordVisible = false;
        } else {
            etConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            btnToggleConfirmPassword.setImageResource(android.R.drawable.ic_menu_view);
            isConfirmPasswordVisible = true;
        }
        etConfirmPassword.setSelection(etConfirmPassword.getText().length());
    }

    private void updatePasswordStrength(String password) {
        if (password.isEmpty()) {
            tvStrength.setText("Strength: ");
            tvStrength.setTextColor(Color.parseColor("#666666"));
            return;
        }

        int score = 0;

        if (password.length() >= 8) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*[0-9].*")) score++;
        if (password.matches(".*[!@#$%^&*].*")) score++;

        if (score <= 2) {
            tvStrength.setText("Strength: Weak");
            tvStrength.setTextColor(Color.RED);
        } else if (score <= 4) {
            tvStrength.setText("Strength: Medium");
            tvStrength.setTextColor(Color.parseColor("#FFA500"));
        } else {
            tvStrength.setText("Strength: Strong");
            tvStrength.setTextColor(Color.parseColor("#2E7D32"));
        }
    }

    private String generateStrongPassword() {
        SecureRandom random = new SecureRandom();

        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*";
        String all = upper + lower + digits + special;

        List<Character> chars = new ArrayList<>();

        chars.add(upper.charAt(random.nextInt(upper.length())));
        chars.add(lower.charAt(random.nextInt(lower.length())));
        chars.add(digits.charAt(random.nextInt(digits.length())));
        chars.add(special.charAt(random.nextInt(special.length())));

        for (int i = 4; i < 12; i++) {
            chars.add(all.charAt(random.nextInt(all.length())));
        }

        Collections.shuffle(chars, random);

        StringBuilder sb = new StringBuilder();
        for (char c : chars) {
            sb.append(c);
        }

        return sb.toString();
    }

    private void addClearErrorWatcher(EditText editText, TextView errorView) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    errorView.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }
}