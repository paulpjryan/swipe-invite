package com.team16.swipeinvite;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

/**
 * Fragment that shows which groups a user has been invited to
 */
public class GroupsFragment extends Fragment {
    private static final String LOG_TAG = "GROUP_FRAGMENT";

    private GroupsAdapter mArrayAdapter;

    private Model m;

    public GroupsFragment() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreateViewCalled");

        // Create some dummy data for the ListView.  Here's a sample weekly forecast
        String[] data = {
                "CS307 - Group 16",
                "CS354 Project Group",
                "SIGAPP Purdue",
                "Zombie Impersonators of Tippecanoe County",
                "Friends of Miur Valley",
                "The three best friends that anyone could have",
                "BBQ Sauce Club",
                "We like wine and we don't care who knows",
                "Mountain biking friends",
                "People who don't go to Indiana University",
                "The whitest kids u know",
                "CS 391"
        };

        m = Model.getInstance(getActivity());
        //List<Group2> groupData = m.activeGroups;   //OLD implementation
        //List<Group2> groupData = m.getActiveGroups();    //Synchronized methodology 1
        mArrayAdapter =
                new GroupsAdapter(
                        getActivity(),
                        m.getActiveGroups());


        View rootView = inflater.inflate(R.layout.fragment_group, container, false);

        // THIS CRASHES THE APP. IT SHOULD PROBABLY BE SOMEWHERE ELSE
        //TextView mTextView;
        //mTextView = (TextView)getView().findViewById(R.id.textViewGroupsFragment);
        //mTextView.setText("Current User: " + BaasUser.current().getName() + " With PW: " + BaasUser.current().getPassword());

        EditText inputSearch = (EditText) rootView.findViewById(R.id.et_search_group);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_groups);
        listView.setAdapter(mArrayAdapter);

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

        //This just opens group_edit on tap. We should have some sort of edit action or button.
        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id)
            {
                //Figure out which group is being referred to and pass it to the activity
                Group2 g = (Group2) mArrayAdapter.getItem(position);
                //Call the method in the main activity to start the group edit activity
                ((MainActivity) getActivity()).startGroupEdit(g.getId());
            }
        });

        getActivity().setTitle("Groups");
        return rootView;
    }
}
