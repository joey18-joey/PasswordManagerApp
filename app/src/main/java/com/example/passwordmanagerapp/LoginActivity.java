package com.example.passwordmanagerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private TextView tvPhraseValue, tvPinError;
    private LinearLayout layoutPhraseSection, layoutLoginSection;
    private EditText etPin;
    private Button btnPhraseYes, btnPhraseNo, btnLogin, btnForgotPin;
    private int failedAttempts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);

        if (!prefs.contains("pin")) {
            startActivity(new Intent(this, SetupActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        layoutPhraseSection = findViewById(R.id.layoutPhraseSection);
        layoutLoginSection = findViewById(R.id.layoutLoginSection);

        tvPhraseValue = findViewById(R.id.tvPhraseValue);
        tvPinError = findViewById(R.id.tvPinError);

        btnPhraseYes = findViewById(R.id.btnPhraseYes);
        btnPhraseNo = findViewById(R.id.btnPhraseNo);

        etPin = findViewById(R.id.etPin);
        btnLogin = findViewById(R.id.btnLogin);
        btnForgotPin = findViewById(R.id.btnForgotPin);

        String savedPhrase = prefs.getString("login_phrase", "No login phrase set");
        tvPhraseValue.setText("🔐 " + savedPhrase);

        layoutPhraseSection.setVisibility(View.VISIBLE);
        layoutLoginSection.setVisibility(View.GONE);

        if (getIntent().getBooleanExtra("logout_success", false)) {
            CustomToast.show(this, "Logout successful");
        }

        btnPhraseYes.setOnClickListener(v -> {
            layoutPhraseSection.setVisibility(View.GONE);
            layoutLoginSection.setVisibility(View.VISIBLE);
            tvPinError.setVisibility(View.GONE);
            CustomToast.show(this, "Phrase confirmed");
        });

        btnPhraseNo.setOnClickListener(v -> {
            DialogHelper.showSingleButtonDialog(
                    this,
                    DialogHelper.TYPE_WARNING,
                    "Security Warning",
                    "This login phrase does not look correct.\n\nPlease stop and verify the account before continuing.",
                    "OK",
                    null
            );
        });

        btnLogin.setOnClickListener(v -> login());
        btnForgotPin.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPinActivity.class))
        );

        etPin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    tvPinError.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void login() {
        String enteredPin = etPin.getText().toString().trim();

        tvPinError.setVisibility(View.GONE);

        if (enteredPin.isEmpty()) {
            tvPinError.setText("* PIN is required");
            tvPinError.setVisibility(View.VISIBLE);
            CustomToast.show(this, "Please enter your PIN");
            return;
        }

        if (enteredPin.length() != 4) {
            tvPinError.setText("* PIN must be 4 digits");
            tvPinError.setVisibility(View.VISIBLE);
            CustomToast.show(this, "PIN must be 4 digits");
            return;
        }

        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String savedPin = prefs.getString("pin", "");

        if (enteredPin.equals(savedPin)) {
            failedAttempts = 0;
            tvPinError.setVisibility(View.GONE);
            CustomToast.show(this, "Login successful. Welcome back!");

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            failedAttempts++;
            etPin.setText("");
            tvPinError.setText("* Incorrect PIN");
            tvPinError.setVisibility(View.VISIBLE);

            if (failedAttempts == 1) {
                CustomToast.show(this, "Incorrect PIN");
            } else if (failedAttempts == 2) {
                CustomToast.show(this, "Incorrect PIN. Please try again");
            } else {
                DialogHelper.showTwoButtonDialog(
                        this,
                        DialogHelper.TYPE_ERROR,
                        "Security Alert",
                        "Multiple incorrect PIN attempts detected.\n\nWould you like to recover your PIN using your security questions?",
                        "Yes",
                        "No",
                        () -> startActivity(new Intent(this, ForgotPinActivity.class)),
                        null
                );
                failedAttempts = 0;
                tvPinError.setVisibility(View.GONE);
            }
        }
    }
}