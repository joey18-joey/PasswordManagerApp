package com.example.passwordmanagerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private PasswordDatabase database;
    private PasswordAdapter adapter;

    private FrameLayout mainRoot;
    private RecyclerView rvPasswords;
    private FloatingActionButton fabAdd;

    private View securityCard;
    private View searchContainer;
    private View chipsScroll;

    private TextView tvEmpty;
    private TextView tvGuideHelp;
    private EditText etSearch;
    private TextView tvTotal, tvStrong, tvMedium, tvWeak, tvSecuritySuggestion;
    private TextView tvSortButton, tvSortLabel;

    private Chip chipAll, chipSocial, chipShopping, chipFinance, chipStudy, chipEmail, chipGeneral;

    private int totalPasswordCount = 0;
    private String currentCategory = "All";
    private String currentSort = "Newest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        database = PasswordDatabase.getInstance(this);

        mainRoot = findViewById(R.id.mainRoot);
        rvPasswords = findViewById(R.id.rvPasswords);
        fabAdd = findViewById(R.id.fabAdd);

        securityCard = findViewById(R.id.securityCard);
        searchContainer = findViewById(R.id.searchContainer);
        chipsScroll = findViewById(R.id.chipsScroll);

        tvEmpty = findViewById(R.id.tvEmpty);
        tvGuideHelp = findViewById(R.id.tvGuideHelp);
        etSearch = findViewById(R.id.etSearch);

        tvTotal = findViewById(R.id.tvTotal);
        tvStrong = findViewById(R.id.tvStrong);
        tvMedium = findViewById(R.id.tvMedium);
        tvWeak = findViewById(R.id.tvWeak);
        tvSecuritySuggestion = findViewById(R.id.tvSecuritySuggestion);

        tvSortButton = findViewById(R.id.tvSortButton);
        tvSortLabel = findViewById(R.id.tvSortLabel);

        chipAll = findViewById(R.id.chipAll);
        chipSocial = findViewById(R.id.chipSocial);
        chipShopping = findViewById(R.id.chipShopping);
        chipFinance = findViewById(R.id.chipFinance);
        chipStudy = findViewById(R.id.chipStudy);
        chipEmail = findViewById(R.id.chipEmail);
        chipGeneral = findViewById(R.id.chipGeneral);

        adapter = new PasswordAdapter();
        rvPasswords.setLayoutManager(new LinearLayoutManager(this));
        rvPasswords.setAdapter(adapter);

        preloadIfEmpty();

        adapter.setOnItemClickListener(p -> {
            Intent i = new Intent(MainActivity.this, PasswordDetailActivity.class);
            i.putExtra("id", p.getId());
            i.putExtra("site", p.getSiteName());
            i.putExtra("username", p.getUsername());
            i.putExtra("password", p.getPassword());
            i.putExtra("extraNote", p.getExtraNote());
            startActivity(i);
        });

        fabAdd.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, AddEditActivity.class);
            startActivity(i);
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        tvSortButton.setOnClickListener(v -> showSortDialog());
        tvSortLabel.setOnClickListener(v -> showSortDialog());

        tvGuideHelp.setOnClickListener(v -> showGuideOverlay(false));

        setupCategoryChips();
        setupSwipeToDelete();

        updateSortLabel();
        loadData();

        mainRoot.post(this::showGuideIfNeeded);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_logout) {
            DialogHelper.showTwoButtonDialog(
                    MainActivity.this,
                    DialogHelper.TYPE_WARNING,
                    "Logout",
                    "Are you sure you want to logout from your account?",
                    "Logout",
                    "Cancel",
                    () -> {
                        Intent i = new Intent(MainActivity.this, LoginActivity.class);
                        i.putExtra("logout_success", true);
                        startActivity(i);
                        finish();
                    },
                    null
            );
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSortDialog() {
        String[] sortOptions = {
                "Newest",
                "Oldest",
                "A → Z",
                "Z → A",
                "Weak → Strong",
                "Strong → Weak"
        };

        int checkedItem = 0;
        if (currentSort.equals("Newest")) checkedItem = 0;
        else if (currentSort.equals("Oldest")) checkedItem = 1;
        else if (currentSort.equals("A → Z")) checkedItem = 2;
        else if (currentSort.equals("Z → A")) checkedItem = 3;
        else if (currentSort.equals("Weak → Strong")) checkedItem = 4;
        else if (currentSort.equals("Strong → Weak")) checkedItem = 5;

        new AlertDialog.Builder(this)
                .setTitle("Sort Passwords")
                .setSingleChoiceItems(sortOptions, checkedItem, (dialog, which) -> {
                    currentSort = sortOptions[which];
                    updateSortLabel();
                    applyFilters();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateSortLabel() {
        tvSortLabel.setText("Sort: " + currentSort);
    }

    private void setupCategoryChips() {
        chipAll.setOnClickListener(v -> {
            currentCategory = "All";
            updateChipSelection();
            applyFilters();
        });

        chipGeneral.setOnClickListener(v -> {
            currentCategory = "General";
            updateChipSelection();
            applyFilters();
        });

        chipSocial.setOnClickListener(v -> {
            currentCategory = "Social";
            updateChipSelection();
            applyFilters();
        });

        chipShopping.setOnClickListener(v -> {
            currentCategory = "Shopping";
            updateChipSelection();
            applyFilters();
        });

        chipFinance.setOnClickListener(v -> {
            currentCategory = "Finance";
            updateChipSelection();
            applyFilters();
        });

        chipStudy.setOnClickListener(v -> {
            currentCategory = "Study";
            updateChipSelection();
            applyFilters();
        });

        chipEmail.setOnClickListener(v -> {
            currentCategory = "Email";
            updateChipSelection();
            applyFilters();
        });

        updateChipSelection();
    }

    private void updateChipSelection() {
        chipAll.setChecked(currentCategory.equals("All"));
        chipGeneral.setChecked(currentCategory.equals("General"));
        chipSocial.setChecked(currentCategory.equals("Social"));
        chipShopping.setChecked(currentCategory.equals("Shopping"));
        chipFinance.setChecked(currentCategory.equals("Finance"));
        chipStudy.setChecked(currentCategory.equals("Study"));
        chipEmail.setChecked(currentCategory.equals("Email"));
    }

    private void loadData() {
        List<Password> list = database.passwordDao().getAllPasswordsNewest();
        totalPasswordCount = (list == null) ? 0 : list.size();

        adapter.setData(list);

        int strong = 0;
        int medium = 0;
        int weak = 0;

        if (list != null) {
            for (Password p : list) {
                String pwd = EncryptionHelper.decrypt(p.getPassword());
                int strengthType = getPasswordStrengthType(pwd);

                if (strengthType == 2) strong++;
                else if (strengthType == 1) medium++;
                else weak++;
            }
        }

        tvTotal.setText("Total: " + totalPasswordCount);
        tvStrong.setText("Strong: " + strong);
        tvMedium.setText("Medium: " + medium);
        tvWeak.setText("Weak: " + weak);

        if (weak > 0) {
            tvSecuritySuggestion.setText("Warning: " + weak + " weak password(s) need attention.");
            tvSecuritySuggestion.setTextColor(Color.parseColor("#C62828"));
        } else if (medium > 0) {
            tvSecuritySuggestion.setText("Good: " + medium + " medium password(s) could be improved.");
            tvSecuritySuggestion.setTextColor(Color.parseColor("#EF6C00"));
        } else if (totalPasswordCount > 0) {
            tvSecuritySuggestion.setText("Excellent: all saved passwords look strong.");
            tvSecuritySuggestion.setTextColor(Color.parseColor("#2E7D32"));
        } else {
            tvSecuritySuggestion.setText("Your vault health summary will appear here.");
            tvSecuritySuggestion.setTextColor(Color.parseColor("#5E4A9E"));
        }

        applyFilters();
    }

    private void applyFilters() {
        String keyword = etSearch.getText().toString().trim().toLowerCase();
        List<Password> allPasswords = getSortedPasswords();

        adapter.clearData();

        if (allPasswords != null) {
            for (Password p : allPasswords) {
                String siteName = p.getSiteName() == null ? "" : p.getSiteName().toLowerCase();
                String userName = p.getUsername() == null ? "" : p.getUsername().toLowerCase();
                String extraNote = p.getExtraNote() == null ? "" : p.getExtraNote().toLowerCase();

                boolean matchesKeyword =
                        keyword.isEmpty()
                                || siteName.contains(keyword)
                                || userName.contains(keyword)
                                || extraNote.contains(keyword);

                boolean matchesCategory =
                        currentCategory.equals("All")
                                || getPasswordCategory(p).equals(currentCategory);

                if (matchesKeyword && matchesCategory) {
                    adapter.addItem(p);
                }
            }
        }

        updateEmptyState();
    }

    private List<Password> getSortedPasswords() {
        List<Password> baseList = database.passwordDao().getAllPasswordsNewest();
        if (baseList == null) return null;

        switch (currentSort) {
            case "Oldest":
                return database.passwordDao().getAllPasswordsOldest();

            case "A → Z":
                return database.passwordDao().getAllPasswordsAZ();

            case "Z → A":
                return database.passwordDao().getAllPasswordsZA();

            case "Weak → Strong": {
                List<Password> sorted = new ArrayList<>(baseList);
                Collections.sort(sorted, (p1, p2) -> {
                    int s1 = getPasswordStrengthType(EncryptionHelper.decrypt(p1.getPassword()));
                    int s2 = getPasswordStrengthType(EncryptionHelper.decrypt(p2.getPassword()));
                    if (s1 != s2) return Integer.compare(s1, s2);
                    return Long.compare(p2.getUpdatedAt(), p1.getUpdatedAt());
                });
                return sorted;
            }

            case "Strong → Weak": {
                List<Password> sorted = new ArrayList<>(baseList);
                Collections.sort(sorted, (p1, p2) -> {
                    int s1 = getPasswordStrengthType(EncryptionHelper.decrypt(p1.getPassword()));
                    int s2 = getPasswordStrengthType(EncryptionHelper.decrypt(p2.getPassword()));
                    if (s1 != s2) return Integer.compare(s2, s1);
                    return Long.compare(p2.getUpdatedAt(), p1.getUpdatedAt());
                });
                return sorted;
            }

            case "Newest":
            default:
                return database.passwordDao().getAllPasswordsNewest();
        }
    }

    private void updateEmptyState() {
        String keyword = etSearch.getText().toString().trim();

        if (adapter.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);

            if (totalPasswordCount == 0) {
                tvEmpty.setText("🔐 No passwords saved yet\n\nTap + to add your first password");
            } else if (!keyword.isEmpty()) {
                tvEmpty.setText("No matching results found\n\nTry a different keyword");
            } else if (!currentCategory.equals("All")) {
                tvEmpty.setText("No passwords in the " + currentCategory + " category");
            } else {
                tvEmpty.setText("No passwords to display");
            }
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
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

    private String getPasswordCategory(Password password) {
        if (password == null) return "General";

        String savedCategory = password.getCategory();
        if (savedCategory != null && !savedCategory.trim().isEmpty()) {
            return savedCategory;
        }

        return getCategoryForSite(password.getSiteName());
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

    private void preloadIfEmpty() {
        List<Password> list = database.passwordDao().getAllPasswordsNewest();

        if (list == null || list.isEmpty()) {
            database.passwordDao().insert(new Password("Google", "Jojo.student@gmail.com", EncryptionHelper.encrypt("G00gle!Pass"), "Personal email", "Email"));
            database.passwordDao().insert(new Password("Instagram", "Ali_photo", EncryptionHelper.encrypt("Insta@2026"), "Social media", "Social"));
            database.passwordDao().insert(new Password("Shopee", "Channel_shop", EncryptionHelper.encrypt("Shop!2026"), "Shopping account", "Shopping"));
            database.passwordDao().insert(new Password("TikTok", "Cindy.tiktok", EncryptionHelper.encrypt("aa111"), "Entertainment", "Social"));
            database.passwordDao().insert(new Password("Public Bank", "Joey.bank", EncryptionHelper.encrypt("Bank@Secure1"), "ATM PIN: 258099", "Finance"));
            database.passwordDao().insert(new Password("UTAR Portal", "21ACB12345", EncryptionHelper.encrypt("Utar@Login"), "Student portal", "Study"));
        }
    }

    private void showDeleteDialog(Password p) {
        DialogHelper.showTwoButtonDialog(
                MainActivity.this,
                DialogHelper.TYPE_WARNING,
                "Delete Password",
                "Are you sure you want to delete this record?\n\nThis action cannot be undone.",
                "Delete",
                "Cancel",
                () -> {
                    database.passwordDao().delete(p);
                    loadData();
                    CustomToast.show(MainActivity.this, "Password deleted");
                },
                this::loadData
        );
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            private final Paint bgPaint = new Paint();
            private final Paint textPaint = new Paint();

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                Password swipedPassword = adapter.getItemAtPosition(position);

                if (swipedPassword != null) {
                    showDeleteDialog(swipedPassword);
                } else {
                    loadData();
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c,
                                    @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX,
                                    float dY,
                                    int actionState,
                                    boolean isCurrentlyActive) {

                View itemView = viewHolder.itemView;

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX < 0) {
                    bgPaint.setColor(Color.parseColor("#D32F2F"));

                    float left = itemView.getRight() + dX;
                    float top = itemView.getTop();
                    float right = itemView.getRight();
                    float bottom = itemView.getBottom();

                    c.drawRoundRect(left, top, right, bottom, 24f, 24f, bgPaint);

                    textPaint.setColor(Color.WHITE);
                    textPaint.setTextSize(42f);
                    textPaint.setFakeBoldText(true);

                    String text = "Delete";
                    float textWidth = textPaint.measureText(text);
                    float textX = right - textWidth - 40f;
                    float textY = top + (bottom - top) / 2f + 15f;

                    c.drawText(text, textX, textY, textPaint);
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(rvPasswords);
    }

    private void showGuideIfNeeded() {
        SharedPreferences prefs = getSharedPreferences("main_page_guide", MODE_PRIVATE);
        boolean alreadyShown = prefs.getBoolean("shown", false);

        if (!alreadyShown) {
            showGuideOverlay(true);
        }
    }

    private void showGuideOverlay(boolean saveAsShownWhenFinished) {
        List<GuideOverlayView.Step> steps = new ArrayList<>();

        steps.add(new GuideOverlayView.Step(
                securityCard,
                "Security Overview",
                "Check how many passwords are total, strong, medium, or weak."
        ));

        steps.add(new GuideOverlayView.Step(
                searchContainer,
                "Search & Sort",
                "Search by site, username, or note. Tap the sort button to change order."
        ));

        steps.add(new GuideOverlayView.Step(
                chipsScroll,
                "Quick Filters",
                "Tap category chips like General, Social, Finance, or Email to filter faster."
        ));

        if (rvPasswords.getVisibility() == View.VISIBLE && adapter.getItemCount() > 0) {
            steps.add(new GuideOverlayView.Step(
                    rvPasswords,
                    "Password Cards",
                    "Tap a card to view details. Swipe left on any card to delete it."
            ));
        }

        steps.add(new GuideOverlayView.Step(
                fabAdd,
                "Add Password",
                "Tap the + button anytime to add a new password."
        ));

        GuideOverlayView overlayView = new GuideOverlayView(this, steps, () -> {
            if (saveAsShownWhenFinished) {
                SharedPreferences prefs = getSharedPreferences("main_page_guide", MODE_PRIVATE);
                prefs.edit().putBoolean("shown", true).apply();
            }
        });

        mainRoot.addView(overlayView);
    }
}