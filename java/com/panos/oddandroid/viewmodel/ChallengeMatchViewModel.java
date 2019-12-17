package com.panos.oddandroid.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.panos.oddandroid.GameConstants;
import com.panos.oddandroid.model.ChallengeMatchModel;

public class ChallengeMatchViewModel extends CurrentChallengeViewModel {

    private ChallengeMatchModel challengeMatchModel;

    public ChallengeMatchViewModel(@NonNull Application application) {
        super(application);
        challengeMatchModel = new ChallengeMatchModel(application);
        phase = challengeMatchModel.getPhase();
    }

    public void findMatch(int opponent, int mode, int category) {
        challengeMatchModel.findMatch(opponent, mode, category);
    }

    public void cancelMatch() {
        challengeMatchModel.cancelMatch();
    }

    // DATA BINDING
    public boolean userIsChallenger() {
        return challengeMatchModel.userIsChallenger();
    }

    public String getUserCategory() {
        return GameConstants.CATEGORIES[challengeMatchModel.getUserCategory()];
    }

    public String getEnemyCategory() {
        return GameConstants.CATEGORIES[challengeMatchModel.getEnemyCategory()];
    }

    @Override
    public String getEnemyName() {
        return challengeMatchModel.getEnemyName();
    }

    @Override
    public LiveData<Boolean> enemyIsUsingImage() {
        return challengeMatchModel.enemyIsUsingImage();
    }

    @Override
    public LiveData<byte[]> getEnemyImage() {
        return challengeMatchModel.getEnemyImage();
    }

    @Override
    public LiveData<Integer> getEnemyLevel() {
        return challengeMatchModel.getEnemyLevel();
    }
}
