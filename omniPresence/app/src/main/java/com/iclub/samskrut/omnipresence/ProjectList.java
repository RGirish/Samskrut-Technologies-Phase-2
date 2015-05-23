
package com.iclub.samskrut.omnipresence;

import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.v4.widget.SwipeRefreshLayout;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.Parse;
import com.parse.ParseCrashReporting;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.microedition.khronos.egl.EGLConfig;

public class ProjectList extends CardboardActivity implements SwipeRefreshLayout.OnRefreshListener, TextToSpeech.OnInitListener,CardboardView.StereoRenderer, SensorEventListener {

    public static SQLiteDatabase db;
    public static int projectCount=0;
    ProgressDialog dialog1;
    SwipeRefreshLayout swipeLayout;
    public static TextToSpeech tts;
    int COUNT_th=0,CURR_COUNT_th=0;
    ArrayList<Integer> notAvailableList_th;
    int COUNT=0,CURR_COUNT=0;
    ArrayList<String> notAvailableList;
    ScrollView mainScrollView;
    LinearLayout mainll;
    static int currentProject;
    int d=0;
    int Xint,Yint,Zint;
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    boolean FLAG=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
        setContentView(R.layout.activity_project_list);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        currentProject = 0;

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        setFullscreen(true);

        mainScrollView = (ScrollView)findViewById(R.id.parent);
        mainll = (LinearLayout)findViewById(R.id.mainll);
        mainScrollView.setSmoothScrollingEnabled(true);

        mainScrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollYPx = mainScrollView.getScrollY();
                int scrollYDp = pxToDp(scrollYPx);
                currentProject = scrollYDp/360;
            }
        });

        try{ParseCrashReporting.enable(this);}catch (Exception e){}
        Parse.initialize(this, "Sq2yle2ei4MmMBXAChjGksJDqlwma3rjarvoZCsk", "vMw4I2I0fdSD1frBohAvWCaXZYqLaHZ8ljnwqavg");

        db = openOrCreateDatabase("omniPresence.db",SQLiteDatabase.CREATE_IF_NECESSARY, null);
        createTables();

        checkForDownload();

        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorScheme(android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark);

        displayEverything();

        tts = new TextToSpeech(this,this);

    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return (int)((dp * displayMetrics.density) + 0.5);
    }

    public int pxToDp(int px) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return (int) ((px/displayMetrics.density)+0.5);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP)){

            d++;
            Handler handler = new Handler();
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    if (d == 1){
                        //Scroll Down to next project in list
                        Cursor cursor = ProjectList.db.rawQuery("SELECT COUNT(pos) FROM projects;", null);
                        cursor.moveToFirst();
                        int COUNT = cursor.getInt(0);
                        cursor.close();
                        if(currentProject+1<COUNT) {
                            currentProject++;
                            int scrollYPx = mainScrollView.getScrollY();
                            int scrollYDp = pxToDp(scrollYPx);

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                            {
                                ValueAnimator realSmoothScrollAnimation =
                                        ValueAnimator.ofInt(mainScrollView.getScrollY(), mainScrollView.getScrollY() + dpToPx(360 - scrollYDp % 360));
                                realSmoothScrollAnimation.setDuration(800);
                                realSmoothScrollAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator animation)
                                    {
                                        int scrollTo = (Integer) animation.getAnimatedValue();
                                        mainScrollView.scrollTo(0, scrollTo);
                                    }
                                });

                                realSmoothScrollAnimation.start();
                            }
                            else{
                                mainScrollView.smoothScrollBy(0, dpToPx(360 - scrollYDp % 360));
                            }

                        }else{
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                            {
                                ValueAnimator realSmoothScrollAnimation =
                                        ValueAnimator.ofInt(mainScrollView.getScrollY(), 0);
                                realSmoothScrollAnimation.setDuration(800);
                                realSmoothScrollAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator animation)
                                    {
                                        int scrollTo = (Integer) animation.getAnimatedValue();
                                        mainScrollView.scrollTo(0, scrollTo);
                                    }
                                });

                                realSmoothScrollAnimation.start();
                            }
                            else{
                                mainScrollView.smoothScrollTo(0,0);
                            }
                        }
                    }
                    if (d == 2){
                        //Open current project in list
                        Cursor c = db.rawQuery("SELECT mediatype FROM subProjects WHERE projectPos="+currentProject+" AND pos=0;",null);
                        c.moveToFirst();
                        String type = c.getString(0);
                        if(type.equals("image")){
                            Intent intent = new Intent(ProjectList.this, MyVrView.class);
                            intent.putExtra("projectPos",currentProject);
                            intent.putExtra("pos",0);
                            startActivity(intent);
                        }else if(type.equals("video")){
                            Intent intent = new Intent(ProjectList.this, MyVrVideoView.class);
                            intent.putExtra("projectPos",currentProject);
                            intent.putExtra("pos",0);
                            startActivity(intent);
                        }
                    }
                    d = 0;
                }
            };
            if (d == 1) {
                handler.postDelayed(r, 500);
            }
        }else if((keyCode == KeyEvent.KEYCODE_BACK)){
            super.onBackPressed();
        }
        return true;
    }

    protected void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        senSensorManager.unregisterListener(this);
    }

    @Override
    public void onResume(){
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        tts.stop();
        View decorView = getWindow().getDecorView();
        if (android.os.Build.VERSION.SDK_INT >= 19) {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }else{
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    @Override
    public void onRefresh() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(false);
            }
        }, 2000);

        download();
    }

    public void checkForDownload(){
        Cursor cursor = db.rawQuery("SELECT COUNT(pos) FROM projects;", null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        if(count == 0){
            if(checkConnection()){
                download();
            }else{
                Toast.makeText(this,"Please check your Internet Connection!",Toast.LENGTH_LONG).show();
            }
        }
        cursor.close();
    }

    public void download(){
        dialog1 = ProgressDialog.show(this,null,"Downloading data...");
        Log.e("download", "download");

        /*
        db.execSQL("DELETE FROM projects_prev;");
        db.execSQL("DELETE FROM subProjects_prev;");

        Cursor cursor = db.rawQuery("SELECT * FROM projects", null);
        cursor.moveToFirst();

        while(true){
            int pos = cursor.getInt(0);
            String ts = cursor.getString(1);
            db.execSQL("INSERT INTO projects_prev VALUES("+pos+",'"+ts+"');");
            cursor.moveToNext();
            if(cursor.isAfterLast()){
                break;
            }
        }

        cursor = db.rawQuery("SELECT * FROM projects", null);
        cursor.moveToFirst();

        while(true){
            int projectPos = cursor.getInt(0);
            int pos = cursor.getInt(1);
            String tts = cursor.getString(2);
            String ts = cursor.getString(3);
            db.execSQL("INSERT INTO subProjects_prev VALUES("+projectPos+","+pos+",'"+tts+"','"+ts+"');");
            cursor.moveToNext();
            if(cursor.isAfterLast()){
                break;
            }
        }
        cursor.close();
        */


        db.execSQL("DELETE FROM projects;");
        db.execSQL("DELETE FROM subProjects;");

        final ParseQuery<ParseObject> query = ParseQuery.getQuery("Projects");
        query.orderByAscending("pos");
        query.selectKeys( Arrays.asList("pos","updatedAt"));
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    for (ParseObject ob : objects) {
                        db.execSQL("INSERT INTO projects VALUES(" + ob.getNumber("pos") + ",'" + ob.getUpdatedAt() + "');");
                        Log.e("QUERY","INSERT INTO projects VALUES(" + ob.getNumber("pos") + ",'" + ob.getUpdatedAt() + "');");
                    }

                    final ParseQuery<ParseObject> query = ParseQuery.getQuery("subProjects");
                    query.orderByAscending("projectPos");
                    query.addAscendingOrder("pos");
                    query.selectKeys(Arrays.asList("projectPos", "pos", "tts", "mediaType"));
                    query.findInBackground(new FindCallback<ParseObject>() {
                        public void done(List<ParseObject> objects, ParseException e) {
                            if (e == null) {
                                for (ParseObject ob : objects) {
                                    db.execSQL("INSERT INTO subProjects VALUES(" + ob.getNumber("projectPos") + ",'" + ob.getNumber("pos") + "','" + ob.getString("tts") + "','"+ob.getString("mediaType")+"','" + ob.getUpdatedAt() + "');");
                                    Log.e("QUERY","INSERT INTO subProjects VALUES(" + ob.getNumber("projectPos") + ",'" + ob.getNumber("pos") + "','" + ob.getString("tts") + "','"+ob.getString("mediaType")+"','" + ob.getUpdatedAt() + "');");
                                }
                                downloadProjectsThumbnails();
                            } else {
                                Log.e("PARSE", "Error: " + e.getMessage());
                            }
                        }
                    });

                } else {
                    Log.e("PARSE", "Error: " + e.getMessage());
                }
            }
        });
    }

    public void downloadProjectsThumbnails() {

        Log.e("downloadProjectsTh","downloadProjectsTh");

        //set notavailablelist for projects
        Cursor cursor = db.rawQuery("SELECT COUNT(pos) FROM projects;",null);
        cursor.moveToFirst();
        COUNT_th = cursor.getInt(0);
        notAvailableList_th = new ArrayList<>(COUNT_th);
        cursor.close();

        cursor = db.rawQuery("SELECT pos FROM projects ORDER BY pos;", null);
        try{
            cursor.moveToFirst();
            while(true){
                int pos = cursor.getInt(0);
                if (!exists(pos + "_th.jpg")) {
                    notAvailableList_th.add(pos);
                }
                cursor.moveToNext();
                if(cursor.isAfterLast()){
                    break;
                }
            }
        }catch (Exception e){}
        cursor.close();

        //set notavailablelist for subprojects
        cursor = db.rawQuery("SELECT COUNT(pos) FROM subProjects;",null);
        cursor.moveToFirst();
        COUNT = cursor.getInt(0);
        notAvailableList = new ArrayList<String>(COUNT);
        cursor.close();

        cursor = db.rawQuery("SELECT projectPos,pos,timestamp FROM subProjects ORDER BY projectPos,pos;", null);

            cursor.moveToFirst();
            while(true){
                int projectPos = cursor.getInt(0);
                int pos = cursor.getInt(1);
                if (!exists(projectPos + "_" + pos + ".jpg") && !exists(projectPos + "_" + pos + ".mp4")) {
                    notAvailableList.add(projectPos + "_" + pos);
                }
                cursor.moveToNext();
                if(cursor.isAfterLast()){
                    break;
                }
            }

        cursor.close();



        if(notAvailableList_th.size()>0){
            dialog1.setMessage("Downloading Thumbnail 1/" + notAvailableList_th.size());
        }else{
            if(notAvailableList.size()>0){
                downloadSubProjectsMedia();
            }else{
                dialog1.dismiss();
                displayEverything();
            }
        }
        CURR_COUNT_th=0;
        for (final int k : notAvailableList_th) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Projects");
            query.whereEqualTo("pos", k);
            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null) {
                        ParseFile myFile = objects.get(0).getParseFile("thumbnail");
                        myFile.getDataInBackground(new GetDataCallback() {
                            public void done(byte[] data, ParseException e) {
                                if (e == null) {
                                    writeFile(data, k + "_th.jpg");
                                    CURR_COUNT_th++;
                                    dialog1.setMessage("Downloading Thumbnail "+(CURR_COUNT_th+1)+"/"+notAvailableList_th.size());
                                    if (CURR_COUNT_th == notAvailableList_th.size()) {

                                        if(notAvailableList.size()>0){
                                            downloadSubProjectsMedia();
                                        }else{
                                            dialog1.dismiss();
                                            displayEverything();
                                        }

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

    }

    public void downloadSubProjectsMedia(){

        Log.e("downloadSubProjectsMedia","downloadSubProjectsMedia");
        dialog1.setMessage("Downloading Panorama 1/" + notAvailableList.size());
        if(notAvailableList.size()==0){
            dialog1.dismiss();
            displayEverything();
        }
        CURR_COUNT=0;
        for (final String s : notAvailableList) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("subProjects");
            String[] parts = s.split("_");
            final int projectPos = Integer.parseInt(parts[0]);
            final int pos = Integer.parseInt(parts[1]);
            query.whereEqualTo("pos", pos);
            query.whereEqualTo("projectPos", projectPos);
            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null) {
                        final ParseFile myFile = objects.get(0).getParseFile("photoSphere");
                        myFile.getDataInBackground(new GetDataCallback() {
                            public void done(byte[] data, ParseException e) {
                                if (e == null) {
                                    if(myFile.getName().endsWith("jpg")){
                                        Log.e("FILENAME", "jpg");
                                        writeFile(data, projectPos + "_" + pos + ".jpg");
                                    }else if(myFile.getName().endsWith("mp4")){
                                        Log.e("FILENAME", "mp4");
                                        writeFile(data, projectPos + "_" + pos + ".mp4");
                                    }
                                    CURR_COUNT++;
                                    dialog1.setMessage("Downloading Panorama "+(CURR_COUNT+1)+"/"+notAvailableList.size());
                                    if (CURR_COUNT == notAvailableList.size()) {
                                        dialog1.dismiss();
                                        displayEverything();
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
    }

    public void displayEverything(){

        LinearLayout mainll = (LinearLayout)findViewById(R.id.mainll);
        mainll.removeAllViews();


        Cursor cursor = db.rawQuery("SELECT pos FROM projects ORDER BY pos;",null);
        try{
            cursor.moveToFirst();
            while(true){
                projectCount++;
                final int projectPos = cursor.getInt(0);

                LinearLayout ll = new LinearLayout(this);
                ll.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                ll.setPadding(0, 0, 0, (int) getResources().getDimension(R.dimen.dp20));
                ll.setLayoutParams(params);
                ll.setBackgroundColor(Color.BLACK);
                ll.setGravity(Gravity.CENTER_HORIZONTAL);

                ImageView imageButton = new ImageView(this);
                imageButton.setTag("project" + projectPos);
                InputStream is = openFileInput(projectPos + "_th.jpg");
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                imageButton.setImageBitmap(bitmap);
                params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)getResources().getDimension(R.dimen.dp330));
                params.setMargins(0, (int) getResources().getDimension(R.dimen.dp5), 0, (int) getResources().getDimension(R.dimen.dp5));
                imageButton.setLayoutParams(params);
                imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Cursor c = db.rawQuery("SELECT mediatype FROM subProjects WHERE projectPos="+projectPos+" AND pos=0;",null);
                        c.moveToFirst();
                        String type = c.getString(0);
                        if(type.equals("image")){
                            Intent intent = new Intent(ProjectList.this, MyVrView.class);
                            intent.putExtra("projectPos",projectPos);
                            intent.putExtra("pos",0);
                            startActivity(intent);
                        }else if(type.equals("video")){
                            Intent intent = new Intent(ProjectList.this, MyVrVideoView.class);
                            intent.putExtra("projectPos",projectPos);
                            intent.putExtra("pos",0);
                            startActivity(intent);
                        }

                    }
                });
                ll.addView(imageButton);

                mainll.addView(ll);

                cursor.moveToNext();
                if(cursor.isAfterLast()){
                    break;
                }
            }
        }catch (Exception e){}

        cursor.close();


    }

    public void createTables() {

        /*db.execSQL("DROP TABLE projects;");
        db.execSQL("DROP TABLE subProjects;");
        db.execSQL("DROP TABLE projects_prev;");
        db.execSQL("DROP TABLE subProjects_prev;");*/

        try{
            db.execSQL("CREATE TABLE projects(pos NUMBER,timestamp String);");
        }catch(Exception e){}
        try{
            db.execSQL("CREATE TABLE subProjects(projectPos NUMBER, pos NUMBER, tts TEXT, mediatype TEXT, timestamp DATE);");
        }catch(Exception e){}
        try{
            db.execSQL("CREATE TABLE projects_prev(pos NUMBER,timestamp String);");
        }catch(Exception e){}
        try{
            db.execSQL("CREATE TABLE subProjects_prev(projectPos NUMBER, pos NUMBER, tts TEXT, mediatype TEXT, timestamp DATE);");
        }catch(Exception e){}
    }

    public boolean checkConnection(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo == null ) return false;
        else return true;
    }

    public void writeFile(byte[] data, String fileName) {
        try {
            FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE);
            fos.write(data);
            fos.close();
        }catch(Exception e){
            Log.e("WriteFile",e.getMessage());
        }
    }

    private void setFullscreen(boolean fullscreen) {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        if (fullscreen) {
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }
        else{
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }
        getWindow().setAttributes(attrs);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            tts.setSpeechRate(0.75f);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                Toast.makeText(this,"This Language is not supported!",Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(this,"TTS Initialization Failed!",Toast.LENGTH_LONG).show();
        }
    }

    public boolean exists(String fname){
        File file = getBaseContext().getFileStreamPath(fname);
        return file.exists();
    }

    @Override
    protected void onDestroy() {
        if(tts != null) {
            tts.stop();
            tts.shutdown();
        }
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onDestroy();
    }

    @Override
    public void onCardboardTrigger(){

        if(!((Xint == 6 || Xint == 7 || Xint == 8) && (Yint == -2 || Yint == -1 || Yint == 0 || Yint == 1 || Yint == 2) && (Zint == 6 || Zint == 7 || Zint == 8)) && !((Xint == 6 || Xint == 7 || Xint == 8) && (Yint == -2 || Yint == -1 || Yint == 0 || Yint == 1 || Yint == 2) && (Zint == -6 || Zint == -7 || Zint == -8))){
            Cursor c = db.rawQuery("SELECT mediatype FROM subProjects WHERE projectPos="+currentProject+" AND pos=0;",null);
            c.moveToFirst();
            String type = c.getString(0);
            if(type.equals("image")){
                Intent intent = new Intent(ProjectList.this, MyVrView.class);
                intent.putExtra("projectPos",currentProject);
                intent.putExtra("pos",0);
                startActivity(intent);
            }else if(type.equals("video")){
                Intent intent = new Intent(ProjectList.this, MyVrVideoView.class);
                intent.putExtra("projectPos",currentProject);
                intent.putExtra("pos",0);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(FLAG){
            Sensor mySensor = sensorEvent.sensor;
            if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                Xint = (int) sensorEvent.values[0];
                Yint = (int) sensorEvent.values[1];
                Zint = (int) sensorEvent.values[2];

                //Log.e("COORDINATES", Xint + " " + Yint + " " + Zint);

                if (((Xint == 6 || Xint == 7 || Xint == 8) && (Yint == -2 || Yint == -1 || Yint == 0 || Yint == 1 || Yint == 2) && (Zint == 6 || Zint == 7 || Zint == 8))) {

                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(200);

                    Cursor cursor = ProjectList.db.rawQuery("SELECT COUNT(pos) FROM projects;", null);
                    cursor.moveToFirst();
                    int COUNT = cursor.getInt(0);
                    cursor.close();
                    if (currentProject + 1 < COUNT) {
                        currentProject++;
                        int scrollYPx = mainScrollView.getScrollY();
                        int scrollYDp = pxToDp(scrollYPx);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                        {
                            ValueAnimator realSmoothScrollAnimation =
                                    ValueAnimator.ofInt(mainScrollView.getScrollY(), mainScrollView.getScrollY() + dpToPx(360 - scrollYDp % 360));
                            realSmoothScrollAnimation.setDuration(800);
                            realSmoothScrollAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
                                @Override
                                public void onAnimationUpdate(ValueAnimator animation)
                                {
                                    int scrollTo = (Integer) animation.getAnimatedValue();
                                    mainScrollView.scrollTo(0, scrollTo);
                                }
                            });

                            realSmoothScrollAnimation.start();
                        }
                        else{
                            mainScrollView.smoothScrollBy(0, dpToPx(360 - scrollYDp % 360));
                        }


                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                        {
                            ValueAnimator realSmoothScrollAnimation = ValueAnimator.ofInt(mainScrollView.getScrollY(), 0);
                            realSmoothScrollAnimation.setDuration(800);
                            realSmoothScrollAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
                                @Override
                                public void onAnimationUpdate(ValueAnimator animation)
                                {
                                    int scrollTo = (Integer) animation.getAnimatedValue();
                                    mainScrollView.scrollTo(0, scrollTo);
                                }
                            });

                            realSmoothScrollAnimation.start();
                        }
                        else{
                            mainScrollView.smoothScrollTo(0,0);
                        }
                    }
                }else if (((Xint == 6 || Xint == 7 || Xint == 8) && (Yint == -2 || Yint == -1 || Yint == 0 || Yint == 1 || Yint == 2) && (Zint == -6 || Zint == -7 || Zint == -8))) {

                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(200);

                    if (currentProject - 1 >= 0) {
                        currentProject--;
                        int scrollYPx = mainScrollView.getScrollY();
                        int scrollYDp = pxToDp(scrollYPx);
                        int val = mainScrollView.getScrollY() - dpToPx(scrollYDp % 360);
                        if(dpToPx(scrollYDp % 360)==0)val=mainScrollView.getScrollY()-dpToPx(360);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                        {
                            ValueAnimator realSmoothScrollAnimation =
                                    ValueAnimator.ofInt(mainScrollView.getScrollY(), val);
                            realSmoothScrollAnimation.setDuration(800);
                            realSmoothScrollAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
                                @Override
                                public void onAnimationUpdate(ValueAnimator animation) {
                                    int scrollTo = (Integer) animation.getAnimatedValue();
                                    mainScrollView.scrollTo(0, scrollTo);
                                }
                            });
                            realSmoothScrollAnimation.start();
                        }else{
                            mainScrollView.smoothScrollTo(0, mainScrollView.getScrollY() - dpToPx(scrollYDp % 360));
                        }
                    }else{
                        Cursor cursor = ProjectList.db.rawQuery("SELECT MAX(pos) FROM projects;", null);
                        cursor.moveToFirst();
                        int MAX = cursor.getInt(0);
                        cursor.close();
                        currentProject = MAX;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                        {
                            ValueAnimator realSmoothScrollAnimation = ValueAnimator.ofInt(mainScrollView.getScrollY(), dpToPx(360*(currentProject)));
                            realSmoothScrollAnimation.setDuration(800);
                            realSmoothScrollAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
                                @Override
                                public void onAnimationUpdate(ValueAnimator animation)
                                {
                                    int scrollTo = (Integer) animation.getAnimatedValue();
                                    mainScrollView.scrollTo(0, scrollTo);
                                }
                            });

                            realSmoothScrollAnimation.start();
                        }
                        else{
                            mainScrollView.smoothScrollTo(0, dpToPx(360*(currentProject)));
                        }
                    }
                }
            }
            FLAG = false;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    FLAG = true;
                }
            }, 1300);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onNewFrame(HeadTransform headTransform) {}

    @Override
    public void onDrawEye(Eye eye) {}

    @Override
    public void onFinishFrame(Viewport viewport) {}

    @Override
    public void onSurfaceChanged(int i, int i1) {}

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {}

    @Override
    public void onRendererShutdown() {}
}