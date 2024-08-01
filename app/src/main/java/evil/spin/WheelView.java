package evil.spin;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class WheelView extends View {
    private List<String> options = new ArrayList<>();
    private Paint paint;
    private RectF rectF;
    private int[] defaultColors;
    private int[] pastelColors;
    private float rotation = 0f;
    private Path indicatorPath;
    private String colorPalette = "Default";

    public WheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rectF = new RectF();
        indicatorPath = new Path();

        defaultColors = new int[] {
                Color.parseColor("#FF4136"), // Red
                Color.parseColor("#FF851B"), // Orange
                Color.parseColor("#FFDC00"), // Yellow
                Color.parseColor("#2ECC40"), // Green
                Color.parseColor("#0074D9"), // Blue
                Color.parseColor("#B10DC9")  // Purple
        };

        pastelColors = new int[] {
                Color.parseColor("#FFB3BA"), // Pastel Red
                Color.parseColor("#FFDFBA"), // Pastel Orange
                Color.parseColor("#FFFFBA"), // Pastel Yellow
                Color.parseColor("#BAFFC9"), // Pastel Green
                Color.parseColor("#BAE1FF"), // Pastel Blue
                Color.parseColor("#E6BAFF")  // Pastel Purple
        };
    }

    public void setOptions(List<String> options) {
        this.options = options;
        invalidate();
    }

    public void setColorPalette(String palette) {
        this.colorPalette = palette;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (options.isEmpty()) return;

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float radius = Math.min(centerX, centerY) * 0.9f;

        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

        canvas.save();
        canvas.rotate(rotation, centerX, centerY);

        float startAngle = 0;
        float sweepAngle = 360f / options.size();

        for (int i = 0; i < options.size(); i++) {
            int colorIndex = i % (colorPalette.equals("Pastel") ? pastelColors.length : defaultColors.length);
            int color = colorPalette.equals("Pastel") ? pastelColors[colorIndex] : defaultColors[colorIndex];

            paint.setColor(color);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawArc(rectF, startAngle, sweepAngle, true, paint);

            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2f);
            canvas.drawArc(rectF, startAngle, sweepAngle, true, paint);

            drawTextRadially(canvas, options.get(i), centerX, centerY, radius, startAngle, sweepAngle, color);

            startAngle += sweepAngle;
        }

        canvas.restore();

        drawIndicator(canvas, centerX, centerY, radius);
    }

    private void drawIndicator(Canvas canvas, float centerX, float centerY, float radius) {
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);

        float indicatorSize = radius * 0.1f;
        indicatorPath.reset();
        indicatorPath.moveTo(centerX + radius, centerY);
        indicatorPath.lineTo(centerX + radius + indicatorSize, centerY - indicatorSize);
        indicatorPath.lineTo(centerX + radius + indicatorSize, centerY + indicatorSize);
        indicatorPath.close();

        canvas.drawPath(indicatorPath, paint);

        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        canvas.drawPath(indicatorPath, paint);
    }

    private void drawTextRadially(Canvas canvas, String text, float centerX, float centerY, float radius, float startAngle, float sweepAngle, int backgroundColor) {
        paint.setColor(getContrastColor(backgroundColor));
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setStyle(Paint.Style.FILL);

        float midAngle = startAngle + sweepAngle / 2;

        canvas.save();
        canvas.rotate(midAngle, centerX, centerY);

        float startX = centerX + radius * 0.3f;
        float stopX = centerX + radius * 0.9f;
        float availableWidth = stopX - startX;

        float textSize = getOptimalTextSize(text, availableWidth, sweepAngle, radius);
        paint.setTextSize(textSize);

        float textWidth = paint.measureText(text);
        float textX = startX + (availableWidth - textWidth) / 2;

        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float textHeight = fontMetrics.bottom - fontMetrics.top;
        float textY = centerY + (textHeight / 2) - fontMetrics.bottom;

        canvas.drawText(text, textX, textY, paint);

        canvas.restore();
    }


    private float getOptimalTextSize(String text, float maxWidth, float sweepAngle, float radius) {
        float low = 1f;
        float high = 100f;
        float optimalSize = low;
        float arcLength = (float) (2 * Math.PI * radius * (sweepAngle / 360));

        while (low <= high) {
            float mid = (low + high) / 2;
            paint.setTextSize(mid);
            float textWidth = paint.measureText(text);
            float textHeight = paint.descent() - paint.ascent();

            if (textWidth <= maxWidth * 0.9f && textHeight <= arcLength * 0.9f) {
                optimalSize = mid;
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        // Apply more aggressive scaling factor based on the number of options
        float scaleFactor = Math.max(0.5f, 1f - (options.size() / 70f)); // Adjust this formula as needed
        return optimalSize * scaleFactor;
    }

    private int getContrastColor(int color) {
        double luminance = (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return luminance > 0.5 ? Color.BLACK : Color.WHITE;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
        invalidate();
    }

    public String getSelectedOption() {
        float normalizedRotation = (360 - (rotation % 360)) % 360;
        int index = (int) (normalizedRotation / (360f / options.size()));
        return options.get(index);
    }
}