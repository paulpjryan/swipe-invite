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
 * Fragment that shows which events a user has been invited to
 */
public class EventsFragment extends Fragment {
    private static final String LOG_TAG = "EVENT_FRAGMENT";

    private EventsAdapter mEventsAdapter;
    private RefreshableView refreshableView;

    public EventsFragment() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreateViewCalled");

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

        refreshableView = (RefreshableView) rootView.findViewById(R.id.refreshable_view);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_events);
        //listView.setItemsCanFocus(false);
        listView.setAdapter(mEventsAdapter);

        refreshableView.setOnRefreshListener(new RefreshableView.PullToRefreshListener() {
            @Override
            public void onRefresh() {
                ((MainActivity) getActivity()).startUpdateService();
            }
        }, 0);
        ((MainActivity) getActivity()).setRefreshableView(refreshableView);

        //Give the main activity the event adapter
        ((MainActivity) getActivity()).setEventsAdapter(mEventsAdapter);

        EditText inputSearch = (EditText) rootView.findViewById(R.id.et_search_event);

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


        //This just opens event_edit on tap. We should have some sort of edit action or button.
        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //Figure out which group is being referred to and pass it to the activity
                Event g = (Event) mEventsAdapter.getItem(position);
                //Call the method in the main activity to start the group edit activity
                ((MainActivity) getActivity()).startEventEdit(g.getId());
            }
        });


        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.eventsfab);
        fab.attachToListView(listView);
        fab.setShadow(true);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).startEventCreate();
            }
        });

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