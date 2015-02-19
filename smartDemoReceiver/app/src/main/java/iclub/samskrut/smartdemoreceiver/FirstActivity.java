
package iclub.samskrut.smartdemoreceiver;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.Parse;
import com.parse.ParseCrashReporting;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class FirstActivity extends ActionBarActivity{

    public int tabPosition;
    SectionsPagerAdapter mSectionsPagerAdapter;
    MyViewPager mPager;
    int COUNT_SS=0,CURR_COUNT_SS=0;
    ArrayList<Integer> notAvailableList_SS;
    int COUNT_360=0,CURR_COUNT_360=0;
    ArrayList<Integer> notAvailableList_360;
    int COUNT_V=0,CURR_COUNT_V=0;
    ArrayList<Integer> notAvailableList_V;
    int COUNT_VDB=0,CURR_COUNT_VDB=0;
    ArrayList<Integer> notAvailableList_VDB;
    public static int PID;
    ProgressDialog dialog,dialog2,dialog3,dialog0,dialog4;
    public static String videoID;
    public static String MYURL;
    public static SQLiteDatabase db;
    TheClass ob;
    public static Firebase ref;
    public static int NO_360,NO_SS,NO_V,VIDEO_ACTIVITY_REQUEST_CODE;
    public static ActionBar actionBar;
    boolean flag=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        VIDEO_ACTIVITY_REQUEST_CODE=99;

        Intent intent=getIntent();
        PID = intent.getIntExtra("PID",130);
        tabPosition = intent.getIntExtra("tabPosition",777);
        ob=new TheClass();

        try{ParseCrashReporting.enable(this);}catch (Exception e){}
        Parse.initialize(this, "28rzQwSoD7MFQOOViu9awAI0giaUDK8E7ADYbXAz", "jbYQAqhT1jcRiIUrS3UwuFuFOipjv04kUYhZpkEN");

        if(checkConnection()){
            Firebase.setAndroidContext(this);
            ref=new Firebase("https://smartdemo.firebaseio.com/1234");
            ref.removeValue();


            //Firebase Listener for PID
            ref.child("pid").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                    Intent intent = new Intent(FirstActivity.this, FirstActivity.class);
                    String[] parts;
                    String pid="",tabPosition="";
                    try {
                        parts = snapshot.getValue().toString().split(";");
                        pid = parts[0];
                        tabPosition = parts[1];
                    }catch (Exception e){
                        tabPosition="777";
                    }
                    intent.putExtra("PID", Integer.parseInt(pid));
                    intent.putExtra("tabPosition", Integer.parseInt(tabPosition));
                    startActivity(intent);
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


            //Firebase Listener for Change of Tab
            ref.child("tab").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    try {
                        new Thread(new Task_TAB(snapshot.getValue().toString())).start();
                    } catch (NullPointerException e) {
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    Log.e("ERROR at onCancelled()", firebaseError.getMessage());
                }
            });


            //Firebase Listener for Slideshow
            ref.child("ss").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    try {
                        new Thread(new Task_SS(snapshot.getValue().toString())).start();
                    } catch (NullPointerException e) {
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    Log.e("ERROR at onCancelled()", firebaseError.getMessage());
                }
            });


            //Firebase Listener for Video
            ref.child("video").child("id").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                    try{
                        new Thread(new Task_V(snapshot.getValue().toString())).start();
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



            //Firebase Listener for onBackPressed
            ref.child("video").child("back").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                    finishActivity(VIDEO_ACTIVITY_REQUEST_CODE);
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

            //Firebase Listener for 360 View
            ref.child("360").addChildEventListener(new ChildEventListener() {

                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    if (dataSnapshot.getValue().toString().equals("r")) {
                        new Thread(new Task_360("r")).start();
                    } else {
                        new Thread(new Task_360("l")).start();
                    }
                }
                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {}
                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
                @Override
                public void onCancelled(FirebaseError firebaseError) {}
            });
        }

        db=openOrCreateDatabase("smartdemo.db",SQLiteDatabase.CREATE_IF_NECESSARY, null);
        createTables();

        //Setup folders, get counts and download all the images from Parse if needed
        foldersAndCount();

    }

    class Task_TAB implements Runnable {
        String s;
        Task_TAB(String parameter){
            s=parameter;
        }
        @Override
        public void run() {
            ob.send_tab(s);
        }
    }

    class Task_SS implements Runnable {
        String s;
        Task_SS(String parameter){
            s=parameter;
        }
        @Override
        public void run() {
            ob.send_ss(s);
        }
    }

    class Task_V implements Runnable {
        String s;
        Task_V(String parameter){
            s=parameter;
        }
        @Override
        public void run() {
            ob.send_v(s);
        }
    }

    class Task_360 implements Runnable {
        String s;
        Task_360(String parameter){
            s=parameter;
        }

        @Override
        public void run() {
            ob.send_360(s);
        }
    }

    class TheClass{

        public synchronized void send_tab(final String s){
            runOnUiThread(new Runnable(){
                public void run() {
                    int position = Integer.parseInt(s);
                    mPager.setCurrentItem(position);
                }
            });
        }

        public synchronized void send_ss(final String s){
            runOnUiThread(new Runnable(){
                public void run() {
                    SlideshowFragment.mPager.setCurrentItem(Integer.parseInt(s));
                }
            });
        }

        public synchronized void send_v(final String s){
            runOnUiThread(new Runnable(){
                public void run() {
                    VIDEO_ACTIVITY_REQUEST_CODE++;
                    Intent intent=new Intent(FirstActivity.this,YoutubeActivity.class);
                    intent.putExtra("videoid",s);
                    startActivityForResult(intent,VIDEO_ACTIVITY_REQUEST_CODE);
                }
            });
        }

        public synchronized void send_360(final String s){
            runOnUiThread(new Runnable(){

                public void run() {
                    if(s.equals("r")){
                        Three60Fragment.moveRight();
                    }else{
                        Three60Fragment.moveLeft();
                    }
                }

            });
        }
    }

    public void setupTabs()
    {
        actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.rgb(26, 183, 80)));
        actionBar.setTitle(Html.fromHtml("<font color='#ffffff'>Slideshow</font>"));

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mPager = (MyViewPager) findViewById(R.id.pager_main);
        mPager.setSwipeable(false);
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {}

            @Override
            public void onPageSelected(int i) {
                if(i==0)actionBar.setTitle(Html.fromHtml("<font color='#ffffff'>Slideshow</font>"));
                else if(i==1)actionBar.setTitle(Html.fromHtml("<font color='#ffffff'>360 View</font>"));
                else actionBar.setTitle(Html.fromHtml("<font color='#ffffff'>Videos</font>"));
            }

            @Override
            public void onPageScrollStateChanged(int i) {}
        });
        mPager.setAdapter(mSectionsPagerAdapter);
        if(tabPosition!=777) mPager.setCurrentItem(tabPosition);
    }


    public void createTables(){
        try{
            db.execSQL("CREATE TABLE video(pid NUMBER, position NUMBER, url TEXT, videoid TEXT);");
        }catch(Exception e){}
        try{
            db.execSQL("CREATE TABLE no(pid NUMBER, no_ss NUMBER, no_360 NUMBER, no_v NUMBER);");
        }catch(Exception e){}
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position==0)return SlideshowFragment.newInstance(position + 1);
            else if(position==1)return Three60Fragment.newInstance(position + 1);
            else return VideoFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {return 3;}

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Slideshow";
                case 1:
                    return "360 View";
                case 2:
                    return "Videos";
            }
            return null;
        }
    }






    public static class Three60Fragment extends Fragment implements View.OnTouchListener {

        private static final String ARG_SECTION_NUMBER = "section_number";
        private static ImageView imageview;
        int x,y,cx,cy;
        private static int currentimagenumber=1;

        public static Three60Fragment newInstance(int sectionNumber) {
            Three60Fragment fragment = new Three60Fragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public Three60Fragment() {}

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_360, container, false);
            imageview=(ImageView)rootView.findViewById(R.id.three60ImageView);
            Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().toString()+"/showcommerce/p"+PID+"/360/"+PID+"_"+currentimagenumber+".jpg");
            imageview.setImageBitmap(bitmap);
            imageview.setOnTouchListener(this);
            return rootView;
        }

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch(event.getAction()){
                case MotionEvent.ACTION_DOWN: {
                    cx=(int)event.getX();
                    cy=(int)event.getY();
                    x=(int)event.getX();
                    y=(int)event.getY();
                    return true;
                }
                case MotionEvent.ACTION_UP: {
                    x=(int)event.getX();
                    y=(int)event.getY();
                    return true;
                }
                case MotionEvent.ACTION_MOVE: {
                    x=(int)event.getX();
                    y=(int)event.getY();

                    if(x>=cx+10){
                        cx=x;
                        moveRight();
                        return true;
                    }else if(x<=cx-10){
                        cx=x;
                        moveLeft();
                        return true;
                    }
                }
            }
            return false;
        }

        public static void moveRight(){
            currentimagenumber--;
            if (currentimagenumber == 0) currentimagenumber = NO_360;
            Bitmap bmp = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().toString()+"/showcommerce/p"+PID+"/360/"+PID+"_"+currentimagenumber+".jpg");
            imageview.setImageBitmap(bmp);
        }

        public static void moveLeft(){
            currentimagenumber++;
            if(currentimagenumber==(NO_360+1)) currentimagenumber=1;
            Bitmap bmp = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().toString()+"/showcommerce/p"+PID+"/360/"+PID+"_"+currentimagenumber+".jpg");
            imageview.setImageBitmap(bmp);
        }

    }






    public static class SlideshowFragment extends Fragment{

        private static final String ARG_SECTION_NUMBER = "section_number";
        static View rootView;
        private static int NUM_PAGES = NO_SS;
        private static ViewPager mPager;
        private static PagerAdapter mPagerAdapter;

        public static SlideshowFragment newInstance(int sectionNumber){
            SlideshowFragment fragment = new SlideshowFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public SlideshowFragment() {}

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_slideshow, container, false);
            this.rootView=rootView;
            mPager = (ViewPager) rootView.findViewById(R.id.slideshowViewPager);
            mPagerAdapter = new ScreenSlidePagerAdapter(getChildFragmentManager());
            mPager.setAdapter(mPagerAdapter);
            return rootView;
        }

        private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
            public ScreenSlidePagerAdapter(android.support.v4.app.FragmentManager fm) {
                super(fm);
            }

            @Override
            public android.support.v4.app.Fragment getItem(int position) {
                return ScreenSlidePageFragment.create(position);
            }

            @Override
            public int getCount() {
                return NUM_PAGES;
            }
        }

    }








    public static class VideoFragment extends Fragment{

        private static final String ARG_SECTION_NUMBER = "section_number";

        public static VideoFragment newInstance(int sectionNumber) {
            VideoFragment fragment = new VideoFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public VideoFragment() {}

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_video, container, false);
            LinearLayout mainll=(LinearLayout)rootView.findViewById(R.id.videoLinearLayout);
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            int width = display.getWidth();
            LinearLayout ll = new LinearLayout(getActivity());
            LinearLayout.LayoutParams params;
            for(int i=1 ; i<=NO_V ; ++i){
                final int i2=i;
                if(i==1 || i%2!=0){
                    ll = new LinearLayout(getActivity());
                    params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    ll.setLayoutParams(params);
                    ll.setPadding(5, 5, 5, 5);
                    ll.setGravity(Gravity.CENTER);
                }
                ImageView iv=new ImageView(getActivity());
                params=new LinearLayout.LayoutParams(width/2-10, (width/2-10)*2/3);
                params.setMargins(5,5,5,5);
                iv.setLayoutParams(params);
                Bitmap bitmap=BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().toString()+"/showcommerce/p"+PID+"/video/"+PID+"_"+i+".jpg");
                Drawable d = new BitmapDrawable(getResources(),bitmap);
                iv.setBackgroundDrawable(d);
                iv.setImageResource(R.drawable.ic_play);
                iv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Cursor c=db.rawQuery("SELECT videoid FROM video WHERE pid="+PID+" AND position="+i2,null);
                        try{
                            c.moveToFirst();
                            videoID=c.getString(0);
                            c.close();
                        }catch(Exception e){videoID="nothing";}

                        Intent intent=new Intent(getActivity(),YoutubeActivity.class);
                        intent.putExtra("videoid",videoID);
                        startActivity(intent);

                        //Intent intent2 = YouTubeStandalonePlayer.createVideoIntent(getActivity(), "AIzaSyCYcvf7CDroQuJv0UYmlON8Eb-TBngEbdI", videoID, 0, true, true);
                        //getActivity().startActivity(intent2);

                    }
                });
                ll.addView(iv);
                if(i==1 || i%2!=0){
                    mainll.addView(ll);
                }
            }

            return rootView;
        }


    }

    public boolean checkConnection(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo == null ) return false;
        else return true;
    }

    public void foldersAndCount(){

        //First check if the thumbnails are already downloaded and download only those unavailable
        //create necessary folders in external sd card
        File folder = new File(Environment.getExternalStorageDirectory() + "/showcommerce");
        if (!folder.exists()) {
            folder.mkdir();
        }
        File folder2 = new File(Environment.getExternalStorageDirectory() + "/showcommerce/p"+PID);
        if (!folder2.exists()) {
            folder2.mkdir();
        }
        File folder3 = new File(Environment.getExternalStorageDirectory() + "/showcommerce/p"+PID+"/360");
        if (!folder3.exists()) {
            folder3.mkdir();
        }
        File folder4 = new File(Environment.getExternalStorageDirectory() + "/showcommerce/p"+PID+"/ss");
        if (!folder4.exists()) {
            folder4.mkdir();
        }
        File folder5 = new File(Environment.getExternalStorageDirectory() + "/showcommerce/p"+PID+"/video");
        if (!folder5.exists()) {
            folder5.mkdir();
        }


        //Set number of slideshow, 360, video thumbnails images
        Cursor c=null;
        try{
            c = db.rawQuery("SELECT no_ss,no_360,no_v FROM no WHERE pid=" + PID + ";", null);
            c.moveToFirst();
            NO_SS=c.getInt(0);
            NO_360=c.getInt(1);
            NO_V=c.getInt(2);
            SlideshowFragment.NUM_PAGES=NO_SS;
            Three60Fragment.currentimagenumber=1;
            c.close();
            downloadEverything();
            setupTabs();
        }catch(Exception e) {

            if (checkConnection()) {

                dialog0 = ProgressDialog.show(this, null, "Just a moment...", true);

                //SS
                final ParseQuery<ParseObject> query1 = ParseQuery.getQuery("Test2Slideshow");
                final ParseQuery<ParseObject> query2 = ParseQuery.getQuery("Test2");
                final ParseQuery<ParseObject> query3 = ParseQuery.getQuery("productvideos");
                query1.whereEqualTo("pid", PID);
                query2.whereEqualTo("pid", PID);
                query3.whereEqualTo("pid", PID);

                query1.countInBackground(new CountCallback() {
                    @Override
                    public void done(int count, com.parse.ParseException e) {
                        if (e == null) {
                            NO_SS = count;
                            SlideshowFragment.NUM_PAGES=NO_SS;
                            query2.countInBackground(new CountCallback() {
                                @Override
                                public void done(int count, com.parse.ParseException e) {
                                    if (e == null) {
                                        NO_360 = count;
                                        Three60Fragment.currentimagenumber=1;
                                        query3.countInBackground(new CountCallback() {
                                            @Override
                                            public void done(int count, com.parse.ParseException e) {
                                                if (e == null) {
                                                    NO_V = count;
                                                    db.execSQL("INSERT INTO no VALUES(" + PID + "," + NO_SS + "," + NO_360 + "," + NO_V + ");");
                                                    dialog0.dismiss();
                                                    downloadEverything();
                                                    setupTabs();
                                                } else {
                                                }
                                            }
                                        });
                                    } else {
                                    }
                                }
                            });
                        } else {
                        }
                    }
                });

            }else{
                Toast.makeText(this,"Check your Internet Connection!",Toast.LENGTH_LONG).show();
            }
        }
    }


    public void downloadEverything()
    {
        //360
        notAvailableList_360 = new ArrayList<>(NO_360);
        notAvailableList_360.clear();
        for (int i = 1; i <= NO_360; ++i) {
            String FILENAME = Environment.getExternalStorageDirectory().toString() + "/showcommerce/p" + PID + "/360/" + PID + "_" + i + ".jpg";
            File file = new File(FILENAME);
            if (!file.exists()) {
                notAvailableList_360.add(i);
            }
        }

        if (notAvailableList_360.size() > 0) {
            if (checkConnection()) {
                dialog = ProgressDialog.show(this, null, "Download 360 View Images...", true);
                CURR_COUNT_360 = 0;
                COUNT_360 = notAvailableList_360.size();
                for (final int k : notAvailableList_360) {
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Test2");
                    query.whereEqualTo("pid", PID);
                    query.whereEqualTo("position", k);
                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, com.parse.ParseException e) {
                            if (e == null) {
                                ParseFile myFile = objects.get(0).getParseFile("imageFile");
                                myFile.getDataInBackground(new GetDataCallback() {
                                    @Override
                                    public void done(byte[] bytes, com.parse.ParseException e) {
                                        if (e == null) {
                                            writeFile_360(bytes, PID + "_" + k + ".jpg");
                                            CURR_COUNT_360++;
                                            if (CURR_COUNT_360 == COUNT_360){
                                                dialog.dismiss();
                                                Three60Fragment.currentimagenumber = 1;
                                                Three60Fragment.imageview.setImageBitmap(BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().toString()+"/showcommerce/p"+PID+"/360/"+PID+"_1.jpg"));
                                            }
                                        } else {
                                            Log.e("Something went wrong", "Something went wrong");
                                        }
                                    }
                                });
                            } else {
                                Log.e("PARSE", "Error: " + e.getMessage());
                            }
                        }
                    });
                }
            } else {
                Toast.makeText(this, "Internet Connection unavailable!", Toast.LENGTH_LONG).show();
            }
        } else {
            Three60Fragment.currentimagenumber = 1;
        }


        //SLIDESHOW
        notAvailableList_SS = new ArrayList<>(NO_SS);
        notAvailableList_SS.clear();
        for (int i = 1; i <= NO_SS; ++i) {
            String FILENAME = Environment.getExternalStorageDirectory().toString() + "/showcommerce/p" + PID + "/ss/" + PID + "_" + i + ".jpg";
            File file = new File(FILENAME);
            if (!file.exists()) {
                notAvailableList_SS.add(i);
            }
        }

        if (notAvailableList_SS.size() > 0) {
            if (checkConnection()) {
                dialog2 = ProgressDialog.show(this, null, "Downloading Slideshow Images...", true);
                CURR_COUNT_SS = 0;
                COUNT_SS = notAvailableList_SS.size();
                for (final int k : notAvailableList_SS) {
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Test2Slideshow");
                    query.whereEqualTo("pid", PID);
                    query.whereEqualTo("position", k);
                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, com.parse.ParseException e) {
                            if (e == null) {
                                ParseFile myFile = objects.get(0).getParseFile("imageFile");
                                myFile.getDataInBackground(new GetDataCallback() {
                                    @Override
                                    public void done(byte[] bytes, com.parse.ParseException e) {
                                        if (e == null) {
                                            writeFile_SS(bytes, PID + "_" + k + ".jpg");
                                            CURR_COUNT_SS++;
                                            if (CURR_COUNT_SS == COUNT_SS) {
                                                SlideshowFragment.mPager = (ViewPager) SlideshowFragment.rootView.findViewById(R.id.slideshowViewPager);
                                                SlideshowFragment.mPager.setAdapter(SlideshowFragment.mPagerAdapter);
                                                dialog2.dismiss();
                                            }
                                        } else {
                                            Log.e("Something went wrong", "Something went wrong");
                                        }
                                    }
                                });
                            } else {
                                Log.e("PARSE", "Error: " + e.getMessage());
                            }
                        }
                    });
                }
            } else {
                Toast.makeText(this, "Internet Connection unavailable!", Toast.LENGTH_LONG).show();
            }
        }




        //VIDEO THUMBNAILS
        notAvailableList_V = new ArrayList<>(NO_V);
        notAvailableList_V.clear();
        for (int i = 1; i <= NO_V; ++i) {
            String FILENAME = Environment.getExternalStorageDirectory().toString() + "/showcommerce/p" + PID + "/video/" + PID + "_" + i + ".jpg";
            File file = new File(FILENAME);
            if (!file.exists()) {
                notAvailableList_V.add(i);
            }
        }

        if (notAvailableList_V.size() > 0){
            if (checkConnection()) {
                dialog3 = ProgressDialog.show(this, null, "Downloading Video Thumbnails...", true);
                CURR_COUNT_V = 0;
                COUNT_V = NO_V;

                for (final int k : notAvailableList_V) {
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("productvideos");
                    query.whereEqualTo("pid", PID);
                    query.whereEqualTo("position", k);
                    query.findInBackground(new FindCallback<ParseObject>() {
                        public void done(List<ParseObject> objects, ParseException e) {
                            if (e == null) {
                                String videoUrl = objects.get(0).getString("pvideo_urls");
                                MYURL = videoUrl;
                                String[] parts = videoUrl.split("v=");
                                videoID = parts[1];
                                db.execSQL("INSERT INTO video VALUES(" + PID + "," + k + ",'" + MYURL + "','" + videoID + "');");
                                String thumbnailUrl = "http://img.youtube.com/vi/" + parts[1] + "/0.jpg";
                                startDownload(thumbnailUrl, PID + "_" + k + ".jpg");
                            } else {
                                Log.e("PARSE", "Error: " + e.getMessage());
                            }
                        }
                    });
                }

            } else {
                Toast.makeText(this, "Internet Connection unavailable!", Toast.LENGTH_LONG).show();
            }
        }




        //VIDEO IDS
        notAvailableList_VDB = new ArrayList<>(NO_V);
        notAvailableList_VDB.clear();
        for (int pos = 1; pos <= NO_V; ++pos) {
            Cursor c = db.rawQuery("SELECT videoid FROM video WHERE pid=" + PID + " AND position=" + pos + ";", null);
            try{
                c.moveToFirst();
                c.getString(0);
            }catch (Exception e){
                notAvailableList_VDB.add(pos);
            }
        }


        if(notAvailableList_VDB.size()>0) {
            if(checkConnection()) {
                dialog4 = ProgressDialog.show(this, null, "Downloading Video Details...", true);
                CURR_COUNT_VDB = 0;
                COUNT_VDB = notAvailableList_VDB.size();

                for (final int pos : notAvailableList_VDB) {
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("productvideos");
                    query.whereEqualTo("pid", PID);
                    query.whereEqualTo("position", pos);
                    query.findInBackground(new FindCallback<ParseObject>() {
                        public void done(List<ParseObject> objects, ParseException e) {
                            if (e == null) {
                                String videoUrl = objects.get(0).getString("pvideo_urls");
                                MYURL = videoUrl;
                                String[] parts = videoUrl.split("v=");
                                videoID = parts[1];
                                db.execSQL("INSERT INTO video VALUES(" + PID + "," + pos + ",'" + MYURL + "','" + videoID + "');");
                                CURR_COUNT_VDB++;
                                if (CURR_COUNT_VDB == COUNT_VDB) dialog4.dismiss();
                            } else {
                                Log.e("PARSE", "Error: " + e.getMessage());
                            }
                        }
                    });
                }
            }
        }



    }







    private void startDownload(String url,String filename) {
        new DownloadVideoThumbnailAsync().execute(url, filename);
    }

    class DownloadVideoThumbnailAsync extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... aurl) {
            int count;
            try {
                URL url = new URL(aurl[0]);
                String filename = aurl[1];
                URLConnection conexion = url.openConnection();
                conexion.connect();
                int lenghtOfFile = conexion.getContentLength();
                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory().toString() + "/showcommerce/p"+PID+"/video/" + filename);

                byte data[] = new byte[512];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                input.close();

            }
            catch(Exception e){
                File file = new File(Environment.getExternalStorageDirectory() + "/showcommerce/p"+PID+"/video/" + aurl[1]);
                file.delete();
            }
            return null;
        }

        @Override
        protected synchronized void onPostExecute(String unused) {
            CURR_COUNT_V++;
            if(CURR_COUNT_V==COUNT_V){
                dialog3.dismiss();
            }
        }
    }







    public void writeFile_360(byte[] data, String fileName) {
        try {
            FileOutputStream out = new FileOutputStream(Environment.getExternalStorageDirectory() + "/showcommerce/p"+PID+"/360/"+fileName);
            out.write(data);
            out.close();
        }catch(Exception e){
            Log.e("WriteFile_360",e.getMessage());
        }
    }

    public void writeFile_SS(byte[] data, String fileName) {
        try {
            FileOutputStream out = new FileOutputStream(Environment.getExternalStorageDirectory() + "/showcommerce/p"+PID+"/ss/"+fileName);
            out.write(data);
            out.close();
        }catch(Exception e){
            Log.e("WriteFile_SS",e.getMessage());
        }
    }

}