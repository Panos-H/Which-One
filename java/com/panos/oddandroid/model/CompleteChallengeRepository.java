package com.panos.oddandroid.model;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

public class CompleteChallengeRepository {

    private CompleteChallengeDao completeChallengeDao;
    private LiveData<List<CompleteChallenge>> allChallenges;

    private static class insertAsyncTask extends AsyncTask<CompleteChallenge,Void,Void> {

        private CompleteChallengeDao dao;

        insertAsyncTask(CompleteChallengeDao dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(final CompleteChallenge... completeChallenges) {
            dao.insert(completeChallenges[0]);
            return null;
        }
    }

    private static class deleteAllAsyncTask extends AsyncTask<Void,Void,Void> {

        private CompleteChallengeDao dao;

        deleteAllAsyncTask(CompleteChallengeDao dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            dao.deleteAll();
            return null;
        }
    }

    public CompleteChallengeRepository(Application application) {
        CompleteChallengeDatabase db = CompleteChallengeDatabase.getInstance(application);
        completeChallengeDao = db.challengeDao();
        allChallenges = completeChallengeDao.getAllChallenges();
    }

    public LiveData<List<CompleteChallenge>> getAllChallenges() {
        return allChallenges;
    }

    public void insert(CompleteChallenge challenge) {
        new insertAsyncTask(completeChallengeDao).execute(challenge);
    }

    public void deleteAllChallenges() {
        new deleteAllAsyncTask(completeChallengeDao).execute();
    }
}
