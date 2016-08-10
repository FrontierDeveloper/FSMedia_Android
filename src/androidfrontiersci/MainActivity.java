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

import androidfrontiersci.Download.JSONParser;
import androidfrontiersci.about.AboutActivity;
import androidfrontiersci.articles.ArticlesXmlParser;
import androidfrontiersci.askascientist.AskAScientistActivity;
import androidfrontiersci.listviews.CustomListViewAdapter;
import androidfrontiersci.listviews.RowItem;
import androidfrontiersci.maps.MapsActivity;
import androidfrontiersci.research.ResearchActivity;
import androidfrontiersci.videos.VideosListActivity;
import androidfrontiersci.Download.Downloader;

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
public class MainActivity extends FragmentActivity {

/*
    These are variables accessed from throughout the project, initialized here to be as global
    variables.
*/
	public static boolean fromResearch = false;
    public static boolean fromVideosOrMaps = false;
    public static boolean manageDownloads = false;
    public static boolean forArticlesList = false;
    public static boolean updated = false;
    public static boolean offlineMode = false;
    public static String fileName = "frontSciData.json";
    public static File frontSciData;
    public static Map<String, String> downloaded_videos = new HashMap<String, String>();
    public static List<String> old_articles = new ArrayList<String>();
    public static int index = 0;

    public static String jsonString = "";

    // The class' private variables
    public static CustomProgressDialog progress;
    private static ListView MainMenu;
    private Downloader downloader;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen); // Open the app with the splash screen.

        downloader = new Downloader(this);

        // This Runnable simply changes the content view to the MainActivity layout.
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                MainActivity.this.setContentView(R.layout.activity_main);
            }
        };

        android.os.Handler handler = new android.os.Handler();
        // Run the Runnable after 1.5 seconds, changing the
        // content view away from the splash screen.
        handler.postDelayed(runnable, 1500);


        // The file object is defined either as the persistent file in place or is non-existent
        progress = new CustomProgressDialog(MainActivity.this); // Set progress to custom dialog.
        progress.show();
        if (isNetworkAvailable()) {
            downloader.execute("http://frontierscientists.com/api/get_posts/?post_type=projects&count=100");
        } else {
            View simple_dialog = getLayoutInflater().inflate(R.layout.dialog_simple, null);
            TextView message = (TextView) simple_dialog.findViewById(R.id.message);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            message.setText("Frontier Science Media requires an internet connection to run!");
            builder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(418);
                        }
                    });
            builder.setView(simple_dialog);
            builder.show();
        }
    }

/*
    These are the interface functions, declared in AsyncFollowUp.java.
    They are called in the onPostExecute() functions of the various classes that extend
    AsyncTask.
*/
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
            MainActivity.index = 0;
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