package com.iclub.samskrut.omnipresence;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProjectList extends ActionBarActivity implements SwipeRefreshLayout.OnRefreshListener{

    public SQLiteDatabase db;
    ProgressDialog dialog1;
    SwipeRefreshLayout swipeLayout;
    int COUNT=0,CURR_COUNT=0;
    ArrayList<Integer> notAvailableList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
        setContentView(R.layout.activity_project_list);

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
        Cursor cursor = db.rawQuery("SELECT COUNT(title) FROM projects;", null);
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
        dialog1 = ProgressDialog.show(this,null,"Just a moment...");
        db.execSQL("DELETE FROM projects;");
        final ParseQuery<ParseObject> query = ParseQuery.getQuery("Projects");
        query.orderByAscending("pos");
        query.selectKeys( Arrays.asList("pos", "projectName", "description"));
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    for (ParseObject ob : objects) {
                        db.execSQL("INSERT INTO projects VALUES(" + ob.getNumber("pos") + ",'" + ob.getString("projectName") + "','" + ob.getString("description") + "');");
                    }
                    downloadImages();
                } else {
                    Log.e("PARSE", "Error: " + e.getMessage());
                }
            }
        });
    }

    public void downloadImages(){

        File folder = new File(Environment.getExternalStorageDirectory() + "/omniPresence");
        if (!folder.exists()) {
            folder.mkdir();
        }

        Cursor cursor = db.rawQuery("SELECT COUNT(title) FROM projects;",null);
        cursor.moveToFirst();
        COUNT = cursor.getInt(0);
        notAvailableList = new ArrayList<>(COUNT);
        cursor.close();

        cursor = db.rawQuery("SELECT pos FROM projects ORDER BY pos;", null);
        try{
            cursor.moveToFirst();
            while(true){
                int pos = cursor.getInt(0);
                String FILENAME = Environment.getExternalStorageDirectory().toString() + "/omniPresence/" + pos + ".jpg";
                File file = new File(FILENAME);
                if (!file.exists()) {
                    notAvailableList.add(pos);
                }
                cursor.moveToNext();
                if(cursor.isAfterLast()){
                    break;
                }
            }
        }catch (Exception e){}
        cursor.close();




        CURR_COUNT=0;
        for (final int k : notAvailableList) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Projects");
            query.whereEqualTo("pos", k);
            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null) {
                        ParseFile myFile = objects.get(0).getParseFile("photoSphere");
                        myFile.getDataInBackground(new GetDataCallback() {
                            public void done(byte[] data, ParseException e) {
                                if (e == null) {
                                    writeFile(data, k + ".jpg");
                                    CURR_COUNT++;
                                    if (CURR_COUNT == COUNT) {
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


        Cursor cursor = db.rawQuery("SELECT pos,title,desc FROM projects ORDER BY pos;",null);
        try{
            cursor.moveToFirst();
            while(true){
                int pos = cursor.getInt(0);

                LinearLayout ll = new LinearLayout(this);
                ll.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                ll.setLayoutParams(params);
                ll.setGravity(Gravity.CENTER_HORIZONTAL);

                ImageButton imageButton = new ImageButton(this);
                Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/omniPresence/" + pos + ".jpg");
                imageButton.setImageBitmap(bitmap);
                if (Build.VERSION.SDK_INT > 21) {
                    imageButton.setTranslationZ(getResources().getDimension(R.dimen.dp4));
                }
                imageButton.setScaleType(ImageView.ScaleType.CENTER_CROP);
                params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)getResources().getDimension(R.dimen.dp200));
                imageButton.setLayoutParams(params);
                imageButton.setBackgroundColor(Color.WHITE);
                imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(ProjectList.this, "Hello!", Toast.LENGTH_SHORT).show();
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
        }catch (Exception e){}

        cursor.close();
    }

    public void createTables(){
        try{
            db.execSQL("CREATE TABLE projects(pos NUMBER, title TEXT, desc TEXT);");
        }catch(Exception e){}
    }

    public void function(View view) {
        Toast.makeText(this,"Hello",Toast.LENGTH_SHORT).show();
    }

    public boolean checkConnection(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo == null ) return false;
        else return true;
    }

    public void writeFile(byte[] data, String fileName) {
        try {
            FileOutputStream out = new FileOutputStream(Environment.getExternalStorageDirectory() + "/omniPresence/"+fileName);
            out.write(data);
            out.close();
        }catch(Exception e){
            Log.e("WriteFile",e.getMessage());
        }
    }
}