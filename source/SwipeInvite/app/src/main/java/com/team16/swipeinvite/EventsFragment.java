package com.team16.swipeinvite;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by PRyan on 10/16/2014.
 */
public class EventsFragment extends Fragment {
    public static final String ARG_PLANET_NUMBER = "planet_number";

    public EventsFragment() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        getActivity().setTitle("Groups");
        return rootView;
    }
}