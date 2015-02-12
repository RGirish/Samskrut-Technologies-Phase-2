package iclub.samskrut.smartdemo;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.ErrorReason;
import com.google.android.youtube.player.YouTubePlayer.PlaybackEventListener;
import com.google.android.youtube.player.YouTubePlayer.PlayerStateChangeListener;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerView;

public class YoutubeActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    public static final String API_KEY = "AIzaSyAHAXqbOC8IiAuKQwGhM4k3pSorZOdYbwE";

    //http://youtu.be/<VIDEO_ID>
    public static String VIDEO_ID = "dKLftgvYsVU";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /** attaching layout xml **/
        setContentView(R.layout.youtube_player);

        Intent intent = getIntent();
        VIDEO_ID=intent.getStringExtra("videoid");

        /** Initializing YouTube player view **/
        YouTubePlayerView youTubePlayerView = (YouTubePlayerView) findViewById(R.id.youtube_player);
        youTubePlayerView.initialize(API_KEY, this);

    }

    @Override
    public void onInitializationFailure(Provider provider, YouTubeInitializationResult result) {
        Toast.makeText(this, "Failured to Initialize!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onInitializationSuccess(Provider provider, YouTubePlayer player, boolean wasRestored) {

        /** add listeners to YouTubePlayer instance **/
        player.setPlayerStateChangeListener(playerStateChangeListener);
        player.setPlaybackEventListener(playbackEventListener);
        //player.setFullscreen(true);

        /** Start buffering **/
        if (!wasRestored) {
            player.cueVideo(VIDEO_ID);
        }
    }

    private PlaybackEventListener playbackEventListener = new PlaybackEventListener() {

        @Override
        public void onBuffering(boolean arg0) {
            Log.e("LOG","onBuffering");
        }

        @Override
        public void onPaused() {
            Log.e("LOG","onPaused");
        }

        @Override
        public void onPlaying() {
            Log.e("LOG","onPlaying");
        }

        @Override
        public void onSeekTo(int arg0) {
            Log.e("LOG","onSeekTo");
        }

        @Override
        public void onStopped() {
            Log.e("LOG","onStopped");
        }

    };

    private PlayerStateChangeListener playerStateChangeListener = new PlayerStateChangeListener() {

        @Override
        public void onAdStarted() {
            Log.e("LOG","onAdStarted");
        }

        @Override
        public void onError(ErrorReason arg0) {
            Log.e("LOG",arg0.name()+"__"+arg0.toString());
        }

        @Override
        public void onLoaded(String arg0) {
            Log.e("LOG","onLoaded");
        }

        @Override
        public void onLoading() {
            Log.e("LOG","onLoading");
        }

        @Override
        public void onVideoEnded() {
            Log.e("LOG","onVideoEnded");
        }

        @Override
        public void onVideoStarted() {
            Log.e("LOG","onVideoStarted");
        }
    };
}