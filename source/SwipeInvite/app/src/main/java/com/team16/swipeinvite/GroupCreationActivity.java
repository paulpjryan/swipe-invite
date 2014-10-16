package com.team16.swipeinvite;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.*;

public class GroupCreationActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_creation);
		
		TextView nameview = (TextView) findViewById(R.id.textView_group_name);
		TextView descview = (TextView) findViewById(R.id.textView_group_description);
		String groupname = nameview.getText().toString();
		String desc = descview.getText().toString();
	}
	
	public void onCheckboxClicked(View view) {
	    // Is the view now checked?
		boolean ispriv;
	    if(view.getId() == R.id.checkBox_group_type1)
	    	ispriv = true;
	    else if(view.getId() == R.id.checkBox_group_type2)
	    	ispriv = false;
	    
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.group_creation, menu);
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