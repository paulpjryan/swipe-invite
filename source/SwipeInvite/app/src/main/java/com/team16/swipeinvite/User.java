package com.team16.swipeinvite;


import java.util.*;

public class User {
	String name;
	String username;
	String email;
	boolean male;
	int frndcount = 0;
	int grpcount = 0;
	ArrayList<User> friends = new ArrayList<User>();
	ArrayList<Group> groups = new ArrayList<Group>();
	
	public User(String nm, String usrnm) {
		this.name = nm;
		this.username = usrnm;
		
	}
	
	public void setGender(boolean value) {
		this.male = value;
		
	}
	
	public void setEmail(String addr) {
		this.email = addr;
		
	}
	
	public void getEmail() {
		return this.email;
		
	}
	
	public boolean isMale() {
		return this.male;
		
	}
	
	public void addFriend(User frnd) {
		this.friends.add(frnd);
		this.frndcount++;
		
	}
	
	public void getFullName() {
		return this.name;
		
	}
	
	public void getUserName() {
		return this.username;
		
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