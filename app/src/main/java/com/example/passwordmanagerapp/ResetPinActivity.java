package com.example.passwordmanagerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ResetPinActivity extends AppCompatActivity {

    private EditText etNewPin, etConfirmPin;
    private TextView tvNewPinError, tvConfirmPinError;
    private Button btnReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_pin);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        etNewPin = findViewById(R.id.etNewPin);
        etConfirmPin = findViewById(R.id.etConfirmPin);
        tvNewPinError = findViewById(R.id.tvNewPinError);
        tvConfirmPinError = findViewById(R.id.tvConfirmPinError);
        btnReset = findViewById(R.id.btnReset);

        btnReset.setOnClickListener(v -> resetPin());

        addClearErrorWatcher(etNewPin, tvNewPinError);
        addClearErrorWatcher(etConfirmPin, tvConfirmPinError);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void resetPin() {
        String newPin = etNewPin.getText().toString().trim();
        String confirmPin = etConfirmPin.getText().toString().trim();

        boolean hasError = false;

        tvNewPinError.setVisibility(View.GONE);
        tvConfirmPinError.setVisibility(View.GONE);

        if (newPin.isEmpty()) {
            tvNewPinError.setText("* New PIN is required");
            tvNewPinError.setVisibility(View.VISIBLE);
            hasError = true;
        }

        if (confirmPin.isEmpty()) {
            tvConfirmPinError.setText("* Confirm PIN is required");
            tvConfirmPinError.setVisibility(View.VISIBLE);
            hasError = true;
        }

        if (hasError) {
            CustomToast.show(this, "Please fill all fields");
            return;
        }

        if (newPin.length() != 4) {
            tvNewPinError.setText("* PIN must be 4 digits");
            tvNewPinError.setVisibility(View.VISIBLE);
            CustomToast.show(this, "PIN must be 4 digits");
            return;
        }

        if (confirmPin.length() != 4) {
            tvConfirmPinError.setText("* Confirm PIN must be 4 digits");
            tvConfirmPinError.setVisibility(View.VISIBLE);
            CustomToast.show(this, "PIN must be 4 digits");
            return;
        }

        if (!newPin.equals(confirmPin)) {
            tvConfirmPinError.setText("* PIN and Confirm PIN do not match");
            tvConfirmPinError.setVisibility(View.VISIBLE);
            CustomToast.show(this, "PIN and Confirm PIN do not match");
            return;
        }

        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String oldPin = prefs.getString("pin", "");

        if (newPin.equals(oldPin)) {
            tvNewPinError.setText("* New PIN cannot be same as old PIN");
            tvNewPinError.setVisibility(View.VISIBLE);
            CustomToast.show(this, "New PIN cannot be same as old PIN");
            return;
        }

        prefs.edit().putString("pin", newPin).apply();

        CustomToast.show(this, "PIN reset successful");
        startActivity(new Intent(this, LoginActivity.class));
        finish();
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