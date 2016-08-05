// ImageProcessor.java

package androidfrontiersci;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.String;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

/*
    This is the ImageProcessor class, an asynchronous task.
    It is executed after the final parse of dumpedSelectQuery.xml. This task processes each of the
    image files mentioned in dumpedSelectQuery.xml. If the file is already present on the device,
    nothing is done. If not, the image file is downloaded and stored.
    This task also cleans out any files that are not specified in the saved_files list. This ensures
    old file clean up and disallows implicit file storing.
*/
public class ImageProcessor extends AsyncTask<Void, Void, Void> {

/*
    These are public variables to be accessed elsewhere in the project.
    The two maps are used in the Research section and the Drawable is accessed for the Ask a
    Scientist section. The interface is defined in MainActivity.java.
*/
    public static Map<String, Drawable> project_images = new HashMap<String, Drawable>();
    public static Map<String, Drawable> project_thumbnails = new HashMap<String, Drawable>();
    public static Drawable ask_a_scientist_photo = null;
    public AsyncFollowUp delegate = null;

    // The class' private variables
    private static final String TAG = "ImageProcessor";
    private static List<String> saved_files = new ArrayList<String>();
    private static final int THUMBNAIL_SIZE = 64;
    Context context;

/*
    The constructor, setting the correct Context.
*/
    public ImageProcessor(Context context) {
        this.context = context;
    }

/*
    AsyncTask's usual doInBackground() function.
    It processes each of the images and does the file purging.
*/
    @Override
    protected Void doInBackground(Void... params) {
        Log.d("IMAGE", "DO I RUN????? WTF IS LIFE????");
        // These file names are added to the saved_files list so they won't be purged
        saved_files.add("frontSciData.json");
        saved_files.add("old_articles.txt");
        saved_files.add("downloaded_videos.txt");

        String value = "";
        String file_name = "";
        // The files are processed for the projects...
        for (Entry<String, Object> project : JsonParser.ProjectData.entrySet()) {
            for (Entry<String, Object> entry : ((Map<String, Object>) project.getValue()).entrySet()
                    ) {
                if (entry.getKey().equals("preview_image")) {
                    value = (String) entry.getValue();
                    file_name = value.substring(value.lastIndexOf('/') + 1);
                    processImageFile(project.getKey(), file_name, value, "project");
                }
            }
        }
        // the Ask A Scientist section...
        value = JsonParser.ask_a_scientist_info.get("image");
        file_name = value.substring(value.lastIndexOf('/') + 1);
        processImageFile("", file_name, value, "scientist");

        // Delete any stored files that are not included in the list obtained from the XML
        for (String file : context.fileList()) {
            if (!saved_files.contains(file)) {
                Log.e(TAG, "Deleting file: "+file);
                context.deleteFile(file);
            }
        }

        return null;
    }

/*
    The helper function, processImageFile().
    This function takes the information provided in dumpedSelectQuery.xml and ensures that each
    image file is stored on the device. If a file is not present, it is downloaded.
    The next step is to create Drawables from the stored files, to be accessed in the building of
    other sections.
*/
    private void processImageFile(String project_title, String file_name, String url, String
            photo_type) {
        File imageFile = new File(context.getFilesDir(), file_name); // Temp file using the name
        Bitmap bitmapImage = null;
        // Check to see if the image file was already in place
        if (!imageFile.exists()) {
            // If not, download and store it internally
            Log.e(TAG, "Downloading image: "+file_name);
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(imageFile);
                bitmapImage = BitmapFactory.decodeStream(new URL(url).openStream());
                // Compress the file to save space in internal storage
                bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                fileOutputStream.close();
                Log.e(TAG, file_name+" downloaded");

            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Download failed for "+file_name);
            }
        }

        saved_files.add(file_name);

        // After that, or if the image file was already stored, read it in and put it in the map as
        // a Drawable
        try {
            // Store the image as its normal size
            bitmapImage = BitmapFactory.decodeStream(new FileInputStream(imageFile));
            ImageView imageView = new ImageView(context);
            imageView.setImageBitmap(bitmapImage);
            if (photo_type.equals("scientist")) {
                ask_a_scientist_photo = imageView.getDrawable();
            } else if (photo_type.equals("project")) {
                project_images.put(project_title, imageView.getDrawable());
                // Also store the image as a thumbnail
                bitmapImage = ThumbnailUtils.extractThumbnail(bitmapImage, THUMBNAIL_SIZE,
                        THUMBNAIL_SIZE);
                imageView.setImageBitmap(bitmapImage);
                project_thumbnails.put(project_title, imageView.getDrawable());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

/*
    onPostExecute
    After the task is completed, the function is called to hide the loading animation, start up
    tasks are complete, the UI is ready.
*/
    @Override
    protected void onPostExecute(Void result) {
        delegate.hideLoadingScreen();
    }
}