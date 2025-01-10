package evil.spin;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.List;

public interface IWheelSerializer {
    String SerializeWheels(Collection<Wheel> wheels) throws JSONException;

    void SaveWheelsToSharedPreferences(Collection<Wheel> wheels, SharedPreferences preferences) throws JSONException;

    Collection<Wheel> LoadWheelsFromSharedPreferences(SharedPreferences preferences) throws JSONException;

    Collection<Wheel> DeserializeWheels(String json) throws JSONException;

    @NonNull
    List<Wheel> getWheelsFromJsonArray(JSONArray wheelsJsonArray) throws JSONException;

    @NonNull
    Wheel getWheelFromJson(String wheelJson) throws JSONException;

    String[] toStringArray(JSONArray array);
}
