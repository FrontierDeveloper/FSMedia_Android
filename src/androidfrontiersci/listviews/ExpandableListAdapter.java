// ExpandableListAdapter.java

package androidfrontiersci.listviews;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeApiServiceUtil;
import com.google.android.youtube.player.YouTubeInitializationResult;

import androidfrontiersci.Download.Downloader;
import androidfrontiersci.textviews.CustomTextView;
import androidfrontiersci.videos.VideoActivity;
import androidfrontiersci.videos.VideosListActivity;

import frontsci.android.R;

import java.util.HashMap;
import java.util.List;

// ###########################################################
//    This is the ExpandableListAdapter class, used by VideosListActivity.java. It allows for the
//    creation of an ExpandableListView that includes icons in the group items as well as the standard
//    TextView.
// ###########################################################
public class ExpandableListAdapter extends BaseExpandableListAdapter {

    // The class' private variables
    private Context context;
    private List<String> _listDataHeader;
    private HashMap<String, List<String>> _listDataChild;

// ###########################################################
//    The constructor, setting the correct Context, the list of header data and the map of child data.
// ###########################################################
    public ExpandableListAdapter(Context context, List<String> listDataHeader, HashMap<String,
            List<String>> listChildData) {
        this.context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
    }

// ###########################################################
//    Required overrides to implement a class that extends BaseExpandableListAdapter:
// ###########################################################
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    // This function creates and customizes the view for each child item in the ExpandableListView
    // being created, then returns that view. There are two modes, each customizing the child views
    // differently.
    // Normal Mode:
    //     - The child items are selectable
    //     - The text of every child item has 100% alpha (no transparency)
    //     - The download_or_delete_icon is hidden
    //     - The download_or_delete_icon is not selectable
    // Manage Downloads Mode:
    //     - The child items are not selectable (they are but it doesn't do anything)
    //     - Only the text of the child items with available downloads have 100% alpha
    //     - The texts of child items without available downloads have 45% alpha
    //     - The download_or_delete_icon is shown
    //     - The download_or_delete_icon is selectable
    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, final ViewGroup parent) {

        final String childText = (String) getChild(groupPosition, childPosition);
        final LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context
                .LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.expandable_list_item, null);
        }

        final View simple_dialog = layoutInflater.inflate(R.layout.dialog_simple, null);
        final TextView message = (TextView) simple_dialog.findViewById(R.id.message);

        // This determines the on touch behavior for the list items when in the different modes.
        // Normal Mode:
        //  - Items are selectable
        //  - Items are highlighted when selected
        // Manage Downloads Mode:
        //  - Items do nothing on select (including highlight)
        convertView.setOnTouchListener(null); // Removes any previous listeners to make this one
        // primary.
        VideosListActivity.expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        selectVideo(groupPosition, childPosition, layoutInflater);
        return true;
        }
        });

        TextView txtListChild = (CustomTextView) convertView.findViewById(R.id.lblListItem);
        txtListChild.setText(childText);
        txtListChild.setTextColor(Color.BLACK);

        // Set temporary values needed
        VideosListActivity.video_name = childText;
        VideosListActivity.project_name = _listDataHeader.get(groupPosition);
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    // This function creates and customizes the view for each group item in the ExpandableListView
    // being created, then returns that view. This is the same for both normal and manage downloads
    // mode.
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_group, null, false);
        }

        TextView lblListHeader = (TextView) convertView.findViewById(R.id.title);
        lblListHeader.setText(headerTitle);
        lblListHeader.setTextSize(25);
        ImageView lblHeaderIcon = (ImageView) convertView.findViewById(R.id.icon);
        lblHeaderIcon.setImageBitmap(Downloader.RPMap.get(VideosListActivity.videoListToRPMap.get(groupPosition)).image);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

// ###########################################################
//    Helper functions:
// ###########################################################
    // selectVideo
    // This function is called when in normal mode and a child item is selected. It starts the
    // VideoActivity to play the video, only after setting the needed values.
    private void selectVideo(int groupPosition, int childPosition, LayoutInflater layoutInflater) {
        if (YouTubeApiServiceUtil.isYouTubeApiServiceAvailable(context).equals(
                YouTubeInitializationResult.SUCCESS)) {
            // Reset values needed
            int realIndex = VideosListActivity.videoListToRPMap.get(groupPosition);
            VideosListActivity.video_name = Downloader.RPMap.get(realIndex).videos.get(childPosition).youtube;
            Log.d("VIDEO", "selectVideo: "+groupPosition+" - "+childPosition);
            Intent intent = new Intent(context, VideoActivity.class);
            context.startActivity(intent);
        } else {
            View simple_dialog = layoutInflater.inflate(R.layout.dialog_simple, null);
            TextView message = (TextView) simple_dialog.findViewById(R.id.message);
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            message.setText("Device must have recent version of YouTube app to play video.");
            builder.setPositiveButton("View in Play Store",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse("market://details?id=com.google.android." +
                                            "youtube"));
                            context.startActivity(intent);
                        }
                    })
                    .setNegativeButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // The user doesn't want to deal with it right now.
                        }
                    });
            builder.setView(simple_dialog);
            builder.show();
        }
    }
}
