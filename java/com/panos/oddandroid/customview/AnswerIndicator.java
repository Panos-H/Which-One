package com.panos.oddandroid.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.ArcShape;
import android.util.AttributeSet;
import android.view.View;

import com.panos.oddandroid.GameConstants;
import com.panos.oddandroid.R;

/**
 * Indicates one of the 10 Answers a Player will give, as a circle shape.
 * The circle has an Emboss filter to give a sense of depth.
 * It also changes color according to given Answer.
 */
public class AnswerIndicator extends View {

    // CONSTANTS

    // DEPENDENCIES
    private ShapeDrawable indicatorCircle;          // The Shape is a full arc
    private EmbossMaskFilter embossFilter;          // The Emboss filter which gives a "depth" feel to the Indicator

    // VARIABLES

    public AnswerIndicator(Context context) {
        super(context);
        init(null,0);
    }

    public AnswerIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs,0);
    }

    public AnswerIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {

        // Disable hardware acceleration | Most filters and effects(EmbossMaskFilter in our case) doesn't work otherwise
        setLayerType(LAYER_TYPE_SOFTWARE,null);

        // Load View's custom attributes (horizontal direction of light)
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.AnswerIndicator,0,0);
        // Get horizontal direction of light as defined in XML | The closer the Indicator to the centre, the closer the direction to 0
        float horizontalDirection = typedArray.getFloat(R.styleable.AnswerIndicator_lightHorizontalDirection,0);
        typedArray.recycle();

        // Initialize circle
        indicatorCircle = new ShapeDrawable((new ArcShape(0,360)));

        // Set light's direction | Horizontal: from 0.5 to 0, Vertical: top of view, Height: 1/3rd of max
        float[] direction = new float[]{horizontalDirection, -0.5f, 0.3f};
        // Initialize Emboss filter | Direction: defined, Ambient, Specular: by experience :P, Radius: small depth
        embossFilter = new EmbossMaskFilter(direction,0.6f,20f,1 * GameConstants.DENSITY_MULTIPLIER);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Set Indicator's size | Use all of view's space
        indicatorCircle.setBounds(0,0, w, h);
        // Set Indicator's Paint | Color: Light dark blue-grey, Filter: The defined Emboss filter
        indicatorCircle.getPaint().setColor(Color.rgb(50,50,80));
        indicatorCircle.getPaint().setMaskFilter(embossFilter);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        indicatorCircle.draw(canvas);
    }

    /**
     * Changes Indicator's color.
     * @param color Indicates the result of the Answer | Correct: green, Wrong: red.
     */
    public void changeColor(int color) {

        indicatorCircle.getPaint().setColor(color);
        invalidate();
    }
}
