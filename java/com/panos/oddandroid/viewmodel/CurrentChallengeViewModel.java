package com.panos.oddandroid.viewmodel;

import android.app.Application;
import android.graphics.Color;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.panos.oddandroid.model.ChallengeGameModel;
import com.panos.oddandroid.model.ChallengeMatchModel;
import com.panos.oddandroid.model.CurrentChallenge;
import com.panos.oddandroid.GameConstants;

public class CurrentChallengeViewModel extends AndroidViewModel {

    MutableLiveData<String> phase;

    public CurrentChallengeViewModel(@NonNull Application application) {
        super(application);
    }

    public String getEnemyName() {
        return null;
    }

    public LiveData<Boolean> enemyIsUsingImage() {
        return null;
    }

    public LiveData<byte[]> getEnemyImage() {
        return null;
    }

    public LiveData<Integer> getEnemyLevel() {
        return null;
    }

    public LiveData<String> getPhase() {
        return phase;
    }
}
