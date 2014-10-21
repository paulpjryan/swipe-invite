package com.team16.swipeinvite;

import com.baasbox.android.BaasDocument;
import com.baasbox.android.json.JsonArray;
import com.baasbox.android.json.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by kylekrynski on 10/20/14.
 */
public class Group2 {
    //region Constant keys for the Json data contained in the BaasDocument
    private static final String COLLECTION_NAME = "group";
    private static final String NAME_KEY = "name";
    private static final String DESCRIPTION_KEY = "description";
    private static final String PRIVACY_KEY = "privacy";
    private static final String MEMBER_ARRAY_KEY = "members";
    private static final String EVENT_ARRAY_KEY = "events";
    //endregion


    //region Instance variables for a group
    private BaasDocument group;
    //endregion


    //region Constructors for the group class
    //Create a brand new group object from scratch
    public Group2(String name, String description, boolean privacy, String creatorName) {
        this.group = new BaasDocument(COLLECTION_NAME);
        setName(name);
        setDescription(description);
        setPrivate(privacy);
        initializeMemberArray();
        initializeEventArray();
        addUser(creatorName);
    }
    //Create a brand new group object from an existing BaasDocument
    public Group2(BaasDocument d) throws GroupException {
        setBaasDocument(d);
    }
    //Create a brand new group object from a Json object
    public Group2(JsonObject j) throws GroupException  {
        setFromJson(j);
    }
    //endregion


    //region Methods to initialize the arrays upon creation of new group
    private void initializeMemberArray() {
        JsonArray j = new JsonArray();
        this.group.putArray(MEMBER_ARRAY_KEY, j);
    }
    private void initializeEventArray() {
        JsonArray j = new JsonArray();
        this.group.putArray(EVENT_ARRAY_KEY, j);
    }
    //endregion


    //region Getter and setter for the BaasDocument
    public void setBaasDocument(BaasDocument d) throws GroupException {
        if (!(d.getCollection().equals(COLLECTION_NAME))) {   //If the json object was not a group
            throw new GroupException("BaasDocument was not a group: " + d.toString());
        }
        this.group = d;
    }

    public BaasDocument getBaasDocument() {
        return this.group;
    }
    //endregion


    //region Getter and setter with JsonObjects
    public void setFromJson(JsonObject j) throws GroupException {
        if (!(j.getString("@class").equals(COLLECTION_NAME))) {   //If the json object was not a group
            throw new GroupException("Json object was not a group: " + j.toString());
        }
        this.group = BaasDocument.from(j);
    }

    public JsonObject toJson() {
        return this.group.toJson();
    }
    //endregion


    //region Getter and setter for group name
    public void setName(String name) {
        group.putString(NAME_KEY, name);
    }

    public String getName() {
        return group.getString(NAME_KEY);
    }
    //endregion


    //region Getter and setter for description
    public void setDescription(String desc) {
        group.putString(DESCRIPTION_KEY, desc);
    }

    public String getDescription() {
        return group.getString(DESCRIPTION_KEY);
    }
    //endregion


    //region Getters and setters for privacy
    public void setPrivate(boolean privacy) {
        group.putBoolean(PRIVACY_KEY, privacy);
    }

    public boolean isPrivate () {
        return group.getBoolean(PRIVACY_KEY);
    }
    //endregion


    //region Getter for author
    public String getCreator() {
        return group.getAuthor();
    }
    //endregion


    //region Methods for altering the members of the group
    public void addUser(String username) {
        JsonArray ja = this.group.getArray(MEMBER_ARRAY_KEY);
        ja.addString(username);
        this.group.putArray(MEMBER_ARRAY_KEY, ja);
    }

    public void removeUser(String username) {
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

    public boolean containsUser(String username) {
        return this.group.getArray(MEMBER_ARRAY_KEY).contains(username);
    }
    //endregion


    public void addEvent() {

    }


    //region Nested class for group exceptions to be thrown
    public class GroupException extends RuntimeException {

        public GroupException(String message) {
            super(message);
        }

    }
    //endregion

}
