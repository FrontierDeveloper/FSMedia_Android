package androidfrontiersci.Download;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import androidfrontiersci.MainActivity;

public class Downloader extends AsyncTask<String, Void, Void> {


    public static ArrayList<ResearchProject> RPMap;
    public static FSScientist sciOnCall;
    private static final String TAG = "Downloader";
    private static final String Server_URL = "http://frontierscientists.com/api/get_posts/?post_type=projects&count=100";
    Context context;
    private JSONParser jsonParser;


    public Downloader(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(String... urlString) {
        try {
            jsonParser = new JSONParser(context);
            URL url = new URL(urlString[0]);
            Log.e(TAG, "Download beginning...");
            Log.e(TAG, "Download url: " + url);
            Log.e(TAG, "File name: " + "download.json");

            HttpURLConnection serverConnection = (HttpURLConnection) url.openConnection();
            BufferedReader buffRead = new BufferedReader(new InputStreamReader(serverConnection.getInputStream()));
            String inputLine = "";
            String tempString = "";
            while ((inputLine = buffRead.readLine()) != null) {
                tempString += inputLine;
                Log.e("WHILE", "JSON read line");
            }
            buffRead.close();
            MainActivity.jsonString = tempString;
            // Open a connection to that URL.
            Log.e(TAG, "Download complete.");


        } catch (IOException e) {
            Log.d(TAG, "Error: " + e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        //dismiss the dialog after the file was downloaded
        Log.wtf("async","post execute");
        jsonParser.execute(MainActivity.jsonString);
    }
}
