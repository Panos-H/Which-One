package com.panos.oddandroid.model;

/**
 * Singleton representing User entity.
 */
public class User extends Player {

    /**
     * User's display level used on UI to represent User's actual level. Every time
     * the User gains a level, it increases to match the actual XP.
     */
    int transientLevel;
    /**
     * User's actual experience points in float. Starting from level 1 and each level thereafter
     * it requires 1.0 XP point, starting from 0.0, to gain a level. XP is gained after each Match
     * and it is calculated based on User's achieved Score.
     */
    float xp;
    /**
     * User's display experience points used on UI to represent User's actual XP. Every time
     * the User gains some XP, it increases through an animation to match the actual XP.
     */
    float transientXp;

    /**
     * One and only User Object.
     */
    private static User instance;

    /**
     * Passes User's given information to Constructor and returns the created User instance.
     * Called only on App's startup.
     * @param GSId User's Game Sparks ID.
     * @param displayName User's display name.
     * @param level User's actual level.
     * @param xp User's actual XP.
     * @return The single User instance.
     */
    public static User initialize(String GSId, String displayName, boolean usingImage, int level, float xp) {

        instance = new User(GSId, displayName, usingImage, level, xp);
        return instance;
    }

    /**
     * Provides reference to User Instance after its creation.
     * @return User Instance.
     */
    public static User getUser() {
        return instance;
    }

    /**
     * Initializes User information.
     * @param GSId User's Game Sparks ID.
     * @param displayName User's display name.
     * @param level User's actual level.
     * @param xp User's actual XP.
     */
    private User(String GSId, String displayName, boolean usingImage, int level, float xp) {
        super(GSId, displayName, usingImage, level);

        this.xp = xp;
        transientXp = xp;
        transientLevel = level;
    }

    /**
     * Returns User's UI level.
     * @return User's UI level.
     */
    public int getTransientLevel() { return transientLevel; }

    /**
     * Returns User's actual experience points.
     * @return User's actual experience points.
     */
    public float getXp() { return xp; }

    /**
     * Returns User's UI experience points.
     * @return User's UI experience points.
     */
    public float getTransientXp() { return transientXp; }

    public void setTransientLevel(int transientLevel) {
        this.transientLevel = transientLevel;
    }

    public void setXp(float xp) {
        this.xp = xp;
    }

    public void setTransientXp(float transientXp) {
        this.transientXp = transientXp;
    }
}