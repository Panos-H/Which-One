package com.panos.oddandroid.viewmodel;

import android.app.Application;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.panos.oddandroid.activity.GameActivity;
import com.panos.oddandroid.model.ChallengeGameModel;

public class ChallengeGameViewModel extends CurrentChallengeViewModel {

    private ChallengeGameModel challengeGameModel;

    private Handler phaseHandler = new Handler();

    private long lastMessageMillis;
    private boolean activityPaused;

    public ChallengeGameViewModel(@NonNull Application application) {
        super(application);
        challengeGameModel = new ChallengeGameModel(application);

        phase = new MediatorLiveData<>();
        phase.setValue("");

        lastMessageMillis = 0;

        ((MediatorLiveData<String>) phase).addSource(challengeGameModel.getPhase(), new Observer<String>() {
            @Override
            public void onChanged(String s) {

                String value = s;
                if (activityPaused) {
                    // Calculate the time passed(in milliseconds) from the last "ANSWER" message
                    lastMessageMillis = System.currentTimeMillis() - challengeGameModel.getMessageTimestamp();

                    if (s.equals("ANSWER")) {

                        if (lastMessageMillis < 1450) {
                            setResultPhase(1450 - lastMessageMillis);
                        } else if (lastMessageMillis < 1950) {
                            value = "RESULT";
                            setResetQuestionPhase(500);
                        } else {
                            value = "RESET_ALL";
                        }
                    } else if (s.equals("QUESTION")) {
                        value = "RESET_ALL";
                        setQuestionPhase(500);
                    }
                    activityPaused = false;
                    lastMessageMillis = 0;
                } else if (s.equals("ANSWER")) {

                    setResultPhase(1000);
                } else {
                    value = s;
                }
                phase.setValue(value);
            }
        });
    }

    @Override
    protected void onCleared() {
        phaseHandler.removeCallbacksAndMessages(null);
        super.onCleared();
    }

    private void setQuestionPhase(long delay) {
        phaseHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                phase.setValue("QUESTION");
            }
        }, delay);
    }

    private void setResultPhase(long delay) {
        phaseHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                phase.setValue("RESULT");
                setResetQuestionPhase(1000);
            }
        }, delay);
    }

    private void setResetQuestionPhase(long delay) {
        phaseHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                phase.setValue("RESET_QUESTION");
                setResetOptionsPhase(2000);
            }
        }, delay);
    }

    private void setResetOptionsPhase(long delay) {
        phaseHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                phase.setValue("RESET_OPTIONS");
            }
        }, delay);
    }

    public void setResetAllPhase(long delay) {
        phaseHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                phase.setValue("RESET_ALL");
            }
        }, delay);
    }

    public void requestGameStart() {
        challengeGameModel.requestGameStart();
    }

    public void checkAnswer(int answer, int time) {
        challengeGameModel.checkAnswer(answer, time);
    }

    public void resumeChallenge() {
        challengeGameModel.resumeChallenge();
    }

    public void quitChallenge() {
        challengeGameModel.quitChallenge();
    }

    public void setActivityPaused(boolean activityPaused) {
        this.activityPaused = activityPaused;
    }

    public long getLastMessageMillis() {
        return lastMessageMillis;
    }

    /**
     * Returns User's Score.
     * @return User's Score.
     */
    public int getUserScore() { return challengeGameModel.getUserScore(); }

    /**
     * Returns Enemy's Score.
     * @return Enemy's Score.
     */
    public int getEnemyScore() { return challengeGameModel.getEnemyScore(); }

    /**
     * Returns a Color depending on Challenge's Result.
     * @return The Color integer.
     */
    public int getResultColor() {
        switch (challengeGameModel.getResult()) {
            case 0: return Color.GREEN;
            case 1: return Color.RED;
            default: return Color.BLUE;
        }
    }

    /**
     * Returns a Light Color depending on Challenge's Result.
     * @return The Color integer.
     */
    public int getResultLighterColor() {
        switch (challengeGameModel.getResult()) {
            case 0: return Color.rgb(200,255,200);
            case 1: return Color.rgb(255,200,200);
            default: return Color.rgb(200,200,255);
        }
    }

    /**
     * Returns an appropriate Text depending on Challenge's Result.
     * @return The Color integer.
     */
    public String getResultText() {
        switch (challengeGameModel.getResult()) {
            case 0: return "VICTORY";
            case 1: return "DEFEAT";
            default: return "TIE";
        }
    }

    public LiveData<Integer> getStep() {
        return challengeGameModel.getStep();
    }

    public LiveData<Boolean> aPlayerIsNotReady() {
        return challengeGameModel.aPlayerIsNotReady();
    }

    public LiveData<String> getQuestion() {
        return challengeGameModel.getQuestion();
    }

    public LiveData<String[]> getOptions() {
        return challengeGameModel.getOptions();
    }

    public LiveData<ChallengeGameModel.AnswerData> getAnswerData() {
        return challengeGameModel.getAnswerData();
    }

    public long getMessageTimestamp() {
        return challengeGameModel.getMessageTimestamp();
    }

    @Override
    public String getEnemyName() {
        return challengeGameModel.getEnemyName();
    }

    @Override
    public LiveData<Boolean> enemyIsUsingImage() {
        return challengeGameModel.enemyIsUsingImage();
    }

    @Override
    public LiveData<byte[]> getEnemyImage() {
        return challengeGameModel.getEnemyImage();
    }

    @Override
    public LiveData<Integer> getEnemyLevel() {
        return challengeGameModel.getEnemyLevel();
    }
}
