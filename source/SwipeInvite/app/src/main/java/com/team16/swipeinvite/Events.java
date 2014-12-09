package com.team16.swipeinvite;

import java.util.ArrayList;
import java.util.Date;

public class Events {

    User creator;
    String eventName;
    String description;
    ArrayList<User> administrators = new ArrayList<User>();
    Date beginDate;
    Date endDate;
    ArrayList<User> invites = new ArrayList<User>();
    int numGuests = 0;
    Group parentGroup;

    public Events(User creator, String eventName, String description, Date beginDate, Date endDate) {
        this.creator = creator;
        this.eventName = eventName;
        this.description = description;
        this.beginDate = beginDate;
        this.endDate = endDate;

        for (int i = 0; i < parentGroup.members.size(); i++)
            this.invites.add(parentGroup.members.get(i));
    }

    public void inviteFriend(User friend) {
        this.invites.add(friend);
        this.numGuests++;
    }

    public void setParent(Group parent) {
        this.parentGroup = parent;
    }

    public void addAdmin(User admin) {
        this.administrators.add(admin);
    }

}
