package evil.spin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SettingsActivity extends AppCompatActivity {
    private EditText titleEditText;
    private Spinner colorPaletteSpinner;
    private Spinner backgroundSpinner;
    private SeekBar wheelSpeedSeekBar;
    private SeekBar minWheelSpinSeekBar;
    private Button importButton;
    private Button exportButton;
    private Button saveButton;
    private Button cancelButton;
    private Button resetWheelButton;
    private Button defaultGermanNamesButton;

    private SharedPreferences sharedPreferences;
    private static final int READ_REQUEST_CODE = 42;
    private static final int WRITE_REQUEST_CODE = 43;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        titleEditText = findViewById(R.id.titleEditText);
        colorPaletteSpinner = findViewById(R.id.colorPaletteSpinner);
        backgroundSpinner = findViewById(R.id.backgroundSpinner);
        wheelSpeedSeekBar = findViewById(R.id.wheelSpeedSeekBar);
        minWheelSpinSeekBar = findViewById(R.id.minWheelSpinSeekBar);
        importButton = findViewById(R.id.importButton);
        exportButton = findViewById(R.id.exportButton);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
        resetWheelButton = findViewById(R.id.resetWheelButton);
        defaultGermanNamesButton = findViewById(R.id.defaultGermanNamesButton);

        setupColorPaletteSpinner();
        setupBackgroundSpinner();
        setupSeekBars();
        setupButtons();
        loadSettings();
    }

    private void setupColorPaletteSpinner() {
        List<String> colorPalettes = Arrays.asList("Default", "Pastel");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, colorPalettes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorPaletteSpinner.setAdapter(adapter);
    }
    private void setupBackgroundSpinner() {
        List<String> backgrounds = Arrays.asList("red", "orange", "yellow", "green", "lime", "turk", "blue", "indigo", "purple", "gray");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, backgrounds);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        backgroundSpinner.setAdapter(adapter);
    }
    private void setupSeekBars() {
        wheelSpeedSeekBar.setMax(5000); // 5 seconds max
        minWheelSpinSeekBar.setMax(3600); // 4 full rotations max
    }

    private void setupButtons() {
        importButton.setOnClickListener(v -> importOptions());
        exportButton.setOnClickListener(v -> exportOptions());
        saveButton.setOnClickListener(v -> saveSettings());
        cancelButton.setOnClickListener(v -> finish());
        resetWheelButton.setOnClickListener(v -> resetWheel());
        defaultGermanNamesButton.setOnClickListener(v -> setMIGUUU());
    }
    private void resetWheel() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("wheel_options", new HashSet<>());
        editor.apply();
        Toast.makeText(this, "Wheel reset to empty", Toast.LENGTH_SHORT).show();
    }

    private void setMIGUUU() {
        Set<String> defaultNames = new HashSet<>(Arrays.asList(
                "Teto", "Miku", "Gumi", "Rin", "Len", "Fukase"
        ));
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("wheel_options", defaultNames);
        editor.apply();
        Toast.makeText(this, "Example wheel has been set. Click save and exit.", Toast.LENGTH_SHORT).show();
    }

    private void loadSettings() {
        String title = sharedPreferences.getString("wheel_title", "Spin the Wheel");
        String colorPalette = sharedPreferences.getString("color_palette", "Default");
        String background = sharedPreferences.getString("background", "red");
        int wheelSpeed = sharedPreferences.getInt("wheel_speed", 3000);
        int minWheelSpin = sharedPreferences.getInt("min_wheel_spin", 720);

        titleEditText.setText(title);
        colorPaletteSpinner.setSelection(colorPalette.equals("Pastel") ? 1 : 0);
        backgroundSpinner.setSelection(((ArrayAdapter<String>)backgroundSpinner.getAdapter()).getPosition(background));
        wheelSpeedSeekBar.setProgress(wheelSpeed);
        minWheelSpinSeekBar.setProgress(minWheelSpin);
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("wheel_title", titleEditText.getText().toString());
        editor.putString("color_palette", colorPaletteSpinner.getSelectedItem().toString());
        editor.putString("background", backgroundSpinner.getSelectedItem().toString());
        editor.putInt("wheel_speed", wheelSpeedSeekBar.getProgress());
        editor.putInt("min_wheel_spin", minWheelSpinSeekBar.getProgress());
        editor.apply();

        setResult(RESULT_OK);
        finish();
    }

    private void importOptions() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    private void exportOptions() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, "wheel_options.txt");
        startActivityForResult(intent, WRITE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == READ_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            readTextFromUri(uri);
        } else if (requestCode == WRITE_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            writeTextToUri(uri);
        }
    }

    private void readTextFromUri(Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            List<String> options = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                options.add(line.trim());
            }
            saveOptionsToPreferences(options);
            Toast.makeText(this, "Options imported successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Error reading file", Toast.LENGTH_SHORT).show();
        }
    }

    private void writeTextToUri(Uri uri) {
        try (OutputStream outputStream = getContentResolver().openOutputStream(uri);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            List<String> options = getOptionsFromPreferences();
            for (String option : options) {
                writer.write(option);
                writer.newLine();
            }
            Toast.makeText(this, "Options exported successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Error writing file", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveOptionsToPreferences(List<String> options) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("wheel_options", new HashSet<>(options));
        editor.apply();
    }

    private List<String> getOptionsFromPreferences() {
        Set<String> savedOptions = sharedPreferences.getStringSet("wheel_options", new HashSet<>());
        return new ArrayList<>(savedOptions);
    }
}