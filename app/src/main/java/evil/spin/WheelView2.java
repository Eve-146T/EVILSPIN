package evil.spin;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class WheelView2 extends View {
    private List<String> options = new ArrayList<>();
    private Paint paint;
    private RectF rectF;
    private int[] colors;
    private static final float MIN_TEXT_SIZE = 12f;
    private static final float MAX_TEXT_SIZE = 100f;

    public WheelView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rectF = new RectF();

        Resources res = context.getResources();
        colors = new int[] {
                res.getColor(R.color.wheel_blue),
                res.getColor(R.color.wheel_green),
                res.getColor(R.color.wheel_yellow),
                res.getColor(R.color.wheel_red)
        };
    }

    public void setOptions(List<String> options) {
        this.options = options;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (options.isEmpty()) return;

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float radius = Math.min(centerX, centerY);

        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

        float startAngle = 0;
        float sweepAngle = 360f / options.size();

        for (int i = 0; i < options.size(); i++) {
            int colorIndex = getColorIndex(i, options.size(), colors.length);
            paint.setColor(colors[colorIndex]);
            canvas.drawArc(rectF, startAngle, sweepAngle, true, paint);

            drawTextRadially(canvas, options.get(i), centerX, centerY, radius, startAngle, sweepAngle, colors[colorIndex]);

            startAngle += sweepAngle;
        }
    }

    private void drawTextRadially(Canvas canvas, String text, float centerX, float centerY, float radius, float startAngle, float sweepAngle, int backgroundColor) {
        paint.setColor(getContrastColor(backgroundColor));
        paint.setTextAlign(Paint.Align.LEFT);

        float midAngle = startAngle + sweepAngle / 2;

        canvas.save();
        canvas.rotate(midAngle, centerX, centerY);

        float startX = centerX + radius * 0.3f; // Start text at 30% from center
        float stopX = centerX + radius * 0.9f; // End text at 90% from center
        float availableWidth = stopX - startX;

        float textSize = getOptimalTextSize(text, availableWidth, sweepAngle, radius);
        paint.setTextSize(textSize);

        float textWidth = paint.measureText(text);
        float textX = startX + (availableWidth - textWidth) / 2; // Center text horizontally

        canvas.drawText(text, textX, centerY, paint);

        canvas.restore();
    }

    private float getOptimalTextSize(String text, float maxWidth, float sweepAngle, float radius) {
        float low = MIN_TEXT_SIZE;
        float high = MAX_TEXT_SIZE;
        float optimalSize = low;
        float arcLength = (float) (2 * Math.PI * radius * (sweepAngle / 360));

        while (low <= high) {
            float mid = (low + high) / 2;
            paint.setTextSize(mid);
            float textWidth = paint.measureText(text);
            float textHeight = paint.descent() - paint.ascent();

            if (textWidth <= maxWidth && textHeight <= arcLength) {
                optimalSize = mid;
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        return optimalSize;
    }

    private int getColorIndex(int optionIndex, int totalOptions, int totalColors) {
        if (totalOptions % totalColors == 1 && optionIndex == totalOptions - 1) {
            return (totalColors - 3 + totalColors) % totalColors; // Second to last color
        }
        return optionIndex % totalColors;
    }

    private int getContrastColor(int color) {
        // Calculate the perceptive luminance (human eye favors green color)
        double luminance = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return luminance < 0.5 ? Color.BLACK : Color.WHITE;
    }

    public String getSelectedOption() {
        float rotation = getRotation() % 360;
        int index = (int) (rotation / (360f / options.size()));
        return options.get(index);
    }
}