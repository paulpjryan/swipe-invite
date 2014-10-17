package com.team16.swipeinvite;


import java.util.ArrayList;
import java.util.Date;
import java.util.Observable;


public class Group extends Observable{
    User creator;
    String groupname;
    String description;
    ArrayList<User> members = new ArrayList<User>();
    ArrayList<Events> events = new ArrayList<Events>();
    int frndcount = 0;
    int eventcount = 0;
    boolean isprivate;

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

}

