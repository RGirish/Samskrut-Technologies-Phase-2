package com.samskrut.omnipresence;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
    int COUNT=0,CURR_COUNT=0;
    ArrayList<Integer> notAvailableList,toBeDeletedList;
    SwipeRefreshLayout swipeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        setFullscreen(true);
        db = openOrCreateDatabase("omniPresence.db",SQLiteDatabase.CREATE_IF_NECESSARY, null);
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorScheme(android.R.color.black);
        checkForDownload();
        display();
    }

    public void display(){
        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.splash);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int height = displayMetrics.heightPixels;
        linearLayout.setMinimumHeight(height);

        InputStream is = null;
        try{
            is = openFileInput(Login.USERNAME+"_splash_0.jpg");
        }catch(Exception e){
            Log.e("display()","File not found yo man");
        }
        Bitmap bitmap = BitmapFactory.decodeStream(is);
        Drawable drawable = new BitmapDrawable(getResources(), bitmap);
        linearLayout.setBackground(drawable);

        final TextView skip = (TextView)findViewById(R.id.skip);
        skip.setText("<Skip>");
        skip.setTextSize(19);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(Splash.this, Countdown.class);
                Splash.this.startActivity(mainIntent);
                Splash.this.finish();
            }
        });

        final TextView logout = (TextView)findViewById(R.id.logout);
        logout.setText("<Logout>");
        logout.setTextSize(18);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLiteDatabase db = openOrCreateDatabase("omniPresence.db",SQLiteDatabase.CREATE_IF_NECESSARY, null);
                db.execSQL("UPDATE session SET projectsTableName='NONE',subProjectsTableName='NONE';");
                db.close();
                Intent mainIntent = new Intent(Splash.this, Login.class);
                //mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                Splash.this.startActivity(mainIntent);
                Splash.this.finish();
            }
        });


        final TextView conti = (TextView)findViewById(R.id.conti);
        conti.setTextSize(18);
        conti.setText("<Continue>");
        conti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Splash.this,Instructions.class));
                finish();
            }
        });

    }

    public void checkForDownload(){
        Cursor cursor = db.rawQuery("SELECT COUNT(pos) FROM splash WHERE username='"+Login.USERNAME+"';", null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        if(count == 0){
            if(checkConnection()){
                download();
            }else{
                Toast.makeText(this, "Please check your Internet Connection!", Toast.LENGTH_LONG).show();
            }
        }
        cursor.close();
    }

    public void download(){
        dialog1 = ProgressDialog.show(this, null, "Downloading data...");
        Log.e("download", "download");

        final ParseQuery<ParseObject> query = ParseQuery.getQuery("Splash");
        query.orderByAscending("pos");
        query.whereEqualTo("username", Login.USERNAME);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    for (ParseObject ob : objects) {
                        db.execSQL("INSERT INTO splash_temp VALUES('" + ob.getString("username") + "'," + ob.getNumber("pos") + ",'"+ob.getUpdatedAt()+"');");
                        Log.e("QUERY","INSERT INTO splash_temp VALUES('" + ob.getString("username") + "'," + ob.getNumber("pos") + ",'"+ob.getUpdatedAt()+"');");
                    }
                    downloadSplashImages();
                } else {
                    Log.e("PARSE", "Error: " + e.getMessage());
                }
            }
        });
    }

    public void downloadSplashImages() {

        Log.e("downloadSplashImages","downloadSplashImages");

        //SET NOTAVAILABLELIST FOR SPLASH IMAGES
        Cursor cursor = db.rawQuery("SELECT COUNT(pos) FROM splash WHERE username='"+Login.USERNAME+"';",null);
        cursor.moveToFirst();
        COUNT = cursor.getInt(0);
        notAvailableList = new ArrayList<>(COUNT);
        toBeDeletedList = new ArrayList<>(COUNT);
        cursor.close();

        cursor = db.rawQuery("SELECT pos,timestamp FROM splash_temp WHERE username='"+Login.USERNAME+"' ORDER BY pos;", null);
        try{
            cursor.moveToFirst();
            while(true){
                int pos = cursor.getInt(0);
                if (!exists(Login.USERNAME+"_splash_" + pos + ".jpg")) {
                    //2 casees: case1:if its a new item. case2: if an existing item(with or without change) has been deleted somehow
                    notAvailableList.add(pos);
                }

                Cursor c = db.rawQuery("SELECT pos,timestamp FROM splash WHERE pos="+pos+" AND username='"+Login.USERNAME+"';", null);
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


        //SET TOBEDELETEDLIST FOR SPLASH IMAGES
        cursor = db.rawQuery("SELECT pos FROM splash WHERE username='"+Login.USERNAME+"' ORDER BY pos;", null);
        try{
            cursor.moveToFirst();
            while(true){
                int pos = cursor.getInt(0);
                Cursor c = db.rawQuery("SELECT pos FROM splash_temp WHERE pos="+pos+" AND username='"+Login.USERNAME+"';", null);
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
        Log.e("notAvailableList_splash",s);
        s="";
        for(int i: toBeDeletedList){
            s= s+ (i+" ");
        }
        Log.e("toBeDeletedList_splash",s);



        //MOVE FROM TEMP TABLES TO ORIGINAL TABLES


        db.execSQL("DELETE FROM splash WHERE username='"+Login.USERNAME+"';");
        cursor = db.rawQuery("SELECT * FROM splash_temp WHERE username='"+Login.USERNAME+"' ORDER BY pos;", null);
        try{
            cursor.moveToFirst();
            while(true){
                String username = cursor.getString(0);
                int pos = cursor.getInt(1);
                String ts = cursor.getString(2);
                db.execSQL("INSERT INTO splash VALUES('"+username+"',"+pos+",'"+ts+"');");
                cursor.moveToNext();
                if(cursor.isAfterLast()){
                    break;
                }
            }
        }catch (Exception e){}
        cursor.close();
        db.execSQL("DELETE FROM splash_temp;");


        //DELETE FILES IF ANY
        File dir = getFilesDir();
        for(int i : toBeDeletedList){
            File file = new File(dir, Login.USERNAME+"_splash_" + i + ".jpg");
            file.delete();
        }



        //START DOWNLOAD IF ANY


        if(notAvailableList.size()>0){
            dialog1.setMessage("Downloading Splash Image 1/" + notAvailableList.size());
        }else{
            dialog1.dismiss();
            display();
        }
        CURR_COUNT=0;
        for (final int k : notAvailableList) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Splash");
            query.whereEqualTo("pos", k);
            query.whereEqualTo("username", Login.USERNAME);
            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null) {
                        ParseFile myFile = objects.get(0).getParseFile("image");
                        myFile.getDataInBackground(new GetDataCallback() {
                            public void done(byte[] data, ParseException e) {
                                if (e == null) {
                                    writeFile(data, Login.USERNAME+"_splash_" + k + ".jpg");
                                    CURR_COUNT++;
                                    dialog1.setMessage("Downloading Thumbnail "+(CURR_COUNT+1)+"/"+notAvailableList.size());
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