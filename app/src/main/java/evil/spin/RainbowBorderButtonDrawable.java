package evil.spin;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

public class RainbowBorderButtonDrawable extends Drawable {

    private final Paint paint;
    private final ValueAnimator animator;
    private final RectF rectF;
    private int currentColor;

    private static final int[] COLORS = {
            Color.RED,
            Color.BLUE,
            Color.GREEN,
            Color.YELLOW,
            Color.MAGENTA,
            Color.CYAN
    };

    public RainbowBorderButtonDrawable(Context context) {
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(6f);
        paint.setAntiAlias(true);

        rectF = new RectF();

        animator = ValueAnimator.ofFloat(0, COLORS.length);
        animator.setDuration(3000 * COLORS.length);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.addUpdateListener(animation -> {
            float position = (float) animation.getAnimatedValue();
            int index = (int) position;
            float fraction = position - index;
            int startColor = COLORS[index % COLORS.length];
            int endColor = COLORS[(index + 1) % COLORS.length];
            currentColor = interpolateColor(startColor, endColor, fraction);
            invalidateSelf();
        });
        animator.start();
    }

    private int interpolateColor(int startColor, int endColor, float fraction) {
        int startA = (startColor >> 24) & 0xff;
        int startR = (startColor >> 16) & 0xff;
        int startG = (startColor >> 8) & 0xff;
        int startB = startColor & 0xff;

        int endA = (endColor >> 24) & 0xff;
        int endR = (endColor >> 16) & 0xff;
        int endG = (endColor >> 8) & 0xff;
        int endB = endColor & 0xff;

        return Color.argb(
                (int) (startA + (endA - startA) * fraction),
                (int) (startR + (endR - startR) * fraction),
                (int) (startG + (endG - startG) * fraction),
                (int) (startB + (endB - startB) * fraction)
        );
    }

    @Override
    public void draw(Canvas canvas) {
        int width = getBounds().width();
        int height = getBounds().height();
        float cornerRadius = Math.min(width, height) / 8f;

        rectF.set(getBounds());
        rectF.inset(paint.getStrokeWidth() / 2, paint.getStrokeWidth() / 2);

        paint.setColor(currentColor);
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint);
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}