package com.team16.swipeinvite;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
public class AcquaintanceAdapter extends BaseAdapter implements Filterable {
    private static final String LOG_TAG = "AcquantanceAdapter";

    private List<Acquaintance>originalData = null;
    protected List<Acquaintance>filteredData = null;
    private LayoutInflater mInflater;
    private ItemFilter mFilter = new ItemFilter();
    protected int type; //0 = accepted, 1 = pending, 2 = declined

    public AcquaintanceAdapter(Context context, List<Acquaintance> data, int type) {
        this.filteredData = data ;
        this.originalData = data ;
        mInflater = LayoutInflater.from(context);
        this.type = type;
    }

    //region Methods to handle updating the list view from other threads in a static context
    protected synchronized void updateData(List<Acquaintance> g) {
        Log.d(LOG_TAG, "Received request to update list view.");
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
        //TODO: Change view to inflate
        convertView = mInflater.inflate(R.layout.list_item_event_accepted, null);

        // Creates a ViewHolder and store references to the two children views
        // we want to bind data to.
        //TODO: Get proper view fields for view
        holder = new ViewHolder();
        holder.title = (TextView) convertView.findViewById(R.id.list_item_event_name);
        // Bind the data efficiently with the holder.

        convertView.setTag(holder);
       /* } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder) convertView.getTag();
        }*/

        // If weren't re-ordering this you could rely on what you set last time
        //TODO set textview text and stuff in holder
        holder.title.setText("Text here");

        return convertView;
    }

    static class ViewHolder {
        //TODO: change fields
        TextView title;
    }

    public Filter getFilter() {
        return mFilter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final List<Acquaintance> list = originalData;

            int count = list.size();
            final ArrayList<Acquaintance> nlist = new ArrayList<Acquaintance>(count);

            String filterableString ;

            for (int i = 0; i < count; i++) {
                filterableString = list.get(i).getCommonName();
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
            filteredData = (ArrayList<Acquaintance>) results.values;
            notifyDataSetChanged();
        }

    }
}