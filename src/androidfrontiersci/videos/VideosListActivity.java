package androidfrontiersci.videos;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidfrontiersci.JsonParser;
import androidfrontiersci.listviews.ExpandableListAdapter;
import androidfrontiersci.MainActivity;
import androidfrontiersci.research.ResearchActivity;

import frontsci.android.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/*
    This is the VideosListActivity, the activity started when the Videos section is selected from
    the main menu.
    Its layout file is activity_videos_list.xml.
    Its menu file is videos_list.xml.
    This class displays an ExpandableListView with all the research categories that have videos. The
    groups of the ExpandableListView are the categories and each of children of those groups are the
    videos under that category. This class works closely with ExpandableListAdapter.java to provide
    the functionality of the two modes, normal mode and manage downloads mode.  This activity
    instantiates and populates the ExpandableListView, controls the action bar and sets the flags;
    ExpandableListAdapter.java does the rest.
*/
public class VideosListActivity extends Activity {

/*
    These public variables are accessed elsewhere in the project. The first is accessed in
    ExpandableListAdapter.java. The second, more popular, is accessed in ExpandableListAdapter.java,
    VideoActivity.java, VideoDeleter.java and VideoDownloader.java. The adapter is accessed in both
    VideoDeleter.java and VideoDownloader.java to refresh the ListView.
*/
    public static String project_name = "";
    public static String video_name = "";
    public static ExpandableListAdapter listAdapter;
    public static ExpandableListView expListView;

    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videos_list);
        MainActivity.fromVideosOrMaps = true;
        // Get the ListView.
        expListView = (ExpandableListView) findViewById(R.id.expandable_listview);
        // Prepare the list data.
        prepareListData();
        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
        // Set the list adapter.
        expListView.setAdapter(listAdapter);

        final View simple_dialog = getLayoutInflater().inflate(R.layout.dialog_simple, null);
        final TextView message = (TextView) simple_dialog.findViewById(R.id.message);

        // This code is temporarily out or production, as it was causing errors. Further development
        // is to be done here.
//        expListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(final AdapterView<?> parent, final View v,
//                                           final int position, long id) {
//                if (ExpandableListView.getPackedPositionType(id) ==
//                        ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
//                    final Intent intent = new Intent(getApplicationContext(),
//                            ResearchActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    AlertDialog.Builder builder = new AlertDialog.Builder(VideosListActivity.this);
//                    message.setText("Go to project description?");
//                    builder.setPositiveButton("Proceed",
//                                    new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialog, int which) {
//                                            MainActivity.manageDownloads = false; // Leaving, so set
//                                                                                  // to false.
//                                            ResearchActivity.mTitle = (String) ((CustomTextView)
//                                                    v.findViewById(R.id.title)).getText();
//                                            startActivity(intent); // Proceed to Research section.
//                                        }
//                                    })
//                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    // The user doesn't want to go to the Research section.
//                                }
//                            });
//                    builder.setView(dialog_simple);
//
//                    builder.show();
//                    return true;
//                }
//                return false;
//            }
//        });

        if (MainActivity.fromResearch) { // Go to and expand the category from which it was linked
                                         // in the Research section.
            expListView.setSelection(listDataHeader.indexOf(ResearchActivity.mTitle));
            expListView.expandGroup(listDataHeader.indexOf(ResearchActivity.mTitle));
        }
    }

    // prepareListData
    // This function is called from onCreate(). It populate the listDataHeader list and the
    // listDataChild map with the data from the displayable_categories list and the
    // ResearchCategories map.
    private void prepareListData() {
        listDataChild = new HashMap<String, List<String>>();
        listDataHeader = new ArrayList<String>();
        String title = "";

        if (MainActivity.offlineMode) {
            // Create an ordered list of projects with downloaded videos
            List<String> projects_with_downloads = new ArrayList<String>();
            for (Map.Entry<String, String> entry : MainActivity.downloaded_videos.entrySet()) {
                for (Map.Entry<String, Object> project : JsonParser.ProjectData.entrySet()) {
                    for (Map.Entry<String, Object> video : ((Map<String, Object>) ((Map<String,
                            Object>) project.getValue()).get("videos")).entrySet()) {
                        if (entry.getKey().equals(video.getKey())) {
                            if (!projects_with_downloads.contains(project.getKey())) {
                                projects_with_downloads.add(project.getKey());
                            }
                        }
                    }
                }
            }
            Collections.sort(projects_with_downloads);

            // Populate the ExpandableListView data with the downloads of each project
            for (String project_name : projects_with_downloads) {
                for (Map.Entry<String, Object> video : ((Map<String, Object>) ((Map<String,
                        Object>) JsonParser.ProjectData.get(project_name)).get("videos"))
                        .entrySet()) {
                    if (MainActivity.downloaded_videos.keySet().contains(video.getKey())) {
                        if (listDataChild.get(project_name) == null) {
                            listDataHeader.add(project_name);
                            listDataChild.put(project_name, new ArrayList<String>());
                        }
                        listDataChild.get(project_name).add(video.getKey());
                    }
                }
            }
        } else {
            for (int i = 0; i < JsonParser.displayable_categories.size(); i++) {
                title = JsonParser.displayable_categories.get(i);

                // Research categories with no corresponding videos are left off the list
                if (!((Map<String, Map<String, Object>>) JsonParser.ProjectData.get(title)).get(
                        "videos").isEmpty()) {
                    listDataHeader.add(title);
                    List<String> project = new ArrayList<String>();
                    for (Map.Entry<String, Map<String,String>> video : ((Map<String, Map<String,
                            String>>) ((Map<String, Object>) JsonParser.ProjectData.get(title)).get(
                            "videos")).entrySet()) {
                        if (video.getValue().get("utubeurl").contains("youtube")) {
                            project.add(video.getKey());
                        }

                    }
                    listDataChild.put(title, project);
                }
            }
        }
        for (Map.Entry<String, List<String>> child_list : listDataChild.entrySet()) {
            Collections.sort(child_list.getValue()); // Alphabetize each group of videos.
        }
    }

/*
    The action bar functions:
*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.videos_list, menu);
        MenuItem manage_downloads = menu.findItem(R.id.manage_downloads);
        manage_downloads.setTitle("Manage Downloads"); // Set action bar button title to "Manage
                                                       // Downloads" when first entering the
                                                       // activity. It always starts in normal mode.
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.manage_downloads:
                if (MainActivity.manageDownloads) { // If in manage downloads mode when the button
                                                    // is pressed...
                    item.setTitle("Manage Downloads"); // reset the title...
                    MainActivity.manageDownloads = false; // reset the helper value...
                    listAdapter.notifyDataSetChanged(); // and redraw the list in normal mode.
                } else { // If in normal mode when the button is pressed...
                    item.setTitle("Done"); // reset the title...
                    MainActivity.manageDownloads = true; // reset the helper value...
                    listAdapter.notifyDataSetChanged(); // and redraw the list in manage downloads
                                                        // mode.
                    // Also, when in manage downloads mode, expand the groups.
                    for (int i = 0; i < listDataHeader.size(); ++i) {
                        expListView.expandGroup(i);
                    }
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    // When leaving the activity...
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                finish();
                MainActivity.manageDownloads = false; // ensure that the VideosListActivity will not
                                                      // be in manage downloads mode when next
                                                      // called.
                MainActivity.fromVideosOrMaps = false;
                MainActivity.offlineMode = false;
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

}
