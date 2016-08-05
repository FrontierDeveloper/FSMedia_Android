package androidfrontiersci.Download;

import android.app.ExpandableListActivity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.String;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import androidfrontiersci.MainActivity;
import androidfrontiersci.Download.Downloader;

/**
 * Created by jtnewell2 on 8/4/16.
 */
public class JSONParser extends AsyncTask<String, Void, Void> {

    Context context;

    public JSONParser(Context context_) {
        context = context_;
    }

    @Override
    protected Void doInBackground(String... jsonString) {
        Downloader.RPMap = new ArrayList<ResearchProject>();

        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> json = new Gson().fromJson(jsonString[0], type);
        Double nPosts = (Double) json.get("count_total");
        List<Map<String, Object>> posts = (List<Map<String, Object>>) json.get("posts");
        for (int i = 0; i < nPosts; i++) {
            // Get Project Title
            String title = (String) posts.get(i).get("title");
            title = title.replace("&#8217;", "'");

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
            String inString = customFields.get("videos").get(0);
            for (String element:inString.split("\\]\\[")) {
                element = element.replace("[", "");
                element = element.replace("]", "");
                tempArray = element.split(", https");
                String vTitle = tempArray[0];
                String vLink = "https" + tempArray[1];
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
                // i hope this url is perma static
                imagePath = "http://fsci15.wpengine.com" + imagePath;
            }
            Log.i("RPImage", imagePath);
            // Image File Logic Here
            Bitmap bitmapImage = downloadImage(title, imagePath);
            Downloader.RPMap.add(new ResearchProject(title, desc, videos, mapData, bitmapImage, imagePath));
        }
        Collections.sort(Downloader.RPMap, new Comparator<ResearchProject>() {
            @Override
            public int compare(ResearchProject lhs, ResearchProject rhs) {
                return lhs.title.compareTo(rhs.title);
            }
        });
        int i = 0;
        for (ResearchProject RP: Downloader.RPMap) {
            Log.d("JSONParser", "RP Loaded: " + RP.title);
            RP.index = i;
            i++;
        }
        parseScientist();
        return null;
    }
    private void parseScientist() {
        try {
            URL url = new URL("http://frontierscientists.com/feed-scientists-is-on-call/?feedonly=true");
            Log.e("parseSci", "Download beginning...");
            Log.e("parseSci", "Download url: " + url);
            Log.e("parseSci", "File name: " + "download.json");


            HttpURLConnection serverConnection = (HttpURLConnection) url.openConnection();
            BufferedReader buffRead = new BufferedReader(new InputStreamReader(serverConnection.getInputStream()));
            String inputLine = "";
            String tempString = "";
            while ((inputLine = buffRead.readLine()) != null) {
                tempString += inputLine;
                Log.e("parseSci", "SciOnCall read line");
            }
            buffRead.close();
            // parse sci
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

    @Override
    protected void onPostExecute(Void result) {
        //dismiss the dialog after the file was downloaded
        Log.d("async","post execute");
        MainActivity.progress.dismiss();
    }
//        // init this RP
//        let RP = ResearchProject(title_: title, description_: desc, videos_: myFSVideo, mapData_: mapData, image_: image, imagePath_: imagePath)
//        // Add it to the Global Map
//        RPMap.append(RP)
//        // use below for to print this research project
//        //prettyPrint(RP)
//    }
}

