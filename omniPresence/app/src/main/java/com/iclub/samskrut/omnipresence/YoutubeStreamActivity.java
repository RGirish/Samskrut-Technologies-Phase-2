package com.iclub.samskrut.omnipresence;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

public class YoutubeStreamActivity extends FragmentActivity {

    String videoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_stream);

        Intent intent = getIntent();
        videoId = intent.getStringExtra("videoId");
        //DhNRNS3UPnU

        PlayerYouTubeFrag myFragment = PlayerYouTubeFrag.newInstance(videoId);
        getSupportFragmentManager().beginTransaction().replace(R.id.myContainer, myFragment).commit();
        myFragment.init();

    }


    public static class PlayerYouTubeFrag extends YouTubePlayerSupportFragment
    {
        private YouTubePlayer activePlayer;

        public static PlayerYouTubeFrag newInstance(String videoid)        {

            PlayerYouTubeFrag playerYouTubeFrag = new PlayerYouTubeFrag();
            Bundle bundle = new Bundle();
            bundle.putString("videoId", videoid);
            playerYouTubeFrag.setArguments(bundle);

            return playerYouTubeFrag;
        }

        private void init(){

            initialize("AIzaSyCs5d7A6gSUW1kxjjLnQjq6spU_x9JPZyY", new YouTubePlayer.OnInitializedListener(){

                @Override
                public void onInitializationFailure(YouTubePlayer.Provider arg0, YouTubeInitializationResult arg1){}

                @Override
                public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {

                    activePlayer = player;
                    activePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
                    if (!wasRestored) {
                        activePlayer.loadVideo(getArguments().getString("videoId"), 0);
                    }
                }
            });
        }
    }

}