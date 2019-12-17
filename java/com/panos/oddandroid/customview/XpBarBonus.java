package com.panos.oddandroid.customview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.TextView;

import com.panos.oddandroid.R;
import com.panos.oddandroid.model.User;

/**
 * Two rounded Bars indicating User's current and bonus XP. Bonus Xp Bar appears under current XP
 * Bar. Upon starting the Animation, that showcases the amount of XP earned, the current XP Bar
 * will slowly overlap the bonus XP Bar.
 */
public class XpBarBonus extends PathTimer {

    Path bonusPath;                 // Indicates the XP left to obtain | Drawn under the actual XP Path
    Paint bonusPaint;               // Bonus Path's Paint | Same as actual Paint with lower alpha

    User user;

    float bonusDashPercent;         // The amount of clipping affecting the Bonus XP Path
    float bonusLength;              // The length of the Path | Used in Dash Path Effect

    public XpBarBonus(Context context) {
        super(context);
    }

    public XpBarBonus(Context context, AttributeSet attrs) {
        super(context,attrs);
    }

    public XpBarBonus(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    void init(AttributeSet attrs, int defStyle) {
        super.init(attrs, defStyle);

        user = User.getUser();

        // Current XP Bar's Dash percentage is calculated based on UI experience points
        dashPercent = 1 - user.getTransientXp();
        // Bonus XP Bar's Dash percentage is calculated based on XP earned | If XP is enough to gain a level, circle is fully filled
        bonusDashPercent = user.getXp() - user.getTransientXp() < 0 ? 1 : user.getXp();

        bonusPath = new Path();

        bonusPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bonusPaint.setStyle(Paint.Style.STROKE);                     // Drawing Path requires stroke style
        bonusPaint.setStrokeCap(Paint.Cap.ROUND);                    // Start and end of Path will be round
        bonusPaint.setStrokeWidth(pathWidth);
        bonusPaint.setColor(pathColor);
        bonusPaint.setAlpha(100);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        bonusPath.rewind();
        bonusPath.addArc(new RectF(borderOffset, borderOffset,w-borderOffset,h-borderOffset),startAngle,359);

        // Measure the created Path and get its length
        PathMeasure measure = new PathMeasure(bonusPath,false);
        bonusLength = measure.getLength();
        setBonusDashPercent(1 - bonusDashPercent);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draw Bonus Xp Path under the Xp Path
        canvas.drawPath(bonusPath, bonusPaint);
        super.onDraw(canvas);
    }

    public void startXpAnimation() {

        // In case User reached next level..
        if (bonusDashPercent == 1) {
            // Calculate current level Animation's duration | Subtract the Old Xp from the Maximum Xp(1) | Duration: 1sec / 0.1xp
            long duration = (long) ((1 - user.getTransientXp()) * 10000);
            // Assign a Runnable to execute when current level Animation is completed
            dashAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    animation.removeListener(this);
                    // Update User level Text
                    TextView levelText = ((ViewGroup)getParent()).findViewById(R.id.level);
                    levelText.setText(String.valueOf(user.getLevel()));
                    // Reset Xp Bar
                    setDashPercent(1f);
                    setBonusDashPercent(1 - user.getXp());
                    // Calculate new level Animation's duration
                    long newDuration = (long) (user.getXp() * 10000);
                    // Start Animation for the remaining XP
                    user.setTransientLevel(user.getLevel());
                    user.setTransientXp(user.getXp());
                    startDashAnimation(1 - user.getXp(), newDuration,500);
                }
            });
            // Start current level Animation
            startDashAnimation(0f, duration,1000);
        } else {
            long duration = (long) ((user.getXp() - user.getTransientXp()) * 10000);
            user.setTransientLevel(user.getLevel());
            user.setTransientXp(user.getXp());
            startDashAnimation(1 - user.getXp(), duration,1000);
        }
    }

    public float getBonusDashPercent() {
        return bonusDashPercent;
    }

    public void setBonusDashPercent(float bonusDashPercent) {
        this.bonusDashPercent = bonusDashPercent;
        // Show interval & Dash interval: Path's length or any greater value, Starting Dash: Clip percentage with backwards direction(CCW)
        bonusPaint.setPathEffect(new DashPathEffect(new float[]{bonusLength,bonusLength},bonusDashPercent *bonusLength));
    }
}
