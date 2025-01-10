package evil.spin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class Wheel implements Serializable {
    public int Id = 0;
    public String Name = "";
    public Collection<String> Options = new ArrayList<>();

    public Wheel(int id, String name, Collection<String> options)
    {
        Id = id;
        Name = name;
        Options = options;
    }
    public Wheel() {}

    public String Serialize()
    {
        JSONObject json = new JSONObject();
        try {
            json.put("Id",Id);
            json.put("Name",Name);
            JSONArray options = new JSONArray(Options);
            json.put("Options",options);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return json.toString();
    }
}
