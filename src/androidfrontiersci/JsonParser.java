// JsonParser.java

package androidfrontiersci;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.String;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/*
    This is the JsonParser class, an asynchronous task.
    It is executed after an updated version of frontSciData.json is confirmed on the device.
    This task opens a stream from the internally stored json file and parses the content, creating
    maps of the data. These maps contain the information of the file and provide a means of
    obtaining that information.
*/
public class JsonParser extends AsyncTask<Void, Void, Void> {

    /*
        These are public variables to be accessed elsewhere in the project.
        The entirety of the incoming data is contained in the maps below. The structure of the
        incoming data is further explained on the wiki:
            https://intrawiki.arsc.edu/index.php/Frontier_Scientists_Android_App_Development
        The ProjectData map contains the project information.
        The displayable_categories list is a list of all the titles of the research projects in
        alphabetical order. To display the project titles in order, this list is used.
        The ask_a_scientist_info map contains the scientist information.
        The about_page_info map contains the About page information.
        And the final item is an interface defined in MainActivity.java.
    */
    public static Map<String, Object> ProjectData = new HashMap<String, Object>();
    public static List<String> displayable_categories = new ArrayList<String>();
    public static Map<String, String> ask_a_scientist_info = new HashMap<String, String>();
    public AsyncFollowUp delegate = null;

    // The class' private variables
    private static String next_update = "00/00/00";
    private static final String TAG = "JsonParser";
    Context context;

    /*
        The constructor, setting the correct Context.
    */
    public JsonParser(Context context) {
        this.context = context;
    }

    /*
        AsyncTask's usual doInBackground() function.
        It begins by attempting to access the json file and, upon success, continues by parsing the
        file.
    */
    @Override
    protected Void doInBackground(Void... params) {
        Log.i(TAG, "Now accessing...");
        String json_input = "";

        try {
            json_input = readFileAsString(MainActivity.frontSciData.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> inputData = new Gson().fromJson(json_input, type);

        // Assign all incoming data to their designated holders.
        ProjectData = (Map<String, Object>) inputData.get("project_data");
        next_update = (String) inputData.get("next_update");
        ask_a_scientist_info = (Map<String, String>) inputData.get("scientist");

        // Create an ordered list of project titles.
        if (displayable_categories.isEmpty()) {
            for (Entry<String, Object> project : ProjectData.entrySet()) {
                displayable_categories.add(project.getKey());
            }
        }
        Collections.sort(displayable_categories);

        Log.i(TAG,"Parsed");
        return null;
    }

    /*
        Helper functions:
    */
    // isNetworkAvailable
    private boolean isNetworkAvailable() {
        // Get connectivity information
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
    // readFileAsString
    private String readFileAsString(String filePath) throws IOException {
        StringBuffer fileData = new StringBuffer();
        BufferedReader reader = new BufferedReader(
                new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }

    /*
        onPostExecute
        After the parsing and map building is completed, the last_update has been received. It is then
        checked and the decision is made to either re-download and re-parse dumpedSelectQuery.xml (in
        the case that an update is needed) or continue on to processing the images.
    */
    @Override
    protected void onPostExecute(Void result) {
        Date last_update_date = new Date();

        try {
            last_update_date = new SimpleDateFormat("MM/dd/yyyy").parse(next_update);
        } catch (Exception e) {
            Log.e(TAG, "Could not parse date.");
            e.printStackTrace();
        }

        Date today = new Date();

        if (last_update_date.before(today)) {
            // The alreadyDownloaded flag is in place to avoid a crash in the case that the xml file
            // is not being updated, meaning the last_update field of the file is an old date. In
            // this case, the file will be parsed, the date recognized as old, the file
            // re-downloaded and again recognized as old. This will cause the same instance of the
            // XmlDownloader class to attempt to execute twice, causing the crash. With the flag,
            // this block will never be entered twice, solving the problem.
            if (isNetworkAvailable() && !MainActivity.alreadyDownloaded) {
                MainActivity.alreadyDownloaded = true;
                delegate.downloadXML();
                Log.e(TAG, "Downloading updated XML.");
            } else {
                Log.e(TAG, "Displaying old content.");
                delegate.postParseImageDownload();
            }
        } else {
            delegate.postParseImageDownload();
        }
    }

}
