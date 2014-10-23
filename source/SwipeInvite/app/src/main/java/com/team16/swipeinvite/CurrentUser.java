package com.team16.swipeinvite;

import android.os.Parcel;
import android.os.Parcelable;

import com.baasbox.android.BaasDocument;
import com.baasbox.android.BaasUser;

/**
 * Created by kylekrynski on 10/22/14.
 */
class CurrentUser implements Parcelable {
    //region Constant keys for json data contained in user object
    private static final String COMMON_NAME_KEY =  "common_name";
    private static final String EMAIL_KEY = "email";
    private static final String GENDER_KEY = "gender";
    //endregion


    //region Local instance variables for user class
    private BaasUser user;
    //endregion


    //region Methods for Parcelable interface
    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(user, flags);
    }

    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<CurrentUser> CREATOR
            = new Parcelable.Creator<CurrentUser>() {
        public CurrentUser createFromParcel(Parcel in) {
            return new CurrentUser(in);
        }

        public CurrentUser[] newArray(int size) {
            return new CurrentUser[size];
        }
    };

    private CurrentUser(Parcel in) {
        user = in.readParcelable(BaasUser.class.getClassLoader());
    }
    //endregion


    //region Constructors
    //Create a new BaasUser completely from scratch - should only be used for a new user
    public CurrentUser(String username, String password) {
        this.user = BaasUser.withUserName(username);
        this.user.setPassword(password);
    }
    //Create a user object from a server pulldown in which you dont already have an instance
    public CurrentUser(BaasUser u) {
        this.user = u;
    }
    //endregion


    //region Getter and setter from a BaasUser object
    protected BaasUser getBaasUser() {
        return this.user;
    }
    //endregion


    //region Getter for username
    protected String getUsername() {
        return this.user.getName();
    }
    //endregion


    //region Getter and setter for common name of user
    //SET IN THE REGISTERED SCOPE OF A USER OBJECT
    protected void setCommonName(String name) {
        this.user.getScope(BaasUser.Scope.REGISTERED).putString(COMMON_NAME_KEY, name);
    }
    protected String getCommonName() {
        return this.user.getScope(BaasUser.Scope.REGISTERED).getString(COMMON_NAME_KEY);
    }
    //endregion


    //region Getter and setter for the user email
    //SET IN THE PROVATE SCOPE OF A USER OBJECT
    protected void setEmail(String email) {
        this.user.getScope(BaasUser.Scope.PRIVATE).putString(EMAIL_KEY, email);
    }
    protected String getEmail() {
        return this.user.getScope(BaasUser.Scope.PRIVATE).getString(EMAIL_KEY);
    }
    //endregion


    //region Getter and setter for user gender
    protected void setMale(boolean male) {
        this.user.getScope(BaasUser.Scope.REGISTERED).putBoolean(GENDER_KEY, male);
    }
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