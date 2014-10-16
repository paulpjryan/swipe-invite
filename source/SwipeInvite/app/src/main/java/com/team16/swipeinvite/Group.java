


import java.util.*;

public class Group extends Observable{
    User creator;
    String groupname;
    ArrayList<User> members = new ArrayList<User>();
    ArrayList<CalendarContract.Events> events = new ArrayList<Events>();
    int frndcount = 0;
    int eventcount = 0;
    boolean isprivate;

    public Group(User creat, String group, boolean priv) {
        this.creator = new User(creat.name, creat.username);
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



}

