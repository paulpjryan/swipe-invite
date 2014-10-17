package com.team16.swipeinvite;


import java.util.*;
import com.baasbox.android.BaasUser;

public class User {
	String name;  //registered
	String username;  //default
    private String password;  //default
	String email;  //private
	boolean male;  //registered
	int frndcount = 0;  //registered
	int grpcount = 0;  //private
	ArrayList<User> friends = new ArrayList<User>();  //server call, friend
	ArrayList<Group> groups = new ArrayList<Group>();  //server call, private

    //Defualt user object creation for startup
    public User() {
    }
	
	public User(String nm, String usrnm, String pass) {
		this.name = nm;
		this.username = usrnm;
        this.password = pass;
		
	}

    public void setPassword(String p) {
        this.password = p;
    }

    public String getPassword() {
        return this.password;
    }
	
	public void setGender(boolean value) {
		this.male = value;
		
	}
	
	public void setEmail(String addr) {
		this.email = addr;
		
	}
	
	public String getEmail() {
		return this.email;
		
	}
	
	public boolean isMale() {
		return this.male;
		
	}
	
	public void addFriend(User frnd) {
		this.friends.add(frnd);
		this.frndcount++;
		
	}
	
	public String getFullName() {
		return this.name;
		
	}
	
	public String getUserName() {
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

    //Method used to unwrap the contents of a BaasUser object to this user object
    public void unWrapUser (BaasUser u) {
        //Importing default properties of BaasUser
        this.username = u.getName();
        this.password = u.getPassword();

        //Importing FRIEND properties of BaasUser
        if (u.getScope(BaasUser.Scope.FRIEND) != null) {   //making sure the current user has access

        }

        //Importing REGISTERED properties of BaasUser
        if (u.getScope(BaasUser.Scope.REGISTERED) != null) {   //making sure the current user has access
            this.name = u.getScope(BaasUser.Scope.REGISTERED).getString("name");
            this.male = u.getScope(BaasUser.Scope.REGISTERED).getBoolean("male");
            this.frndcount = u.getScope(BaasUser.Scope.REGISTERED).getLong("frndcount").intValue();
        }

        //Importing PRIVATE properties of BaasUser
        if (u.getScope(BaasUser.Scope.PRIVATE) != null) {   //making sure the current user has access
            this.email = u.getScope(BaasUser.Scope.PRIVATE).getString("email");
            this.grpcount = u.getScope(BaasUser.Scope.PRIVATE).getLong("grpcount").intValue();
        }

        return;
    }

    //Method used to wrap the contents of this user object to a BaasUser
    public BaasUser wrapUser() {
        //Setting default properties of BaasUser
        BaasUser u = BaasUser.withUserName(this.username);
        u.setPassword(this.password);

        //Setting FRIEND properties

        //Setting REGISTERED properties
        u.getScope(BaasUser.Scope.REGISTERED).putString("name", this.name);
        u.getScope(BaasUser.Scope.REGISTERED).putBoolean("male" , this.male);
        u.getScope(BaasUser.Scope.REGISTERED).putLong("frndcount", this.frndcount);

        //Setting PRIVATE properties
        u.getScope(BaasUser.Scope.PRIVATE).putString("email", this.email);
        u.getScope(BaasUser.Scope.PRIVATE).putLong("grpcount", this.grpcount);

        //Returning BaasUser object with set profile data
        return u;
    }
	

}
