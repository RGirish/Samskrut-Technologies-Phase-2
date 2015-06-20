
package com.samskrut.unrealestate;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Splash extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    ProgressDialog dialog1;
    SQLiteDatabase db;
    SwipeRefreshLayout swipeLayout;
    int COUNT=0,CURR_COUNT=0;
    ArrayList<Integer> notAvailableList,toBeDeletedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        setFullscreen(true);
        db = openOrCreateDatabase("unrealestate.db",SQLiteDatabase.CREATE_IF_NECESSARY, null);
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorScheme(android.R.color.black);

        checkForDownload();
        display();

        (findViewById(R.id.logout)).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    ((TextView) v).setTextColor(Color.parseColor("#aaffffff"));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    ((TextView) v).setTextColor(Color.parseColor("#ffffff"));
                    SQLiteDatabase db = openOrCreateDatabase("unrealestate.db", SQLiteDatabase.CREATE_IF_NECESSARY, null);
                    db.execSQL("DELETE FROM session;");
                    db.close();
                    Intent mainIntent = new Intent(Splash.this, Login.class);
                    Splash.this.startActivity(mainIntent);
                    Splash.this.finish();
                } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                    ((TextView) v).setTextColor(Color.parseColor("#ffffff"));
                }
                return true;
            }
        });

        (findViewById(R.id.instructions)).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    ((TextView) v).setTextColor(Color.parseColor("#aaffffff"));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    ((TextView) v).setTextColor(Color.parseColor("#ffffff"));
                    startActivity(new Intent(Splash.this, Instructions.class));
                    finish();
                } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                    ((TextView) v).setTextColor(Color.parseColor("#ffffff"));
                }
                return true;
            }
        });

        (findViewById(R.id.viewVirtualTours)).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    ((TextView) v).setTextColor(Color.parseColor("#aaffffff"));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    ((TextView) v).setTextColor(Color.parseColor("#ffffff"));
                    Intent mainIntent = new Intent(Splash.this, MainActivity.class);
                    Splash.this.startActivity(mainIntent);
                    Splash.this.finish();
                } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                    ((TextView) v).setTextColor(Color.parseColor("#ffffff"));
                }
                return true;
            }
        });

    }

    public void checkForDownload(){
        Cursor cursor = Login.db.rawQuery("SELECT COUNT(name) FROM "+Login.USERNAME+"_projects WHERE username='"+Login.USERNAME+"';", null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        if (count == 0) {
            if (checkConnection()) {
                download();
            } else {
                Toast.makeText(this, "Please check your Internet Connection!", Toast.LENGTH_LONG).show();
                (findViewById(R.id.logout)).setVisibility(View.GONE);
                (findViewById(R.id.instructions)).setVisibility(View.GONE);
                (findViewById(R.id.viewVirtualTours)).setVisibility(View.GONE);
            }
        }
        cursor.close();
    }

    public void display(){
        LinearLayout ll = (LinearLayout)findViewById(R.id.splashBackground);
        InputStream is = null;
        try{
            is = openFileInput(Login.USERNAME + "_splash.jpg");
        }catch (Exception ex){}
        Bitmap bitmap = BitmapFactory.decodeStream(is);
        Drawable d = new BitmapDrawable(getResources(), bitmap);
        ll.setBackground(d);
    }

    public void download(){
        dialog1 = ProgressDialog.show(this, null, "Downloading Data...");
        Login.db.execSQL("DELETE FROM "+Login.USERNAME+"_projects_temp;");
        final ParseQuery<ParseObject> query = ParseQuery.getQuery("unrealEstate");
        query.whereEqualTo("username",Login.USERNAME);
        query.orderByAscending("pos");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    for (ParseObject ob : objects) {
                        Login.db.execSQL("INSERT INTO "+Login.USERNAME+"_projects_temp VALUES(" + ob.getNumber("pos") + ",'" + ob.getString("name") + "','" + ob.getString("description") + "','" + ob.getString("url") + "','" + ob.getString("username") + "','" + ob.getUpdatedAt() + "');");
                        Log.e("QUERY","INSERT INTO "+Login.USERNAME+"_projects_temp VALUES(" + ob.getNumber("pos") + ",'" + ob.getString("name") + "','" + ob.getString("description") + "','" + ob.getString("url") + "','" + ob.getString("username") + "','" + ob.getUpdatedAt() + "');");
                    }
                    downloadSplashImage();
                } else {
                    Log.e("PARSE", "Error: " + e.getMessage());
                }
            }
        });
    }

    public void downloadSplashImage() {
        Log.e("downloadSplashImage","downloadSplashImage");

        dialog1.setMessage("Downloading Splash Image...");
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Splash");
        query.whereEqualTo("username", Login.USERNAME);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    ParseFile myFile = objects.get(0).getParseFile("image");
                    myFile.getDataInBackground(new GetDataCallback() {
                        public void done(byte[] data, ParseException e) {
                            if (e == null) {
                                writeFile(data, Login.USERNAME+"_splash.jpg");
                                downloadImages();
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

    public void downloadImages(){


        //SET NOTAVAILABLELIST
        Cursor cursor = Login.db.rawQuery("SELECT COUNT(pos) FROM "+Login.USERNAME+"_projects_temp WHERE username='"+Login.USERNAME+"';",null);
        cursor.moveToFirst();
        COUNT = cursor.getInt(0);
        notAvailableList = new ArrayList<>(COUNT);
        toBeDeletedList = new ArrayList<>(COUNT);
        cursor.close();

        cursor = Login.db.rawQuery("SELECT pos,timestamp FROM "+Login.USERNAME+"_projects_temp WHERE username='"+Login.USERNAME+"' ORDER BY pos;", null);
        try{
            cursor.moveToFirst();
            while(true){
                int pos = cursor.getInt(0);
                if (!exists(Login.USERNAME+"_" + pos + ".jpg")) {
                    //2 casees: case1:if its a new item. case2: if an existing item(with or without change) has been deleted somehow
                    notAvailableList.add(pos);
                }

                Cursor c = Login.db.rawQuery("SELECT pos,timestamp FROM "+Login.USERNAME+"_projects WHERE pos="+pos+" AND username='"+Login.USERNAME+"';", null);
                try {
                    c.moveToFirst();
                    int n = c.getInt(0);
                    String currentTime = c.getString(1);
                    String updatedTime = cursor.getString(1);
                    if(!currentTime.equals(updatedTime)){
                        //the item has been modified
                        if(!notAvailableList.contains(pos)) notAvailableList.add(pos);
                    }
                }catch (Exception e){
                    //it's a new item and it has already been added to the list
                }

                cursor.moveToNext();
                if(cursor.isAfterLast()){
                    break;
                }
            }
        }catch (Exception e){}
        cursor.close();


        //SET TOBEDELETEDLIST
        cursor = Login.db.rawQuery("SELECT pos FROM "+Login.USERNAME+"_projects WHERE username='"+Login.USERNAME+"' ORDER BY pos;", null);
        try{
            cursor.moveToFirst();
            while(true){
                int pos = cursor.getInt(0);
                Cursor c = Login.db.rawQuery("SELECT pos FROM "+Login.USERNAME+"_projects_temp WHERE username='"+Login.USERNAME+"' WHERE pos="+pos+";", null);
                try {
                    c.moveToFirst();
                    int n = c.getInt(0);
                }catch (Exception e){
                    toBeDeletedList.add(pos);
                }
                cursor.moveToNext();
                if(cursor.isAfterLast()){
                    break;
                }
            }
        }catch (Exception e){}
        cursor.close();


        String s="";
        for(int i: notAvailableList){
            s= s+ (i+" ");
        }
        Log.e("notAvailableList",s);
        s="";
        for(int i: toBeDeletedList){
            s= s+ (i+" ");
        }
        Log.e("toBeDeletedList",s);


        //MOVE FROM TEMP TABLE TO ORIGINAL TABLE


        Login.db.execSQL("DELETE FROM "+Login.USERNAME+"_projects;");
        cursor = Login.db.rawQuery("SELECT * FROM "+Login.USERNAME+"_projects_temp ORDER BY pos;", null);
        try{
            cursor.moveToFirst();
            while(true){
                int pos = cursor.getInt(0);
                String name = cursor.getString(1);
                String desc = cursor.getString(2);
                String url = cursor.getString(3);
                String username = cursor.getString(4);
                String ts = cursor.getString(5);
                Login.db.execSQL("INSERT INTO "+Login.USERNAME+"_projects VALUES("+pos+",'"+name+"','"+desc+"','"+url+"','"+username+"','"+ts+"');");
                cursor.moveToNext();
                if(cursor.isAfterLast()){
                    break;
                }
            }
        }catch (Exception e){}
        cursor.close();



        //CLEAR THE TEMP TABLE
        Login.db.execSQL("DELETE FROM "+Login.USERNAME+"_projects_temp WHERE username='"+Login.USERNAME+"';");



        //DELETE FILES IF ANY
        File dir = getFilesDir();
        for(int i : toBeDeletedList){
            File file = new File(dir, Login.USERNAME+"_" + i + ".jpg");
            file.delete();
        }



        //START DOWNLOADS IF ANY


        dialog1.setMessage("Downloading Image 1/"+notAvailableList.size());
        CURR_COUNT=0;
        for (final int k : notAvailableList) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("unrealEstate");
            query.whereEqualTo("pos", k);
            query.whereEqualTo("username", Login.USERNAME);
            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null) {
                        ParseFile myFile = objects.get(0).getParseFile("image");
                        myFile.getDataInBackground(new GetDataCallback() {
                            public void done(byte[] data, ParseException e) {
                                if (e == null) {
                                    writeFile(data, Login.USERNAME + "_" + k + ".jpg");
                                    CURR_COUNT++;
                                    dialog1.setMessage("Downloading Image "+(CURR_COUNT+1)+"/"+notAvailableList.size());
                                    if (CURR_COUNT == notAvailableList.size()) {
                                        dialog1.dismiss();
                                        display();
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

        if(notAvailableList.size()==0){
            dialog1.dismiss();
            display();
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

    public void writeFile(byte[] data, String fileName) {
        try {
            FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE);
            fos.write(data);
            fos.close();
        }catch(Exception e){
            Log.e("WriteFile",e.getMessage());
        }
    }

    public boolean exists(String fname){
        File file = getBaseContext().getFileStreamPath(fname);
        return file.exists();
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(false);
            }
        }, 1000);
        download();
    }

    public boolean checkConnection(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo == null ) return false;
        else return true;
    }

}