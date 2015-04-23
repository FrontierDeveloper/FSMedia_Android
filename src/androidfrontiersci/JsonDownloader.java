package androidfrontiersci;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.util.ByteArrayBuffer;

/*
    This is the XmlDownloader class, an asynchronous task.
    It is executed either when dumpedSelectQuery.xml has not yet been saved to the device or when an
    updated version of the file is available. This task very simply downloads and stores
    dumpedSelectQuery.xml.
*/
public class JsonDownloader extends AsyncTask<Void, Void, Void> {

    /*
        The only public variable to accessed elsewhere in the project, defined in MainActivity.java.
    */
    public AsyncFollowUp delegate = null;

    // The class' private variables
    private static final String TAG = "JsonDownloader";
    private static final String Server_URL = "http://frontsci.arsc.edu/frontsci/frontSciData.json";
    Context context;

    /*
        The constructor, setting the correct Context.
    */
    public JsonDownloader(Context context) {
        this.context = context;
    }

    /*
        AsyncTask's usual doInBackground() function.
        It downloads and stores dumpedSelectQuery.xml.
    */
    @Override
    protected Void doInBackground(Void... params) {
        MainActivity.frontSciData = new File(context.getFilesDir(), "frontSciData.json");
        try {
            URL url = new URL(Server_URL);

            Log.e(TAG, "Download beginning...");
            Log.e(TAG, "Download url: " + url);
            Log.e(TAG, "File name: " + "frontSciData.json");
            // Open a connection to that URL.
            URLConnection ucon = url.openConnection();

            // Define InputStreams to read from the URLConnection.
            InputStream is = ucon.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);

            // Read bytes to the Buffer until there is nothing more to read(-1).
            ByteArrayBuffer baf = new ByteArrayBuffer(50);
            int current = 0;
            while ((current = bis.read()) != -1) {
                baf.append((byte) current);
            }

            // Convert the Bytes read to a String.
            FileOutputStream fos = new FileOutputStream(MainActivity.frontSciData);
            fos.write(baf.toByteArray());
            fos.close();
            Log.e(TAG, "Download complete.");

        } catch (IOException e) {
            Log.d(TAG, "Error: " + e);
        }
        return null;
    }

    /*
        onPostExecute
        After the task is completed, the correct interface function is called to parse the xml file.
        If the file was already stored but needed to be updated, it was parsed before this task was
        executed. Thus, it must be re-parsed. If the file was not previously stored, it can be parsed
        for the first time.
        The distinction here is made because the task of one instance of a class that extends AsyncTask
        cannot be executed more than once. So, if the parsing must be done twice, separate instances
        must be used.
    */
    @Override
    protected void onPostExecute(Void result) {
        if (!MainActivity.updated) {
            delegate.postDownloadParse();
        } else {
            delegate.reparseXML();
        }
    }
}