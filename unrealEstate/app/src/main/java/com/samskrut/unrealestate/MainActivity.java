package com.samskrut.unrealestate;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.crashlytics.android.Crashlytics;
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
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    ProgressDialog dialog1;
    SwipeRefreshLayout swipeLayout;
    int COUNT=0,CURR_COUNT=0;
    ArrayList<Integer> notAvailableList,toBeDeletedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"#ffffff\">unrealEstate</font>"));
        checkForDownload();
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorScheme(android.R.color.holo_orange_dark, android.R.color.holo_red_dark);
        displayEverything();
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

    public void checkForDownload() {
        Cursor cursor = Login.db.rawQuery("SELECT COUNT(name) FROM "+Login.USERNAME+"_projects WHERE username='"+Login.USERNAME+"';", null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        if (count == 0) {
            if (checkConnection()) {
                download();
            } else {
                Toast.makeText(this, "Please check your Internet Connection!", Toast.LENGTH_LONG).show();
            }
        }
        cursor.close();
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
                    downloadImages();
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

        if(notAvailableList.size()==0){
            dialog1.dismiss();
            displayEverything();
        }

    }

    public void displayEverything(){

        LinearLayout mainll = (LinearLayout)findViewById(R.id.mainll);
        mainll.removeAllViews();

        Cursor cursor = Login.db.rawQuery("SELECT pos,name,desc,url FROM "+Login.USERNAME+"_projects WHERE username='"+Login.USERNAME+"' ORDER BY pos;",null);
        try{
            cursor.moveToFirst();
            while(true){
                final int pos = cursor.getInt(0);
                final String url = cursor.getString(3);

                LinearLayout ll = new LinearLayout(this);
                ll.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                ll.setLayoutParams(params);
                ll.setBackgroundColor(Color.WHITE);
                ll.setGravity(Gravity.CENTER_HORIZONTAL);

                ImageView imageButton = new ImageView(this);
                InputStream is = openFileInput(Login.USERNAME + "_" + pos + ".jpg");
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                imageButton.setImageBitmap(bitmap);
                imageButton.setScaleType(ImageView.ScaleType.CENTER_CROP);
                params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)getResources().getDimension(R.dimen.dp200));
                params.setMargins(0, (int) getResources().getDimension(R.dimen.dp5), 0, (int) getResources().getDimension(R.dimen.dp5));
                imageButton.setLayoutParams(params);
                imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MainActivity.this, MyWebView.class);
                        intent.putExtra("url", url);
                        startActivity(intent);
                    }
                });
                ll.addView(imageButton);

                TextView textView = new TextView(this);
                textView.setText(cursor.getString(1));
                textView.setTextSize(20);
                params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, (int) getResources().getDimension(R.dimen.dp10), 0, 0);
                textView.setLayoutParams(params);
                textView.setTextColor(Color.parseColor("#666666"));
                textView.setTypeface(null, Typeface.BOLD);
                ll.addView(textView);


                textView = new TextView(this);
                textView.setText(cursor.getString(2));
                textView.setTextSize(16);
                textView.setLayoutParams(params);
                textView.setTextColor(Color.parseColor("#666666"));
                textView.setGravity(Gravity.CENTER);
                ll.addView(textView);

                Button button = new Button(this);
                params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen.dp1));
                params.setMargins(0, (int) getResources().getDimension(R.dimen.dp20), 0, (int) getResources().getDimension(R.dimen.dp20));
                button.setLayoutParams(params);
                button.setBackgroundColor(Color.parseColor("#cccccc"));
                button.setPadding((int) getResources().getDimension(R.dimen.dp10), 0, (int) getResources().getDimension(R.dimen.dp10), 0);
                ll.addView(button);

                mainll.addView(ll);

                cursor.moveToNext();
                if(cursor.isAfterLast()){
                    break;
                }
            }
        }catch (Exception e){
            Log.e(e.toString(),e.getMessage());
        }

        cursor.close();

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

    public boolean exists(String fname){
        File file = getBaseContext().getFileStreamPath(fname);
        return file.exists();
    }

}