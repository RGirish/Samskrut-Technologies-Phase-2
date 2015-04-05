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
    int n10;

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
        final File[] filelist= dir.listFiles();
        LinearLayout.LayoutParams params;
        LinearLayout ll=new LinearLayout(this);
        LinearLayout mainll = (LinearLayout)findViewById(R.id.mainll);

        int count=0;
        for ( int i=filelist.length-1 ; i>=1 ; --i ){
            final int i2=i;

            if(count==0 || count%3==0){
                ll = new LinearLayout(this);
                params = new LinearLayout.LayoutParams((int)getResources().getDimension(R.dimen.n320), ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0,n10,0,n10);
                ll.setLayoutParams(params);
                ll.setOrientation(LinearLayout.HORIZONTAL);
                mainll.addView(ll);
            }
            ImageView imageView = new ImageView(this);
            params= new LinearLayout.LayoutParams((int)getResources().getDimension(R.dimen.photo_height),(int)getResources().getDimension(R.dimen.photo_height));
            if(count==1 || (count-1)%3==0){
                params.setMargins(n10,0,n10,0);
            }
            imageView.setLayoutParams(params);
            Bitmap bitmap = BitmapFactory.decodeFile(filelist[i].getPath());
            Log.e("PATTHHHHHHH",filelist[i].getPath());
            imageView.setImageBitmap(bitmap);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(GalleryActivity.this, ImagesSlideshow.class);
                    intent.putExtra("page", i2);
                    intent.putExtra("total", filelist.length-1);
                    Toast.makeText(GalleryActivity.this, String.valueOf(i2), Toast.LENGTH_LONG).show();
                    startActivity(intent);
                }
            });
            ll.addView(imageView);
            count++;
        }
    }


    public void downloadLatestThumbnails(){
        File folder = new File(Environment.getExternalStorageDirectory() + "/galleryandupload");
        if (!folder.exists()) {
            folder.mkdir();
        }
        File folder2 = new File(Environment.getExternalStorageDirectory() + "/galleryandupload/large");
        if (!folder2.exists()) {
            folder2.mkdir();
        }

        if (checkConnection()){
            dialog1 = ProgressDialog.show(this, null, "Just a moment...", true);
            final ParseQuery<ParseObject> query = ParseQuery.getQuery("PhotoTable");
            //query.addDescendingOrder("createdAt");
            query.addAscendingOrder("createdAt");
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    ParseObject lastObject = objects.get(objects.size()-1);
                    //'d' - latest photo date from Parse
                    Date d = lastObject.getCreatedAt();
                    Cursor cursor = db.rawQuery("SELECT thetime FROM latestphototime;",null);
                    cursor.moveToFirst();
                    String currentlatesttime = cursor.getString(0);
                    DateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US);
                        //'dd' - latest photo date from sqlite
                    Date dd=null;
                    try{dd = dateFormat.parse(currentlatesttime);}catch (Exception ee){}
                        if(dd.toString().equals(d.toString())){
                            displayThumbnails();
                            Toast.makeText(GalleryActivity.this,"equals",Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(GalleryActivity.this,"not equals",Toast.LENGTH_LONG).show();
                            File dir = new File(Environment.getExternalStorageDirectory() + "/galleryandupload");
                            File[] filelist = dir.listFiles();
                            int pos = filelist.length;

                            if(filelist.length==1) {
                                for (int i = 0; i < objects.size(); ++i) {
                                    if (objects.get(i).getCreatedAt().toString().equals(currentlatesttime)) {
                                        break;
                                    }
                                    ParseFile myFile = objects.get(i).getParseFile("pic_thumbnail");
                                    byte[] data = null;
                                    try {
                                        data = myFile.getData();
                                    } catch (Exception eee) {
                                    }
                                    String fn = String.valueOf(pos) + "_th.jpg";
                                    pos++;
                                    writeFile(data, fn);
                                }
                            }else{
                                for (int i = objects.size()-1; i >=0 ; --i) {
                                    if (objects.get(i).getCreatedAt().toString().equals(currentlatesttime)) {
                                        break;
                                    }
                                    ParseFile myFile = objects.get(i).getParseFile("pic_thumbnail");
                                    byte[] data = null;
                                    try {
                                        data = myFile.getData();
                                    } catch (Exception eee) {
                                    }
                                    String fn = String.valueOf(pos) + "_th.jpg";
                                    pos++;
                                    writeFile(data, fn);
                                }
                            }

                            displayThumbnails();
                            db.execSQL("UPDATE latestphototime SET thetime='" + d.toString() + "';");
                        }

                        //One more case left out. When some row is deleted from Parse db, the latest date stored locally could be AFTER the latest date from Parse in which case we will have to delete a couple of photos from the local storage.


                    dialog1.dismiss();
                }
            });

        }else{
            Toast.makeText(this, "Check your Internet Connection!", Toast.LENGTH_LONG).show();
            displayThumbnails();
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