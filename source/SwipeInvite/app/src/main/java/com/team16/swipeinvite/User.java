package com.team16.swipeinvite;


import android.util.Log;

import java.util.*;
import com.baasbox.android.BaasUser;

public class User {
	String name;  //registered
	String username;  //default
    //private String password;  //default  //OBSOLETE WITH NEW PROTOCOL FOR USER HANDLING
	String email;  //private
	boolean male;  //registered
	int frndcount = 0;  //registered
	int grpcount = 0;  //private
	ArrayList<User> friends = new ArrayList<User>();  //server call, friend
	ArrayList<Group> groups = new ArrayList<Group>();  //server call, private

    private BaasUser userObj;

    //Defualt user object creation for startup
    public User() {
    }

    /*
	public User(String nm, String usrnm, String pass) {
		this.name = nm;
		this.username = usrnm;
        this.password = pass;
		
	}
	*/

    public User(String nm, String usrnm) {
        this.name = nm;
        this.username = usrnm;
    }

    /*
    public void setPassword(String p) {
        this.password = p;
    }

    public String getPassword() {
        return this.password;
    }
	*/

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
        if (u == null) {
            Log.d("LOG", "User null.");
            return;
        }
        userObj = u;
        //Importing default properties of BaasUser
        this.username = u.getName();
        /*
        try {
            this.password = u.getPassword();
        } catch (NullPointerException g) {
            Log.d("LOG", "No password parsed.");
        }
        */

        //Importing FRIEND properties of BaasUser
        if (u.getScope(BaasUser.Scope.FRIEND) != null) {   //making sure the current user has access
            try {

            } catch (NullPointerException f) {
                Log.d("LOG", "No user data.");
            }
        }

        //Importing REGISTERED properties of BaasUser
        if (u.getScope(BaasUser.Scope.REGISTERED) != null) {   //making sure the current user has access
            try {
                this.name = u.getScope(BaasUser.Scope.REGISTERED).getString("name");
                this.male = u.getScope(BaasUser.Scope.REGISTERED).getBoolean("male");
                this.frndcount = u.getScope(BaasUser.Scope.REGISTERED).getLong("frndcount").intValue();
            } catch (NullPointerException e) {
                Log.d("LOG", "No user data.");
            }
        }

        //Importing PRIVATE properties of BaasUser
        if (u.getScope(BaasUser.Scope.PRIVATE) != null) {   //making sure the current user has access
            try {
                this.email = u.getScope(BaasUser.Scope.PRIVATE).getString("email");
                this.grpcount = u.getScope(BaasUser.Scope.PRIVATE).getLong("grpcount").intValue();
            } catch (NullPointerException i) {
                Log.d("LOG", "No user data.");
            }
        }

        return;
    }

    //Method used to wrap the contents of this user object to a provided BaasUser
    //returns null if this is not the correct user
    public BaasUser wrapToCurrentUser() {
        BaasUser u = BaasUser.current();

        //Checking default properties of BaasUser
        if (!(u.getName().equals(this.username))) {
            Log.d("LOG", "Tried to save incorrect user profile to current user.");
            return null;
        }

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

    //Method to get the BaasUser object stored in this
    public BaasUser getUserObj(){
        return userObj;
    }

}
