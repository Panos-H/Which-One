package com.panos.oddandroid.customview;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
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

import static com.panos.oddandroid.GameConstants.AMOUNT_OF_QUESTIONS;

/**
 * Indicates one of the (current Category) 5 Questions as an arc Path.
 * It also animates according to Question changes.
 */
public class QuestionIndicator extends View {

    // CONSTANTS

    // DEPENDENCIES
    private Path path;                              // The arc Path
    private Paint paint;                            // Path's Paint
    private ObjectAnimator alphaAnimator;           // Animates the View(Indicator) | Gradually changes Animating Path's alpha

    // VARIABLES
    private float startAngle;                       // The starting angle of the arc | 0rad: 3 o'clock, 270rad: 12 o'clock

    public QuestionIndicator(Context context) {
        super(context);
        init(null,0);
    }

    public QuestionIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs,0);
    }

    public QuestionIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {

        // Disable hardware acceleration | Most filters and effects(BlurMaskFilter in our case) doesn't work otherwise
        setLayerType(LAYER_TYPE_SOFTWARE,null);

        // Load View's custom attributes (starting angle)
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.QuestionIndicator,0,0);
        // Get starting angle as defined in XML
        startAngle = typedArray.getFloat(R.styleable.QuestionIndicator_startAngle,0);
        typedArray.recycle();

        // Initialize Path
        path = new Path();

        // Initialize Paint
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);                     // Drawing Path requires stroke style
        paint.setStrokeCap(Paint.Cap.ROUND);                    // Start and end of Path will be round
        paint.setStrokeWidth(4 * GameConstants.DENSITY_MULTIPLIER);
        paint.setColor(Color.CYAN);

        // Initialize Phase Animator | Animating property: Abstract value phase(0f to 1f), Ending value: full or no phase
        alphaAnimator = ObjectAnimator.ofFloat(this,"alpha",1f);
        alphaAnimator.setInterpolator(null);
        alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                postInvalidate();
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Calculate the distance from View's borders | Must be respected upon setting Path's size
        float offset = 6 * GameConstants.DENSITY_MULTIPLIER;
        // Calculate the length of the arc | 75% of Indicator's reserved 1/5th of the circle
        float sweepAngle = 0.75f * (360f / AMOUNT_OF_QUESTIONS);
        // Set Path | Shape: arc, Size: View's allowed dimensions, Starting angle: XML defined, Ending angle: add sweep angle
        path.arcTo(new RectF(offset,offset,w-offset,h-offset),startAngle,sweepAngle,false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawPath(path, paint);
    }

    /**
     * Adds a Blur effect around the Indicator.
     * Called prior to the animation that increases this View's alpha.
     * The Indicator now represents the current Question.
     */
    public void addBlur() {
        paint.setMaskFilter(new BlurMaskFilter(3 * GameConstants.DENSITY_MULTIPLIER, BlurMaskFilter.Blur.SOLID));
        postInvalidate();
    }

    /**
     * Removes the Blur effect around the Indicator.
     * Called prior to the animation that decreases this View's alpha.
     * The Indicator now represents the previous Question.
     */
    public void removeBlur() {
        paint.setMaskFilter(null);
        postInvalidate();
    }

    /**
     * Starts View's(Indicator) Alpha Animation upon setting Alpha Animator first.
     * Called when 1) There's a new Question, 2) Resetting views for next Question.
     * @param endValue The View's final alpha percentage in float 1) 0.2 or 1.0, 2) 0.6.
     * @param duration The duration of the Animation in milliseconds 1) 800, 2) 500.
     * @param delay The delay before the start of Animation in milliseconds 1) 0, 2) Based on Indicator's turn.
     */
    public void startAlphaAnimation(float endValue, long duration, long delay) {

        alphaAnimator.setFloatValues(endValue);
        alphaAnimator.setDuration(duration);
        alphaAnimator.setStartDelay(delay);
        alphaAnimator.start();
    }

    /**
     * Cancels View's(Indicator) Alpha Animation.
     * Called when 1) Dunno.
     */
    public void cancelAlphaAnimation() {
        alphaAnimator.cancel();
    }
}
