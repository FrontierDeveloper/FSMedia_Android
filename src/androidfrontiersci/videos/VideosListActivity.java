package androidfrontiersci.videos;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidfrontiersci.Download.Downloader;
import androidfrontiersci.Download.FSVideo;
import androidfrontiersci.Download.ResearchProject;
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
    public static ArrayList<Integer> videoListToRPMap;

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
            expListView.setSelection(MainActivity.index);
            expListView.expandGroup(MainActivity.index);
        }
    }

    // prepareListData
    // This function is called from onCreate(). It populate the listDataHeader list and the
    // listDataChild map with the data from the displayable_categories list and the
    // ResearchCategories map.
    private void prepareListData() {
        listDataChild = new HashMap<String, List<String>>();
        listDataHeader = new ArrayList<String>();
        videoListToRPMap = new ArrayList<Integer>();
        String title = "";

        for (int i = 0; i < Downloader.RPMap.size(); i++) {
            // if the project does not have any videos, skip it.
            if (Downloader.RPMap.get(i).videos.size() == 0) {
                Log.d("continue", "No videos, project:  " + Downloader.RPMap.get(i).title);
                continue;
            }
            videoListToRPMap.add(i);
            // load the project videos
            ResearchProject RP = Downloader.RPMap.get(i);
            title = RP.title;
            listDataHeader.add(title);
            List<String> project = new ArrayList<String>();
            for (FSVideo video : RP.videos) {
                project.add(video.title);
            }
            listDataChild.put(title, project);
        }
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
