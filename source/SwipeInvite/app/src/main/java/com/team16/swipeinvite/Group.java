import java.util.*;

public class Group {
	User creator;
	String groupname;
	String description;
	ArrayList<User> members = new ArrayList<User>();
	ArrayList<Events> events = new ArrayList<Events>();
	int frndcount = 1;
	int eventcount = 0;
	boolean isprivate;
		
	public Group(User creat, String group, String desc, boolean priv) {
		this.creator = new User(creat.name, creat.username);
		this.isprivate = priv;
		this.groupname = group;
		this.description = desc;
		this.members.add(this.creator);
		
	}
	
	public void createEvent(User creat, String eventnm, String Desc, Date begdate, Date enddte) {
		Events temp = new Events(creat, eventnm, Desc, begdate, enddte);
		this.events.add(temp);
		this.eventcount++;
		
	}
	
	public void removeEvent(int i) {
		this.events.remove(i);
		this.eventcount--;
		
	}
	
	public void removeEvent(Events event) {
		this.events.remove(event);
		this.eventcount--;
		
	}
	
	public void addUser(User frnd) {
		this.members.add(frnd);
		this.frndcount++;
		
	}
	
	public void removeUser(User frnd) {
		this.members.remove(frnd);
		this.frndcount--;
		
	}
	
	public void removeUser(int i) {
		this.members.remove(i);
		this.frndcount--;
		
	}
	
	public int getFriendCount() {
		return frndcount;
		
	}
	
	public int getEventCount() {
		return eventcount;
		
	}
	
	

}
