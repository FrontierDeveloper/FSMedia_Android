package androidfrontiersci.Download;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.lang.String;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import androidfrontiersci.MainActivity;
// ###########################################################
// This async activity is the JSON Parser, it parses the
// downloaded JSON and creates the RPMap array
// that is stored in Downloader
// ###########################################################
public class JSONParser extends AsyncTask<String, Void, Void> {
// ###########################################################
// Variables
// ###########################################################
    Context context;
// ###########################################################
// Functions
// ###########################################################
    public JSONParser(Context context_) {
        context = context_;
    }
    @Override
    protected Void doInBackground(String... jsonString) {
        // init RPMap
        Downloader.RPMap = new ArrayList<ResearchProject>();
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> json = new Gson().fromJson(jsonString[0], type);
        Double nPosts = (Double) json.get("count_total");
        List<Map<String, Object>> posts = (List<Map<String, Object>>) json.get("posts");
        // For all of the posts, save the data
        for (int i = 0; i < nPosts; i++) {
            // Get Project Title
            String title = (String) posts.get(i).get("title");
            title = title.replace("&#8217;", "'");
            // logging for completeness
            Log.i("RPTitle", "****************************");
            Log.i("RPTitle", "   "+title);
            Log.i("RPTitle", "****************************");
            // Get project Custom Fields
            Map<String,List<String>> customFields = (Map<String,List<String>>) posts.get(i).get("custom_fields");
            // Get project description
            String desc = customFields.get("project_description").get(0);
            Log.i("RPDesc", desc);
            // Get project map data
            String mapString = customFields.get("longlat").get(0);
            String[] mapArray = mapString.split(", ");
            FSMapData mapData;
            // If a project doesn't have a map location, place it at the center of Alaska
            if (mapArray[0].equals("TD")) {
                mapData = new FSMapData(62.89447956, -152.756170369);
            } else {
                mapData = new FSMapData(Double.parseDouble(mapArray[0]), Double.parseDouble(mapArray[1]));
                Log.i("RPMapData", mapString);
            }
            // Get project videos array
            String[] tempArray;
            // Array of FSVideo
            ArrayList<FSVideo> videos = new ArrayList<FSVideo>();
            // Get the string of all the videos
            String inString = customFields.get("videos").get(0);
            for (String element:inString.split("\\]\\[")) {
                element = element.replace("[", "");
                element = element.replace("]", "");
                tempArray = element.split(", https");
                String vTitle = tempArray[0];
                String vLink = "https" + tempArray[1];
                // Design choice to skip promo videos in all forms,
                // in some cases, this eliminates the projects entire video list
                if (vTitle.contains("PROMO")) {
                    Log.w("FSVideoPROMO", "Promo video Found, Skipping...");
                    continue;
                }
                Log.i("FSVideo", element);
                videos.add(new FSVideo(vTitle, vLink));
            }
            // get project image url
            String imagePath = customFields.get("preview_image").get(0);
            // check image path (some preview_images are missing the vm.site.com... we fix this here)
            if (!imagePath.contains("http:")) {
                // As of 08/11/2016 this is the server where the images are stored.
                // This is hard coded, and we hope the upstream data doesn't break.
                imagePath = "http://fsci15.wpengine.com" + imagePath;
            }
            Log.i("RPImage", imagePath);
            // download image here
            Bitmap bitmapImage = downloadImage(title, imagePath);
            // Load all of the data into a new RP
            Downloader.RPMap.add(new ResearchProject(title, desc, videos, mapData, bitmapImage, imagePath));
        }
        // Sort the projects to alphabetical
        Collections.sort(Downloader.RPMap, new Comparator<ResearchProject>() {
            @Override
            public int compare(ResearchProject lhs, ResearchProject rhs) {
                return lhs.title.compareTo(rhs.title);
            }
        });
        // More logging
        int i = 0;
        for (ResearchProject RP: Downloader.RPMap) {
            Log.d("JSONParser", "RP Loaded: " + RP.title);
            RP.index = i;
            i++;
        }
        // We have now downloaded and parsed the json, which in turn downloaded or loaded images
        // last thing is to download and parse the scientist on call's information.
        // As of 08/11/2016, the scientist on call's information does not appear to be rotating
        // more upstream data issues. ALSO: this is parsing html. It does the job, but it is nasty.
        // GLHF
        parseScientist();
        return null;
    }
    // ###########################################################
    // As of 08/11/2016, the scientist on call's information does not appear to be rotating
    // more upstream data issues. ALSO: this is parsing html. It does the job, but it is nasty.
    // This function downloads and parses the scientist; GLHF
    // ###########################################################
    private void parseScientist() {
        try {
            // Practice safe logging
            URL url = new URL("http://frontierscientists.com/feed-scientists-is-on-call/?feedonly=true");
            Log.e("parseSci", "Download beginning...");
            Log.e("parseSci", "Download url: " + url);
            Log.e("parseSci", "File name: " + "download.json");
            // open connection
            HttpURLConnection serverConnection = (HttpURLConnection) url.openConnection();
            BufferedReader buffRead = new BufferedReader(new InputStreamReader(serverConnection.getInputStream()));
            String inputLine = "";
            String tempString = "";
            // while there is a new line, read in the data
            while ((inputLine = buffRead.readLine()) != null) {
                tempString += inputLine;
            }
            buffRead.close();
            // Parse the scientist html string
            String[] tempArray = tempString.split("title=\"");
            String[] tempArray2 = tempArray[1].split(",");
            String tempName = tempArray2[0];
            tempArray2 = tempArray[1].split("\" rel");
            String tempBio = tempArray2[0];
            String replaceString = tempName + ", ";
            tempBio = tempBio.replace(replaceString, "");
            tempArray2 = tempArray[1].split("src=\"");
            tempArray2 = tempArray2[1].split("\"");
            String tempImageURL = tempArray2[0];
            // Print the scientist
            Log.d("parseSci", tempName + " -- " + tempBio + " -- " + tempImageURL);
            // download sci image
            Bitmap tempImage = downloadImage(tempName, tempImageURL);
            // init scientist
            Downloader.sciOnCall = new FSScientist(tempName,tempBio,tempImage);
            // Open a connection to that URL.
            Log.e("parseSci", "Download complete.");
        } catch (Exception e){
            e.printStackTrace();
        }

    }
// ###########################################################
// This is the download image function, it checks if an image exists,
// and if it doesn't it downloads it
// ###########################################################
    public Bitmap downloadImage (String title, String imagePath) {
        File imageFile = new File(context.getFilesDir(), title+".jpg");
        Log.i("imagefile", imageFile.toString());
        Bitmap bitmapImage = null;
        if (!imageFile.exists()) {
            Log.i("RPImage", "Downloading new image: " + title + ".jpg");
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(imageFile);
                bitmapImage = BitmapFactory.decodeStream(new URL(imagePath).openStream());
                bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                fileOutputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("RPImage", "Download failed for "+title+".jpg");
            }
        }
        try {
            bitmapImage = BitmapFactory.decodeStream(new FileInputStream(imageFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmapImage;
    }
// ###########################################################
// This runs, when the Parser is complete GO Async tasks!
// ###########################################################
    @Override
    protected void onPostExecute(Void result) {
        //dismiss the dialog after the file was downloaded
        Log.d("async", "post execute");
        MainActivity.progress.dismiss();
    }
}

