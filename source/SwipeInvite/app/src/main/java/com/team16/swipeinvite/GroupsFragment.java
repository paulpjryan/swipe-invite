package com.team16.swipeinvite;

import android.app.Fragment;
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

import com.melnykov.fab.FloatingActionButton;

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

        m = Model.getInstance(getActivity());
        //List<Group2> groupData = m.activeGroups;   //OLD implementation
        //List<Group2> groupData = m.getActiveGroups();    //Synchronized methodology 1
        mArrayAdapter = new GroupsAdapter(getActivity(), m.getActiveGroups());

        View rootView = inflater.inflate(R.layout.fragment_group, container, false);

        // THIS CRASHES THE APP. IT SHOULD PROBABLY BE SOMEWHERE ELSE
        //TextView mTextView;
        //mTextView = (TextView)getView().findViewById(R.id.textViewGroupsFragment);
        //mTextView.setText("Current User: " + BaasUser.current().getName() + " With PW: " + BaasUser.current().getPassword());

        EditText inputSearch = (EditText) rootView.findViewById(R.id.et_search_group);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_groups);
        listView.setAdapter(mArrayAdapter);

        //Give the main activity the group adapter
        ((MainActivity) getActivity()).setGroupsAdapter(mArrayAdapter);

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

        FloatingActionButton fab = (FloatingActionButton)rootView.findViewById(R.id.groupsfab);
        fab.attachToListView(listView);
        fab.setShadow(true);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).startGroupCreate();
            }
        });

        getActivity().setTitle("Groups");
        return rootView;
    }
}
