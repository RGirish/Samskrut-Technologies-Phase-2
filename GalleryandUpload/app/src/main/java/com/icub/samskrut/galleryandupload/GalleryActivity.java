package com.icub.samskrut.galleryandupload;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GalleryActivity extends ActionBarActivity {

    ProgressDialog dialog1;
    SQLiteDatabase db;
    int COUNT=0,TOTAL=0,n10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        db=openOrCreateDatabase("iclub_samskrut_gallery.db", SQLiteDatabase.CREATE_IF_NECESSARY, null);
        n10 = (int)getResources().getDimension(R.dimen.n10);
        downloadLatestThumbnails();
    }

    public void displayThumbnails(){
        File dir = new File(Environment.getExternalStorageDirectory() + "/galleryandupload");
        File[] filelist = dir.listFiles();
        LinearLayout.LayoutParams params;
        LinearLayout ll=null;
        LinearLayout mainll = (LinearLayout)findViewById(R.id.mainll);

        for (int i=0 ; i<filelist.length ; ++i){
            Log.e("YO YO YO YO", filelist[i].getPath());
            if(i==0 || i%3==0){
                ll = new LinearLayout(this);
                params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0,n10,0,n10);
                ll.setLayoutParams(params);
                ll.setOrientation(LinearLayout.HORIZONTAL);
                mainll.addView(ll);
            }
            ImageView imageView = new ImageView(this);
            params= new LinearLayout.LayoutParams((int)getResources().getDimension(R.dimen.photo_height),(int)getResources().getDimension(R.dimen.photo_height));
            if(i==1 || (i-1)%3==0){
                params.setMargins(n10,0,n10,0);
            }
            imageView.setLayoutParams(params);
            Bitmap bitmap = BitmapFactory.decodeFile(filelist[i].getPath());
            imageView.setImageBitmap(bitmap);
            ll.addView(imageView);
        }
    }

    public void downloadLatestThumbnails(){
        File folder = new File(Environment.getExternalStorageDirectory() + "/galleryandupload");
        if (!folder.exists()) {
            folder.mkdir();
        }

        if (checkConnection()){
            dialog1 = ProgressDialog.show(this, null, "Just a moment...", true);
            final ParseQuery<ParseObject> query = ParseQuery.getQuery("PhotoTable");
            query.addDescendingOrder("createdAt");
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    ParseObject firstObject = objects.get(0);
                    //'d' - latest photo date from Parse
                    Date d = firstObject.getCreatedAt();
                    Log.e("LPD FROM PARSE", d.toString());
                    Cursor cursor = db.rawQuery("SELECT thetime FROM latestphototime;",null);
                    cursor.moveToFirst();
                    String currentlatesttime = cursor.getString(0);
                    DateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US);
                    try{
                        //'dd' - latest photo date from sqlite
                        Date dd = dateFormat.parse(currentlatesttime);
                        if(dd.toString().equals(d.toString())){
                            Toast.makeText(GalleryActivity.this,"equals",Toast.LENGTH_LONG).show();
                            Log.e("LPD FROM SQLITE", dd.toString());
                            displayThumbnails();
                        }else{
                            Toast.makeText(GalleryActivity.this,"before",Toast.LENGTH_LONG).show();
                            Log.e("LPD FROM SQLITE", dd.toString());

                            for(final ParseObject o : objects){
                                if(o.getCreatedAt().toString().equals(currentlatesttime)) break;
                                ParseFile myFile = o.getParseFile("pic_thumbnail");
                                byte[] data = myFile.getData();
                                String fn = Long.toString(System.currentTimeMillis()) + ".jpg";
                                writeFile(data, fn);
                            }
                            displayThumbnails();

                            db.execSQL("UPDATE latestphototime SET thetime='" + d.toString() + "';");
                        }

                        //One more case left out. When some row is deleted from Parse db, the latest date stored locally could be AFTER the latest date from Parse in which case we will have to delete a couple of photos from the local storage.

                    }catch (Exception exc){}
                    dialog1.dismiss();
                }
            });

        }else{
            Toast.makeText(this, "Check your Internet Connection!", Toast.LENGTH_LONG).show();
        }

    }

    public void onClickRefresh(){

    }

    public void onClickNew(View view){
        Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
        startActivity(intent);
    }

    public void writeFile(byte[] data, String fileName) {
        try {
            FileOutputStream out = new FileOutputStream(Environment.getExternalStorageDirectory() + "/galleryandupload/"+fileName);
            out.write(data);
            out.close();
        }catch(Exception e){
            Log.e("WriteFile",e.getMessage());
        }
    }


    public boolean checkConnection(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo == null ) return false;
        else return true;
    }

}