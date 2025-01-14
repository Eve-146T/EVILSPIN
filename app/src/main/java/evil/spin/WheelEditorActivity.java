package evil.spin;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class WheelEditorActivity extends AppCompatActivity {
    private EditText wheelTitleEdit;
    private Spinner colorPaletteSpinner;
    private Spinner backgroundSpinner;
    private RecyclerView slicesRecyclerView;
    private Button addSliceButton;
    private Button saveButton;
    private Button deleteButton;
    private WheelView previewWheel;

    private SliceAdapter sliceAdapter;
    private WheelData currentWheel;
    private SharedPreferences sharedPreferences;
    private long wheelId;

    // Hard-coded values
    private final String[] COLOR_PALETTES = {"Default", "Pastel"};
    private final String[] BACKGROUNDS = {
            "red", "orange", "yellow", "green", "lime",
            "turk", "blue", "indigo", "purple", "gray"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wheel_editor);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        wheelId = getIntent().getLongExtra("wheel_id", -1);

        initializeViews();
        setupSpinners();
        setupRecyclerView();
        loadWheel();
        setupClickListeners();
        updatePreview();
    }

    private void initializeViews() {
        wheelTitleEdit = findViewById(R.id.wheelTitleEdit);
        colorPaletteSpinner = findViewById(R.id.colorPaletteSpinner);
        backgroundSpinner = findViewById(R.id.backgroundSpinner);
        slicesRecyclerView = findViewById(R.id.slicesRecyclerView);
        addSliceButton = findViewById(R.id.addSliceButton);
        saveButton = findViewById(R.id.saveButton);
        deleteButton = findViewById(R.id.deleteButton);
        previewWheel = findViewById(R.id.previewWheel);
    }

    private void setupSpinners() {
        // Color palette spinner
        ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                COLOR_PALETTES
        );
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorPaletteSpinner.setAdapter(colorAdapter);
        colorPaletteSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                updatePreview();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Background spinner
        ArrayAdapter<String> bgAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                BACKGROUNDS
        );
        bgAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        backgroundSpinner.setAdapter(bgAdapter);
        backgroundSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String background = BACKGROUNDS[position];
                setBackgroundByName(background);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void updatePreview() {
        if (previewWheel != null && currentWheel != null) {
            List<String> options = new ArrayList<>();
            for (WheelData.SliceData slice : currentWheel.getSlices()) {
                for (int i = 0; i < slice.getWeight(); i++) {
                    options.add(slice.getName());
                }
            }
            previewWheel.setOptions(options);
            previewWheel.setColorPalette(colorPaletteSpinner.getSelectedItem().toString());
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
                findViewById(android.R.id.content).setBackgroundResource(resourceId);
            }
        } catch (Exception e) {
            findViewById(android.R.id.content).setBackgroundResource(R.drawable.screen_red);
        }
    }


    private void setupRecyclerView() {
        sliceAdapter = new SliceAdapter();
        slicesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        slicesRecyclerView.setAdapter(sliceAdapter);
    }

    private void loadWheel() {
        List<WheelData> wheels = loadWheels();
        for (WheelData wheel : wheels) {
            if (wheel.getId() == wheelId) {
                currentWheel = wheel;
                break;
            }
        }

        if (currentWheel == null) {
            currentWheel = new WheelData();
            currentWheel.setId(wheelId);
        }

        updateUI();
    }

    private void updateUI() {
        wheelTitleEdit.setText(currentWheel.getTitle());
        colorPaletteSpinner.setSelection(
                currentWheel.getColorPalette().equals("Pastel") ? 1 : 0
        );
        int bgPosition = 0;
        String[] backgrounds = getResources().getStringArray(R.array.backgrounds);
        for (int i = 0; i < backgrounds.length; i++) {
            if (backgrounds[i].equals(currentWheel.getBackground())) {
                bgPosition = i;
                break;
            }
        }
        backgroundSpinner.setSelection(bgPosition);
        sliceAdapter.updateSlices(currentWheel.getSlices());
    }

    private void setupClickListeners() {
        addSliceButton.setOnClickListener(v -> showAddSliceDialog());
        saveButton.setOnClickListener(v -> saveWheel());
        deleteButton.setOnClickListener(v -> confirmDelete());
    }

    private void showAddSliceDialog() {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_slice, null);

        EditText nameEdit = dialogView.findViewById(R.id.sliceNameEdit);
        EditText weightEdit = dialogView.findViewById(R.id.sliceWeightEdit);

        new AlertDialog.Builder(this)
                .setTitle("Add Slice")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = nameEdit.getText().toString();
                    String weightStr = weightEdit.getText().toString();
                    int weight = weightStr.isEmpty() ? 1 : Integer.parseInt(weightStr);

                    if (!name.isEmpty()) {
                        currentWheel.getSlices().add(new WheelData.SliceData(name, weight));
                        sliceAdapter.updateSlices(currentWheel.getSlices());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveWheel() {
        currentWheel.setTitle(wheelTitleEdit.getText().toString());
        currentWheel.setColorPalette(
                colorPaletteSpinner.getSelectedItemPosition() == 0 ? "Default" : "Pastel"
        );
        currentWheel.setBackground(
                backgroundSpinner.getSelectedItem().toString()
        );

        List<WheelData> wheels = loadWheels();
        boolean found = false;
        for (int i = 0; i < wheels.size(); i++) {
            if (wheels.get(i).getId() == currentWheel.getId()) {
                wheels.set(i, currentWheel);
                found = true;
                break;
            }
        }

        if (!found) {
            wheels.add(currentWheel);
        }

        saveWheels(wheels);
        setResult(RESULT_OK);
        finish();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Wheel")
                .setMessage("Are you sure you want to delete this wheel?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteWheel();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteWheel() {
        List<WheelData> wheels = loadWheels();
        wheels.removeIf(wheel -> wheel.getId() == currentWheel.getId());
        saveWheels(wheels);
        setResult(RESULT_OK);
        finish();
    }

    private List<WheelData> loadWheels() {
        String wheelsJson = sharedPreferences.getString("saved_wheels", "[]");
        Type wheelListType = new TypeToken<ArrayList<WheelData>>(){}.getType();
        List<WheelData> wheels = new Gson().fromJson(wheelsJson, wheelListType);
        return wheels != null ? wheels : new ArrayList<>();
    }

    private void saveWheels(List<WheelData> wheels) {
        String wheelsJson = new Gson().toJson(wheels);
        sharedPreferences.edit().putString("saved_wheels", wheelsJson).apply();
    }

    private class SliceAdapter extends RecyclerView.Adapter<SliceAdapter.SliceViewHolder> {
        private List<WheelData.SliceData> slices = new ArrayList<>();

        @Override
        public SliceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_slice, parent, false);
            return new SliceViewHolder(view);
        }

        @Override
        public void onBindViewHolder(SliceViewHolder holder, int position) {
            WheelData.SliceData slice = slices.get(position);
            holder.nameText.setText(slice.getName());
            holder.weightText.setText(String.valueOf(slice.getWeight()));

            holder.deleteButton.setOnClickListener(v -> {
                slices.remove(position);
                notifyDataSetChanged();
                currentWheel.setSlices(new ArrayList<>(slices));
                updatePreview();
            });

            holder.editButton.setOnClickListener(v -> showEditSliceDialog(position));
        }

        private void showEditSliceDialog(int position) {
            WheelData.SliceData slice = slices.get(position);
            View dialogView = LayoutInflater.from(WheelEditorActivity.this)
                    .inflate(R.layout.dialog_add_slice, null);

            EditText nameEdit = dialogView.findViewById(R.id.sliceNameEdit);
            EditText weightEdit = dialogView.findViewById(R.id.sliceWeightEdit);

            nameEdit.setText(slice.getName());
            weightEdit.setText(String.valueOf(slice.getWeight()));

            new AlertDialog.Builder(WheelEditorActivity.this)
                    .setTitle("Edit Slice")
                    .setView(dialogView)
                    .setPositiveButton("Save", (dialog, which) -> {
                        String name = nameEdit.getText().toString();
                        String weightStr = weightEdit.getText().toString();
                        int weight = weightStr.isEmpty() ? 1 : Integer.parseInt(weightStr);

                        if (!name.isEmpty()) {
                            slice.setName(name);
                            slice.setWeight(weight);
                            notifyDataSetChanged();
                            currentWheel.setSlices(new ArrayList<>(slices));
                            updatePreview();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }

        void updateSlices(List<WheelData.SliceData> newSlices) {
            slices = new ArrayList<>(newSlices);
            notifyDataSetChanged();
            updatePreview();
        }

        @Override
        public int getItemCount() {
            return slices.size();
        }

        class SliceViewHolder extends RecyclerView.ViewHolder {
            TextView nameText;
            TextView weightText;
            ImageButton deleteButton;
            ImageButton editButton;

            SliceViewHolder(View itemView) {
                super(itemView);
                nameText = itemView.findViewById(R.id.sliceNameText);
                weightText = itemView.findViewById(R.id.sliceWeightText);
                deleteButton = itemView.findViewById(R.id.sliceDeleteButton);
                editButton = itemView.findViewById(R.id.sliceEditButton);
            }
        }
    }
}