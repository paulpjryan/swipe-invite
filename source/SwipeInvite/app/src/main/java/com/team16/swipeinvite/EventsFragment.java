package com.team16.swipeinvite;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.melnykov.fab.FloatingActionButton;

/**
 * Fragment that shows which events a user has been invited to
 */
public class EventsFragment extends Fragment {

    private EventsAdapter mEventsAdapter;
    RefreshableView refreshableView;

    public EventsFragment() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Figure out type of list to use
        String type = getArguments().getString("type");

        Model m = Model.getInstance(getActivity());
        if (type.equals("accepted")) {
            mEventsAdapter =
                    new EventsAdapter(
                            getActivity(),
                            m.getAcceptedEvents(), 0);
        } else if (type.equals("waiting")) {
            mEventsAdapter =
                    new EventsAdapter(
                            getActivity(),
                            m.getWaitingEvents(), 1);
        } else {
            mEventsAdapter =
                    new EventsAdapter(
                            getActivity(),
                            m.getRejectedEvents(), 2);
        }


        View rootView = inflater.inflate(R.layout.fragment_events, container, false);

        // THIS CRASHES THE APP. IT SHOULD PROBABLY BE SOMEWHERE ELSE
        //TextView mTextView;
        //mTextView = (TextView)getView().findViewById(R.id.textViewGroupsFragment);
        //mTextView.setText("Current User: " + BaasUser.current().getName() + " With PW: " + BaasUser.current().getPassword());
        refreshableView = (RefreshableView) rootView.findViewById(R.id.refreshable_view);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_events);
        listView.setAdapter(mEventsAdapter);
        refreshableView.setOnRefreshListener(new RefreshableView.PullToRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                refreshableView.finishRefreshing();
            }
        }, 0);

        //Give the main activity the group adapter
        ((MainActivity) getActivity()).setEventsAdapter(mEventsAdapter);


        EditText inputSearch = (EditText) rootView.findViewById(R.id.et_search_group);

        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                mEventsAdapter.getFilter().filter(cs);
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
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id)
            {
                //Figure out which group is being referred to and pass it to the activity
                Event e = (Event) mEventsAdapter.getItem(position);
                //Call the method in the main activity to start the group edit activity
                ((MainActivity) getActivity()).startEventEdit(e.getId());
            }
        });


        FloatingActionButton fab = (FloatingActionButton)rootView.findViewById(R.id.eventsfab);
        fab.attachToListView(listView);
        fab.setShadow(true);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).startEventCreate();
            }
        });

        //getActivity().setTitle("TEST");
        return rootView;
    }

  /*  @Override
    public void onPause()
    {
        super.onPause();
        if(getView().findFocus() == null) {
            Toast.makeText(getActivity(), "NULL FOCUS", Toast.LENGTH_LONG).show();
            return;
        }
        View target = getView();
        if (target != null) {
            InputMethodManager imm = (InputMethodManager) target.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(target.getWindowToken(), 0);
        }
    }*/

}