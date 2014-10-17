package com.team16.swipeinvite;


import com.baasbox.android.BaasDocument;

import java.util.ArrayList;
import java.util.Date;
import java.util.Observable;
import android.util.Log;
import com.baasbox.android.BaasUser;


public class Group extends Observable{
    User creator;
    String groupname;
    String description;
    ArrayList<User> members = new ArrayList<User>();
    ArrayList<Events> events = new ArrayList<Events>();
    int frndcount = 0;
    int eventcount = 0;
    boolean isprivate;

    BaasDocument groupDoc;

    public Group(User creat, String group, boolean priv) {
        this.creator = creat;
        this.isprivate = priv;
        this.groupname = group;

    }

    public void createEvent(User creat, String eventnm, String Desc, Date begdate, Date enddte) {
        Events temp = new Events(creat, eventnm, Desc, begdate, enddte);
        events.add(temp);
        eventcount++;

    }

    public void removeEvent(int i) {
        events.remove(i);
        eventcount--;

    }

    public void setDescription(String desc) {
        this.description = desc;

    }

    public void removeEvent(Events event) {
        events.remove(event);
        eventcount--;

    }

    public void addUser(User frnd) {
        members.add(frnd);
        frndcount++;

        setChanged();
        notifyObservers();

    }

    public void removeUser(User frnd) {
        members.remove(frnd);
        frndcount--;

    }

    public void removeUser(int i) {
        members.remove(i);
        frndcount--;

    }

    public int getFriendCount() {
        return frndcount;

    }

    public int getEventCount() {
        return eventcount;

    }

    public void setGroupname(String newname) {
        this.groupname = newname;

    }

    //Method to store a Baas document of representation inside this group
    public void setBaasGroup(BaasDocument d) {
        //Checking if this is the correct type of document
        if (!d.getCollection().equals("group")) {
            Log.d("LOG", "Incorrect document type trying to write to group.");
            return;
        }

        //Setting document locally and inflating properties
        this.groupDoc = d;
        String authorname = d.getAuthor();
        BaasUser author = BaasUser.withUserName(authorname);
        User auth = new User();
        auth.unWrapUser(author);
        this.creator = auth;
        this.groupname = d.getString("name");
        this.description = d.getString("description");
        this.frndcount = d.getInt("frndcount");
        this.eventcount = d.getInt("eventcount");

        return;
    }

    //Method to get the BaasGroup representation of this object
    public BaasDocument getBaasGroup() {
        //Pushing the local data to a server object
        groupDoc.putString("name", this.groupname);
        groupDoc.putString("description", this.description);
        groupDoc.putLong("frndcount", this.frndcount);
        groupDoc.putLong("eventcount", this.eventcount);

        return groupDoc;
    }

}

