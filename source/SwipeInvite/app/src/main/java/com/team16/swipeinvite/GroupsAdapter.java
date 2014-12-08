package com.team16.swipeinvite;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

// The standard text view adapter only seems to search from the beginning of whole words
// so we've had to write this whole class to make it possible to search
// for parts of the arbitrary string we want
public class GroupsAdapter extends BaseAdapter implements Filterable {
    private static final String LOG_TAG = "Groups_Adapter";

    private List<Group2> originalData = null;
    private List<Group2> filteredData = null;
    private LayoutInflater mInflater;
    private ItemFilter mFilter = new ItemFilter();
    //private static GroupsAdapter selfInstance;

    public GroupsAdapter(Context context, List<Group2> data) {
        this.filteredData = data;
        this.originalData = data;
        mInflater = LayoutInflater.from(context);
        //GroupsAdapter.selfInstance = this;     //keep track of the instance of this class that is used
    }


    //region Methods to handle updating the list view from other threads, non static
    protected void updateData(List<Group2> g) {
        /*if (selfInstance == null) {
            return;
        } */
        Log.d(LOG_TAG, "Received request to update list view, size: " + g.size());
        this.originalData = g;
        try {
            this.notifyDataSetChanged();
        } catch (Exception e) {
            Log.d(LOG_TAG, "Caught exception: " + e.toString());
            return;
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

    public View getView(int position, View convertView, ViewGroup parent) {
        // A ViewHolder keeps references to children views to avoid unnecessary calls
        // to findViewById() on each row.
        ViewHolder holder;

        // When convertView is not null, we can reuse it directly, there is no need
        // to reinflate it. We only inflate a new View when the convertView supplied
        // by ListView is null.
        //if (convertView == null) {
        convertView = mInflater.inflate(R.layout.list_item_group, null);

        // Creates a ViewHolder and store references to the two children views
        // we want to bind data to.
        holder = new ViewHolder();
        holder.text = (TextView) convertView.findViewById(R.id.list_item_group_name);

        // Bind the data efficiently with the holder.

        convertView.setTag(holder);
       /* } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder) convertView.getTag();
        }*/

        // If weren't re-ordering this you could rely on what you set last time
        holder.text.setText((filteredData.get(position).getName()));

        return convertView;
    }

    static class ViewHolder {
        TextView text;
    }

    public Filter getFilter() {
        return mFilter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final List<Group2> list = originalData;

            int count = list.size();
            final ArrayList<Group2> nlist = new ArrayList<Group2>(count);

            String filterableString;

            for (int i = 0; i < count; i++) {
                filterableString = list.get(i).getName();
                if (filterableString.toLowerCase().contains(filterString)) {
                    nlist.add(list.get(i));
                }
            }

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredData = (ArrayList<Group2>) results.values;
            notifyDataSetChanged();
        }

    }
}

