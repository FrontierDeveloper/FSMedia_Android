package androidfrontiersci.videos;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidfrontiersci.MainActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/*
    This is the VideoDeleter class, an asynchronous task.
    It is executed when a user selects the delete icon in VideosListActivity while in manage
    downloads mode and then confirms the delete in the dialog. This task simply removes the selected
    video from storage.
*/
public class VideoDeleter extends AsyncTask<Void, Void, Void> {

    public static String deleting_video_name = "";

    // The class' private variables
    private static final String TAG = "VideoDeleter";
    private static boolean deleted = false;
    private String currently_deleting_video = "";
    Context context;

/*
    The constructor, setting the correct Context
*/
    public VideoDeleter(Context context) {
        this.context = context;
    }

/*
    onPreExecute
    Before the task begins, set the needed value and add that video to those in the deleting
    process.
*/
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        currently_deleting_video = deleting_video_name;
        MainActivity.deleting_videos.add(currently_deleting_video);
        VideosListActivity.listAdapter.notifyDataSetChanged();
    }

    /*
    AsyncTask's usual doInBackground() function.
    It attempts to delete the video file.
*/
    @Override
    protected Void doInBackground(Void...params) {
        String file_name = Uri.parse(MainActivity.downloaded_videos.get(
                currently_deleting_video)).getLastPathSegment();
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES), file_name);
        try {
            file.delete();
            deleted = true;
            Log.e(TAG, file_name+" deleted.");
            MainActivity.downloaded_videos.remove(currently_deleting_video); // Remove the entry
                                                                             // from the downloaded
                                                                             // videos map after
                                                                             // deleting.
        } catch (Exception e) {
            Log.e(TAG, file_name+" not deleted.");
            e.printStackTrace();
        }
        return null;
    }

/*
    onPostExecute
    After the task is completed, the video file has either been deleted or it hasn't.
*/
    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        if (deleted) { // If it has...
            Toast toast = Toast.makeText(context, currently_deleting_video + " deleted.",
                    Toast.LENGTH_LONG); // say so.
            toast.show();
        } else { // If the delete failed...
            Toast toast = Toast.makeText(context, "Unable to delete " + currently_deleting_video
                    + ".", Toast.LENGTH_LONG); // tell the user.
            toast.show();
        }
        MainActivity.deleting_videos.remove(currently_deleting_video);
        VideosListActivity.listAdapter.notifyDataSetChanged(); // Refresh the ListView to change the
                                                               // download_or_delete_icon
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