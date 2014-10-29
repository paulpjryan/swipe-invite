package com.team16.swipeinvite;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class UserProfileActivity extends ActionBarActivity {

    private static final String LOG_TAG = "USERPROFILE";

    private TextView fullnameField;
    private TextView emailField;
    private RadioGroup genderGroup;
    private RadioButton genderButton;
    private Model model;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_profile);
		fullnameField = (TextView) findViewById(R.id.editText_user_name);
        emailField = (TextView) findViewById(R.id.editText_user_email);
        genderGroup = (RadioGroup) findViewById(R.id.RadioGroup_gender);
		/*TextView usrnm = (TextView) findViewById(R.id.textView_user_username);
		TextView nme = (TextView) findViewById(R.id.textView_user_name);
        TextView pass = (TextView) findViewById((R.id.textView_user_password));
		TextView mail = (TextView) findViewById(R.id.textView_user_email);
       // CheckBox male = (CheckBox) findViewById(R.id.checkBox_user_male);
       // boolean ismale = male.isChecked();
        RadioGroup radgroup = (RadioGroup) findViewById(R.id.radioGroup);
        int selectedId = radgroup.getCheckedRadioButtonId();
        boolean ismale = false;
        RadioButton gender = (RadioButton) findViewById(selectedId);
        if((gender.getText()).equals("Male"))
            ismale = true;
		String username = usrnm.getText().toString();
        String password = pass.getText().toString();
		String fullname = nme.getText().toString();
		String emailadd = mail.getText().toString();*/

        //activeUser = new User(fullname, username, password);
        //activeUser.setEmail(mail);
        //activeUser.setGender(ismale);

        /*Button tut1 = (Button)findViewById(R.id.button_user_submit);
        tut1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(UserProfileActivity.this, MainActivity.class);

                startActivity(mainIntent);


            }
        });*/
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

    public void onClickListener(View v)
    {
        //show progress here **************************************

        String fullname = fullnameField.getText().toString();
        String email = emailField.getText().toString();
        int selectedid = genderGroup.getCheckedRadioButtonId();
        boolean ismale = false;
        // need to fix if none of them are checked**********************************
        genderButton = (RadioButton) findViewById(selectedid);
        String gender = genderButton.getText().toString();

        //Catch faulty inputs
        if (TextUtils.isEmpty(fullname)) {
            //NOTIFY USER OF EMPTY FIELD
            Log.d(LOG_TAG, "Username cannot be empty");
            //showProgress(false); *******************************
            fullnameField.setError("Cannot be left blank");
            fullnameField.requestFocus();
            return;
        } else if (TextUtils.isEmpty(email)) {
            //NOTIFY USER OF EMPTY FIELD
            Log.d(LOG_TAG, "Password cannot be empty");
            //showProgress(false); *******************************
            emailField.setError("Cannot be left blank");
            emailField.requestFocus();
            return;
        } else if (fullname.length() > 30 || fullname.length() < 4) {
            //NOTIFY USER OF EMPTY FIELD
            Log.d(LOG_TAG, "Username must be between 4 and 30 characters.");
            //showProgress(false); *******************************
            fullnameField.setError("Must be between 4 and 30 characters");
            fullnameField.requestFocus();
            return;
        } else if (email.length() < 6) {
            //NOTIFY USER OF EMPTY FIELD
            Log.d(LOG_TAG, "Password must be more than 6 characters.");
            //showProgress(false); *******************************
            emailField.setError("Must be greater than 6 characters");
            emailField.requestFocus();
            return;
        }

        if((gender).equals("Male"))
            ismale=true;
        else if((gender).equals("Female"))
            ismale = false;
        else
        {
            Log.d(LOG_TAG, "Gender cannot be empty");
            // showProgress(false); *******************************
            genderGroup.requestFocus();
            return;

        }

        // UNCOMMENT THIS ************************
        //model.currentUser.setMale(ismale);
        //model.currentUser.setEmail(email);
        //model.currentUser.setCommonName(fullname);
    }

    // PUSH TO SERVER **********************************************
}
