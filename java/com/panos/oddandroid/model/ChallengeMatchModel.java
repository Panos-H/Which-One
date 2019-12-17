package com.panos.oddandroid.model;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.gamesparks.sdk.GSEventConsumer;
import com.gamesparks.sdk.android.GSAndroidPlatform;
import com.gamesparks.sdk.api.autogen.GSMessageHandler;
import com.gamesparks.sdk.api.autogen.GSResponseBuilder;
import com.gamesparks.sdk.api.autogen.GSTypes;

import java.io.FileOutputStream;

public class ChallengeMatchModel {

    private CurrentChallenge challenge;
    private Application application;

    private Handler handler = new Handler();
    private String matchID;

    private MutableLiveData<String> phase = new MutableLiveData<>();

    private int challengeMode;

    public ChallengeMatchModel(@NonNull Application application) {
        this.application = application;
        destroyMatchFoundListener();
        buildMatchFoundListener();
    }

    /**
     * Builds and sends a Request to find a Match, taking into account the User's default/chosen Match criteria.
     * Updates UI upon receiving a valid response.
     * In case the Match(actually issued Challenge) is created on behalf of User, get Match's ID.
     * In addition, set a timer to auto-cancel Matching in 10 seconds.
     */
    public void findMatch(int opponent, int mode, int category) {

        if (GSAndroidPlatform.gs().isAvailable()) {
            GSAndroidPlatform.gs()
                    .getRequestBuilder()
                    .createLogEventRequest()
                    .setEventKey("findMatch")
                    .setEventAttribute("opponent", opponent)
                    .setEventAttribute("mode", mode)
                    .setEventAttribute("category", category)
                    .send(new GSEventConsumer<GSResponseBuilder.LogEventResponse>() {
                        @Override
                        public void onEvent(final GSResponseBuilder.LogEventResponse response) {
                            phase.setValue("FINDING");
                            if ((matchID = response.getScriptData().getString("matchId")) != null) {
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        cancelMatch();
                                    }
                                }, 10000);
                            }
                        }
                    });
        }
    }

    /**
     * Builds and sends a Withdraw Challenge Request to cancel matching(actually issued Challenge).
     * Updates UI upon receiving the response.
     */
    public void cancelMatch() {

        handler.removeCallbacksAndMessages(null);
        phase.setValue("CANCELLED");
        GSAndroidPlatform.gs()
                .getRequestBuilder()
                .createWithdrawChallengeRequest()
                .setChallengeInstanceId(matchID)
                .send(new GSEventConsumer<GSResponseBuilder.WithdrawChallengeResponse>() {
                    @Override
                    public void onEvent(GSResponseBuilder.WithdrawChallengeResponse response) {
                    }
                });
    }

    public void getEnemyData() {

        GSAndroidPlatform.gs().getRequestBuilder().createLogEventRequest()
                .setEventKey("downloadImage")
                .setEventAttribute("userId", challenge.getEnemy().getId())
                .send(new GSEventConsumer<GSResponseBuilder.LogEventResponse>() {
                    @Override
                    public void onEvent(GSResponseBuilder.LogEventResponse response) {
                        if (!response.hasErrors()) {

                            challenge.getEnemy().setLevel(response.getScriptData().getInteger("level"));

                            if (response.getScriptData().getString("imageB64") != null) {

                                //challenge.getEnemy().setImage(Base64.decode(response.getScriptData().getString("imageB64"), Base64.DEFAULT));

                                FileOutputStream fos;
                                try {
                                    fos = application.getApplicationContext().openFileOutput("enemy_image", Context.MODE_PRIVATE);
                                    fos.write(Base64.decode(response.getScriptData().getString("imageB64"), Base64.DEFAULT));
                                    fos.close();
                                    challenge.getEnemy().setUsingImage(true);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
    }

    /**
     * Builds a GS Listener to receive Challenge and Enemy Data when a Match is found.
     * Instantiates both the Enemy and the Challenge according to the User's Challenger Status.
     * Informs Activity to update UI.
     */
    private void buildMatchFoundListener() {

        GSAndroidPlatform.gs().getMessageHandler().setChallengeStartedMessageListener(new GSEventConsumer<GSMessageHandler.ChallengeStartedMessage>() {
            @Override
            public void onEvent(GSMessageHandler.ChallengeStartedMessage challengeStartedMessage) {

                destroyMatchFoundListener();
                handler.removeCallbacksAndMessages(null);
                GSTypes.Challenge GSChallenge = challengeStartedMessage.getChallenge();
                int mode = (GSChallenge.getShortCode().equals("randomNormalChal") || GSChallenge.getShortCode().equals("botNormalChal")) ? 0 : 1;

                if (GSChallenge.getChallenger().getId().equals(User.getUser().GSId)) {
                    challenge = CurrentChallenge.initialize(GSChallenge.getChallengeId(),mode,true,
                            GSChallenge.getObject("scriptData").getInteger("challengerCategory"),
                            GSChallenge.getObject("scriptData").getInteger("challengedCategory"),
                            GSChallenge.getChallenged().get(0).getId(), GSChallenge.getChallenged().get(0).getName());
                } else {
                    challenge = CurrentChallenge.initialize(GSChallenge.getChallengeId(),mode,false,
                            GSChallenge.getObject("scriptData").getInteger("challengedCategory"),
                            GSChallenge.getObject("scriptData").getInteger("challengerCategory"),
                            GSChallenge.getChallenger().getId(), GSChallenge.getChallenger().getName());
                }
                phase.setValue("FOUND");
                getEnemyData();
            }
        });
    }

    private void destroyMatchFoundListener() {
        GSAndroidPlatform.gs().getMessageHandler().setChallengeStartedMessageListener(null);
    }


    //////////////////////////
    // DATA BINDING METHODS //
    //////////////////////////

    public MutableLiveData<String> getPhase() { return phase; }

    public LiveData<Integer> getEnemyLevel() { return challenge.getEnemy().getLevel(); }

    public LiveData<Boolean> enemyIsUsingImage() { return challenge.getEnemy().isUsingImage(); }

    public LiveData<byte[]> getEnemyImage() { return challenge.getEnemy().getImage(); }

    public boolean userIsChallenger() { return challenge.userIsChallenger(); }

    public int getUserCategory() { return challenge.getUserCategory(); }

    public int getEnemyCategory() { return challenge.getEnemyCategory(); }

    public String getEnemyName() { return challenge.getEnemy().getDisplayName(); }
}
