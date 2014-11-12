package com.team16.swipeinvite;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.baasbox.android.BaasUser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class MainActivity extends ActionBarActivity {
    /* -------------------- LOG TAG CONSTANTS --------------------------- */
    private final static String LOG_TAG = "MAIN_ACT";
    /* -------------------- END LOG TAG CONSTANTS ----------------------- */

    
    //region Local instance variables for the View elements
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private String[] drawerTitleList;
    //endregion


    //region Local variable for the model
    private Model model;
    private static final String MODEL_KEY = "model_d";
    private static final String MODEL_INTENT_KEY = "model_data";
    //endregion


    //region Lifecycle methods for the main activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate called");

        //DO NOT ADD ANY CODE BEFORE THIS LINE PERTAINING TO THE MAIN ACTIVITY
        //This checks to see if there is a current user logged in or not
        //This if statement must be placed in the new launcher, class if it changes
        if (BaasUser.current() == null){
            startLoginScreen();
            return;
        }

        //Load the model
        model = Model.getInstance(this);
        Log.d(LOG_TAG, "Model active group size: " + /*model.activeGroups.size()*/ model.getActiveGroups().size());

        setContentView(R.layout.activity_main);

        mTitle = getTitle();
        mDrawerTitle = getTitle();
        //Names of tabs in the drawer
        drawerTitleList = new String[]{"Invites", "Events", "Groups"};

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, drawerTitleList));

        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());


        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                //R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            selectItem(0);

        }

    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume called");
        checkPlayServices();    //Make sure user still has valid play service
        if (model == null) {
            model = Model.getInstance(this);
            Log.d(LOG_TAG, "Model active group size: " + /*model.activeGroups.size()*/ model.getActiveGroups().size());
        }
        if (BaasUser.current() == null){    //Check if somehow the user got logged out
            model = null;    //nullify the model because something bad has happened to the user
            startLoginScreen();
            return;
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause called");
    }
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop called");
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(LOG_TAG, "onSaveInstanceState called");
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }
    //endregion


    //region Method to start the login screen, either on new startup or on logout button push
    //This method is called at the startup of onCreate
    //It will direct the user to a login activity
    private void startLoginScreen(){
        if (model == null) {     //Absolutely no data to save, must be first time startup
            Intent intent = new Intent(this, LoginActivity2.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(this, LogoutActivity.class);
            startActivity(intent);
        }
    }
    //endregion


    //region Method to start the group creation activty
    private void startGroupCreate() {
        Intent intent = new Intent(this, GroupCreationActivity.class);
        startActivityForResult(intent, GROUP_CREATE_REQUEST_CODE);
    }
    //endregion


    protected void startGroupEdit(String id) {
        Intent intent = new Intent(this, GroupEditActivity.class);
        intent.putExtra("id", id);
        startActivityForResult(intent, GROUP_EDIT_REQUEST_CODE);
    }


    //region Method to start the profile edit activity
    private void startProfileEdit() {
        //Create the intent
        Intent intent = new Intent(this, UserProfileActivity.class);
        startActivityForResult(intent, PROFILE_EDIT_REQUEST_CODE);
    }
    //endregion


    //region Method to handle returning results from side activities around the main
    private static final int GROUP_CREATE_REQUEST_CODE = 1;
    private static final int PROFILE_EDIT_REQUEST_CODE = 2;
    private static final int GROUP_EDIT_REQUEST_CODE = 3;
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Figure out which activity is returning a result
        switch (requestCode) {
            case GROUP_CREATE_REQUEST_CODE:    //Group Create activity result
                if(resultCode == RESULT_OK) {
                    Log.d(LOG_TAG, "Got ok result from group creation.");
                    //NEED TO REPOPULATE FRAGMENT IF IT IS ACTIVE
                    //selectItem(2);    NO LONGER NEEDED WITH UPDATE IN GROUP ADAPTER
                } else if (resultCode == RESULT_CANCELED) {
                    Log.d(LOG_TAG, "Got canceled result from group creation.");
                    //DO NOTHING
                }
                break;
            case PROFILE_EDIT_REQUEST_CODE:    //Profile Edit activity result
                if (resultCode == RESULT_OK) {
                    Log.d(LOG_TAG, "Got ok result from profile edit.");
                } else if (resultCode == RESULT_CANCELED) {
                    Log.d(LOG_TAG, "Got canceled result from profile edit, bad.");
                }
                break;
            case GROUP_EDIT_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Log.d(LOG_TAG, "Got ok result from group edit.");
                } else if (resultCode == RESULT_CANCELED) {
                    Log.d(LOG_TAG, "Got canceled result from group edit, bad.");
                }
                break;
            default:
                Log.d(LOG_TAG, "Request code not set.");
                break;
        }
        Log.d(LOG_TAG, "Model active group size: " + /*model.activeGroups.size()*/ model.getActiveGroups().size());
    }
    //endregion


    //region Methods for the options menus and drawers
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        //This gets disabled when the drawer is open
        //menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch(item.getItemId()) {
            // Logout
            case R.id.action_logout:
                //Decide how to logout
                startLoginScreen();
                return true;

            // User profile
            case R.id.action_profile:
                //Start user profile activity
                startProfileEdit();
                return true;

            // Search Group
            case R.id.action_search_group:
                Intent intent_sg = new Intent(this,SearchGroupActivity.class);
                if (intent_sg.resolveActivity(getPackageManager()) != null) {

                    startActivity(intent_sg);
                } else {
                    Toast.makeText(this, "Action unavailable", Toast.LENGTH_LONG).show();
                }
                return true;

            // Create Group
            case R.id.action_create_group:
                //Start the group creation activity
                startGroupCreate();
                return true;

            // Create Event
            case R.id.action_event_creation:
                Intent intent_sg2 = new Intent(this,EventCreationActivity.class);
                if (intent_sg2.resolveActivity(getPackageManager()) != null) {

                    startActivity(intent_sg2);
                } else {
                    Toast.makeText(this, "Action unavailable", Toast.LENGTH_LONG).show();
                }
                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //endregion


    //region Methods for fragment management
    private void selectItem(int position) {
        // update the main content by replacing fragments
        Fragment fragment;
        switch(position) {
            case 2:
                fragment = new GroupsFragment();
                break;
            default:
                fragment = new EventsFragment();
                break;
        }
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        mTitle = drawerTitleList[position];

        mDrawerLayout.closeDrawer(mDrawerList);
    }
    //endregion


    //region Method and variables to check if a valid Google play services is found
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(LOG_TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
    //endregion


    //region Nested class for drawer click listeners
    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }
    //endregion

}
