package com.team16.swipeinvite;

/**
 * 
 */

import java.util.ArrayList;
import java.util.Date;
/**
 * @author Tej
 *
 */
public class Events {
	User creator;
	String eventname;
	String description;
	ArrayList<User> administrators= new ArrayList<User>();
	Date begindate;
	Date enddate;
	ArrayList<User> invites= new ArrayList<User>();
	int numguests = 0;
	Group parentgroup;
	
	public Events(User creat, String event, String desc, Date begdate, Date enddte) {
		this.creator = creat;
		this.eventname = event;
		this.description = desc;
		this.begindate = begdate;
		this.enddate = enddte;

		for(int i = 0; i < parentgroup.members.size(); i++)
		{
			this.invites.add(parentgroup.members.get(i));
			
		}
		
	}
	
	public void inviteFriend(User frnd) {
		this.invites.add(frnd);
		this.numguests++;
		
	}
	
	public void setParent(Group par) {
		this.parentgroup = par;
		
	}
	
	public void addAdmin(User admin) {
		this.administrators.add(admin);
		
	}
	

}