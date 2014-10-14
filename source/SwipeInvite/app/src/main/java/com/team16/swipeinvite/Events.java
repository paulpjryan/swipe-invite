/**
 * 
 */
import java.util.*;
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
			invites.add(parentgroup.members.get(i));
			
		}
		
	}
	
	public void inviteFriend(User frnd) {
		invites.add(frnd);
		numguests++;
		
	}
	
	public void setParent(Group par) {
		parentgroup = par;
		
	}
	
	public void addAdmin(User admin) {
		administrators.add(admin);
		
	}
	

}
