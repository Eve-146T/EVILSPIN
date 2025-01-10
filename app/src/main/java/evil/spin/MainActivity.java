package evil.spin;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private WheelView wheelView;
    private Button spinButton;
    private EditText optionInput;
    private Button addOptionButton;
    private Button settingsButton;
    private Button saveButton;
    private Button addWheelButton;
    private Button deleteWheelButton;
    private EditText titleBar;
    private DrawerLayout mainLayout;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;
    private Menu wheelMenu;
    private List<String> options = new ArrayList<>();
    private Wheel CurrentWheel;
    private final IWheelSerializer wheelSerializer = new WheelSerializer();
    public WheelDB wheelDB = new WheelDB();
    private SharedPreferences sharedPreferences;
    private boolean wheelIsSpinning = false;

    private static final int SETTINGS_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        wheelView = findViewById(R.id.wheelView);
        spinButton = findViewById(R.id.spinButton);
        optionInput = findViewById(R.id.optionInput);
        addOptionButton = findViewById(R.id.addOptionButton);
        settingsButton = findViewById(R.id.settingsButton);
        titleBar = findViewById(R.id.titlebar);
        mainLayout = findViewById(R.id.activity_main);
        setUpSettingsButtons();
        setUpSideMenu();

        try {
            loadWheels();
        } catch (JSONException e) {
            Toast.makeText(mainLayout.getContext(),"Wheels settings malformed, making hard-reset",Toast.LENGTH_SHORT).show();
            try {
                wheelSerializer.SaveWheelsToSharedPreferences(wheelDB.getWheels(),sharedPreferences);
                loadWheels();
            } catch (JSONException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        }
        prepareWheelMenu();

        LoadCurrentWheel();

        addOptionButton.setOnClickListener(v -> addOption());
        spinButton.setOnClickListener(v -> spinWheel());
        settingsButton.setOnClickListener(v -> openSettings());
        RainbowBorderButtonDrawable rainbowDrawable = new RainbowBorderButtonDrawable(this);
        spinButton.setBackground(rainbowDrawable);
        updateWheelAppearance();
        updateBackground();
        checkAnimationsEnabled();
    }

    private void LoadCurrentWheel() {
        if (wheelDB.IsEmpty())
            addWheel();
        else
            CurrentWheel = wheelDB.GetFirst();
        updateWheel(CurrentWheel);
    }

    private void setUpSettingsButtons() {
        saveButton = findViewById(R.id.btn_save);
        deleteWheelButton = findViewById(R.id.btn_delete);
        addWheelButton = findViewById(R.id.btn_add_wheel);

        saveButton.setOnClickListener(v-> {
            try {
                saveWheels();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
        deleteWheelButton.setOnClickListener(v->deleteWheel());
        addWheelButton.setOnClickListener(v->addWheel());
    }

    private void setUpSideMenu() {
        navigationView = findViewById(R.id.navigationView);

        wheelMenu = navigationView.getMenu();

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, mainLayout, R.string.nav_open, R.string.nav_close);
        mainLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // Enable the home button to show the drawer toggle
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        this.wheelMenu = menu;
        getMenuInflater().inflate(R.menu.wheel_menu, menu);
        return true;
    }
    private void prepareWheelMenu() {
        // Clean menu from previous wheels
        wheelMenu.clear();
        // Create a separate method for the menu item click listener
        MenuItem.OnMenuItemClickListener menuItemClicked = createMenuItemClickListener();
        List<Wheel> wheels=wheelDB.getWheels();
        for (Wheel wheel : wheels) {
            wheelMenu.add(Menu.NONE, wheel.Id,Menu.NONE,wheel.Id+": "+ wheel.Name).setOnMenuItemClickListener(menuItemClicked);
            addMenuSeparator(wheelMenu);
        }

        // Update navigation view
        navigationView.invalidate();
    }
    private MenuItem.OnMenuItemClickListener createMenuItemClickListener() {
        return menuItem -> {
            // Handle menu item click here
            String wheelName = (String) menuItem.getTitle();
            int wheelId = menuItem.getItemId();

            Toast.makeText(MainActivity.this, wheelName + " clicked", Toast.LENGTH_SHORT).show();

            try {
                CurrentWheel = wheelDB.getWheelById(wheelId);
                loadOptions(CurrentWheel);
                updateTitle(CurrentWheel);
                // Do something with the found wheel
                Toast.makeText(MainActivity.this, "Found wheel: " + CurrentWheel.Name, Toast.LENGTH_SHORT).show();

                drawerLayout.closeDrawers();
                return true;

            } catch (Exception e) {
                // Handle the exception
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                return false;
            }

        };
    }

    private void addMenuSeparator(Menu menu) {
        // Add a separator (divider) to the menu
        menu.add(Menu.NONE, Menu.NONE, Menu.NONE, "").setEnabled(false);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void checkAnimationsEnabled() {
        boolean animationsEnabled = Settings.Global.getFloat(getContentResolver(), Settings.Global.ANIMATOR_DURATION_SCALE, 1) != 0;
        if (!animationsEnabled) {
            new AlertDialog.Builder(this)
                    .setTitle("Animation Disabled")
                    .setMessage("Animations are currently turned off. Please enable them in the device settings for the wheel to animate.")
                    .setPositiveButton("OK", null)
                    .show();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        updateBackground();
    }

    private void updateTitle(Wheel wheel) {
        titleBar.setText(wheel.Name);
        titleBar.refreshDrawableState();
    }

    private void updateBackground() {
        String background = sharedPreferences.getString("background", "red");
        setBackgroundByName(background);
    }
    private void setBackgroundByName(String backgroundName) {
        try {
            String resourceName = "screen_" + backgroundName.toLowerCase();
            int resourceId = getResources().getIdentifier(resourceName, "drawable", getPackageName());
            if (resourceId != 0) {
                mainLayout.setBackgroundResource(resourceId);
            } else {
                // Fallback to default background if the resource is not found
                mainLayout.setBackgroundResource(R.drawable.screen_red);
            }
        } catch (Resources.NotFoundException e) {
            // Fallback to default background if there's an error
            mainLayout.setBackgroundResource(R.drawable.screen_red);
        }
    }

    private void addOption() {
        String option = optionInput.getText().toString().trim();
        if (!option.isEmpty()) {
            CurrentWheel.Options.add(option);
            wheelView.setOptions((List<String>) CurrentWheel.Options);
            optionInput.setText("");
        }
    }
    private void clearEditTextFocus() {
        optionInput.clearFocus();
        // This will hide the soft keyboard if it's visible
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(optionInput.getWindowToken(), 0);
    }
    private void spinWheel() {
        clearEditTextFocus();
        if (CurrentWheel.Options.isEmpty()) {
            Toast.makeText(this, "Please add options before spinning", Toast.LENGTH_SHORT).show();
            return;
        }
        if(wheelIsSpinning) return;
        
        int minSpin = sharedPreferences.getInt("min_wheel_spin", 720);
        int maxSpin = minSpin + 1080;
        Random random = new Random();
        float targetRotation = wheelView.getRotation() + random.nextInt(maxSpin - minSpin) + minSpin;

        int duration = sharedPreferences.getInt("wheel_speed", 5000); // Increased duration for a slower finish
        ValueAnimator animator = ValueAnimator.ofFloat(wheelView.getRotation(), targetRotation);
        animator.setDuration(duration);

        // Use a custom interpolator for fast start and gradual slowdown
        animator.setInterpolator(input -> {
            return (float) (1 - Math.pow(1 - input, 3)); // Ease-out cubic
        });

        animator.addUpdateListener(animation -> {
            float value = (Float) animation.getAnimatedValue();
            wheelView.setRotation(value);
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                showResult(wheelView.getSelectedOption());
                wheelIsSpinning = false;
            }
        });

        animator.start();
        wheelIsSpinning = true;
    }

    private void showResult(String winner) {
        new AlertDialog.Builder(this)
                .setTitle("Result")
                .setMessage("The wheel landed on: " + winner)
                .setPositiveButton("OK", null)
                .show();
    }

    private void openSettings() {
        clearEditTextFocus();
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, SETTINGS_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_REQUEST_CODE && resultCode == RESULT_OK) {
            updateWheelAppearance();
            loadOptions();
        }
    }

    private void updateWheelAppearance() {
        String colorPalette = sharedPreferences.getString("color_palette", "Default");
        wheelView.setColorPalette(colorPalette);
    }

    private void loadOptions() {
        Set<String> savedOptions = sharedPreferences.getStringSet("wheel_options", new HashSet<>());
        options = new ArrayList<>(savedOptions);
        wheelView.setOptions(options);
    }
    private void loadOptions(Wheel wheel)
    {
        wheelView.setOptions((List<String>) wheel.Options);
    }
    private void updateWheel(Wheel wheel)
    {
        loadOptions(wheel);
        updateTitle(wheel);
    }
    private void saveWheels() throws JSONException {
        // Update title
        CurrentWheel.Name=titleBar.getText().toString();
        titleBar.clearFocus();

        List<Wheel> wheels = wheelDB.getWheels();
        if(!wheels.contains(CurrentWheel))
            {
                wheelDB.AddWheel(CurrentWheel);
            }
        wheelSerializer.SaveWheelsToSharedPreferences(wheels, sharedPreferences);

        // Update menu view
        prepareWheelMenu();

        Toast.makeText(MainActivity.this,"Wheels saved",Toast.LENGTH_SHORT).show();
    }

    private void loadWheels() throws JSONException {
        List<Wheel> loadedWheels = (List<Wheel>) wheelSerializer.LoadWheelsFromSharedPreferences(sharedPreferences);
        wheelDB.setWheels(loadedWheels);
        List<Wheel> wheels=wheelDB.getWheels();
        if(wheels.isEmpty())
        {
            wheelDB.AddWheelWithNewId("Example", Arrays.asList("a","b","c"));
        }
        CurrentWheel=wheels.get(0);
        updateWheel(CurrentWheel);
    }

    private void addWheel() {
        CurrentWheel=new Wheel();
        updateWheel(CurrentWheel);
        AskForWheelName();
    }
    private void AskForWheelName(){
        // Create an EditText for the dialog input
        final EditText input = new EditText(MainActivity.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        // Create a dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Enter Wheel title");
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            dialog.dismiss();
            String title = input.getText().toString();
            handleTitleInput(title);  // Call a method to handle the input
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());

        // Show the dialog
        builder.show();
    }
    // Method to handle the name input
    private void handleTitleInput(String title) {
        // Example handling: Show a toast with the entered name
        Toast.makeText(this, "Title entered: " + title, Toast.LENGTH_SHORT).show();
        if(CurrentWheel == null)
            throw new RuntimeException("Current wheel is null, while setting title");
        CurrentWheel.Name = title;
        updateTitle(CurrentWheel);
    }

    private void deleteWheel() {

        wheelDB.RemoveWheel(CurrentWheel);

        try {
            wheelSerializer.SaveWheelsToSharedPreferences(wheelDB.getWheels(), sharedPreferences);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        // Add new wheel as current wheel. This is how you ensure wheel is always present
        addWheel();

        // Update menu view
        prepareWheelMenu();
    }
}