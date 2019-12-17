package com.panos.oddandroid.activity;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.BindingAdapter;

import com.panos.oddandroid.GameConstants;
import com.panos.oddandroid.R;
import com.panos.oddandroid.customview.AnswerIndicator;
import com.panos.oddandroid.customview.OptionButton;
import com.panos.oddandroid.customview.PathTimer;
import com.panos.oddandroid.customview.QuestionIndicator;
import com.panos.oddandroid.model.ChallengeGameModel;

public class MyBindingAdapters {

    // GAME-OPTIONS-LAYOUT ADAPTERS
    @Deprecated
    @BindingAdapter("answerConstraintTo")
    public static void setAnswerIconLayoutParams(View view, int constraintToID) {
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) view.getLayoutParams();
        layoutParams.startToStart = constraintToID;
        layoutParams.topToTop = constraintToID;
        layoutParams.bottomToBottom = constraintToID;
        view.setLayoutParams(layoutParams);
        if (constraintToID != 0) view.setVisibility(View.VISIBLE);
    }

    @Deprecated
    @BindingAdapter("enemyConstraintTo")
    public static void setEnemyIconLayoutParams(View view, int constraintToID) {
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) view.getLayoutParams();
        layoutParams.endToEnd = constraintToID;
        layoutParams.topToTop = constraintToID;
        layoutParams.bottomToBottom = constraintToID;
        view.setLayoutParams(layoutParams);
        if (constraintToID != 0) view.setVisibility(View.VISIBLE);
    }

    static void animateAnswerIcon(View view, int constraintToID) {
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) view.getLayoutParams();
        layoutParams.startToStart = constraintToID;
        layoutParams.topToTop = constraintToID;
        layoutParams.bottomToBottom = constraintToID;
        view.setLayoutParams(layoutParams);
        view.setVisibility(View.VISIBLE);
    }

    static void animateEnemyIcon(View view, int constraintToID) {
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) view.getLayoutParams();
        layoutParams.endToEnd = constraintToID;
        layoutParams.topToTop = constraintToID;
        layoutParams.bottomToBottom = constraintToID;
        view.setLayoutParams(layoutParams);
        view.setVisibility(View.VISIBLE);
    }

    // GAME-QUESTION-LAYOUT ADAPTERS

    @BindingAdapter({"phase","answerData"})
    public static void animateOptionViews(ViewGroup optionsLayout, String phase, ChallengeGameModel.AnswerData answerData) {

        // Proceed only when the pre-start phase ends (LiveData attributes have no value)
        switch (phase) {
            //case "QUESTION": break;
            //case "ANSWER": break;

            case "RESULT":
                // Start result animations
                if (answerData.getUserAnswer() != -1) {
                    // Check whether the User answered right or wrong
                    if (answerData.getUserAnswer() == answerData.getCorrectAnswer()) {
                        // Animate User's correctly pressed Button
                        ((OptionButton) optionsLayout.getChildAt(answerData.getUserAnswer())).startColorAnimation(Color.GREEN);
                    } else {
                        // Animate User's wrongly pressed Button
                        ((OptionButton) optionsLayout.getChildAt(answerData.getUserAnswer())).startColorAnimation(Color.RED);
                        // Animate correct Button
                        ((OptionButton) optionsLayout.getChildAt(answerData.getCorrectAnswer())).startDashAnimation(Color.GREEN, 0);
                        // Animate Wrong Answer Icon
                        animateAnswerIcon(optionsLayout.findViewById(R.id.wrongAnswerIcon), optionsLayout.getChildAt(answerData.getUserAnswer()).getId());
                    }
                } else {
                    // Animate correct Button
                    ((OptionButton) optionsLayout.getChildAt(answerData.getCorrectAnswer())).startDashAnimation(Color.GREEN, 0);
                }
                if (answerData.getEnemyAnswer() != -1) {
                    // Animate Enemy Answer Icon
                    animateEnemyIcon(optionsLayout.findViewById(R.id.enemyAnswerIcon), optionsLayout.getChildAt(answerData.getEnemyAnswer()).getId());
                }
                // Animate Correct Answer Icon
                animateAnswerIcon(optionsLayout.findViewById(R.id.correctAnswerIcon), optionsLayout.getChildAt(answerData.getCorrectAnswer()).getId());
                break;

            //case "RESET_QUESTION": break;

            case "RESET_OPTIONS":
            case "RESET_ALL":
                // Animate Correct and Wrong Icons and Enemy's Answer Text
                optionsLayout.findViewById(R.id.correctAnswerIcon).setVisibility(View.INVISIBLE);
                optionsLayout.findViewById(R.id.wrongAnswerIcon).setVisibility(View.INVISIBLE);
                optionsLayout.findViewById(R.id.enemyAnswerIcon).setVisibility(View.INVISIBLE);
                // Animate the correct Button
                ((OptionButton) optionsLayout.getChildAt(answerData.getCorrectAnswer())).startDashAnimation(100);
                // If the User pressed a Button and it was the wrong one, animate this Button as well.
                if (answerData.getUserAnswer() != -1 && answerData.getUserAnswer() != answerData.getCorrectAnswer()) {
                    ((OptionButton) optionsLayout.getChildAt(answerData.getUserAnswer())).startDashAnimation(100);
                }

            //case "END":
        }
    }

    // GAME-QUESTION-LAYOUT ADAPTERS

    @BindingAdapter(value = {"phase","lastMessageMillis"}, requireAll = false)
    public static void animateQuestionTimer(PathTimer questionTimer, String phase, long lastMessageMillis) {

        switch (phase) {

            case "QUESTION":
                long millis = (lastMessageMillis > 0) ? GameConstants.QUESTION_TIME_FRAME - lastMessageMillis : GameConstants.QUESTION_TIME_FRAME;
                // Start countdown Animation | Ending value: max phase(clipped fully), Duration: time left to answer
                questionTimer.startDashAnimation(1f, millis);
                break;

            case "ANSWER":
                // Stop the countdown Animation
                questionTimer.cancelDashAnimation();
                break;

            case "RESET_QUESTION":
                millis = (lastMessageMillis > 0) ? 5000 - lastMessageMillis : 2500;
                // Start the countdown for the next Question | Ending value: no phase(showing fully), Duration: time left for next Question
                questionTimer.startDashAnimation(0f, millis);
                break;

            case "RESET_ALL":
                questionTimer.cancelDashAnimation();
                questionTimer.setDashPercent(0f);
        }
    }

    @BindingAdapter(value = {"phase","enemyTime"}, requireAll = false)
    public static void animateEnemyTimeIcon(ImageView icon, String phase, int enemyTime) {

        switch (phase) {

            case "RESULT":
                // Calculate rotation for Enemy's Answer Time Icon based on her Answer Time
                float enemyRotation = (GameConstants.QUESTION_TIME_FRAME - enemyTime) * .022f + 15;
                // Show Enemy's Icon and rotate it CCW
                icon.setVisibility(View.VISIBLE);
                icon.setRotation(-enemyRotation);
                break;

            case "RESET_OPTIONS":
            case "RESET_ALL":
                // Hide and rotate back to default position Players' Answer Time Icons
                icon.getRootView().findViewById(R.id.userTimeIcon).setVisibility(View.INVISIBLE);
                icon.setVisibility(View.INVISIBLE);
        }
    }

    @BindingAdapter(value = {"phase","step"}, requireAll = false)
    public static void animateQuestionIndicators(ViewGroup questionIndicatorsFrame, String phase, int step) {

        if (step >= 0) {
            ViewGroup questionIndicatorsLayout = questionIndicatorsFrame.findViewById(R.id.questionIndicatorsLayout);
            QuestionIndicator questionIndicator = (QuestionIndicator) questionIndicatorsLayout.getChildAt(step % 5);

            switch (phase) {

                case "QUESTION":
                    // Start current Question Indicator's Animation upon adding some Blur
                    questionIndicator.addBlur();
                    questionIndicator.startAlphaAnimation(1.0f, 800, 0);
                    break;

                case "RESET_QUESTION":
                    // Remove blur from current Question Indicator
                    questionIndicator.removeBlur();
                    // Check whether that was 1st Category's final Question
                    if (step == GameConstants.AMOUNT_OF_QUESTIONS - 1) {
                        // If so, animate all Question Indicators, beginning with the last, in preparation for the next Category Questions
                        for (int i = step; i >= 0; i--) {
                            // Fade-in one after another with an increasing delay each time
                            ((QuestionIndicator) questionIndicatorsLayout.getChildAt(i)).startAlphaAnimation(0.6f, 500, 200 * (step - i));
                        }
                    } else {
                        // Otherwise, just the current one
                        questionIndicator.startAlphaAnimation(0.2f, 800, 0);
                    }
                    break;

                case "RESET_ALL":
                    // Remove blur from current Question Indicator
                    questionIndicator.removeBlur();
                    // Check whether that was 1st Category's final Question
                    if (step == GameConstants.AMOUNT_OF_QUESTIONS - 1) {
                        // If so, reset all Question Indicators, beginning with the last, in preparation for the next Category Questions
                        for (int i = step; i >= 0; i--) {
                            (questionIndicatorsLayout.getChildAt(i)).setAlpha(0.6f);
                        }
                    } else {
                        // Otherwise, just the current one
                        questionIndicator.setAlpha(0.2f);
                    }
            }
        }
    }

    // GAME-OPTIONS-LAYOUT ADAPTERS

    @BindingAdapter({"phase","step","answerData"})
    public static void animatePlayerViews(ViewGroup playerLayout, String phase, int step, ChallengeGameModel.AnswerData answerData) {

        if (step >= 0) {
            switch (phase) {
                //case "QUESTION": break;
                //case "ANSWER": break;

                case "RESULT":
                case "RESET_ALL":
                    // Start result animations
                    if (answerData.getUserAnswer() != -1) {
                        // Check whether the User answered right or wrong
                        if (answerData.getUserAnswer() == answerData.getCorrectAnswer()) {
                            // Update User's Answer Indicators
                            ((AnswerIndicator) ((ViewGroup) playerLayout.findViewById(R.id.userAnswerIndicatorsLayout)).getChildAt(step)).changeColor(Color.GREEN);
                        } else {
                            // Update User's Answer Indicators
                            ((AnswerIndicator) ((ViewGroup) playerLayout.findViewById(R.id.userAnswerIndicatorsLayout)).getChildAt(step)).changeColor(Color.RED);
                        }
                    }
                    if (answerData.getEnemyAnswer() != -1) {
                        // Check whether the Enemy answered right or wrong
                        if (answerData.getEnemyAnswer() == answerData.getCorrectAnswer()) {
                            // Update Enemy's Answer Indicators
                            ((AnswerIndicator) ((ViewGroup) playerLayout.findViewById(R.id.enemyAnswerIndicatorsLayout)).getChildAt(step)).changeColor(Color.GREEN);
                        } else {
                            // Update Enemy's Answer Indicators
                            ((AnswerIndicator) ((ViewGroup) playerLayout.findViewById(R.id.enemyAnswerIndicatorsLayout)).getChildAt(step)).changeColor(Color.RED);
                        }
                    }
                    break;

                //case "RESET_QUESTION": break;
                //case "RESET_OPTIONS": break;
                //case "END":
            }
        }
    }
}
