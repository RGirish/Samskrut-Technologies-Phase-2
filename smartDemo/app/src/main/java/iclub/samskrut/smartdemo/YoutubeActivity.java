
package iclub.samskrut.smartdemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

public class YoutubeActivity extends FragmentActivity {

    public static String videoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.youtube_player);

        Intent intent = getIntent();
        videoId = intent.getStringExtra("videoid");
        String pos = intent.getStringExtra("pos");

        if (!Connection.CONNECTED) {
            PlayerYouTubeFrag myFragment = PlayerYouTubeFrag.newInstance(videoId);
            getSupportFragmentManager().beginTransaction().replace(R.id.myContainer, myFragment).commit();
            myFragment.init();
        } else {
            LinearLayout ll = new LinearLayout(this);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER;
            ll.setLayoutParams(params);
            Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().toString() + "/showcommerce/p" + Connection.PID + "/video/" + Connection.PID + "_" + pos + ".jpg");
            Drawable d = new BitmapDrawable(getResources(), bitmap);
            ll.setOrientation(LinearLayout.HORIZONTAL);
            ll.setBackground(d);
            ll.setGravity(Gravity.CENTER);
            FrameLayout fl = (FrameLayout) findViewById(R.id.myContainer);

            ImageView imageView = new ImageView(this);
            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(100, 100);
            imageView.setLayoutParams(params1);
            imageView.setImageResource(R.drawable.play);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(Connection.CONNECTED)Connection.ref.child("video").child("playback").push().setValue("playing");
                }
            });
            ll.addView(imageView);

            imageView = new ImageView(this);
            params1 = new LinearLayout.LayoutParams(100, 100);
            params1.setMargins(20, 0, 0, 0);
            imageView.setLayoutParams(params1);
            imageView.setImageResource(R.drawable.pause);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(Connection.CONNECTED)Connection.ref.child("video").child("playback").push().setValue("paused");
                }
            });
            ll.addView(imageView);

            imageView = new ImageView(this);
            params1 = new LinearLayout.LayoutParams(100, 100);
            params1.setMargins(20, 0, 0, 0);
            imageView.setLayoutParams(params1);
            imageView.setImageResource(R.drawable.back);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(Connection.CONNECTED)Connection.ref.child("video").child("back").push().setValue("yeah");
                    if(Connection.CONNECTED)Connection.ref.child("video").child("playback").removeValue();
                    finish();
                }
            });
            ll.addView(imageView);
            fl.addView(ll);
        }
    }

    public void onBackPressed(){
        if(Connection.CONNECTED)Connection.ref.child("video").child("back").push().setValue("yeah");
        if(Connection.CONNECTED)Connection.ref.child("video").child("playback").removeValue();
        super.onBackPressed();
    }

    public static class PlayerYouTubeFrag extends YouTubePlayerSupportFragment
    {
        private YouTubePlayer activePlayer;

        public static PlayerYouTubeFrag newInstance(String videoid)        {

            PlayerYouTubeFrag playerYouTubeFrag = new PlayerYouTubeFrag();
            Bundle bundle = new Bundle();
            bundle.putString("videoid", videoid);
            playerYouTubeFrag.setArguments(bundle);
            return playerYouTubeFrag;
        }

        private void init(){

            initialize("AIzaSyAHAXqbOC8IiAuKQwGhM4k3pSorZOdYbwE", new YouTubePlayer.OnInitializedListener(){

                @Override
                public void onInitializationFailure(YouTubePlayer.Provider arg0, YouTubeInitializationResult arg1){}

                @Override
                public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {

                    activePlayer = player;
                    activePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
                    if (!wasRestored) {
                        activePlayer.loadVideo(getArguments().getString("videoid"), 0);
                    }
                }
            });
        }
    }


}