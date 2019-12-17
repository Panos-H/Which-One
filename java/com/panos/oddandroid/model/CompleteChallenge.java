package com.panos.oddandroid.model;

import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Represents a completed Challenge.
 */
@Entity(tableName = "challenge_table")
public class CompleteChallenge {

    @PrimaryKey
    @NonNull
    private String id;

    @ColumnInfo(name = "step")
    private int step;

    @ColumnInfo(name = "user_category")
    private int userCategory;

    @ColumnInfo(name = "user_score")
    private int userScore;

    @ColumnInfo(name = "enemy_category")
    private int enemyCategory;

    @ColumnInfo(name = "enemy_score")
    private int enemyScore;

    @ColumnInfo(name = "result")
    private int result;

    //@ColumnInfo(name = "enemy")
    //private Enemy enemy;

    CompleteChallenge(@NonNull String id, int step, int userCategory, int enemyCategory, int userScore, int enemyScore, int result) {

        this.id = id;
        this.step = step;
        this.userCategory = userCategory;
        this.enemyCategory = enemyCategory;
        this.userScore = userScore;
        this.enemyScore = enemyScore;
        this.result = result;
        //this.enemy = enemy;
    }

    public String getId() { return id; }

    public int getStep() { return step; }

    public int getUserCategory() { return userCategory; }

    public int getUserScore() { return userScore; }

    public int getEnemyCategory() { return enemyCategory; }

    public int getEnemyScore() { return enemyScore; }

    public int getResult() { return result; }

    /**
     * Returns a Color depending on Challenge's Result.
     * @return The Color integer.
     */
    public int getResultColor() {
        switch (result) {
            case 0: return Color.GREEN;
            case 1: return Color.RED;
            default: return Color.BLUE;
        }
    }

    //public Enemy getEnemy() { return enemy; }
}
