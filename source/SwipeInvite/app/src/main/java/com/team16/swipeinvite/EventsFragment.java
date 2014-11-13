package com.team16.swipeinvite;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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

        String[] data = {
                "Group 1 Meeting - 4:30pm",
                "Pork tasting - 6:15pm",
                "Project Demo - 11:30am",
                "RANDOM DANCING - ALL THE TIME"
        };

        List<String> dummyData = new ArrayList<String>(Arrays.asList(data));
        mArrayAdapter =
                new ArrayAdapter<String>(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_event, // The name of the layout ID.
                        R.id.list_item_event_name, // The ID of the textview to populate.
                        dummyData);


        View rootView = inflater.inflate(R.layout.fragment_events, container, false);

        // THIS CRASHES THE APP. IT SHOULD PROBABLY BE SOMEWHERE ELSE
        //TextView mTextView;
        //mTextView = (TextView)getView().findViewById(R.id.textViewGroupsFragment);
        //mTextView.setText("Current User: " + BaasUser.current().getName() + " With PW: " + BaasUser.current().getPassword());

        ListView listView = (ListView) rootView.findViewById(R.id.listview_events);
        listView.setAdapter(mArrayAdapter);


        EditText inputSearch = (EditText) rootView.findViewById(R.id.et_search_group);

        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                mArrayAdapter.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
            }
        });



        //getActivity().setTitle("TEST");
        return rootView;
    }

}