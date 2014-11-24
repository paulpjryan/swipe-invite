package com.team16.swipeinvite;

import android.os.Parcel;
import android.os.Parcelable;

import com.baasbox.android.BaasDocument;
import com.baasbox.android.BaasUser;
import com.baasbox.android.json.JsonArray;
import com.baasbox.android.json.JsonObject;

import java.util.ArrayList;


/**
 * Created by kylekrynski on 10/20/14.
 * DO NOT MAKE PUBLIC CLASS
 * MOST DATA METHODS SHOULD BE PROTECTED
 */
class Group2 implements Parcelable {
    //region Constant keys for the Json data contained in the BaasDocument
    private static final String COLLECTION_NAME = "group";
    private static final String NAME_KEY = "name";
    private static final String DESCRIPTION_KEY = "description";
    private static final String PRIVACY_KEY = "privacy";
    private static final String MEMBER_ARRAY_KEY = "members";
    private static final String EVENT_ARRAY_KEY = "events";
    private static final String DETAIL_ADMIN_ARRAY_KEY = "detail_admins";
    private static final String MEMBER_ADMIN_ARRAY_KEY = "member_admins";
    private static final String EVENT_ADMIN_ARRAY_KEY = "event_admins";
    //endregion


    //region Methods for Parcelable interface
    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(group, flags);
    }

    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Group2> CREATOR
            = new Parcelable.Creator<Group2>() {
        public Group2 createFromParcel(Parcel in) {
            return new Group2(in);
        }

        public Group2[] newArray(int size) {
            return new Group2[size];
        }
    };

    private Group2(Parcel in) {
        group = in.readParcelable(BaasDocument.class.getClassLoader());
    }

    //endregion


    //region Instance variables for a group
    private BaasDocument group;
    //endregion


    //region Constructors for the group class
    //Create a brand new group object from scratch
    protected Group2(String name, String description, boolean privacy) {
        this.group = new BaasDocument(COLLECTION_NAME);
        initializeMemberArray();
        initializeEventArray();
        initializeDetailAdminArray();
        initializeEventAdminArray();
        initializeMemberAdminArray();
        setName(name);
        setDescription(description);
        setPrivate(privacy);
    }
    //Create a brand new group object from an existing BaasDocument
    protected Group2(BaasDocument d) throws GroupException {
        setBaasDocument(d);
    }
    //Create a brand new group object from a Json object
    protected Group2(JsonObject j) throws GroupException  {
        setFromJson(j);
    }
    //endregion


    //region Methods to initialize the arrays upon creation of new group
    private void initializeMemberArray() {
        JsonArray j = new JsonArray();
        j.add(BaasUser.current().getName());
        this.group.put(MEMBER_ARRAY_KEY, j);
    }
    private void initializeEventArray() {
        JsonArray j = new JsonArray();
        this.group.put(EVENT_ARRAY_KEY, j);
    }
    private void initializeDetailAdminArray() {
        JsonArray j = new JsonArray();
        j.add(BaasUser.current().getName());
        this.group.put(DETAIL_ADMIN_ARRAY_KEY, j);
    }
    private void initializeEventAdminArray() {
        JsonArray j = new JsonArray();
        j.add(BaasUser.current().getName());
        this.group.put(EVENT_ADMIN_ARRAY_KEY, j);
    }
    private void initializeMemberAdminArray() {
        JsonArray j = new JsonArray();
        j.add(BaasUser.current().getName());
        this.group.put(MEMBER_ADMIN_ARRAY_KEY, j);
    }
    //endregion


    //region Getter and setter for the BaasDocument
    protected synchronized void setBaasDocument(BaasDocument d) throws GroupException {
        if (!(d.getCollection().equals(COLLECTION_NAME))) {   //If the json object was not a group
            throw new GroupException("BaasDocument was not a group: " + d.toString());
        }
        if (isOnServer() && !d.getId().equals(getId())) {    //must be the same document to overwrite
            throw new GroupException("BaasDocument did not match id: " + d.toString());
        }
        this.group = d;
    }

    protected synchronized BaasDocument getBaasDocument() {
        return this.group;
    }
    //endregion


    //region Getter and setter with JsonObjects
    protected synchronized void setFromJson(JsonObject j) throws GroupException {
        if ((j.getString("@class") != null) && !(j.getString("@class").equals(COLLECTION_NAME))) {   //If the json object was not a group
            throw new GroupException("Json object was not a group: " + j.toString());
        }
        if (isOnServer() && !j.getString("id").equals(getId())) {    //must be the same document to overwrite
            throw new GroupException("Json object did not match id: " + j.toString());
        }
        this.group = BaasDocument.from(j);
    }

    protected synchronized JsonObject toJson() {
        return this.group.toJson();
    }
    //endregion


    //region Getter and setter for group name
    protected synchronized void setName(String name) throws GroupException {
        if (!hasDetailPermission()) throw new GroupException("User does not have detail permission.");
        group.put(NAME_KEY, name);
    }

    protected synchronized String getName() {
        return group.getString(NAME_KEY);
    }
    //endregion


    //region Getter and setter for description
    protected synchronized void setDescription(String desc) throws GroupException {
        if (!hasDetailPermission()) throw new GroupException("User does not have detail permission.");
        group.put(DESCRIPTION_KEY, desc);
    }

    protected synchronized String getDescription() {
        return group.getString(DESCRIPTION_KEY);
    }
    //endregion


    //region Getters and setters for privacy
    protected synchronized void setPrivate(boolean privacy) throws GroupException {
        if (!hasDetailPermission()) throw new GroupException("User does not have detail permission.");
        group.put(PRIVACY_KEY, privacy);
    }

    protected synchronized boolean isPrivate () {
        return group.getBoolean(PRIVACY_KEY);
    }
    //endregion


    //region Getter for author
    protected synchronized String getCreator() {
        return group.getAuthor();
    }
    //endregion


    //region Methods for dealing with the server ID for a specific group
    //Method to check if the current group instance is on the server
    protected synchronized boolean isOnServer() {
        if (this.group != null && this.group.getId() != null) {
            return true;
        }
        return false;
    }
    protected synchronized String getId() {
        if (isOnServer()) {
            return this.group.getId();
        }
        return null;
    }
    //endregion


    //region Methods for altering the members of the group
    protected synchronized void addUser(String username) throws GroupException {
        if (!hasMemberPermission()) throw new GroupException("User does not have member permission.");
        if (containsUser(username)) return;
        JsonArray ja = this.group.getArray(MEMBER_ARRAY_KEY);
        ja.add(username);
        this.group.put(MEMBER_ARRAY_KEY, ja);
    }

    protected synchronized void removeUser(String username) throws GroupException {
        if (!hasMemberPermission()) throw new GroupException("User does not have member permission.");
        if (username.equals(getCreator())) throw new GroupException("Cannot remove the creator.");
        //If user is an admin, try to demote
        demoteFromDetailAdmin(username);
        demoteFromEventAdmin(username);
        demoteFromMemberAdmin(username);
        //Removing user
        JsonArray ja = this.group.getArray(MEMBER_ARRAY_KEY);
        if (!(ja.contains(username))) {
            return;
        }
        int size = ja.size();
        for (int i = 0; i < size; i++) {
            if (username.equals(ja.getString(i))) {
                ja.remove(i);
                break;
            }
        }
        this.group.put(MEMBER_ARRAY_KEY, ja);
    }

    protected synchronized void removeSelf() throws GroupException{
        if (BaasUser.current().getName().equals(getCreator())) throw new GroupException("Creator cannot remove themself.");
        //TODO private functions for removal without checks
    }

    protected synchronized int getUserCount() {
        return this.group.getArray(MEMBER_ARRAY_KEY).size();
    }

    protected synchronized ArrayList<String> getUserList() {
        JsonArray ja = this.group.getArray(MEMBER_ARRAY_KEY);
        ArrayList<String> users = new ArrayList<String>();
        for (int i = 0; i < ja.size(); i++) {
            users.add(ja.getString(i));
        }
        return users;
    }

    protected synchronized boolean containsUser(String username) {
        return this.group.getArray(MEMBER_ARRAY_KEY).contains(username);
    }
    //endregion


    //region Methods for altering the events of the group
    protected synchronized void addEvent(String eventID) throws GroupException {
        if (!hasEventPermission()) throw new GroupException("User does not have detail permission.");
        if (containsEvent(eventID)) return;
        JsonArray ja = this.group.getArray(EVENT_ARRAY_KEY);
        ja.add(eventID);
        this.group.put(EVENT_ARRAY_KEY, ja);
    }

    protected synchronized void removeEvent(String eventID) throws GroupException {
        if (!hasMemberPermission()) throw new GroupException("User does not have detail permission.");
        if (!containsEvent(eventID)) {
            return;
        }
        JsonArray ja = this.group.getArray(EVENT_ARRAY_KEY);
        int size = ja.size();
        for (int i = 0; i < size; i++) {
            if (ja.getString(i).equals(eventID)) {
                ja.remove(i);
                break;
            }
        }
        this.group.put(EVENT_ARRAY_KEY, ja);
    }

    protected synchronized int getEventCount() {
        return this.group.getArray(EVENT_ARRAY_KEY).size();
    }

    protected synchronized ArrayList<String> getEventList() {
        JsonArray ja = this.group.getArray(EVENT_ARRAY_KEY);
        ArrayList<String> events = new ArrayList<String>();
        for (int i = 0; i < ja.size(); i++) {
            events.add(ja.getString(i));
        }
        return events;
    }

    protected synchronized boolean containsEvent(String eventID) {
        return this.group.getArray(EVENT_ARRAY_KEY).contains(eventID);
    }
    //endregion


    //region Methods to manage the Detail Admins
    protected synchronized void promoteToDetailAdmin(String username) throws GroupException {
        if (!hasTotalPermission()) throw new GroupException("User does not have total permission.");
        if (!containsUser(username) || containsDetailAdmin(username)) {
            return;
        }
        JsonArray ja = this.group.getArray(DETAIL_ADMIN_ARRAY_KEY);
        ja.add(username);
        this.group.put(DETAIL_ADMIN_ARRAY_KEY, ja);
    }

    protected synchronized void demoteFromDetailAdmin(String username) throws GroupException {
        if (!hasTotalPermission()) throw new GroupException("User does not have total permission.");
        if (username.equals(getCreator())) throw new GroupException("Cannot demote the creator.");
        if (!containsDetailAdmin(username)) {
            return;
        }
        JsonArray ja = this.group.getArray(DETAIL_ADMIN_ARRAY_KEY);
        int size = ja.size();
        for (int i = 0; i < size; i++) {
            if (ja.getString(i).equals(username)) {
                ja.remove(i);
                break;
            }
        }
        this.group.put(DETAIL_ADMIN_ARRAY_KEY, ja);
    }

    protected synchronized int getDetailAdminCount() {
        return this.group.getArray(DETAIL_ADMIN_ARRAY_KEY).size();
    }

    protected synchronized boolean containsDetailAdmin(String username) {
        return this.group.getArray(DETAIL_ADMIN_ARRAY_KEY).contains(username);
    }
    //endregion


    //region Methods to manage the Member Admins
    protected synchronized void promoteToMemberAdmin(String username) throws GroupException {
        if (!hasTotalPermission()) throw new GroupException("User does not have total permission.");
        if (!containsUser(username) || containsMemberAdmin(username)) {
            return;
        }
        JsonArray ja = this.group.getArray(MEMBER_ADMIN_ARRAY_KEY);
        ja.add(username);
        this.group.put(MEMBER_ADMIN_ARRAY_KEY, ja);
    }

    protected synchronized void demoteFromMemberAdmin(String username) throws GroupException {
        if (!hasTotalPermission()) throw new GroupException("User does not have total permission.");
        if (username.equals(getCreator())) throw new GroupException("Cannot demote the creator.");
        if (!containsMemberAdmin(username)) {
            return;
        }
        JsonArray ja = this.group.getArray(MEMBER_ADMIN_ARRAY_KEY);
        int size = ja.size();
        for (int i = 0; i < size; i++) {
            if (ja.getString(i).equals(username)) {
                ja.remove(i);
                break;
            }
        }
        this.group.put(MEMBER_ADMIN_ARRAY_KEY, ja);
    }

    protected synchronized int getMemberAdminCount() {
        return this.group.getArray(MEMBER_ADMIN_ARRAY_KEY).size();
    }

    protected synchronized boolean containsMemberAdmin(String username) {
        return this.group.getArray(MEMBER_ADMIN_ARRAY_KEY).contains(username);
    }
    //endregion


    //region Methods to manage the Event Admins
    protected synchronized void promoteToEventAdmin(String username) throws GroupException {
        if (!hasTotalPermission()) throw new GroupException("User does not have total permission.");
        if (!containsUser(username) || containsEventAdmin(username)) {
            return;
        }
        JsonArray ja = this.group.getArray(EVENT_ADMIN_ARRAY_KEY);
        ja.add(username);
        this.group.put(EVENT_ADMIN_ARRAY_KEY, ja);
    }

    protected synchronized void demoteFromEventAdmin(String username) throws GroupException {
        if (!hasTotalPermission()) throw new GroupException("User does not have total permission.");
        if (username.equals(getCreator())) throw new GroupException("Cannot demote the creator.");
        if (!containsEventAdmin(username)) {
            return;
        }
        JsonArray ja = this.group.getArray(EVENT_ADMIN_ARRAY_KEY);
        int size = ja.size();
        for (int i = 0; i < size; i++) {
            if (ja.getString(i).equals(username)) {
                ja.remove(i);
                break;
            }
        }
        this.group.put(EVENT_ADMIN_ARRAY_KEY, ja);
    }

    protected synchronized int getEventAdminCount() {
        return this.group.getArray(EVENT_ADMIN_ARRAY_KEY).size();
    }

    protected synchronized boolean containsEventAdmin(String username) {
        return this.group.getArray(EVENT_ADMIN_ARRAY_KEY).contains(username);
    }
    //endregion


    //region Methods to check permissions of a user
    protected synchronized boolean hasDetailPermission() {
        return containsDetailAdmin(BaasUser.current().getName());
    }
    protected synchronized boolean hasMemberPermission() {
        return containsMemberAdmin(BaasUser.current().getName());
    }
    protected synchronized boolean hasEventPermission() {
        return containsEventAdmin(BaasUser.current().getName());
    }
    protected synchronized boolean hasTotalPermission() {
        return (hasDetailPermission() && hasMemberPermission() && hasEventPermission());
    }
    //endregion


    //region Methods for checking equality
    protected synchronized boolean equals(Group2 other) {
        return equals(other.getId());
    }

    protected synchronized boolean equals(BaasDocument other) {
        if (!other.getCollection().equals(COLLECTION_NAME)) {
            return false;
        }
        return equals(other.getId());
    }

    protected synchronized boolean equals(String id) {
        try {
            return this.getId().equals(id);
        } catch (NullPointerException e) {
            return false;
        }
    }
    //endregion


    //region Nested class for group exceptions to be thrown
    protected class GroupException extends RuntimeException {

        public GroupException(String message) {
            super(message);
        }

    }
    //endregion

}
