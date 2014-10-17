package com.team16.swipeinvite;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.TextView;

public class UserProfileActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_profile);
		
		TextView usrnm = (TextView) findViewById(R.id.textView_user_username);
		TextView nme = (TextView) findViewById(R.id.textView_user_name);
        TextView pass = (TextView) findViewById((R.id.textView_user_password));
		TextView mail = (TextView) findViewById(R.id.textView_user_email);
        CheckBox male = (CheckBox) findViewById(R.id.checkBox_user_male);
        boolean ismale = male.isChecked();
		String username = usrnm.getText().toString();
        String password = pass.getText().toString();
		String fullname = nme.getText().toString();
		String emailadd = mail.getText().toString();
        //activeUser = new User(fullname, username, password);
        //activeUser.setEmail(mail);
        //activeUser.setGender(ismale);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.user_profile, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
