package com.team16.swipeinvite;


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
		friends.add(frnd);
		frndcount++;
		
	}
	
	public void removeFriend(User frnd) {
		friends.remove(frnd);
		frndcount--;
		
	}
	
	public void removeFriend(int i) {
		friends.remove(i);
		frndcount--;
		
	}
	
	public void addGroup(Group grp) {
		this.groups.add(grp);
		grpcount++;
		
	}
	
	public void removeGroup(Group grp) {
		groups.remove(grp);
		grpcount--;
		
	}
	
	public void removeGroup(int i) {
		groups.remove(i);
		grpcount--;
		
	}
	
	public int getGroupCount() {
		return grpcount;
		
	}
	
	public int getFriendCount() {
		return frndcount;
		
	}
	

}
