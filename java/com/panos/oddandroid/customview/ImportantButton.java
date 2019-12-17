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
import android.view.MotionEvent;
import android.view.View;

import com.panos.oddandroid.GameConstants;
import com.panos.oddandroid.R;

/**
 * The Button that is recommended to press or usually pressed. Comes with a Fill and Glow effect.
 */
public class ImportantButton extends View {

    // CONSTANTS
    private static final int TEXT_COLOR_REST = Color.WHITE;                                 // Text's Color when not pressed
    private static final int FILL_COLOR_REST = Color.rgb(0,200,200);       // Button's Color when not pressed
    private static final int FILL_COLOR_ACTIVE = Color.CYAN;                                // Button's Color when pressed
    private static final float BLUR_RADIUS_REST = 4f;                       // Button's Blur radius when not pressed
    private static final float BLUR_RADIUS_ACTIVE = 8f;                     // Button's Blur radius when pressed

    // DEPENDENCIES
    private Path path;                              // Describes Button's Shape, Size and Coordinates
    private Paint paint;                            // Describes Button's Color and Blur Effect
    private Paint textPaint;                        // Describes Text's Color and Blur Effect
    private ObjectAnimator blurAnimator;            // Controls Button's Blur Animation

    // VARIABLES
    private String text;                            // The text which describes the Button
    private float blurRadius;                       // Button's current Blur Effect radius | Used in Blur Animation | Values: 3f - 8f

    private int w, h;                               // View's dimensions | Needed to detect 'outside' touch events
    private boolean touchOutside;                   // Flag to be raised on 'outside' touch events | Prevents invalid Clicks

    public ImportantButton(Context context) {
        super(context);
        init(null,0);
    }

    public ImportantButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs,0);
    }

    public ImportantButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        init(attrs, defStyle);
    }

    void init(AttributeSet attrs, int defStyle) {

        // Disable hardware acceleration | Most filters and effects(Blur Mask in our case) doesn't work otherwise
        setLayerType(LAYER_TYPE_SOFTWARE,null);

        // Load View's custom Text attribute
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.MyButton,0,0);
        if ((text = typedArray.getString(R.styleable.MyButton_text)) == null) text="";

        path = new Path();

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(FILL_COLOR_REST);
        paint.setMaskFilter(new BlurMaskFilter(BLUR_RADIUS_REST * GameConstants.DENSITY_MULTIPLIER, BlurMaskFilter.Blur.SOLID));

        // Initialize Text Paint | Center Text
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(TEXT_COLOR_REST);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(14 * GameConstants.SCALED_DENSITY_MULTIPLIER);

        // Initialize Blur Animator
        // Animating property: Blur radius surrounding Button
        // Ending values: 3f(min Blur) <-> 8f(max Blur)
        // Repeat: Infinite | Once it starts, User must interact with UI to cancel it
        // Repeat Mode: Reverse | End value 1(3f) -> End value 2(8f) -> End value 1(3f) ...
        // Duration: 1 second
        blurAnimator = ObjectAnimator.ofFloat(this,"blurRadius",3f, 8f);
        blurAnimator.setDuration(1000);
        blurAnimator.setRepeatCount(ValueAnimator.INFINITE);
        blurAnimator.setRepeatMode(ValueAnimator.REVERSE);
        blurAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                postInvalidate();
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Save size dimensions to detect future Touch events that occurred outside the View
        this.w = w;
        this.h = h;

        // Calculate the distance from View's borders | Must be respected upon setting Path's size | Necessary to fit Path's Blur Effect
        float offset = 8 * GameConstants.DENSITY_MULTIPLIER;

        // Construct Path
        // Shape: Rounded Rectangle with full circle corners
        // Size: View's allowed size minus offset
        // Coordinates: Middle
        // Direction: not important
        path.addRoundRect(new RectF(offset, offset,w-offset,h-offset), h, h, Path.Direction.CW);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawPath(path, paint);
        // Draw Text in the middle of the View
        canvas.drawText(text,w/2f,h/2f - ((textPaint.descent() + textPaint.ascent()) / 2), textPaint);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Identical logic with OptionButton's onTouchEvent
        if (isEnabled()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    setPathColor(FILL_COLOR_ACTIVE);
                    setBlurRadius(BLUR_RADIUS_ACTIVE);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    if (!touchOutside) {
                        performClick();
                        resetView();
                    }
                    touchOutside = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (!touchOutside) {
                        float locationX = event.getX();
                        float locationY = event.getY();
                        if (locationY < 0 || locationY > h || locationX < 0 || locationX > w) {
                            touchOutside = true;
                            resetView();
                        }
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    resetView();
            }
            return true;
        }
        return super.onTouchEvent(event);
    }

    public void setText(String text) {
        this.text = text;
        invalidate();
    }

    public String getText() {
        return text;
    }

    public int getPathColor() {
        return paint.getColor();
    }

    public void setPathColor(int color) {
        paint.setColor(color);
    }

    public float getBlurRadius() {
        return blurRadius;
    }

    public void setBlurRadius(float radius) {
        blurRadius=radius;
        paint.setMaskFilter(new BlurMaskFilter(radius * GameConstants.DENSITY_MULTIPLIER, BlurMaskFilter.Blur.SOLID));
    }

    public void startBlurAnimation() {

        blurAnimator.start();
    }

    public void cancelBlurAnimation() {

        blurAnimator.cancel();
        paint.setMaskFilter(new BlurMaskFilter(4 * GameConstants.DENSITY_MULTIPLIER, BlurMaskFilter.Blur.SOLID));
        postInvalidate();
    }

    public void resetView() {
        setPathColor(FILL_COLOR_REST);
        setBlurRadius(BLUR_RADIUS_REST);
        invalidate();
    }
}
