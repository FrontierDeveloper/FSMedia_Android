package androidfrontiersci.videos;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidfrontiersci.MainActivity;
import androidfrontiersci.articles.ArticlesXmlParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/*
    This is the VideoDownloader class, an asynchronous task.
    It is executed when a user selects the download icon in VideosListActivity while in manage
    downloads mode and then confirms it with download specifics in the dialog.  This task opens a
    stream with the video url, downloads the file and stores it in the movies directory of external
    storage.
*/
public class VideoDownloader extends AsyncTask<String, Void, Void> {

    public static String name_of_video_to_be_downloaded = "";

    // The class' private variables
    private static final String TAG = "VideoDownloader";
    private boolean downloaded = false;
    private boolean error = false;
    private String currently_downloading_video_name = "";
    Context context;

/*
    The constructor, setting the correct Context
*/
    public VideoDownloader(Context context) {
        this.context = context;
    }

/*
    onPreExecute
    Before the task begins, let the user know it is beginning.
*/
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // Save current video name to allow more than one at a time.
        currently_downloading_video_name = name_of_video_to_be_downloaded;
        MainActivity.downloading_videos.add(currently_downloading_video_name);
        VideosListActivity.listAdapter.notifyDataSetChanged();
        Toast toast = Toast.makeText(context, "Downloading "+ currently_downloading_video_name +
                        "...",
                Toast.LENGTH_LONG);
        toast.show();
    }

/*
    AsyncTask's usual doInBackground() function.
    It attempts to download the video file.
*/
    @Override
    protected Void doInBackground(String...params) {
        String video_address = params[0]; // Take in the address
        List<String> segments = Uri.parse(video_address).getPathSegments();
        String file_name = Uri.parse(video_address).getLastPathSegment(); // Parse out the file name
        // Saving the video file on external storage
        File video_file = new File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES),
                file_name);

        if (!video_file.exists()) {
            downloaded = true;
            try {
                URL url = new URL(video_address);

                Log.e(TAG, "Downloading "+file_name);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoOutput(true);
                connection.connect();

                // Define InputStreams to read from the URLConnection.
                InputStream is = connection.getInputStream();
                FileOutputStream fos = new FileOutputStream(video_file);

                // Read bytes to the Buffer until there is nothing more to read.
                byte[] buffer = new byte[1024];
                int len1 = 0;

                while ((len1 = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, len1);
                }
                fos.close();

                Log.e(TAG, file_name+" downloaded");
                MainActivity.downloaded_videos.put(currently_downloading_video_name,
                        video_file.getAbsolutePath());
            } catch (IOException e) {
                error = true;
                Log.d(TAG, "Error: " + e);
            }
        }
        return null;
    }

/*
    onPostExecute
    After the task has completed, reveal the results to the user.
*/
    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        // The repetition here is intentional.
        // If a single Toast object is declared with the current context, a RuntimeException is
        // thrown.
        if (!downloaded && !error) { // If the file already existed
            Toast toast = Toast.makeText(context, currently_downloading_video_name
                    + " already downloaded.", Toast.LENGTH_LONG);
            toast.show();
        } else if (error) { // If the file didn't exist but there was a problem with the download
            Toast toast = Toast.makeText(context, "Problem downloading "
                    + currently_downloading_video_name + ".", Toast.LENGTH_LONG);
            toast.show();
        } else { // If the file didn't exist and there were no problems with the download
            Toast toast = Toast.makeText(context, currently_downloading_video_name + " downloaded.",
                    Toast.LENGTH_LONG);
            toast.show();
        }
        MainActivity.downloading_videos.remove(currently_downloading_video_name);
        VideosListActivity.listAdapter.notifyDataSetChanged(); // Refresh the ListView to change the
                                                               // download_or_delete_icon.
        updateDownloadedVideosFile();
    }

    // updateDownloadedVideosFile()
    // This function disallows partial downloads. If a download is cut short because the process is
    // terminated from above, the video name is not added to the downloaded_videos.txt file and is
    // therefore not recognized as a downloaded video.
    private void updateDownloadedVideosFile() {
        String string = "";

        for (String video_name : MainActivity.downloaded_videos.keySet()) {
            string += video_name+"\n";
        }

        // Write the output string to the downloaded_videos.txt file
        byte[] data = string.getBytes();
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(new File(context.getFilesDir(),
                    "downloaded_videos.txt"));
            outputStream.write(data);
            outputStream.flush();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}