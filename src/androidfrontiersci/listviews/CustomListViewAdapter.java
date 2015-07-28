// CustomListViewAdapter.java

package androidfrontiersci.listviews;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidfrontiersci.MainActivity;
import androidfrontiersci.textviews.CustomTextView;

import frontsci.android.R;

import java.util.List;

/*
    This is the CustomListViewAdapter class, used by MainActivity.java, ResearchActivity.java,
    ResearchNavigationDrawerFragment.java and ArticlesListActivity.java. It allows for the creation
    of ListViews that can include icons and other ImageViews as well as the standard TextView.
*/
@SuppressLint("InflateParams") public class CustomListViewAdapter extends ArrayAdapter<RowItem> {

    // The class' private variables
    private int resourceId;
	Context context;

/*
    The constructor, setting the correct context and resource id for the layout file.
*/
    public CustomListViewAdapter(Context context, int resourceId, List<RowItem> items) {
        super(context, resourceId, items);
        this.context = context;
        this.resourceId = resourceId;
    }
     
    // This private class is simply a holder for the elements of the row item view.
    private class ViewHolder {
        ImageView iconView = new ImageView(context);
        TextView txtTitle = new CustomTextView(context);
    }

/*
    This function creates and customizes the view for each item of whatever ListView is being
    created, then returns that view. It has two possible layouts: the one specific for
    ArticlesListActivity and the one used for all other occurrences.
*/
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        RowItem rowItem = getItem(position);
         
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = mInflater.inflate(resourceId, null);
            holder = new ViewHolder();
            holder.txtTitle = (TextView) convertView.findViewById(R.id.title); 
            holder.iconView = (ImageView) convertView.findViewById(R.id.icon);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        holder.txtTitle.setText(rowItem.getTitle());

        if (!MainActivity.forArticlesList) { // For MainActivity, ResearchActivity and its drawer
            holder.iconView.setVisibility(ImageView.VISIBLE);
            holder.txtTitle.setTextSize(21);
            if (rowItem.getIconId() == 0) {
                holder.iconView.setImageDrawable(rowItem.getIcon());
            } else {
                holder.iconView.setImageResource(rowItem.getIconId());
            }
        } else { // For ArticlesListActivity
            holder.txtTitle.setTextSize(26);
            holder.txtTitle.setTextColor(Color.BLACK);

            if (!MainActivity.old_articles.contains(rowItem.getTitle())) { // Then it's new...
                holder.iconView.setVisibility(ImageView.VISIBLE);
            } else { // It's old
                holder.iconView.setVisibility(ImageView.INVISIBLE);
            }
        }
        return convertView;
    }
}