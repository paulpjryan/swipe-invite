package com.team16.swipeinvite;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

// The standard text view adapter only seems to search from the beginning of whole words
// so we've had to write this whole class to make it possible to search
// for parts of the arbitrary string we want
public class EventsAdapter extends BaseAdapter implements Filterable {
    private static final String LOG_TAG = "EventsAdapter";

    public static final int TYPE_ACCEPTED = 0;
    public static final int TYPE_PENDING = 1;
    public static final int TYPE_REJECTED = 2;
    public static final int TYPE_NO_BUTTONS = -1;

    private List<Event>originalData = null;
    protected List<Event>filteredData = null;
    private LayoutInflater mInflater;
    private ItemFilter mFilter = new ItemFilter();
    protected int type; //0 = accepted, 1 = pending, 2 = declined, -1 = NO BUTTONS

    public EventsAdapter(Context context, List<Event> data, int type) {
        this.filteredData = data;
        this.originalData = data;
        mInflater = LayoutInflater.from(context);
        this.type = type;
    }

    //region Methods to handle updating the list view from other threads in a static context
    protected synchronized void updateData(List<Event> g) {
        Log.d(LOG_TAG, "Received request to update list view.");
        this.originalData = g;
        try {
            this.notifyDataSetChanged();
        } catch (Exception e) {
            Log.d(LOG_TAG, "Caught exception: " + e.toString());
        }
    }
    //endregion

    public int getCount() {
        return filteredData.size();
    }

    public Object getItem(int position) {
        return filteredData.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        // A ViewHolder keeps references to children views to avoid unnecessary calls
        // to findViewById() on each row.
        ViewHolder holder;
        final View convertView2 = convertView;

        if( convertView == null ) {
            if(type == TYPE_ACCEPTED)
                convertView = mInflater.inflate(R.layout.list_item_event_accepted, parent, false);
            else if(type == TYPE_PENDING)
                convertView = mInflater.inflate(R.layout.list_item_event, parent, false);
            else if (type == TYPE_REJECTED)
                convertView = mInflater.inflate(R.layout.list_item_event_declined, parent, false);
            else
                convertView = mInflater.inflate(R.layout.list_item_event_nobutton, parent, false);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.list_item_event_name);
            holder.location = (TextView) convertView.findViewById(R.id.list_item_event_location);
            holder.datetime = (TextView) convertView.findViewById(R.id.list_item_event_datetime);
            // Bind the data efficiently with the holder.

            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder) convertView.getTag();
        }


        // If weren't re-ordering this you could rely on what you set last time
        holder.title.setText((filteredData.get(position).getName()));
        holder.location.setText((filteredData.get(position).getLocation()));
        holder.datetime.setText((filteredData.get(position).dateToString()));

        if( type == TYPE_ACCEPTED || type == TYPE_PENDING )
        {
            holder.declineButton = (ImageButton) convertView.findViewById(R.id.list_item_reject_button);
            holder.declineButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(convertView2.getContext(), EventMoveService.class);
                    intent.putExtra(EventMoveService.EVENT_ID, filteredData.get(position).getId());
                    intent.putExtra(EventMoveService.EVENT_DESTINATION, EventMoveService.EVENT_REJECTED);
                    convertView2.getContext().startService(intent);
                }
            });
        }

        if( type == TYPE_PENDING || type == TYPE_REJECTED )
        {
            holder.acceptButton = (ImageButton) convertView.findViewById(R.id.list_item_accept_button);
            holder.acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(convertView2.getContext(), EventMoveService.class);
                    intent.putExtra(EventMoveService.EVENT_ID, filteredData.get(position).getId());
                    intent.putExtra(EventMoveService.EVENT_DESTINATION, EventMoveService.EVENT_ACCEPTED);
                    convertView2.getContext().startService(intent);
                }
            });
        }

        return convertView;
    }

    static class ViewHolder {
        TextView title;
        TextView location;
        TextView datetime;
        ImageButton acceptButton;
        ImageButton declineButton;
    }

    public Filter getFilter() {
        return mFilter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final List<Event> list = originalData;

            int count = list.size();
            final ArrayList<Event> nlist = new ArrayList<Event>(count);

            String filterableString ;

            for (Event aList : list) {
                filterableString = aList.getName();
                if (filterableString.toLowerCase().contains(filterString)) {
                    nlist.add(aList);
                }
            }

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredData = (ArrayList<Event>) results.values;
            notifyDataSetChanged();
        }

    }
}


