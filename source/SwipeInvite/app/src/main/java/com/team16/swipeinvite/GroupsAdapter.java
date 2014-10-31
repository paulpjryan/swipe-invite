package com.team16.swipeinvite;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by PRyan on 10/31/2014.
 */
public class GroupsAdapter extends ArrayAdapter<Group2> {
    public GroupsAdapter(Context context, List<Group2> groups)
    {
        super(context, 0, groups);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Group2 group = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_group, parent, false);
        }
        // Lookup view for data population
        TextView gName = (TextView) convertView.findViewById(R.id.list_item_group_name);
        //TextView gSize = (TextView) convertView.findViewById(R.id.list_item_group_size);
        // Populate the data into the template view using the data object
        gName.setText(group.getName());
        //gSize.setText(group.getUserCount());
        // Return the completed view to render on screen
        return convertView;
    }

}
