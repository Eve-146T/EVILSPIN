package evil.spin;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class RainbowBorderDrawable extends Drawable {
    private final Paint borderPaint;
    private final Paint backgroundPaint;
    private final int[] colors;
    private final RectF rectF;
    private float progress = 0f;
    private int speed = 5; // Default speed
    private final float cornerRadius = 16f; // Rounded corner radius
    private final float borderWidth = 8f; // Thicker border

    public RainbowBorderDrawable(Context context) {
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderWidth);

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(Color.GRAY);

        colors = new int[]{
                0xFFFF0000, 0xFFFF7F00, 0xFFFFFF00, 0xFF00FF00,
                0xFF0000FF, 0xFF4B0082, 0xFF9400D3
        };

        rectF = new RectF();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();
        rectF.set(bounds);

        // Draw background
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, backgroundPaint);

        // Inset the rectangle by half the border width to center the border
        rectF.inset(borderWidth / 2, borderWidth / 2);

        float width = rectF.width();
        float height = rectF.height();

        LinearGradient gradient = new LinearGradient(
                -width + (width * 2 * progress), 0,
                width * 2 * progress, height,
                colors, null, Shader.TileMode.CLAMP
        );

        borderPaint.setShader(gradient);
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, borderPaint);

        progress += 0.001f * speed;
        if (progress > 1f) progress = 0f;
        invalidateSelf();
    }

    @Override
    public void setAlpha(int alpha) {
        borderPaint.setAlpha(alpha);
        backgroundPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        borderPaint.setColorFilter(colorFilter);
        backgroundPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    // Add these methods to make it work with buttons
    @Override
    public boolean getPadding(@NonNull Rect padding) {
        int paddingValue = (int) (borderWidth / 2);
        padding.set(paddingValue, paddingValue, paddingValue, paddingValue);
        return true;
    }

    @Override
    public boolean isStateful() {
        return true;
    }

    @Override
    protected boolean onStateChange(int[] state) {
        invalidateSelf();
        return true;
    }
}