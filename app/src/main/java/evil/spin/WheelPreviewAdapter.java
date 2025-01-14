package evil.spin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class WheelPreviewAdapter extends RecyclerView.Adapter<WheelPreviewAdapter.WheelViewHolder> {
    private List<WheelData> wheels;
    private Context context;
    private OnWheelClickListener listener;

    public interface OnWheelClickListener {
        void onWheelClick(WheelData wheel);
        void onWheelLongClick(WheelData wheel);
    }

    public WheelPreviewAdapter(Context context, List<WheelData> wheels) {
        this.context = context;
        this.wheels = new ArrayList<>(wheels);
    }

    public void setOnWheelClickListener(OnWheelClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public WheelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_wheel_preview, parent, false);
        return new WheelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WheelViewHolder holder, int position) {
        WheelData wheel = wheels.get(position);
        holder.wheelTitle.setText(wheel.getTitle());
        holder.wheelInfo.setText(wheel.getSlices().size() + " slices");
        holder.wheelView.setOptions(getSliceNames(wheel));
        holder.wheelView.setColorPalette(wheel.getColorPalette());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onWheelClick(wheel);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onWheelLongClick(wheel);
            return true;
        });
    }

    private List<String> getSliceNames(WheelData wheel) {
        List<String> names = new ArrayList<>();
        for (WheelData.SliceData slice : wheel.getSlices()) {
            names.add(slice.getName());
        }
        return names;
    }

    @Override
    public int getItemCount() {
        return wheels.size();
    }

    public void updateWheels(List<WheelData> newWheels) {
        wheels.clear();
        wheels.addAll(newWheels);
        notifyDataSetChanged();
    }

    static class WheelViewHolder extends RecyclerView.ViewHolder {
        WheelView wheelView;
        TextView wheelTitle;
        TextView wheelInfo;

        WheelViewHolder(View itemView) {
            super(itemView);
            wheelView = itemView.findViewById(R.id.wheelPreview);
            wheelTitle = itemView.findViewById(R.id.wheelTitle);
            wheelInfo = itemView.findViewById(R.id.wheelInfo);
        }
    }
}