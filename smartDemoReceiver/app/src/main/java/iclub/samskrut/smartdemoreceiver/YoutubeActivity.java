package iclub.samskrut.smartdemoreceiver;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

public class YoutubeActivity extends FragmentActivity {

    public static String videoId;
    public static TheClass ob;
    public static YouTubePlayer activePlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.youtube_player);

        Intent intent=getIntent();
        videoId=intent.getStringExtra("videoid");

        ob=new TheClass();

        Firebase ref = new Firebase("https://smartdemo.firebaseio.com/1234");

        ref.child("video").child("back").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                finish();
            }

            @Override
            public void onChildMoved(DataSnapshot snapshot, String previousChildKey) {}
            @Override
            public void onChildRemoved(DataSnapshot snapshot) {}
            @Override
            public void onChildChanged(DataSnapshot snapshot, String previousChildKey) {}
            @Override
            public void onCancelled(FirebaseError firebaseError) {}
        });

        ref.child("video").child("playback").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                try{
                    new Thread(new Task(snapshot.getValue().toString())).start();
                }catch(NullPointerException e){}
            }

            @Override
            public void onChildMoved(DataSnapshot snapshot, String previousChildKey) {}
            @Override
            public void onChildRemoved(DataSnapshot snapshot) {}
            @Override
            public void onChildChanged(DataSnapshot snapshot, String previousChildKey) {}
            @Override
            public void onCancelled(FirebaseError firebaseError) {}
        });


        ref.child("video").child("seek").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                try{
                    new Thread(new Task_Seek(snapshot.getValue().toString())).start();
                }catch(NullPointerException e){}
            }

            @Override
            public void onChildMoved(DataSnapshot snapshot, String previousChildKey) {}
            @Override
            public void onChildRemoved(DataSnapshot snapshot) {}
            @Override
            public void onChildChanged(DataSnapshot snapshot, String previousChildKey) {}
            @Override
            public void onCancelled(FirebaseError firebaseError) {}
        });


        YouTubePlayerFragment myFragment = YouTubePlayerFragment.newInstance(videoId);
        getSupportFragmentManager().beginTransaction().replace(R.id.myContainer, myFragment).commit();
        myFragment.init();
    }

    class Task implements Runnable {
        String s;
        Task(String parameter){
            s=parameter;
        }
        @Override
        public void run() {
            ob.setPlayback(s);
        }
    }

    class Task_Seek implements Runnable {
        String s;
        Task_Seek(String parameter){
            s=parameter;
        }
        @Override
        public void run() {
            ob.setSeek(s);
        }
    }

    class TheClass{

        public synchronized void setPlayback(final String s){
            runOnUiThread(new Runnable(){
                public void run() {
                    if(s.equals("playing")) {
                        activePlayer.play();
                    }else if(s.equals("paused")) {
                        activePlayer.pause();
                    }
                }
            });
        }

        public synchronized void setSeek(final String s){
            runOnUiThread(new Runnable(){
                public void run() {
                    int pc = Integer.parseInt(s);
                    double time = (pc) * (activePlayer.getDurationMillis()/100);
                    Log.e("S", s);
                    Log.e("time",String.valueOf(time));
                    Log.e("duration",String.valueOf(activePlayer.getDurationMillis()));
                    activePlayer.seekToMillis((int)time);
                }
            });
        }
    }

    public static class YouTubePlayerFragment extends YouTubePlayerSupportFragment
    {

        public static YouTubePlayerFragment newInstance(String videoid)        {

            YouTubePlayerFragment playerYouTubeFrag = new YouTubePlayerFragment();
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

    @Override
    public void onDestroy(){
        activePlayer=null;
        super.onDestroy();
    }
}