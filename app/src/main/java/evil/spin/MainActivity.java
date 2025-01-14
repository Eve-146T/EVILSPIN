package evil.spin;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private WheelView wheelView;
    private TextView titleBar;
    private Button spinButton;
    private Button editButton;
    private SharedPreferences sharedPreferences;
    private WheelData currentWheel;
    private boolean wheelIsSpinning = false;
    private long wheelId;
    private ValueAnimator currentAnimator;
    private boolean isActivityActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        wheelId = getIntent().getLongExtra("wheel_id", -1);

        initializeViews();
        loadWheel();
        if (wheelView != null && spinButton != null) {
            setupClickListeners();
            RainbowBorderButtonDrawable rainbowDrawable = new RainbowBorderButtonDrawable(this);
            spinButton.setBackground(rainbowDrawable);
        }
        checkAnimationsEnabled();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActivityActive = true;
        loadWheel();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityActive = false;
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentAnimator != null) {
            currentAnimator.cancel();
            currentAnimator = null;
        }
    }

    private void initializeViews() {
        wheelView = findViewById(R.id.wheelView);
        titleBar = findViewById(R.id.titlebar);
        spinButton = findViewById(R.id.spinButton);
        editButton = findViewById(R.id.settingsButton); // Using the settings button as edit
    }

    private List<WheelData> loadWheels() {
        String wheelsJson = sharedPreferences.getString("saved_wheels", "[]");
        Type wheelListType = new TypeToken<ArrayList<WheelData>>(){}.getType();
        List<WheelData> wheels = new Gson().fromJson(wheelsJson, wheelListType);
        return wheels != null ? wheels : new ArrayList<>();
    }

    private void loadWheel() {
        if (wheelId == -1) {
            finish();
            return;
        }

        List<WheelData> wheels = loadWheels();
        for (WheelData wheel : wheels) {
            if (wheel.getId() == wheelId) {
                currentWheel = wheel;
                break;
            }
        }

        if (currentWheel == null) {
            finish();
            return;
        }

        updateWheelDisplay();
    }

    private void updateWheelDisplay() {
        if (titleBar != null) {
            titleBar.setText(currentWheel.getTitle());
        }

        if (wheelView != null) {
            List<String> options = new ArrayList<>();
            for (WheelData.SliceData slice : currentWheel.getSlices()) {
                for (int i = 0; i < slice.getWeight(); i++) {
                    options.add(slice.getName());
                }
            }
            wheelView.setOptions(options);
            wheelView.setColorPalette(currentWheel.getColorPalette());
        }

        setBackgroundByName(currentWheel.getBackground());
    }

    private void setupClickListeners() {
        spinButton.setOnClickListener(v -> spinWheel());
        if (editButton != null) {
            editButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, WheelEditorActivity.class);
                intent.putExtra("wheel_id", wheelId);
                startActivityForResult(intent, 1);
            });
        }
    }


    private void spinWheel() {
        if (wheelIsSpinning || currentWheel.getSlices().isEmpty()) {
            return;
        }

        wheelIsSpinning = true;
        Random random = new Random();
        float targetRotation = wheelView.getRotation() +
                random.nextInt(1080 - 720) + 3600;

        if (currentAnimator != null) {
            currentAnimator.cancel();
        }

        currentAnimator = ValueAnimator.ofFloat(
                wheelView.getRotation(),
                targetRotation
        );
        currentAnimator.setDuration(5000);
        currentAnimator.setInterpolator(input -> {
            // Custom easing: fast start, slow end
            return (float) (1 - Math.pow(1 - input, 3));
        });

        currentAnimator.addUpdateListener(animation -> {
            if (wheelView != null) {
                float value = (Float) animation.getAnimatedValue();
                wheelView.setRotation(value);
            }
        });

        currentAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                wheelIsSpinning = false;
                if (isActivityActive && wheelView != null) {
                    showResult(wheelView.getSelectedOption());
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                wheelIsSpinning = false;
            }
        });

        currentAnimator.start();
    }

    private void showResult(String winner) {
        if (!isActivityActive) return;

        try {
            new AlertDialog.Builder(this)
                    .setTitle("Result")
                    .setMessage("The wheel landed on: " + winner)
                    .setPositiveButton("OK", null)
                    .show();
        } catch (Exception e) {
            // Handle any window attachment errors silently
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            loadWheel(); // Reload wheel data after editing
        }
    }



    private void checkAnimationsEnabled() {
        boolean animationsEnabled = Settings.Global.getFloat(
                getContentResolver(),
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1
        ) != 0;

        if (!animationsEnabled) {
            new AlertDialog.Builder(this)
                    .setTitle("Animation Disabled")
                    .setMessage("Please enable animations in device settings for the wheel to animate properly.")
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    private void setBackgroundByName(String backgroundName) {
        try {
            String resourceName = "screen_" + backgroundName.toLowerCase();
            int resourceId = getResources().getIdentifier(
                    resourceName,
                    "drawable",
                    getPackageName()
            );
            if (resourceId != 0) {
                findViewById(R.id.activity_main)
                        .setBackgroundResource(resourceId);
            } else {
                findViewById(R.id.activity_main)
                        .setBackgroundResource(R.drawable.screen_red);
            }
        } catch (Exception e) {
            findViewById(R.id.activity_main)
                    .setBackgroundResource(R.drawable.screen_red);
        }
    }



    @Override
    public void onBackPressed() {
        // Return to welcome screen instead of exiting
        super.onBackPressed();
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}