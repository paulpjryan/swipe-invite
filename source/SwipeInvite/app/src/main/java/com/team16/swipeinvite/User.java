import java.util.*;

public class User {
	String name;
	String username;
	int frndcount = 0;
	int grpcount = 0;
	ArrayList<User> friends = new ArrayList<User>();
	ArrayList<Group> groups = new ArrayList<Group>();
	
	public User(String nm, String usrnm) {
		this.name = nm;
		this.username = usrnm;
		
	}
	
	public void addFriend(User frnd) {
		this.friends.add(frnd);
		this.frndcount++;
		
	}
	
	public void removeFriend(User frnd) {
		this.friends.remove(frnd);
		this.frndcount--;
		
	}
	
	public void removeFriend(int i) {
		this.friends.remove(i);
		this.frndcount--;
		
	}
	
	public void addGroup(Group grp) {
		this.groups.add(grp);
		this.grpcount++;
		
	}
	
	public void removeGroup(Group grp) {
		this.groups.remove(grp);
		this.grpcount--;
		
	}
	
	public void removeGroup(int i) {
		this.groups.remove(i);
		this.grpcount--;
		
	}
	
	public int getGroupCount() {
		return grpcount;
		
	}
	
	public int getFriendCount() {
		return frndcount;
		
	}
	

}
