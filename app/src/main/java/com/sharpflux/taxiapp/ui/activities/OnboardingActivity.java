package com.sharpflux.taxiapp.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.sharpflux.taxiapp.R;
import com.sharpflux.taxiapp.ui.adapter.OnboardingAdapter;
import com.sharpflux.taxiapp.viewmodel.OnboardingItem;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private OnboardingAdapter adapter;
    private Button btnNext;
    private TextView btnSkip;
    private LinearLayout dotsIndicator;
    private List<OnboardingItem> onboardingItems;

    private static final String PREFS_NAME = "OnboardingPrefs";
    private static final String KEY_ONBOARDING_COMPLETED = "onboarding_completed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_onboarding);

        //notification bar
        Window window = getWindow();
        if ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES) {
            // Dark mode
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.black));
            window.getDecorView().setSystemUiVisibility(0); // remove light icons flag
        } else {
            // Light mode
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        //layout
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            getWindow().getDecorView().setOnApplyWindowInsetsListener((v, insets) -> {
                int topInset = insets.getInsets(WindowInsets.Type.statusBars()).top;
                v.setPadding(0, topInset, 0, 0);
                return insets;
            });
        } else {
            getWindow().getDecorView().setOnApplyWindowInsetsListener((v, insets) -> {
                int topInset = insets.getSystemWindowInsetTop();
                v.setPadding(0, topInset, 0, 0);
                return insets.consumeSystemWindowInsets();
            });
        }

        initViews();
        setupOnboardingItems();
        setupViewPager();
        setupClickListeners();
        setupDotsIndicator();
    }

    private void initViews() {
        viewPager = findViewById(R.id.viewPager);
        btnNext = findViewById(R.id.btnNext);
        btnSkip = findViewById(R.id.btnSkip);
        dotsIndicator = findViewById(R.id.dotsIndicator);
    }

    private void setupOnboardingItems() {
        onboardingItems = new ArrayList<>();
        onboardingItems.add(new OnboardingItem(
                R.drawable.onboarding1,
                "Welcome to Logo Mobility",
                "Your reliable ride-sharing companion for safe and convenient transportation"
        ));
        onboardingItems.add(new OnboardingItem(
                R.drawable.onboarding2,
                "Book a Ride",
                "Easily book a ride in just a few taps. Quick, simple, and hassle-free"
        ));
        onboardingItems.add(new OnboardingItem(
                R.drawable.onboarding3,
                "Track Your Journey",
                "Real-time tracking keeps you informed about your ride's location and arrival time"
        ));
    }

    private void setupViewPager() {
        adapter = new OnboardingAdapter(onboardingItems);
        viewPager.setAdapter(adapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateDotsIndicator(position);
                updateButtonText(position);
            }
        });
    }

    private void setupClickListeners() {
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentItem = viewPager.getCurrentItem();
                if (currentItem < onboardingItems.size() - 1) {
                    viewPager.setCurrentItem(currentItem + 1, true);
                } else {
                    completeOnboarding();
                }
            }
        });

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                completeOnboarding();
            }
        });
    }

    private void setupDotsIndicator() {
        dotsIndicator.removeAllViews();

        for (int i = 0; i < onboardingItems.size(); i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    getResources().getDimensionPixelSize(R.dimen.dot_size),
                    getResources().getDimensionPixelSize(R.dimen.dot_size)
            );
            params.setMargins(8, 0, 8, 0);
            dot.setLayoutParams(params);

            if (i == 0) {
                dot.setBackground(ContextCompat.getDrawable(this, R.drawable.dot_active));
            } else {
                dot.setBackground(ContextCompat.getDrawable(this, R.drawable.dot_inactive));
            }

            dotsIndicator.addView(dot);
        }
    }

    private void updateDotsIndicator(int position) {
        for (int i = 0; i < dotsIndicator.getChildCount(); i++) {
            View dot = dotsIndicator.getChildAt(i);
            if (i == position) {
                dot.setBackground(ContextCompat.getDrawable(this, R.drawable.dot_active));
            } else {
                dot.setBackground(ContextCompat.getDrawable(this, R.drawable.dot_inactive));
            }
        }
    }

    private void updateButtonText(int position) {
        if (position == onboardingItems.size() - 1) {
            btnNext.setText("Get Started");
            btnSkip.setVisibility(View.INVISIBLE);
        } else {
            btnNext.setText("Next");
            btnSkip.setVisibility(View.VISIBLE);
        }
    }

    private void completeOnboarding() {
        // Mark onboarding as completed
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, true).apply();

        navigateToLogin();
    }

    private void navigateToLogin() {
        startActivity(new Intent(OnboardingActivity.this, PhoneLoginActivity.class));
        finish();
        // Add smooth transition animation
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private boolean isOnboardingCompleted() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false);
    }

    @Override
    public void onBackPressed() {
        int currentItem = viewPager.getCurrentItem();
        if (currentItem > 0) {
            viewPager.setCurrentItem(currentItem - 1, true);
        } else {
            super.onBackPressed();
        }
    }
}