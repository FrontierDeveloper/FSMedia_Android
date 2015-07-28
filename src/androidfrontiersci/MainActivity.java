// MainActivity.java

package androidfrontiersci;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidfrontiersci.about.AboutActivity;
import androidfrontiersci.articles.ArticlesXmlParser;
import androidfrontiersci.askascientist.AskAScientistActivity;
import androidfrontiersci.listviews.CustomListViewAdapter;
import androidfrontiersci.listviews.RowItem;
import androidfrontiersci.maps.MapsActivity;
import androidfrontiersci.research.ResearchActivity;
import androidfrontiersci.videos.VideosListActivity;

import frontsci.android.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
    This is MainActivity, the launcher activity, the first activity run when the app starts up.
    It switches between two layout files, splash_screen.xml and activity_main.xml.  Its menu file is
    main.xml.
    When using activity_main.xml, it contains two fragments, named Background and Foreground.  The
    corresponding fragment objects in activity_main.xml have the id's background and main_container.
    The Background fragment contains two LinearLayouts with tiled images that make up the scalable
    backdrop.
    The Foreground fragment contains the ListView that is the main menu of the app.
*/
public class MainActivity extends FragmentActivity implements AsyncFollowUp {

/*
    These are variables accessed from throughout the project, initialized here to be as global
    variables.
*/
	public static boolean fromResearch = false;
    public static boolean fromVideosOrMaps = false;
    public static boolean manageDownloads = false;
    public static boolean forArticlesList = false;
    public static boolean updated = false;
    public static boolean alreadyDownloaded = false;
    public static boolean offlineMode = false;
    public static String fileName = "frontSciData.json";
    public static File frontSciData;
    public static Map<String, String> downloaded_videos = new HashMap<String, String>();
    public static List<String> downloading_videos = new ArrayList<String>();
    public static List<String> deleting_videos = new ArrayList<String>();
    public static List<String> old_articles = new ArrayList<String>();

    // The class' private variables
    private static CustomProgressDialog progress;
    private static ListView MainMenu;
    private JsonDownloader jsonDownloader;
    private JsonParser jsonParser;
    private JsonParser jsonReparser;
    private ImageProcessor imageProcessor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen); // Open the app with the splash screen.

        // This Runnable simply changes the content view to the MainActivity layout.
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                MainActivity.this.setContentView(R.layout.activity_main);
            }
        };

        android.os.Handler handler = new android.os.Handler();
        handler.postDelayed(runnable, 3000); // Run the Runnable after 3 seconds, changing the
                                             // content view away from the splash screen.

        // The file object is defined either as the persistent file in place or is non-existent
        frontSciData = new File(getFilesDir(), fileName);

        jsonDownloader = new JsonDownloader(getApplicationContext());
        jsonParser = new JsonParser(getApplicationContext());
        jsonReparser = new JsonParser(getApplicationContext());
        imageProcessor = new ImageProcessor(getApplicationContext());
        // Set up the post task delegation
        jsonDownloader.delegate = jsonParser.delegate = jsonReparser.delegate =
                imageProcessor.delegate = this;

        progress = new CustomProgressDialog(MainActivity.this); // Set progress to custom dialog.
        if (frontSciData.exists()) {
            progress.show();
            jsonParser.execute(); // Skip download, parse the stored dumpedSelectQuery and create
                                  // the maps by which the data can be easily accessed
        } else {
            checkNetworkAndDownload(); // Need internet to continue
        }
    }

/*
    These are the interface functions, declared in AsyncFollowUp.java.
    They are called in the onPostExecute() functions of the various classes that extend
    AsyncTask.
*/
    // Called from XmlDownloader.java
    public void postDownloadParse() {
        jsonParser.execute(); // The stored file is parsed right after being downloaded
    }
    // Called from XmlParser.java
    public void downloadXML() {
        updated = true;
        jsonDownloader.execute();
    }
    // Called from XmlDownloader.java
    public void reparseXML() {
        jsonReparser.execute();
    }
    // Called from XmlParser.java
    public void postParseImageDownload() {
        populateDownloadedVideos();
        imageProcessor.execute();
    }
    // Called from ImageProcessor.java
    public void hideLoadingScreen() {
        Log.e("MainThread", "UI ready!");
        progress.dismiss();
    }

/*
    Class helper functions:
*/
    // populateDownloadedVideos
    // It scans the directory in which the videos of the app are stored and puts their names and
    // paths in the downloaded_videos map. If the video's name is on the list of videos recognized
    // as downloaded, it remains on the list. Videos found in the directory that are not on the list
    // are unwanted partial downloads and excluded.
    private void populateDownloadedVideos() {
        // Get the list of video names that are recognized as downloaded.
        List<String> recognized_downloads = new ArrayList<String>();
        File downloaded_videos_file = new File(getFilesDir(), "downloaded_videos.txt");
        if (downloaded_videos_file.exists()) {
            try {
                FileReader fileReader = new FileReader(downloaded_videos_file);
                BufferedReader reader = new BufferedReader(fileReader);
                String line = "";
                while ((line = reader.readLine()) != null) {
                    recognized_downloads.add(line.replace("\n", ""));
                }
                fileReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_MOVIES) != null) {
            for (File file : getApplicationContext()
                    .getExternalFilesDir(Environment.DIRECTORY_MOVIES).listFiles()) {
                String video_name = getVideoName(file.getName());
                if (recognized_downloads.contains(video_name)) { // Add only the videos that are
                                                                 // recognized downloads, not
                                                                 // partial downloads.
                    downloaded_videos.put(video_name, file.getAbsolutePath());
                }
            }
        }
    }
    // getVideoName
    // This function is called from populateDownloadedVideos(). It takes in a video file name and
    // returns its corresponding video's title.
    private static String getVideoName(String file_name) {
        String name = "";
        // Search the map and find which video the file belongs to...
        for (Map.Entry<String, Object> project : JsonParser.ProjectData.entrySet()) {
            for (Map.Entry<String, Map<String, String>> video : ((Map<String, Map<String, String>>)
                    ((Map<String, Object>) project.getValue()).get("videos")).entrySet()) {
                String current_video_file = Uri.parse(video.getValue().get("MP4"))
                        .getLastPathSegment();
                if (file_name.equals(current_video_file)) {
                    name = video.getKey();
                }
            }
        }
        // and return that video's name.
        return name;
    }
    // checkNetwork
    // This function is called from onCreate(). If there is an internet connection, it starts the
    // xml download. If there isn't a connection, it goes through a retry dialog either until there
    // is a connection or the user exits.
    private void checkNetworkAndDownload() {
        if (!isNetworkAvailable()) {
            View simple_dialog = getLayoutInflater().inflate(R.layout.dialog_simple, null);
            TextView message = (TextView) simple_dialog.findViewById(R.id.message);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            message.setText("No internet connection.");
            builder.setPositiveButton("Try again", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                checkNetworkAndDownload(); // Run the check again
                            }
                        })
                    .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                                System.exit(0);
                            }
                        });
            builder.setView(simple_dialog);
            builder.show();
        } else {
            progress.show();
            jsonDownloader.execute();
        }
    }
    // isNetworkAvailable
    // This function is called from checkNetworkAndDownload(). It returns true if the device has a
    // network connection, false if it doesn't.
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
/*
    This is the Foreground fragment.
    It's layout file is fragment_main.xml.
    It contains a ListView with the six sections of the app:
        1. Research
        2. Videos
        3. Maps
        4. Articles
        5. Ask a Scientist
        6. About
    Selecting any of the six starts its corresponding activity.
*/
	public static class Foreground extends Fragment {

		public Foreground() {}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

            MainMenu = (ListView) inflater.inflate(R.layout.fragment_main, container, false);
			MainMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	            @Override
	            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	                selectItem(position);
	            }
	        });

            // The six titles
            String[] titles = new String[] {
					  getString(R.string.title_activity_research),
					  getString(R.string.title_activity_videos),
					  getString(R.string.title_activity_maps),
					  getString(R.string.title_activity_articles),
					  getString(R.string.title_activity_ask_a_scientist),
					  getString(R.string.title_activity_about)};

            // Their corresponding images
			Integer[] icons = { 
					  R.drawable.research_icon,
					  R.drawable.video_icon, 
					  R.drawable.map_icon, 
					  R.drawable.article_icon,
					  R.drawable.ask_a_scientist_icon,
					  R.drawable.about_icon };

			List<RowItem> rowItems = new ArrayList<RowItem>();

			for (int i = 0; i < titles.length; i++) {
				RowItem item = new RowItem(icons[i], titles[i]);
				rowItems.add(item);
			}

			CustomListViewAdapter adapter = new CustomListViewAdapter(getActivity(),
					R.layout.list_item, rowItems);

			MainMenu.setAdapter(adapter);
			
			return MainMenu;
		}
			
		private void selectItem(int position) {
		    final Intent intent;
            View simple_dialog = getActivity().getLayoutInflater().inflate(R.layout.dialog_simple,
                    null);
            TextView message = (TextView) simple_dialog.findViewById(R.id.message);

            // Get connectivity information
            ConnectivityManager cm = (ConnectivityManager)getActivity().getSystemService(
                    Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();

            switch (position) {
		        case 0:
		        	intent = new Intent(getActivity(), ResearchActivity.class);
		        	startActivity(intent);
		        	break;
		        case 1:
                    intent = new Intent(getActivity(), VideosListActivity.class);
                    if (isConnected) {
                        startActivity(intent);
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        if (downloaded_videos.isEmpty()) {
                            message.setText("No internet connection. No downloads to view.");
                            builder.setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // The user understands.
                                        }
                                    });
                        } else {
                            message.setText("No internet connection. Can only view downloaded " +
                                    "content.");
                            builder.setPositiveButton("Proceed",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            offlineMode = true;
                                            startActivity(intent); // Proceed anyway
                                        }
                                    })
                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // The user doesn't want to look at downloads.
                                        }
                                    });
                        }
                        builder.setView(simple_dialog);
                        builder.show();
                    }
		        	break;
		        case 2:
                    if (isConnected) { // If connected to the internet...
                        intent = new Intent(getActivity(), MapsActivity.class);
                        startActivity(intent); // open up the map.
                    } else { // If not...
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        // let user know they cannot view the map without a connection.
                        message.setText("Cannot view this section without internet connection.");
                        builder.setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // The user understands.
                                    }
                                });
                        builder.setView(simple_dialog);
                        builder.show();
                    }
		        	break;
		        case 3:
                    if (isConnected) { // If connected to the internet...
                        ArticlesXmlParser parser = new ArticlesXmlParser(getActivity()); // go to
                                                                                         // articles
                        parser.execute();
                    } else { // If not...
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        // let user know they cannot view them without a connection.
                        message.setText("Cannot view this section without internet connection.");
                        builder.setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // The user understands.
                                    }
                                });
                        builder.setView(simple_dialog);
                        builder.show();
                    }
		        	break;
		        case 4:
		        	intent = new Intent(getActivity(), AskAScientistActivity.class);
		        	startActivity(intent);
		        	break;
		        case 5:
		        	intent = new Intent(getActivity(), AboutActivity.class);
		        	startActivity(intent);
		        	break;
		    }
		}
	}

/*
    This is the Background fragment.
    Its layout file is main_bg.xml.
*/
    public static class Background extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.main_bg, container, false);
        }
    }

/*
    This is the CustomProgressDialog dialog.
    Its layout file is dialog_progress.xml.
*/
    public class CustomProgressDialog extends AlertDialog {

        public CustomProgressDialog(Context context) {
            super(context);
        }

        @Override
        public void show() {
            super.show();
            setContentView(R.layout.dialog_progress);
        }

    }
}