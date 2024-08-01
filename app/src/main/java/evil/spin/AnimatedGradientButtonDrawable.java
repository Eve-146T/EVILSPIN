package evil.spin;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.animation.LinearInterpolator;

public class AnimatedGradientButtonDrawable extends GradientDrawable {

    private final ValueAnimator animator;
    private final int[] colorSet;
    private final int[] currentColors = new int[2];

    public AnimatedGradientButtonDrawable(Context context) {
        super(Orientation.TL_BR, new int[]{0, 0});

        colorSet = new int[]{
                0xFFFF0000, // Red
                0xFFFF7F00, // Orange
                0xFFFFFF00, // Yellow
                0xFF00FF00, // Green
                0xFF0000FF, // Blue
                0xFF8B00FF,// Violet
                0xFF0000FF,
                0xFF00FF00, // Green
                0xFFFFFF00, // Yellow
                0xFFFF7F00 // Orange

        };

        setShape(GradientDrawable.RECTANGLE);
        setCornerRadius(16f); // Adjust this value to change corner roundness

        animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(3000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            float fraction = animation.getAnimatedFraction();
            updateGradient(fraction);
        });
        animator.start();
    }

    private void updateGradient(float fraction) {
        int index = (int) (fraction * (colorSet.length - 1));
        int nextIndex = (index + 1) % colorSet.length;
        float localFraction = fraction * (colorSet.length - 1) - index;

        currentColors[0] = interpolateColor(colorSet[index], colorSet[nextIndex], localFraction);
        currentColors[1] = interpolateColor(colorSet[(index + 1) % colorSet.length], colorSet[(nextIndex + 1) % colorSet.length], localFraction);

        setColors(currentColors);
        invalidateSelf();
    }

    private int interpolateColor(int colorA, int colorB, float fraction) {
        float[] hsvA = new float[3], hsvB = new float[3];
        android.graphics.Color.colorToHSV(colorA, hsvA);
        android.graphics.Color.colorToHSV(colorB, hsvB);

        for (int i = 0; i < 3; i++) {
            hsvA[i] = hsvA[i] + fraction * (hsvB[i] - hsvA[i]);
        }

        return android.graphics.Color.HSVToColor(hsvA);
    }
}