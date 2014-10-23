package com.team16.swipeinvite;

import com.baasbox.android.BaasUser;

/**
 * Created by kylekrynski on 10/22/14.
 */
class Acquaintence {
    //region Constant keys for json data contained in user object
    private static final String COMMON_NAME_KEY =  "common_name";
    private static final String GENDER_KEY = "gender";
    //endregion


    //region Local instance variables for user class
    private BaasUser user;
    //endregion


    //region Constructors
    //Create a user object from a server pulldown in which you dont already have an instance
    public Acquaintence(BaasUser u) {
        this.user = u;
    }
    //endregion


    //region Getter and setter from a BaasUser object
    protected void setBaasUser(BaasUser u) throws UserException {
        if (!u.getName().equals(user.getName())) throw new UserException("Incorrect user save.");
        this.user = u;
    }

    protected BaasUser getBaasUser() {
        return this.user;
    }
    //endregion


    //region Getter for common name of user
    //SET IN THE REGISTERED SCOPE OF A USER OBJECT
    protected String getCommonName() {
        return this.user.getScope(BaasUser.Scope.REGISTERED).getString(COMMON_NAME_KEY);
    }
    //endregion


    //region Getter for user gender
    protected boolean isMale() {
        return this.user.getScope(BaasUser.Scope.REGISTERED).getBoolean(GENDER_KEY);
    }
    //endregion


    //region Checks if this user object is currently logged in
    protected boolean isCurrentUser() {
        return this.user.isCurrent();
    }
    //endregion


    //region Nested class for user exceptions
    protected class UserException extends RuntimeException {
        public UserException(String message) {
            super(message);
        }
    }
    //endregion

}