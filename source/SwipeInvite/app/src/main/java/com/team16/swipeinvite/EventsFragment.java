package com.team16.swipeinvite;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Fragment that shows which events a user has been invited to
 */
public class EventsFragment extends Fragment {

    private ArrayAdapter<String> mArrayAdapter;

    public EventsFragment() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Create some dummy data for the ListView.  Here's a sample weekly forecast
        String[] data = {
                "CS307 - Group 16 meeting",
                "CS354 Project Group meeting",
                "SIGAPP Purdue meeting",
                "Zombie Impersonators of Tippecanoe County meeting",
                "Friends of Miur Valley meeting",
                "The three best friends that anyone could have meeting",
                "BBQ Sauce Club meeting",
                "We like wine and we don't care who knows meeting",
                "Mountain biking friends meeting",
                "People who don't go to Indiana University quarterly touching base",
                "The whitest kids u know skit planning meeting",
                "CS 391 meeting"
        };

        List<String> dummyData = new ArrayList<String>(Arrays.asList(data));
        mArrayAdapter =
                new ArrayAdapter<String>(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_event, // The name of the layout ID.
                        R.id.list_item_event_textview, // The ID of the textview to populate.
                        dummyData);


        View rootView = inflater.inflate(R.layout.fragment_events, container, false);

        // THIS CRASHES THE APP. IT SHOULD PROBABLY BE SOMEWHERE ELSE
        //TextView mTextView;
        //mTextView = (TextView)getView().findViewById(R.id.textViewGroupsFragment);
        //mTextView.setText("Current User: " + BaasUser.current().getName() + " With PW: " + BaasUser.current().getPassword());

        ListView listView = (ListView) rootView.findViewById(R.id.listview_events);
        listView.setAdapter(mArrayAdapter);

        //getActivity().setTitle("TEST");
        return rootView;
    }

}