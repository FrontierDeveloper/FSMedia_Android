// ArticlesXmlParser.java

package androidfrontiersci.articles;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import frontsci.android.R;

import androidfrontiersci.MainActivity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.text.WordUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

// ###########################################################
//    This is the ArticlesXmlParser class, an asynchronous task.
//    It is executed immediately when the Articles section is selected from the main menu. This task
//    parses the xml file at http://frontierscientists.com/feed, extracting from it the ten newest
//    article titles from frontierscientists.com and their corresponding urls. It stores those values
//    in the article_urls map with the titles as the keys and the urls as the values.
//    When complete, it starts the ArticlesListActivity.
// ###########################################################
public class ArticlesXmlParser extends AsyncTask<Void, Void, Void> {

// ###########################################################
//    The public variable to be accessed from ArticlesListActivity.java.
// ###########################################################
    public static Map<String, String> article_urls = new HashMap<String, String>();

    // The class' private variables
    private static final String articles_info_url = "http://frontierscientists.com/feed";
    private static final String TAG = "ArticlesXmlParser";
    private static ArticlesProgressDialog progress;
    Context context;

// ###########################################################
//    The constructor, setting the correct Context.
// ###########################################################
    public ArticlesXmlParser(Context context) {
        this.context = context;
    }

// ###########################################################
//    onPreExecute
//    Before the task begins, show the loading animation to inform the user the articles are loading.
// ###########################################################
    @Override
    protected void onPreExecute() {
        progress = new ArticlesProgressDialog(context);
        progress.show();
    }

// ###########################################################
//    AsyncTask's usual doInBackground() function.
//    It begins by attempting to access the xml file and, upon success, continues by parsing the file,
//    much like seen in XmlParser.java.
// ###########################################################
    @Override
    protected Void doInBackground(Void... params) {
        XmlPullParser receivedData = tryAccessingXML();
        tryParsingXmlData(receivedData);
        Log.i(TAG, "Parsed");
        return null;
    }

// ###########################################################
//    Main content and helper functions:
// ###########################################################
    // tryAccessingXml
    // This function opens the stream with the xml file, initializes the XmlPullParser to be used
    // throughout the parsing process and returns that XmlPullParser.
    private XmlPullParser tryAccessingXML() {
        try {
            Log.i(TAG,"Now accessing...");
            URL url = new URL(articles_info_url);
            URLConnection connection = url.openConnection();
            InputStream input = new BufferedInputStream(connection.getInputStream());
            XmlPullParser receivedData = XmlPullParserFactory.newInstance().newPullParser();
            receivedData.setInput(input, null);
            return receivedData;
        } catch (XmlPullParserException e){
            Log.e(TAG, "XmlPullParser Exception A", e);
        } catch (IOException e) {
            Log.e(TAG, "XmlPullParser Exception B", e);
        }
        return null;
    }
    // tryParsingXmlData
    // This function simply calls processReceivedData() and handles possible errors accordingly.
    private void tryParsingXmlData(XmlPullParser receivedData) {
        if (receivedData != null) {
            try {
                processReceivedData(receivedData);
            } catch (XmlPullParserException e) {
                Log.e(TAG, "Pull Parser failure", e);
            } catch (IOException e) {
                Log.e(TAG, "IO Exception parsing XML", e);
            }
        }
    }
    // processReceivedData
    // This function takes in an XmlPullParser, walks through the data and stores the needed data in
    // the article_urls map.
    // process.
    private void processReceivedData(XmlPullParser receivedData)  throws XmlPullParserException,
            IOException {
        int recordsFound = 0;
        int eventType = -1;
        while (eventType != XmlResourceParser.END_DOCUMENT) {
            if (eventType == XmlResourceParser.START_TAG) {
                if (receivedData.getName() != null && receivedData.getName().equals("item")) {
                    eventType = processItem(receivedData, eventType);
                    recordsFound++;
                }
            }
            eventType = receivedData.next();
        }
        Log.i(TAG, "Finished processing "+recordsFound+" records.");
    }
    // processItem
    // This function is called from processReceivedData(). It is called when the parser is at an
    // item start tag, indicating a new article's data. The function walks through the data, storing
    // only the title and the url and returns when it hits the item end tag.
    private int processItem(XmlPullParser receivedData, int eventType) throws XmlPullParserException, IOException {
        String title = "";
        String url = "";

        while (true) {
            if (eventType == XmlResourceParser.START_TAG) {
                if (receivedData.getName().equals("title")) {
                    receivedData.next();
                    title = receivedData.getText(); // Store title
                    // Skip the end tag and the link's start tag, stopping at the link's text
                    for (int i = 1; i < 5; ++i) {
                        receivedData.next();
                    }
                    url = receivedData.getText(); // Store url
                    break;
                }
            }
            eventType = receivedData.next();
        }
        article_urls.put(WordUtils.capitalize(title), url); // Add stored values to the map,
                                                            // capitalizing the first letter of each
                                                            // word in the title.
        return eventType;
    }

// ###########################################################
//  onPostExecute
//  After the parsing and map building is completed, the old_articles map is populated from the
//  old_articles.txt file, the forArticlesList flag is set to true, the loading animation is hidden
//  and the ArticlesListActivity is started.
// ###########################################################
    @Override
    protected void onPostExecute(Void result) {
        // Populate the old_articles list so that the articles not in the list can be shown as new
        File file = new File(context.getFilesDir(), "old_articles.txt");
        if (file.exists()) {
            try {
                FileReader fileReader = new FileReader(file);
                BufferedReader reader = new BufferedReader(fileReader);
                String line = "";
                while ((line = reader.readLine()) != null) {
                    MainActivity.old_articles.add(line.replace("\n", ""));
                }
                fileReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Set the forArticlesList flag to true
        MainActivity.forArticlesList = true;
        // Dismiss the progress dialog
        progress.dismiss();
        // And start the ArticlesListActivity
        Intent intent = new Intent(context, ArticlesListActivity.class);
        context.startActivity(intent);
    }

    public class ArticlesProgressDialog extends AlertDialog {

        public ArticlesProgressDialog(Context context) {
            super(context);
        }

        @Override
        public void show() {
            super.show();
            setContentView(R.layout.dialog_progress);
            ((TextView) findViewById(R.id.text)).setText("Loading articles...");
        }

    }
}
