package com.team16.swipeinvite;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;

public class GroupCreationActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_creation);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        /*
        TextView nameview = (TextView) findViewById(R.id.textView_group_name);
		TextView descview = (TextView) findViewById(R.id.textView_group_description);
        RadioGroup radgroup = (RadioGroup) findViewById(R.id.radioGroup);

        int selectedId = radgroup.getCheckedRadioButtonId();
        RadioButton type = (RadioButton) findViewById(selectedId);
        boolean ispriv = false;
        if((type.getText()).equals("text_private"))
            ispriv = true;

		String groupname = nameview.getText().toString();
		String desc = descview.getText().toString();
        User current = ((StartUp) this.getApplication()).getActiveUser();
        Group newgroup = new Group(current, groupname, ispriv);
        newgroup.setDescription(desc);
        current.groups.add(newgroup);
        */
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
        else if(id == android.R.id.home)
        {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
		return super.onOptionsItemSelected(item);
	}
}
