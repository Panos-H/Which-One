package com.panos.oddandroid.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gamesparks.sdk.android.GSAndroidPlatform;
import com.panos.oddandroid.GameConstants;
import com.panos.oddandroid.customview.AnswerIndicator;
import com.panos.oddandroid.customview.OptionButton;
import com.panos.oddandroid.R;
import com.panos.oddandroid.customview.PathTimer;
import com.panos.oddandroid.customview.QuestionIndicator;
import com.panos.oddandroid.databinding.ActivityGameBinding;
import com.panos.oddandroid.model.ChallengeGameModel;
import com.panos.oddandroid.model.User;
import com.panos.oddandroid.viewmodel.ChallengeGameViewModel;
import com.panos.oddandroid.viewmodel.CurrentChallengeViewModel;

import static com.panos.oddandroid.activity.MyBindingAdapters.animateAnswerIcon;
import static com.panos.oddandroid.activity.MyBindingAdapters.animateEnemyIcon;
import static java.lang.Math.round;

/**
 * Controls the UI of the ongoing Challenge.
 * Activity's state cycle consists of two major events: Question Message(QM), Answer Message(AM).
 *
 *  (1) Starting with a QM and each time we receive it, update Question Text and the Options.
 *  (2) Then the User is given a Time Frame to answer by pressing one of the 5 Option Buttons.
 *
 *      (2.1) If the User didn't answer in time, pause Game and wait for him to resume it.
 *
 *           (2.1.1) In case the User never comes back, go back to Main Activity (->EXIT).
 *
 *      (2.2) Upon User's return, wait for the next QM (->1).
 *
 *  (3) If the User has answered in time, send his Answer for verification.
 *  (4) Upon receiving the AM, update UI according to the correctness of the Answer.
 *
 *      (4.1) If Challenge came to a conclusion, proceed to Result Activity (->EXIT).
 *
 *  (5) A bit later, reset views to their initial state and wait for the next QM (->1).
 */
public class GameActivity extends AppCompatActivity {

    // VARIABLES
    private long questionTimestamp;                     // Holds the time we received the Question, in milliseconds | Used to calculate User's Answer Time

    // ANDROID DEPENDENCIES
    Handler handler = new Handler();                    // Handles Activity's 'flow' when delay's needed | Uses: 1) Question time's up, 2) Delays Animations, 3) Changes Activity
    CountDownTimer pauseCountdown;

    // PROJECT DEPENDENCIES
    //CurrentChallengeViewModel challengeViewModel;
    ChallengeGameViewModel challengeViewModel;

    // ANDROID VIEWS
    ImageView userTimeIcon;      // Icons indicating Player's Answer Times on top of Question Timer | They show upon receiving Answer Message

    Button readyButton;
    TextView readyTimer;

    // PROJECT VIEWS
    OptionButton[] optionButtons;                   // Buttons representing each one of the Options | They animate when pressed or upon receiving Answer Message


    // GAME LOGIC FUNCTIONS

    void outOfTime() {

        disableOptionButtons();
    }

    void pauseGame(boolean isUser) {

        readyButton.setVisibility(View.VISIBLE);
        readyTimer.setVisibility(View.VISIBLE);
        readyTimer.setText("30");
        pauseCountdown = new CountDownTimer(30000,1000){
            @Override
            public void onTick(long millisUntilFinished) {
                readyTimer.setText(String.valueOf(millisUntilFinished/1000));
            }
            @Override
            public void onFinish() {
                readyTimer.setVisibility(View.INVISIBLE);
                readyButton.setVisibility(View.INVISIBLE);
            }
        }.start();
    }

    void resumeGame() {

        pauseCountdown.cancel();
        readyTimer.setVisibility(View.INVISIBLE);
        readyButton.setVisibility(View.INVISIBLE);
    }


    // UI FUNCTIONS
    void disableOptionButtons() {

        for (OptionButton b: optionButtons) {
            b.setEnabled(false);
        }
    }

    void enableOptionButtons() {

        for (OptionButton b: optionButtons) {
            b.setEnabled(true);
        }
    }

    public void onOptionButtonClick(View optionButton) {

        if (GSAndroidPlatform.gs().isAvailable()) {

            handler.removeCallbacksAndMessages(null);
            disableOptionButtons();

            // Calculate the time REMAINING to answer in decimal | 15000 millis - time lapsed
            int answerTime = (int) (GameConstants.QUESTION_TIME_FRAME - (System.currentTimeMillis() - questionTimestamp));
            // Calculate the time LAPSED to answer in decimal | Current timestamp - Question start timestamp
            //int answerTime = (int) (System.currentTimeMillis() - questionTimestamp);
            // Calculate the time LEFT to answer in decimal | 15000 millis - time lapsed
            //double answerTime = (GameConstants.QUESTION_TIME_FRAME - (System.currentTimeMillis() - questionTimestamp)) / 1000f;
            // Round Answer time to 3 decimal points
            //double answerTimeRounded = DoubleRounder.round(answerTime, ANSWER_TIME_PRECISION);

            // Find Option Button's index | This will also be the User's Answer
            int optionButtonIndex = 0;
            for (int i = 0; i < GameConstants.AMOUNT_OF_OPTIONS; i++) {
                if (optionButton == optionButtons[i]) {
                    optionButtonIndex = i;
                    break;
                }
            }

            //challengeViewModel.checkAnswer(optionButtonIndex, answerTimeRounded);
            challengeViewModel.checkAnswer(optionButtonIndex, answerTime);

        /* Calculate rotation for User's Answer Time Icon based on her Answer Time
            Offset angle: ±15 rad from North to match the Question Timer Path
            Sweep angle: 330 -> 360(full circle) - 30(start AND end offset)
            Sweep percentage: 0.022 -> 330(Sweep angle) / 15000(Max Answer Time) */
            float userRotation = (GameConstants.QUESTION_TIME_FRAME - answerTime) * .022f + 15;
            // Show User's Icon and rotate it CCW
            userTimeIcon.setVisibility(View.VISIBLE);
            userTimeIcon.setRotation(-userRotation);

        } else {
            ((OptionButton) optionButton).cancelDashAnimation();
            ((OptionButton) optionButton).startDashAnimation(100);
            Toast.makeText(getApplicationContext(),"No Network", Toast.LENGTH_LONG).show();
        }
    }



    // LIFECYCLE FUNCTIONS
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityGameBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_game);
        binding.setLifecycleOwner(this);

        binding.setUser(User.getUser());

        challengeViewModel = new ViewModelProvider.AndroidViewModelFactory(getApplication()).create(ChallengeGameViewModel.class);
        binding.setChallengeModel(challengeViewModel);

        challengeViewModel.getPhase().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String phase) {

                if (phase.equals("QUESTION")) {

                    // Set current time as the Question Timestamp
                    questionTimestamp = System.currentTimeMillis();
                    // Delay a runnable that triggers the expiration of current Question
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            outOfTime();
                        }
                    },GameConstants.QUESTION_TIME_FRAME - challengeViewModel.getLastMessageMillis());
                    enableOptionButtons();

                } else if (phase.equals("END")) {

                    //CurrentChallenge.reset();
                    startActivity(new Intent(getApplicationContext(), PostGameActivity.class));
                    finish();
                }
            }
        });

        challengeViewModel.aPlayerIsNotReady().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isUser) {
                if (isUser != null) pauseGame(isUser);
            }
        });

        userTimeIcon = findViewById(R.id.userTimeIcon);

        // Load Option Views
        ViewGroup optionsLayout = findViewById(R.id.optionsLayout);

        optionButtons = new OptionButton[GameConstants.AMOUNT_OF_OPTIONS];
        for (int i = 0; i < optionButtons.length; i++) {
            optionButtons[i] = (OptionButton) optionsLayout.getChildAt(i);
        }
        disableOptionButtons();

        readyTimer = findViewById(R.id.readyTimer);
        readyButton = findViewById(R.id.readyButton);
        readyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resumeGame();
                if (GSAndroidPlatform.gs().isAvailable()) {
                    challengeViewModel.resumeChallenge();
                } else {
                    Toast.makeText(getApplicationContext(),"No Network", Toast.LENGTH_LONG).show();
                }
            }
        });
        findViewById(R.id.quitButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                disableOptionButtons();
                challengeViewModel.quitChallenge();
            }
        });

        // Finally, request to start the Game
        challengeViewModel.requestGameStart();
    }

    @Override
    public void onBackPressed() { }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        challengeViewModel.setActivityPaused(true);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
