package com.panos.oddandroid.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.panos.oddandroid.GameConstants;
import com.panos.oddandroid.R;

/**
 * Shows a transparent rounded rectangle with blur filter around it, giving a sense of elevation.
 * Used as background in a Group(Layout) of Views.
 */
public class LayoutBackground extends View {

    // CONSTANTS

    // DEPENDENCIES
    private Path path;                   // The rounded Path
    private Paint paint;                 // Path's Paint

    // VARIABLES
    private float cornerRadius;          // Path's Corner radius | Based on View's Width (0-100%)
    private int blurColor;

    public LayoutBackground(Context context) {
        super(context);
        init(null,0);
    }

    public LayoutBackground(Context context, AttributeSet attrs) {
        super(context,attrs);
        init(attrs,0);
    }

    public LayoutBackground(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {

        // Disable hardware acceleration | Most filters and effects(BlurMaskFilter in our case) doesn't work otherwise
        setLayerType(LAYER_TYPE_SOFTWARE,null);

        // Initialize Path
        path = new Path();

        // Get the Corner radius as defined in XML (0-1f, 0: no roundness, 1: corners form a circle)
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.LayoutBackground,0,0);
        cornerRadius = typedArray.getFloat(R.styleable.LayoutBackground_cornerRadiusWidthPercent,1);
        //blurColor = typedArray.getColor(R.styleable.LayoutBackground_blurColor, Color.BLACK);

        // Initialize Paint
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);                       // We actually don't want to draw the Path
        paint.setColor(Color.BLACK);                            // Blur's color
        paint.setMaskFilter(new BlurMaskFilter(6 * GameConstants.DENSITY_MULTIPLIER, BlurMaskFilter.Blur.OUTER));         // Blur around the Path
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Calculate the distance from View's borders | Must be respected upon setting Path's size
        float offset = 8 * GameConstants.DENSITY_MULTIPLIER;
        // Set Path | Shape: rounded rectangle, Size: View's allowed dimensions, Corners: XML Defined, Direction: not important
        path.rewind();
        path.addRoundRect(new RectF(offset, offset,w-offset,h-offset),w * cornerRadius,w * cornerRadius, Path.Direction.CCW);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawPath(path, paint);
    }

    public int getBlurColor() {
        return blurColor;
    }

    public void setBlurColor(int color) {
        blurColor = color;
        paint.setColor(blurColor);
        //invalidate();
    }
}
