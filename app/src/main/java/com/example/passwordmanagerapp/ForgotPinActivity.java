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

public class ForgotPinActivity extends AppCompatActivity {

    private TextView tvQ1, tvQ2, tvQ3;
    private TextView tvA1Error, tvA2Error, tvA3Error;
    private EditText etA1, etA2, etA3;
    private Button btnVerify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pin);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tvQ1 = findViewById(R.id.tvQ1);
        tvQ2 = findViewById(R.id.tvQ2);
        tvQ3 = findViewById(R.id.tvQ3);

        tvA1Error = findViewById(R.id.tvA1Error);
        tvA2Error = findViewById(R.id.tvA2Error);
        tvA3Error = findViewById(R.id.tvA3Error);

        etA1 = findViewById(R.id.etA1);
        etA2 = findViewById(R.id.etA2);
        etA3 = findViewById(R.id.etA3);

        btnVerify = findViewById(R.id.btnVerify);

        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);

        tvQ1.setText(prefs.getString("q1", ""));
        tvQ2.setText(prefs.getString("q2", ""));
        tvQ3.setText(prefs.getString("q3", ""));

        btnVerify.setOnClickListener(v -> verifyAnswers());

        addClearErrorWatcher(etA1, tvA1Error);
        addClearErrorWatcher(etA2, tvA2Error);
        addClearErrorWatcher(etA3, tvA3Error);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void verifyAnswers() {
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);

        String savedA1 = prefs.getString("a1", "");
        String savedA2 = prefs.getString("a2", "");
        String savedA3 = prefs.getString("a3", "");

        String a1 = etA1.getText().toString().trim().toLowerCase();
        String a2 = etA2.getText().toString().trim().toLowerCase();
        String a3 = etA3.getText().toString().trim().toLowerCase();

        boolean hasError = false;

        tvA1Error.setVisibility(View.GONE);
        tvA2Error.setVisibility(View.GONE);
        tvA3Error.setVisibility(View.GONE);

        if (a1.isEmpty()) {
            tvA1Error.setText("* Answer 1 is required");
            tvA1Error.setVisibility(View.VISIBLE);
            hasError = true;
        }

        if (a2.isEmpty()) {
            tvA2Error.setText("* Answer 2 is required");
            tvA2Error.setVisibility(View.VISIBLE);
            hasError = true;
        }

        if (a3.isEmpty()) {
            tvA3Error.setText("* Answer 3 is required");
            tvA3Error.setVisibility(View.VISIBLE);
            hasError = true;
        }

        if (hasError) {
            CustomToast.show(this, "Please complete all fields");
            return;
        }

        if (a1.equals(savedA1) && a2.equals(savedA2) && a3.equals(savedA3)) {
            startActivity(new Intent(this, ResetPinActivity.class));
            finish();
        } else {
            CustomToast.show(this, "Incorrect security answers");
        }
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