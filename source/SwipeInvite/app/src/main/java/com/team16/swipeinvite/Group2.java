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
        group.writeToParcel(out, flags);
    }

    public int describeContents() {
        return group.describeContents();
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
        j.addString(BaasUser.current().getName());
        this.group.putArray(MEMBER_ARRAY_KEY, j);
    }
    private void initializeEventArray() {
        JsonArray j = new JsonArray();
        this.group.putArray(EVENT_ARRAY_KEY, j);
    }
    private void initializeDetailAdminArray() {
        JsonArray j = new JsonArray();
        j.addString(BaasUser.current().getName());
        this.group.putArray(DETAIL_ADMIN_ARRAY_KEY, j);
    }
    private void initializeEventAdminArray() {
        JsonArray j = new JsonArray();
        j.addString(BaasUser.current().getName());
        this.group.putArray(MEMBER_ARRAY_KEY, j);
    }
    private void initializeMemberAdminArray() {
        JsonArray j = new JsonArray();
        j.addString(BaasUser.current().getName());
        this.group.putArray(MEMBER_ARRAY_KEY, j);
    }
    //endregion


    //region Getter and setter for the BaasDocument
    protected void setBaasDocument(BaasDocument d) throws GroupException {
        if (!(d.getCollection().equals(COLLECTION_NAME))) {   //If the json object was not a group
            throw new GroupException("BaasDocument was not a group: " + d.toString());
        }
        if (isOnServer() && !d.getId().equals(getId())) {    //must be the same document to overwrite
            throw new GroupException("BaasDocument did not match id: " + d.toString());
        }
        this.group = d;
    }

    protected BaasDocument getBaasDocument() {
        return this.group;
    }
    //endregion


    //region Getter and setter with JsonObjects
    protected void setFromJson(JsonObject j) throws GroupException {
        if ((j.getString("@class") != null) && !(j.getString("@class").equals(COLLECTION_NAME))) {   //If the json object was not a group
            throw new GroupException("Json object was not a group: " + j.toString());
        }
        if (isOnServer() && !j.getString("id").equals(getId())) {    //must be the same document to overwrite
            throw new GroupException("Json object did not match id: " + j.toString());
        }
        this.group = BaasDocument.from(j);
    }

    protected JsonObject toJson() {
        return this.group.toJson();
    }
    //endregion


    //region Getter and setter for group name
    protected void setName(String name) throws GroupException {
        if (!hasDetailPermission()) throw new GroupException("User does not have detail permission.");
        group.putString(NAME_KEY, name);
    }

    protected String getName() {
        return group.getString(NAME_KEY);
    }
    //endregion


    //region Getter and setter for description
    protected void setDescription(String desc) throws GroupException {
        if (!hasDetailPermission()) throw new GroupException("User does not have detail permission.");
        group.putString(DESCRIPTION_KEY, desc);
    }

    protected String getDescription() {
        return group.getString(DESCRIPTION_KEY);
    }
    //endregion


    //region Getters and setters for privacy
    protected void setPrivate(boolean privacy) throws GroupException {
        if (!hasDetailPermission()) throw new GroupException("User does not have detail permission.");
        group.putBoolean(PRIVACY_KEY, privacy);
    }

    protected boolean isPrivate () {
        return group.getBoolean(PRIVACY_KEY);
    }
    //endregion


    //region Getter for author
    protected String getCreator() {
        return group.getAuthor();
    }
    //endregion


    //region Methods for dealing with the server ID for a specific group
    //Method to check if the current group instance is on the server
    protected boolean isOnServer() {
        if (this.group.getId() != null) {
            return true;
        }
        return false;
    }
    protected String getId() {
        if (isOnServer()) {
            return this.group.getId();
        }
        return null;
    }
    //endregion


    //region Methods for altering the members of the group
    protected void addUser(String username) throws GroupException {
        if (!hasMemberPermission()) throw new GroupException("User does not have detail permission.");
        if (containsUser(username)) return;
        JsonArray ja = this.group.getArray(MEMBER_ARRAY_KEY);
        ja.addString(username);
        this.group.putArray(MEMBER_ARRAY_KEY, ja);
    }

    protected void removeUser(String username) throws GroupException {
        if (!hasMemberPermission()) throw new GroupException("User does not have detail permission.");
        if (username.equals(getCreator())) throw new GroupException("Cannot remove the creator.");
        JsonArray ja = this.group.getArray(MEMBER_ARRAY_KEY);
        if (!(ja.contains(username))) {
            return;
        }
        int size = ja.size();
        for (int i = 0; i < size; i++) {
            if (username.equals(ja.getString(i))) {
                ja.remove(i);
            }
        }
        this.group.putArray(MEMBER_ARRAY_KEY, ja);
    }

    protected int getUserCount() {
        return this.group.getArray(MEMBER_ARRAY_KEY).size();
    }

    protected ArrayList<String> getUserList() {
        JsonArray ja = this.group.getArray(MEMBER_ARRAY_KEY);
        ArrayList<String> users = new ArrayList<String>();
        for (int i = 0; i < ja.size(); i++) {
            users.add(ja.getString(i));
        }
        return users;
    }

    protected boolean containsUser(String username) {
        return this.group.getArray(MEMBER_ARRAY_KEY).contains(username);
    }
    //endregion


    //region Methods for altering the events of the group
    protected void addEvent(String eventID) throws GroupException {
        if (!hasEventPermission()) throw new GroupException("User does not have detail permission.");
        if (containsEvent(eventID)) return;
        JsonArray ja = this.group.getArray(EVENT_ARRAY_KEY);
        ja.addString(eventID);
        this.group.putArray(EVENT_ARRAY_KEY, ja);
    }

    protected void removeEvent(String eventID) throws GroupException {
        if (!hasMemberPermission()) throw new GroupException("User does not have detail permission.");
        if (!containsEvent(eventID)) {
            return;
        }
        JsonArray ja = this.group.getArray(EVENT_ARRAY_KEY);
        int size = ja.size();
        for (int i = 0; i < size; i++) {
            if (ja.getString(i).equals(eventID)) {
                ja.remove(i);
            }
        }
        this.group.putArray(EVENT_ARRAY_KEY, ja);
    }

    protected int getEventCount() {
        return this.group.getArray(EVENT_ARRAY_KEY).size();
    }

    protected ArrayList<String> getEventList() {
        JsonArray ja = this.group.getArray(EVENT_ARRAY_KEY);
        ArrayList<String> events = new ArrayList<String>();
        for (int i = 0; i < ja.size(); i++) {
            events.add(ja.getString(i));
        }
        return events;
    }

    protected boolean containsEvent(String eventID) {
        return this.group.getArray(EVENT_ARRAY_KEY).contains(eventID);
    }
    //endregion


    //region Methods to manage the Detail Admins
    protected void promoteToDetailAdmin(String username) throws GroupException {
        if (!hasTotalPermission()) throw new GroupException("User does not have total permission.");
        if (!containsUser(username) || containsDetailAdmin(username)) {
            return;
        }
        JsonArray ja = this.group.getArray(DETAIL_ADMIN_ARRAY_KEY);
        ja.addString(username);
        this.group.putArray(DETAIL_ADMIN_ARRAY_KEY, ja);
    }

    protected void demoteFromDetailAdmin(String username) throws GroupException {
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
            }
        }
        this.group.putArray(DETAIL_ADMIN_ARRAY_KEY, ja);
    }

    protected int getDetailAdminCount() {
        return this.group.getArray(DETAIL_ADMIN_ARRAY_KEY).size();
    }

    protected boolean containsDetailAdmin(String username) {
        return this.group.getArray(DETAIL_ADMIN_ARRAY_KEY).contains(username);
    }
    //endregion


    //region Methods to manage the Member Admins
    protected void promoteToMemberAdmin(String username) throws GroupException {
        if (!hasTotalPermission()) throw new GroupException("User does not have total permission.");
        if (!containsUser(username) || containsMemberAdmin(username)) {
            return;
        }
        JsonArray ja = this.group.getArray(MEMBER_ADMIN_ARRAY_KEY);
        ja.addString(username);
        this.group.putArray(MEMBER_ADMIN_ARRAY_KEY, ja);
    }

    protected void demoteFromMemberAdmin(String username) throws GroupException {
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
            }
        }
        this.group.putArray(MEMBER_ADMIN_ARRAY_KEY, ja);
    }

    protected int getMemberAdminCount() {
        return this.group.getArray(MEMBER_ADMIN_ARRAY_KEY).size();
    }

    protected boolean containsMemberAdmin(String username) {
        return this.group.getArray(MEMBER_ADMIN_ARRAY_KEY).contains(username);
    }
    //endregion


    //region Methods to manage the Event Admins
    protected void promoteToEventAdmin(String username) throws GroupException {
        if (!hasTotalPermission()) throw new GroupException("User does not have total permission.");
        if (!containsUser(username) || containsEventAdmin(username)) {
            return;
        }
        JsonArray ja = this.group.getArray(EVENT_ADMIN_ARRAY_KEY);
        ja.addString(username);
        this.group.putArray(EVENT_ADMIN_ARRAY_KEY, ja);
    }

    protected void demoteFromEventAdmin(String username) throws GroupException {
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
            }
        }
        this.group.putArray(EVENT_ADMIN_ARRAY_KEY, ja);
    }

    protected int getEventAdminCount() {
        return this.group.getArray(EVENT_ADMIN_ARRAY_KEY).size();
    }

    protected boolean containsEventAdmin(String username) {
        return this.group.getArray(EVENT_ADMIN_ARRAY_KEY).contains(username);
    }
    //endregion


    //region Methods to check permissions of a user
    protected boolean hasDetailPermission() {
        return containsDetailAdmin(BaasUser.current().getName());
    }
    protected boolean hasMemberPermission() {
        return containsMemberAdmin(BaasUser.current().getName());
    }
    protected boolean hasEventPermission() {
        return containsEventAdmin(BaasUser.current().getName());
    }
    protected boolean hasTotalPermission() {
        return (hasDetailPermission() && hasMemberPermission() && hasEventPermission());
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
