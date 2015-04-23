package androidfrontiersci.listviews;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidfrontiersci.ImageProcessor;
import androidfrontiersci.JsonParser;
import androidfrontiersci.MainActivity;
import androidfrontiersci.textviews.CustomTextView;
import androidfrontiersci.videos.VideoActivity;
import androidfrontiersci.videos.VideoDeleter;
import androidfrontiersci.videos.VideoDownloader;
import androidfrontiersci.videos.VideosListActivity;

import com.example.androidfrontiersci.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
    This is the ExpandableListAdapter class, used by VideosListActivity.java. It allows for the
    creation of an ExpandableListView that includes icons in the group items as well as the standard
    TextView.
*/
public class ExpandableListAdapter extends BaseExpandableListAdapter {

/*
    This public variable is defined in this class and accessed in VideoActivity.java.
*/
    public static String current_research_category;

    // The class' private variables
    private static String hd_address;
    private static String compressed_address;
    private Context context;
    private List<String> _listDataHeader;
    private HashMap<String, List<String>> _listDataChild;

/*
    The constructor, setting the correct Context, the list of header data and the map of child data.
*/
    public ExpandableListAdapter(Context context, List<String> listDataHeader, HashMap<String,
            List<String>> listChildData) {
        this.context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
    }

/*
    Required overrides to implement a class that extends BaseExpandableListAdapter:
*/
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
        if (!MainActivity.manageDownloads) {
            convertView.setOnTouchListener(null); // Removes any previous listeners to make this one
                                                  // primary.
            VideosListActivity.expListView.setOnChildClickListener(new ExpandableListView
                    .OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    selectVideo(parent.getExpandableListAdapter().getChild(groupPosition,
                            childPosition).toString(), groupPosition);
                    return true;
                }
            });
        } else { // When in manage downloads mode...
            convertView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true; // do nothing on touch.
                }
            });
        }

        TextView txtListChild = (CustomTextView) convertView.findViewById(R.id.lblListItem);
        txtListChild.setText(childText);
        txtListChild.setTextColor(Color.BLACK);

        final ImageView download_or_delete_icon = (ImageView) convertView.findViewById(
                R.id.download_or_delete);
        final ProgressBar spinner = (ProgressBar) convertView.findViewById(R.id.spinner);

        // Set temporary values needed
        VideosListActivity.video_name = childText;
        VideosListActivity.project_name = _listDataHeader.get(groupPosition);
        String hd_address = (String) ((Map<String, Object>) ((Map<String, Object>) ((Map<String,
                Object>) JsonParser.ProjectData.get(VideosListActivity.project_name)).get("videos"))
                .get(childText)).get("MP4");
        String compressed_address = (String) ((Map<String, Object>) ((Map<String, Object>) (
                (Map<String, Object>) JsonParser.ProjectData.get(VideosListActivity.project_name))
                .get("videos")).get(childText)).get("compressedMP4");

        if (MainActivity.manageDownloads) {
            if (!(hd_address.equals("") && compressed_address.equals(""))) { // If in manage
                                                                             // downloads mode and
                                                                             // there are available
                                                                             // downloads...
                download_or_delete_icon.setVisibility(ImageView.VISIBLE); // show the icon.

                // The source of the icon is decided by whether the current video is among the
                // downloads or not.
                if (MainActivity.downloaded_videos.keySet().contains(childText)) { // If it is...
                    download_or_delete_icon.setImageResource(R.drawable.delete_icon); // make the
                                                                                      // icon the
                                                                                      // trash can.
                    download_or_delete_icon.setTag(R.drawable.delete_icon);
                } else { // If it hasn't been downloaded...
                    download_or_delete_icon.setImageResource(R.drawable.download_icon); // make the
                                                                                        // icon the
                                                                                        // download
                                                                                        // button.
                    download_or_delete_icon.setTag(R.drawable.download_icon);
                }

                spinner.setVisibility(ProgressBar.INVISIBLE);

                if (MainActivity.downloading_videos.contains(childText) || MainActivity
                        .deleting_videos.contains(childText)) { // If the item is currently in line
                                                                // to be downloaded or deleted...
                    spinner.setVisibility(ProgressBar.VISIBLE);
                    download_or_delete_icon.setClickable(false);
                    return convertView; // don't let it do anything else.
                }

                download_or_delete_icon.setOnClickListener(new ImageView.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Integer integer = (Integer) download_or_delete_icon.getTag();
                        int id = integer == null ? 0 : integer;
                        VideosListActivity.video_name = childText;
                        VideosListActivity.project_name = _listDataHeader.get(groupPosition);

                        switch (id) {
                            case R.drawable.delete_icon: // If the icon shown is the delete_icon...
                                                         // delete the current video.
                                VideoDeleter.deleting_video_name = VideosListActivity.video_name;
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                                message.setText("Delete " + VideoDeleter.deleting_video_name + "?");
                                builder.setPositiveButton("Delete", new
                                                DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                VideoDeleter deleter = new VideoDeleter(context);
                                                deleter.execute();
                                            }
                                        })
                                        .setNegativeButton("Cancel", new
                                                DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // User cancelled the dialog
                                            }
                                        });
                                builder.setView(simple_dialog);
                                builder.show();
                                break;
                            case R.drawable.download_icon: // If it's the download_icon, start the
                                                           // download dialog.
                                startDialog(childText, groupPosition, layoutInflater, spinner);
                                break;
                        }
                    }
                });
            } else { // If in manage downloads mode but no downloads are available...
                txtListChild.setTextColor(Color.argb(45, Color.BLACK, 0, 0)); // set titles as
                                                                              // faded...
                download_or_delete_icon.setVisibility(ImageView.INVISIBLE); // and hide...
                spinner.setVisibility(ProgressBar.INVISIBLE);
                download_or_delete_icon.setClickable(false); // and set the icon to un-selectable.
            }
        } else { // If not in manage downloads mode...
            download_or_delete_icon.setVisibility(ImageView.INVISIBLE); // or hide if not...
            spinner.setVisibility(ProgressBar.INVISIBLE);
            download_or_delete_icon.setClickable(false); // and set the icon to un-selectable.
        }
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
        lblHeaderIcon.setImageDrawable(ImageProcessor.project_thumbnails.get(headerTitle));

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

/*
    Helper functions:
*/
    // selectVideo
    // This function is called when in normal mode and a child item is selected. It starts the
    // VideoActivity to play the video, only after setting the needed values.
    private void selectVideo(String video_name, int groupPosition) {
        // Reset values needed
        VideoActivity.video_name = video_name;
        System.out.println(VideoActivity.video_name);
        VideosListActivity.project_name = _listDataHeader.get(groupPosition);
        current_research_category = MainActivity.getResearchCategory(
                VideosListActivity.project_name);
        Intent intent = new Intent(context, VideoActivity.class);
        context.startActivity(intent);
    }
    // startDialog
    // This function is called when in manage downloads mode and the download_or_delete_icon is
    // pressed while it is the download_icon. The function starts a dialog to determine how to
    // continue with the download and then, when the information is given, proceeds with the
    // download.
    private void startDialog(String video_name, final int groupPosition, LayoutInflater
            layoutInflater, final ProgressBar spinner) {

        // Reset values needed
        VideoDownloader.name_of_video_to_be_downloaded = video_name;
        VideosListActivity.project_name = _listDataHeader.get(groupPosition);
        current_research_category = MainActivity.getResearchCategory(
                VideosListActivity.project_name);
        hd_address = (String) ((Map<String, Object>) ((Map<String, Object>) ((Map<String,
                Object>) JsonParser.ProjectData.get(VideosListActivity.project_name)).get("videos"))
                .get(video_name)).get("MP4");
        compressed_address = (String) ((Map<String, Object>) ((Map<String, Object>) (
                (Map<String, Object>) JsonParser.ProjectData.get(VideosListActivity.project_name))
                .get("videos")).get(video_name)).get("compressedMP4");

        View simple_dialog = layoutInflater.inflate(R.layout.dialog_simple, null);
        View choose_dialog = layoutInflater.inflate(R.layout.dialog_choose, null);
        TextView simple_message = (TextView) simple_dialog.findViewById(R.id.message);
        TextView choose_message = (TextView) choose_dialog.findViewById(R.id.message);

        if (isExternalStorageWritable()) {
            List<String> download_options_list = new ArrayList<String>();

            // If the "MP4" field in the XML was populated, it will be added to the options for
            // downloads.
            if (!hd_address.equals("")) {
                download_options_list.add("HD Video");
            }
            // Likewise, the compressed video will be added to the options if its field in the XML
            // was populated.
            if (!compressed_address.equals("")) {
                download_options_list.add("Compressed Video");
            }

            final String[] download_options = download_options_list.toArray(
                    new String[download_options_list.size()]);

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            final VideoDownloader downloader = new VideoDownloader(context);

            switch (download_options.length) {
                // If there is only one option, the options are take it or leave it
                case 1:
                    simple_message.setText("Download "+download_options[0]+"?");
                    builder.setPositiveButton("Download", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    spinner.setVisibility(ProgressBar.VISIBLE);
                                    if (download_options[0].equals("HD Video")) {
                                        downloader.execute(hd_address);
                                    } else {
                                        downloader.execute(compressed_address);
                                    }
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // User cancelled the dialog
                                }
                            });
                    builder.setView(simple_dialog);
                    builder.show();
                    break;
                // If both types are available, the user is asked to select which one they want
                case 2:
                    choose_message.setText("Choose a download type");
                    builder.setView(choose_dialog);

                    final LinearLayout hd_option = (LinearLayout) choose_dialog.findViewById(R.id
                            .option1);
                    final LinearLayout compressed_option = (LinearLayout) choose_dialog
                            .findViewById(R.id.option2);

                    final AlertDialog dialog = builder.create();

                    hd_option.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            spinner.setVisibility(ProgressBar.VISIBLE);
                            downloader.execute(hd_address);
                            dialog.cancel();
                        }
                    });
                    compressed_option.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            spinner.setVisibility(ProgressBar.VISIBLE);
                            downloader.execute(compressed_address);
                            dialog.cancel();
                        }
                    });

                    dialog.show();
                    break;
            }
        } else {
            Toast toast = Toast.makeText(context, "External storage not enabled.", Toast.LENGTH_LONG);
            toast.show();
        }
    }
    // isExternalStorageWritable
    // This function is called from startDialog(). It returns true if external storage is writable
    // and false if it is not.
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
}
