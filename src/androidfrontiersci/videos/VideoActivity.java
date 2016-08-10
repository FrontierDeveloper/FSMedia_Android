package androidfrontiersci.videos;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubeStandalonePlayer;

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
        streamVideo();
        VideosListActivity.listAdapter.notifyDataSetChanged(); // Redraw the list as you reenter it.
    }

/*
    Content functions:
*/
    // streamVideo
    // This function is called from onCreate(). It initializes and starts a YouTubeStandalonePlayer
    // to stream the selected video.
    private void streamVideo() {
        String video_url = VideosListActivity.video_name;
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
    // getYouTubeVideoId
    // This function is called from streamVideo(). It takes a YouTube url as a String and returns
    // the YouTube id found within the url.
    public static String getYouTubeVideoId(String url) {
        String[] tempArray = url.split("\\?v\\=");
        Log.d("YOUTUBE", tempArray[0]);
        return tempArray[1];
    }
}
