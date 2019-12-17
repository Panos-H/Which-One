package com.panos.oddandroid.model;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {CompleteChallenge.class}, version = 1)
public abstract class CompleteChallengeDatabase extends RoomDatabase {

    public abstract CompleteChallengeDao challengeDao();

    private static volatile CompleteChallengeDatabase instance;

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final CompleteChallengeDao dao;

        PopulateDbAsync(CompleteChallengeDatabase db) {
            dao = db.challengeDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            dao.deleteAll();
            /*
            CompleteChallenge word = new CompleteChallenge("Hello",0,0,0,0,0,0);
            dao.insert(word);
            word = new CompleteChallenge("World",0,0,0,0,0,0);
            dao.insert(word);
            */
            return null;
        }
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback(){

        @Override
        public void onOpen (@NonNull SupportSQLiteDatabase db){
            super.onOpen(db);
            new PopulateDbAsync(instance).execute();
        }
    };

    static CompleteChallengeDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (CompleteChallengeDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                            CompleteChallengeDatabase.class,"challenge_database")
                            .fallbackToDestructiveMigration()
                            //.addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return instance;
    }
}
