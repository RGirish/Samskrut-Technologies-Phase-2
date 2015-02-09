package samskrut.iclub.com.scslideshow;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    private static final int NUM_PAGES = 8;
    private static ViewPager mPager;
    public static Firebase ref;
    private static PagerAdapter mPagerAdapter;
    TheClass ob;
    private int PID;
    public ProgressDialog dialog;
    private int COUNT=0,CURR_COUNT=0;
    ArrayList<Integer> notAvailableList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ob=new TheClass();

        if(checkConnection()){
            Firebase.setAndroidContext(this);
            ref=new Firebase("https://smartdemo.firebaseio.com/TV01/ss");
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    try {
                        new Thread(new Task(snapshot.getValue().toString())).start();
                    }catch(NullPointerException e){}
                }
                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    Log.e("ERROR at onCancelled()",firebaseError.getMessage());
                }
            });
        }
        PID=130;

        Parse.initialize(this, "28rzQwSoD7MFQOOViu9awAI0giaUDK8E7ADYbXAz", "jbYQAqhT1jcRiIUrS3UwuFuFOipjv04kUYhZpkEN");

        File folder = new File(Environment.getExternalStorageDirectory() + "/showcommerce");
        if (!folder.exists()) {
            folder.mkdir();
        }
        File folder2 = new File(Environment.getExternalStorageDirectory() + "/showcommerce/p"+PID);
        if (!folder2.exists()) {
            folder2.mkdir();
        }
        File folder3 = new File(Environment.getExternalStorageDirectory() + "/showcommerce/p"+PID+"/ss");
        if (!folder3.exists()) {
            folder3.mkdir();
        }

        downloadEverything();
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

    public boolean checkConnection(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo == null ) return false;
        else return true;
    }

    class Task implements Runnable {
        String s;
        Task(String parameter){
            s=parameter;
        }
        @Override
        public void run() {
            ob.send(s);
        }
    }

    class TheClass{
        public synchronized void send(final String s){
            Log.e("FIREBASE RECEIVED",s);
            runOnUiThread(new Runnable(){
                public void run() {
                    mPager.setCurrentItem(Integer.parseInt(s));
                }
            });
        }
    }

    public void downloadEverything(){
        notAvailableList = new ArrayList<>(8);
        notAvailableList.clear();
        for (int i = 1; i <= 8; ++i) {
            String FILENAME = Environment.getExternalStorageDirectory().toString() + "/showcommerce/p" + PID + "/ss/" + PID + "_" + i + ".jpg";
            File file = new File(FILENAME);
            if (!file.exists()) {
                notAvailableList.add(i);
            }
        }

        if (notAvailableList.size() > 0) {
            if (checkConnection()) {
                dialog = ProgressDialog.show(this, null, "Just a moment...", true);
                CURR_COUNT = 0;
                COUNT = notAvailableList.size();
                for (final int k : notAvailableList) {
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Test2Slideshow");
                    query.whereEqualTo("pid", PID);
                    query.whereEqualTo("position", k);
                    query.findInBackground(new FindCallback<ParseObject>() {
                        public void done(List<ParseObject> objects, ParseException e) {
                            if (e == null) {
                                ParseFile myFile = objects.get(0).getParseFile("imageFile");
                                myFile.getDataInBackground(new GetDataCallback() {
                                    public void done(byte[] data, ParseException e) {
                                        if (e == null) {
                                            writeFile(data, PID + "_" + k + ".jpg");
                                            CURR_COUNT++;
                                            if (CURR_COUNT == COUNT) {
                                                mPager = (ViewPager) findViewById(R.id.slideshowpager);
                                                mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
                                                mPager.setAdapter(mPagerAdapter);
                                                dialog.dismiss();
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
        }else{
            mPager = (ViewPager) findViewById(R.id.slideshowpager);
            mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
            mPager.setAdapter(mPagerAdapter);
        }
    }


    public void writeFile(byte[] data, String fileName) {
        try {
            FileOutputStream out = new FileOutputStream(Environment.getExternalStorageDirectory() + "/showcommerce/p"+PID+"/ss/"+fileName);
            out.write(data);
            out.close();
        }catch(Exception e){
            Log.e("WriteFile",e.getMessage());
        }
    }


}