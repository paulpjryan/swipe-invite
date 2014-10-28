package com.team16.swipeinvite;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.baasbox.android.BaasHandler;
import com.baasbox.android.BaasResult;
import com.baasbox.android.BaasUser;
import com.baasbox.android.RequestToken;

public class MainActivity extends ActionBarActivity {
    /* -------------------- LOG TAG CONSTANTS --------------------------- */
    private final static String LOG_TAG = "MAIN_ACT";
    /* -------------------- END LOG TAG CONSTANTS ----------------------- */

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private String[] drawerTitleList;

    private Model model;

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
        Log.d(LOG_TAG, "Getting model from intent.");
        model = getIntent().getParcelableExtra("model_data");

        if (savedInstanceState != null) {
            logoutToken = RequestToken.loadAndResume(savedInstanceState,LOGOUT_TOKEN_KEY,logoutHandler);
        }

        setContentView(R.layout.activity_main);

        mTitle = getTitle();
        mDrawerTitle = getTitle();
        //Names of tabs in the drawer
        drawerTitleList = new String[]{"Events", "Groups"};

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
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
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
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.putExtra("model_data", model);
            startActivity(intent);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

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
            case R.id.action_logout:
                /* CODE TO LOGOUT GOES IN HERE. THIS OTHER STUFF IS JUST PLACEHOLDER


                // create intent to perform web search for this planet
                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                intent.putExtra(SearchManager.QUERY, getActionBar().getTitle());
                // catch event that there's no activity to handle intent
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Action unavailable", Toast.LENGTH_LONG).show();
                }
                */
                //BaasUser.current().logout(logoutHandler);
                startLoginScreen();
                return true;
            // User profile
            case R.id.action_profile:
                Intent intent_profile = new Intent(this,UserProfileActivity.class);
                if (intent_profile.resolveActivity(getPackageManager()) != null) {
                    intent_profile.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent_profile);
                } else {
                    Toast.makeText(this, "Action unavailable", Toast.LENGTH_LONG).show();
                }
                return true;

            // Search Group

            case R.id.action_search_group:
                Intent intent_sg = new Intent(this,Add_person2group.class);
                if (intent_sg.resolveActivity(getPackageManager()) != null) {

                    startActivity(intent_sg);
                } else {
                    Toast.makeText(this, "Action unavailable", Toast.LENGTH_LONG).show();
                }
                return true;


            case R.id.action_create_group:

                // create intent to perform web search for this planet
                Intent intent = new Intent(this, GroupCreationActivity.class);
                //intent.putExtra(SearchManager.QUERY, getActionBar().getTitle());
                // catch event that there's no activity to handle intent
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Action unavailable", Toast.LENGTH_LONG).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* LOGOUT METHOD SECTION */
    private final static String LOGOUT_TOKEN_KEY = "logout";
    private void onLogout(){
        //Remove active user data from our model
        //DUMP ALL USER DATA
        ((StartUp) this.getApplication()).resetActiveUser();

        //Go back to logout screen
        startLoginScreen();
    }

    private RequestToken logoutToken;
    private final BaasHandler<Void> logoutHandler =
            new BaasHandler<Void>() {
                @Override
                public void handle(BaasResult<Void> voidBaasResult) {
                    logoutToken=null;
                    onLogout();
                }
            };
    /* END LOGOUT SECTION */


    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        // update the main content by replacing fragments
        Fragment fragment;
        switch (position) {
            case 1:
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
}
