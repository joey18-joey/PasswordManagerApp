package com.example.passwordmanagerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SetupActivity extends AppCompatActivity {

    private EditText etPin, etLoginPhrase, etAnswer1, etAnswer2, etAnswer3;
    private Spinner spQuestion1, spQuestion2, spQuestion3;
    private Button btnSave;

    private TextView tvPinError, tvLoginPhraseError, tvAnswer1Error, tvAnswer2Error, tvAnswer3Error;

    private final String[] questions = {
            "What is your pet name?",
            "What is your favourite food?",
            "What is your mother's name?",
            "What is your father's name?",
            "What is your favourite color?",
            "What city were you born in?",
            "What is your best friend's name?"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        etPin = findViewById(R.id.etPin);
        etLoginPhrase = findViewById(R.id.etLoginPhrase);
        etAnswer1 = findViewById(R.id.etAnswer1);
        etAnswer2 = findViewById(R.id.etAnswer2);
        etAnswer3 = findViewById(R.id.etAnswer3);

        spQuestion1 = findViewById(R.id.spQuestion1);
        spQuestion2 = findViewById(R.id.spQuestion2);
        spQuestion3 = findViewById(R.id.spQuestion3);

        btnSave = findViewById(R.id.btnSave);

        tvPinError = findViewById(R.id.tvPinError);
        tvLoginPhraseError = findViewById(R.id.tvLoginPhraseError);
        tvAnswer1Error = findViewById(R.id.tvAnswer1Error);
        tvAnswer2Error = findViewById(R.id.tvAnswer2Error);
        tvAnswer3Error = findViewById(R.id.tvAnswer3Error);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                questions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spQuestion1.setAdapter(adapter);
        spQuestion2.setAdapter(adapter);
        spQuestion3.setAdapter(adapter);

        btnSave.setOnClickListener(v -> saveSetup());

        addClearErrorWatcher(etPin, tvPinError);
        addClearErrorWatcher(etLoginPhrase, tvLoginPhraseError);
        addClearErrorWatcher(etAnswer1, tvAnswer1Error);
        addClearErrorWatcher(etAnswer2, tvAnswer2Error);
        addClearErrorWatcher(etAnswer3, tvAnswer3Error);
    }

    private void saveSetup() {
        String pin = etPin.getText().toString().trim();
        String loginPhrase = etLoginPhrase.getText().toString().trim();

        String q1 = spQuestion1.getSelectedItem().toString();
        String q2 = spQuestion2.getSelectedItem().toString();
        String q3 = spQuestion3.getSelectedItem().toString();

        String a1 = etAnswer1.getText().toString().trim();
        String a2 = etAnswer2.getText().toString().trim();
        String a3 = etAnswer3.getText().toString().trim();

        boolean hasError = false;

        tvPinError.setVisibility(View.GONE);
        tvLoginPhraseError.setVisibility(View.GONE);
        tvAnswer1Error.setVisibility(View.GONE);
        tvAnswer2Error.setVisibility(View.GONE);
        tvAnswer3Error.setVisibility(View.GONE);

        if (pin.isEmpty()) {
            tvPinError.setText("* PIN is required");
            tvPinError.setVisibility(View.VISIBLE);
            hasError = true;
        } else if (pin.length() != 4) {
            tvPinError.setText("* PIN must be 4 digits");
            tvPinError.setVisibility(View.VISIBLE);
            hasError = true;
        }

        if (loginPhrase.isEmpty()) {
            tvLoginPhraseError.setText("* Login phrase is required");
            tvLoginPhraseError.setVisibility(View.VISIBLE);
            hasError = true;
        }

        if (a1.isEmpty()) {
            tvAnswer1Error.setText("* Answer 1 is required");
            tvAnswer1Error.setVisibility(View.VISIBLE);
            hasError = true;
        }

        if (a2.isEmpty()) {
            tvAnswer2Error.setText("* Answer 2 is required");
            tvAnswer2Error.setVisibility(View.VISIBLE);
            hasError = true;
        }

        if (a3.isEmpty()) {
            tvAnswer3Error.setText("* Answer 3 is required");
            tvAnswer3Error.setVisibility(View.VISIBLE);
            hasError = true;
        }

        if (hasError) {
            CustomToast.show(this, "Please complete all fields");
            return;
        }

        if (q1.equals(q2) || q1.equals(q3) || q2.equals(q3)) {
            CustomToast.show(this, "Choose 3 different questions");
            return;
        }

        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("pin", pin);
        editor.putString("login_phrase", loginPhrase);

        editor.putString("q1", q1);
        editor.putString("q2", q2);
        editor.putString("q3", q3);

        editor.putString("a1", a1.toLowerCase());
        editor.putString("a2", a2.toLowerCase());
        editor.putString("a3", a3.toLowerCase());

        editor.apply();

        CustomToast.show(this, "Setup completed successfully");
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