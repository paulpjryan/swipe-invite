package com.team16.swipeinvite;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by PRyan on 10/16/2014.
 */
public class GroupsFragment extends Fragment {

    public GroupsFragment() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_group, container, false);

        //getActivity().setTitle("Groups");
        return rootView;
    }
}
