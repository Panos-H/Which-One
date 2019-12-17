package com.panos.oddandroid.model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.panos.oddandroid.model.CompleteChallenge;

import java.util.List;

@Dao
public interface CompleteChallengeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CompleteChallenge challenge);

    @Query("DELETE FROM challenge_table")
    void deleteAll();

    @Query("SELECT * FROM challenge_table")
    LiveData<List<CompleteChallenge>> getAllChallenges();
}
