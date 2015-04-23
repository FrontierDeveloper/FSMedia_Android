package androidfrontiersci.videos;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;

import androidfrontiersci.JsonParser;
import androidfrontiersci.listviews.ExpandableListAdapter;
import androidfrontiersci.MainActivity;

import com.example.androidfrontiersci.R;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubeStandalonePlayer;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
    This is the VideoActivity, started by VideosListActivity to play the selected video.
    Its optional layout file, used only when playing from internal storage, is activity_video.xml.
    This class has two options for playing the video:
        1. From YouTube:
            - The video is not downloaded to the device
            - The device must have internet connection
            - YouTubeStandalonePlayer is used
        2. From download:
            - The video is downloaded to the device
            - The device does not require an internet connection
            - VideoView is used
*/
public class VideoActivity extends YouTubeBaseActivity implements
        YouTubePlayer.OnInitializedListener {

    public static String video_name;

    // The class' private variables
    private static final String TAG = "VideoActivity";
    private VideoView myVideoView;
    private int position = 0;
    private MediaController mediaControls;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // If the selected video has been downloaded, play it from download.
        if (MainActivity.downloaded_videos.keySet().contains(video_name)) {
            playVideoFromDownload();
        // Otherwise, stream it from YouTube.
        } else {
            streamVideo();
        }
        VideosListActivity.listAdapter.notifyDataSetChanged(); // Redraw the list as you reenter it.
    }

/*
    Content functions:
*/
    // playVideoFromDownload
    // This function is called from onCreate(). It sets up a VideoView and plays the video from an
    // internally stored file.
    private void playVideoFromDownload() {
        Uri file_path = Uri.parse(MainActivity.downloaded_videos.get(video_name));
        // Set the main layout of the activity.
        setContentView(R.layout.activity_video);
        // Set the media controller buttons.
        if (mediaControls == null) {
            mediaControls = new MediaController(VideoActivity.this);
        }
        // Initialize the VideoView.
        myVideoView = (VideoView) findViewById(R.id.video_view);

        try {
            // Set the media controller in the VideoView.
            myVideoView.setMediaController(mediaControls);
            // Set the uri of the video to be played.
            myVideoView.setVideoURI(file_path);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }

        myVideoView.requestFocus();
        // Set a setOnPreparedListener in order to know when the video file is ready for playback.
        myVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mediaPlayer) {
                myVideoView.seekTo(position); // If we have a position on savedInstanceState, the
                                              // video playback should start from here
                if (position == 0) {
                    myVideoView.start();
                } else {
                    myVideoView.pause(); // If we come from a resumed activity, video playback will
                                         // be paused.
                }
            }
        });
    }
    // streamVideo
    // This function is called from onCreate(). It initializes and starts a YouTubeStandalonePlayer
    // to stream the selected video.
    private void streamVideo() {
        String video_url = ((Map<String, String>) ((Map<String, Object>) ((Map<String, Object>)
                JsonParser.ProjectData.get(VideosListActivity.project_name)).get("videos")).get(
                video_name)).get("utubeurl");
        String video_id = getYouTubeVideoId(video_url);
        Intent intent = YouTubeStandalonePlayer.createVideoIntent(this, DeveloperKey.DEVELOPER_KEY,
                video_id);
        startActivity(intent);
        // When exiting the video player, go immediately back to VideosListActivity.
        finish();
    }

/*
    Required functions for any class that extends YouTubeBaseActivity:
*/
    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
                                        boolean wasRestored) {
        Log.e(TAG, "Video Initialized");
    }
    @Override
    public void onInitializationFailure(Provider arg0, YouTubeInitializationResult arg1) {
        Log.e(TAG, "Video Initialization Failure");
    }

/*
    Helper function:
*/
    // getYouTubeVideoId
    // This function is called from streamVideo(). It takes a YouTube url as a String and returns
    // the YouTube id found within the url.
    public static String getYouTubeVideoId(String url) {
        String video_id = "";
        if (url != null && url.trim().length() > 0 && url.startsWith("http")) {
            String expression = "^.*((youtu.be"+ "\\/)" + "|(v\\/)|(\\/u\\/w\\/)|(embed\\/)|" +
                    "(watch\\?))\\??v?=?([^#\\&\\?]*).*";
            Pattern pattern = Pattern.compile(expression,Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(url);
            if (matcher.matches()) {
                String groupIndex1 = matcher.group(7);
                if(groupIndex1 != null && groupIndex1.length() == 11) {
                    video_id = groupIndex1;
                }
            }
        }
        return video_id;
    }
}
