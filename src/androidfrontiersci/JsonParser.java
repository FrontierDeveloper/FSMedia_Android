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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

//import org.apache.commons.collections4.MapUtils;

/*
    This is the XmlParser class, an asynchronous task.
    It is executed after an updated version of dumpedSelectQuery.xml is confirmed on the device.
    This task opens a stream from the internally stored xml file and parses the content by research
    category, creating maps of the data along the way. These maps contain the information of the
    file and provide a means of obtaining that information.
*/
public class JsonParser extends AsyncTask<Void, Void, Void> {

    /*
        These are public variables to be accessed elsewhere in the project.
        The ResearchCategories map contains the entirety of the xml data, organized by research
        category. The structure of this map is further explained on the wiki:
            https://intrawiki.arsc.edu/index.php/Frontier_Scientists_Android_App_Development
        The following three maps are used in ImageProcessor.java. They provide the data needed for each
        image to be processed.
        The displayable_categories list is a list of all the titles of the research projects. For
        example, the research category "alaskasunmannedflightresearch" might have the displayable
        category "Alaska's Unmanned Flight Research". These are used, hopefully obviously, when the
        categories are displayed.
        The next three resources listed here are used in the Ask a Scientist section.
        The about_page_opening String is simply that, a string of the About page content.
        And the final item is an interface defined in MainActivity.java.
    */
    public static Map<String, Object> ProjectData = new HashMap<String, Object>();
    public static List<String> displayable_categories = new ArrayList<String>();
    public static Map<String, String> ask_a_scientist_info = new HashMap<String, String>();
    public static Map<String, Object> about_page_info = new HashMap<String, Object>();
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
        It begins by attempting to access the xml file and, upon success, continues by parsing the file.
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

        ProjectData = (Map<String, Object>) inputData.get("android");
        next_update = (String) inputData.get("next_update");
        ask_a_scientist_info = (Map<String, String>) inputData.get("scientist");
        about_page_info = (Map<String, Object>) inputData.get("about");

        if (displayable_categories.isEmpty()) {
            for (Entry<String, Object> project : ProjectData.entrySet()) {
                displayable_categories.add(project.getKey());
            }
        }
        Collections.sort(displayable_categories);

        Log.i(TAG,"Parsed");
        return null;
    }

//    /*
//        Main content and helper functions:
//    */
//    // tryAccessingXml
//    // This function opens the stream with the xml file, initializes the XmlPullParser to be used
//    // throughout the parsing process and returns that XmlPullParser.
//    private XmlPullParser tryAccessingXml() {
//        try {
//            Log.i(TAG,"Now accessing...");
//            InputStream input = new BufferedInputStream(new FileInputStream(
//                    MainActivity.frontSciData));
//            XmlPullParser receivedData = XmlPullParserFactory.newInstance().newPullParser();
//            receivedData.setInput(input, null);
//            return receivedData;
//        } catch (XmlPullParserException e){
//            Log.e(TAG, "XmlPullParser Exception A", e);
//        } catch (IOException e) {
//            Log.e(TAG, "XmlPullParser Exception B", e);
//        }
//        return null;
//    }
//    // tryParsingXmlData
//    // This function simply calls processReceivedData() and handles possible errors accordingly.
//    private void tryParsingXmlData(XmlPullParser receivedData) {
//        if (receivedData != null) {
//            try {
//                processReceivedData(receivedData);
//            } catch (XmlPullParserException e) {
//                Log.e(TAG, "Pull Parser failure", e);
//            } catch (IOException e) {
//                Log.e(TAG, "IO Exception parsing XML", e);
//            }
//        }
//    }
//    // processReceivedData
//    // This function takes in an XmlPullParser, walks through the data and stores that data in the
//    // in the aforementioned maps, populating the displayable_categories list as a part of this
//    // process.
//    private void processReceivedData(XmlPullParser receivedData)  throws XmlPullParserException,
//            IOException {
//        int recordsFound = 0;
//        int eventType = -1;
//        while (eventType != XmlResourceParser.END_DOCUMENT) {
//            if (eventType == XmlResourceParser.START_TAG) {
//                if (receivedData.getName() != null && !receivedData.getName().equals("research")) {
//                    if (receivedData.getName().equals("AboutPage") || receivedData.getName()
//                            .equals("last_update")) {
//                        eventType = processText(receivedData, eventType, receivedData.getName());
//                    } else {
//                        eventType = processBlock(receivedData, eventType, receivedData.getName());
//                    }
//                }
//            }
//
//            eventType = receivedData.next();
//            recordsFound++;
//        }
//
//        Log.i(TAG, "Finished processing "+recordsFound+" records.");
//
//        // Build ResearchCategories map
//        buildMap();
//
//        // Get a list of the project titles, used in the navigation drawer of the Research section
//        // and the ExpandableListView of the Videos section.
//        for (Entry<String, Map<String, Map<String, Map<String, String>>>> entry :
//                ResearchCategories.entrySet()) {
//            for (Entry<String, Map<String, String>> project : entry.getValue().get("projects")
//                    .entrySet()) {
//                String name = project.getValue().get("project_title");
//                if (!displayable_categories.contains(name)) { // If not already there...
//                    displayable_categories.add(name);
//                }
//            }
//        }
//        Collections.sort(displayable_categories);
//
//        Log.e(TAG, "XML last updated: "+last_update);
//        // Print the map real pretty like
////		for (Entry<String, Map<String, Map<String, Map<String, String>>>> entry :
////              ResearchCategories.entrySet()) {
////		    System.out.println(entry.getKey()+"!!!!!!!!!!!!!!!!!!!!!!!!!!!");
////			MapUtils.debugPrint(System.out, "", entry.getValue());
////		}
//    }
//    // processBlock
//    // This function is called  from processReceivedData(). It is called when the parser is at the
//    // beginning of a block of data: "projects", "videos", "maps" or "scientist_on_call". The
//    // function steps through the block, storing the data, and returns when the block hits its end
//    // tag.
//    private int processBlock(XmlPullParser receivedData, int eventType, String block_type) throws
//            XmlPullParserException, IOException {
//        Map<String, String> data = new HashMap<String, String>();
//        String title = "";
//
//        // Find next block with a defined name
//        while (true) {
//            eventType = receivedData.next();
//            if (receivedData.getName() != null) {
//                break;
//            }
//        }
//
//        while (!receivedData.getName().equals(block_type)) {
//            boolean end_tag = false;
//            String key = "";
//            String value = "";
//
//            while (!end_tag) {
//                if (eventType == XmlResourceParser.START_TAG) {
//                    if (receivedData.getName() != null) {
//                        key = receivedData.getName();
//                    }
//                } else if (eventType == XmlResourceParser.TEXT) {
//                    if (receivedData.getText() != null) {
//                        value = receivedData.getText();
//                    }
//                } else if (eventType == XmlResourceParser.END_TAG) {
//                    if (receivedData.getName() != null) {
//                        end_tag = true;
//                    }
//                }
//                eventType = receivedData.next();
//            }
//
//            String file_name = value.substring(value.lastIndexOf('/') + 1);
//
//            // Use the title of the project/video/map as the key in the project/video/map map
//            if (key.equals("project_title") || key.equals("video_title") ||
//                    key.equals("marker_title")) {
//                if (value.equals("Where is Lake Elâ\u0080\u0099gygytgyn?")) {
//                    value = "Where is Lake El'gygytgyn?";
//                }
//                title = value;
//            } else if (key.equals("meta_value")) {
//                project_image_urls.put(title, value);
//                project_image_file_names.put(title, file_name);
//                project_thumbnail_file_names.put(title, file_name+"-t"); // Mark thumbnails with
//                // "-t"
//            } else if (key.equals("picture")) {
//                ask_a_scientist_photo_url = value;
//                ask_a_scientist_image_file_name = file_name;
//            } else if (key.equals("utubeurl")) {
//                String video_id = value.substring(value.lastIndexOf("?v=") + 1);
//
//            }
//            data.put(key, value);
//
//            // Find next block with a defined name
//            while (true) {
//                eventType = receivedData.next();
//                if (receivedData.getName() != null) {
//                    break;
//                }
//            }
//        }
//
//        // Put temp map in projects, videos or maps
//        if (block_type.equals("projects")) {
//            projects.put(title, data);
//        } else if (block_type.equals("videos")) {
//            videos.put(title, data);
//        } else if (block_type.equals("maps")) {
//            maps.put(title, data);
//        } else if (block_type.equals("scientist_on_call")) {
//            ask_a_scientist_info = data;
//        }
//        return eventType;
//    }
//    // processText
//    // This function is called form processReceivedData(). It is called when the parser is at the
//    // beginning of a text block: "AboutPage" or "last_update". The function stores the text and
//    // returns.
//    private int processText(XmlPullParser receivedData, int eventType, String block_type) throws XmlPullParserException, IOException {
//        if (block_type.equals("AboutPage")) {
//            eventType = receivedData.next();
//            // Remove and replace special characters and beginning whitespace.
//            about_page_opening = receivedData.getText().replaceFirst("\\s*", "").replaceAll("Â", "")
//                    .replaceAll("â\u0080\u0099", "'").replaceAll("â..", "”").replaceFirst("”", "“");
//        } else if (block_type.equals("last_update")) {
//            eventType = receivedData.next();
//            last_update = receivedData.getText();
//        }
//        return eventType;
//    }
//    // buildMap
//    // This function is called from processReceivedData(). It is called when the parsing of the xml
//    // file is complete. At this point, the projects, videos and maps maps have been populated with
//    // the xml data. This function creates the ResearchCategories map by combining those three maps,
//    // organizing them under their research categories.
//    private void buildMap() {
//
//        // First, create a key for each of the research categories. This assumes that no videos or
//        // maps contain research categories that are not already represented in projects.
//        for (Iterator<Entry<String, Map<String, String>>> it = projects.entrySet().iterator();
//             it.hasNext(); ) {
//            Entry<String, Map<String, String>> entry = it.next();
//            Map<String, Map<String, Map<String, String>>> placeholder = new WeakHashMap<String,
//                    Map<String, Map<String, String>>>();
//            String research_category = entry.getValue().get("research_category");
//
//            if (ResearchCategories.get(research_category) == null) {
//                ResearchCategories.put(research_category, placeholder);
//            }
//        }
//        // Next, create a key for projects, videos and maps in each of the research categories and
//        // populate the maps according to research category
//        Iterator<Entry<String, Map<String, Map<String, Map<String, String>>>>> it =
//                ResearchCategories.entrySet().iterator();
//        while (it.hasNext()) {
//            Entry<String, Map<String, Map<String, Map<String, String>>>> entry = it.next();
//            entry.getValue().put("projects", filter(projects, entry.getKey(), "project_title"));
//            entry.getValue().put("videos", filter(videos, entry.getKey(), "video_title"));
//            entry.getValue().put("maps", filter(maps, entry.getKey(), "marker_title"));
//        }
//    }
//    // filter
//    // This function is called from buildMap(). It simply returns all the projects/videos/maps that
//    // correspond to the given research category, in the correct map structure.
//    private Map<String, Map<String, String>> filter(Map<String, Map<String, String>>
//                                                            projects_videos_maps, String research_category, String title_key) {
//        Map<String, Map<String, String>> block = new WeakHashMap<String, Map<String, String>>();
//        for (Iterator<Entry<String, Map<String, String>>> it = projects_videos_maps.entrySet()
//                .iterator(); it.hasNext(); ) {
//            Entry<String, Map<String, String>> entry = it.next();
//            if (entry.getValue().get("research_category").equals(research_category)) {
//                block.put(entry.getValue().get(title_key), entry.getValue());
//            }
//        }
//        return block;
//    }
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
