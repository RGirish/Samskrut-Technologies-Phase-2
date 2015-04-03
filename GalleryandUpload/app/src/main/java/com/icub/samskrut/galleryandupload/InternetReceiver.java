package com.icub.samskrut.galleryandupload;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;

public class InternetReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null ){

            final SQLiteDatabase db = context.openOrCreateDatabase("iclub_samskrut_gallery.db", SQLiteDatabase.CREATE_IF_NECESSARY, null);
            try{
                db.execSQL("CREATE TABLE offlinesaves(path TEXT);");
            }catch (Exception e){}

            Cursor cursor = db.rawQuery("SELECT path FROM offlinesaves;",null);
            try{
                cursor.moveToFirst();
                while(true){
                    final String string = cursor.getString(0);
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
                                    db.execSQL("DELETE FROM offlinesaves WHERE path='" + string + "';");
                                }
                            });
                        }
                    });

                    cursor.moveToNext();
                    if(cursor.isAfterLast()){
                       break;
                    }
                }
            }catch (Exception e){}
        }
    }

    public byte[] bitmapToByteArray(Bitmap b)
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.JPEG, 60, stream);
        return stream.toByteArray();
    }

}