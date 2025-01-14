package evil.spin;

import java.util.ArrayList;
import java.util.List;

public class WheelData {
    private long id;
    private String title;
    private List<SliceData> slices;
    private String colorPalette;
    private String background;

    public WheelData() {
        this.slices = new ArrayList<>();
        this.colorPalette = "Default";
        this.background = "red";
    }

    public static class SliceData {
        private String name;
        private int weight;

        public SliceData(String name, int weight) {
            this.name = name;
            this.weight = weight;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getWeight() { return weight; }
        public void setWeight(int weight) { this.weight = weight; }
    }

    // Getters and setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public List<SliceData> getSlices() { return slices; }
    public void setSlices(List<SliceData> slices) { this.slices = slices; }
    public String getColorPalette() { return colorPalette; }
    public void setColorPalette(String colorPalette) { this.colorPalette = colorPalette; }
    public String getBackground() { return background; }
    public void setBackground(String background) { this.background = background; }
}