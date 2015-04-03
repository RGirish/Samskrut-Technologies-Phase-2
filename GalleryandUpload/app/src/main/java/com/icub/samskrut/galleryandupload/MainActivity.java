
package com.icub.samskrut.galleryandupload;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Gallery;
import android.widget.Toast;
import com.parse.Parse;
import com.parse.ParseCrashReporting;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.SaveCallback;
import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends ActionBarActivity {

    ArrayList<String> imagePaths;
    static ProgressDialog dialog;
    int TOTAL,COUNT;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try{
            ParseCrashReporting.enable(this);
            Parse.enableLocalDatastore(this);
        }catch (Exception e){}
        Parse.initialize(this, "28rzQwSoD7MFQOOViu9awAI0giaUDK8E7ADYbXAz", "jbYQAqhT1jcRiIUrS3UwuFuFOipjv04kUYhZpkEN");

        db=openOrCreateDatabase("iclub_samskrut_gallery.db", SQLiteDatabase.CREATE_IF_NECESSARY, null);
        try{
            db.execSQL("CREATE TABLE offlinesaves(path TEXT);");
        }catch (Exception e){}
        try{
            db.execSQL("CREATE TABLE latestphototime(thetime TEXT);");
            db.execSQL("INSERT INTO latestphototime VALUES('Thu Apr 02 12:07:07 GMT+05:30 2014');");
        }catch (Exception e){}
    }

    public boolean checkConnection(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo == null ) return false;
        else return true;
    }


    public void onClickNew(View view){
        Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
        startActivity(intent);
    }

    public void onClickUpload(View view){
        Intent i = new Intent(Action.ACTION_MULTIPLE_PICK);
        startActivityForResult(i, 200);
    }

    public void onClickGallery(View view){
        startActivity(new Intent(this, GalleryActivity.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imagePaths = new ArrayList<>();
        if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
            String[] all_path = data.getStringArrayExtra("all_path");

            if(all_path.length>0) {
                if (checkConnection()){
                    dialog = ProgressDialog.show(this, null, "Uploading...", true);
                    TOTAL = all_path.length;
                    COUNT=0;
                    for (String string : all_path) {
                        Bitmap bitmap = BitmapFactory.decodeFile(string);
                        String[] parts = string.split("/");
                        final String filename = parts[parts.length-1];
                        byte[] bitmapBytes = bitmapToByteArray(bitmap);
                        final ParseFile file = new ParseFile(filename, bitmapBytes);
                        file.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                ParseObject myobject = new ParseObject("PhotoTable");
                                myobject.put("pic", file);
                                myobject.put("image_name", filename.split(".jpg")[0] + "_th.jpg");
                                myobject.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        COUNT++;
                                        if (COUNT == TOTAL) {
                                            if (checkConnection()) dialog.dismiss();
                                        }
                                    }
                                });
                            }
                        });
                    }
                }else{
                    Toast.makeText(this, "You're offline now. The images will be uploaded later!", Toast.LENGTH_LONG).show();
                    for (String string : all_path) {
                        db.execSQL("INSERT INTO offlinesaves VALUES('"+string+"');");
                    }
                }
            }
        }
    }

    public byte[] bitmapToByteArray(Bitmap b)
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.JPEG, 60, stream);
        return stream.toByteArray();
    }

}