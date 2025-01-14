package evil.spin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class WelcomeActivity extends AppCompatActivity {
    private RecyclerView wheelsRecyclerView;
    private WheelPreviewAdapter wheelAdapter;
    private List<WheelData> wheels = new ArrayList<>();
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        wheelsRecyclerView = findViewById(R.id.wheelsRecyclerView);
        Button newWheelButton = findViewById(R.id.newWheelButton);
        Button importButton = findViewById(R.id.importButton);

        // Setup RecyclerView
        wheelAdapter = new WheelPreviewAdapter(this, wheels);
        wheelsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        wheelsRecyclerView.setAdapter(wheelAdapter);

        // Load saved wheels
        loadWheels();

        // Setup click listeners
        wheelAdapter.setOnWheelClickListener(new WheelPreviewAdapter.OnWheelClickListener() {
            @Override
            public void onWheelClick(WheelData wheel) {
                openWheel(wheel);
            }

            @Override
            public void onWheelLongClick(WheelData wheel) {
                openWheelEditor(wheel);
            }
        });

        newWheelButton.setOnClickListener(v -> createNewWheel());
        importButton.setOnClickListener(v -> importWheel());
    }

    private void loadWheels() {
        String wheelsJson = sharedPreferences.getString("saved_wheels", "[]");
        Type wheelListType = new TypeToken<ArrayList<WheelData>>(){}.getType();
        wheels = new Gson().fromJson(wheelsJson, wheelListType);
        if (wheels == null) wheels = new ArrayList<>();
        wheelAdapter.updateWheels(wheels);
    }

    private void saveWheels() {
        String wheelsJson = new Gson().toJson(wheels);
        sharedPreferences.edit().putString("saved_wheels", wheelsJson).apply();
    }

    private void openWheel(WheelData wheel) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("wheel_id", wheel.getId());
        startActivity(intent);
    }

    private void openWheelEditor(WheelData wheel) {
        Intent intent = new Intent(this, WheelEditorActivity.class);
        intent.putExtra("wheel_id", wheel.getId());
        startActivityForResult(intent, 1);
    }

    private void createNewWheel() {
        WheelData newWheel = new WheelData();
        newWheel.setId(System.currentTimeMillis());
        newWheel.setTitle("New Wheel");
        wheels.add(newWheel);
        saveWheels();
        openWheelEditor(newWheel);
    }

    private void importWheel() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        startActivityForResult(intent, 2);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWheels();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            loadWheels();
        }
    }
}