package iclub.com.showcommerce;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import android.os.AsyncTask;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class MainActivity extends ActionBarActivity implements View.OnTouchListener{

    private int PID;
    public ProgressDialog dialog;
    private int COUNT=0,CURR_COUNT=0;
    private ImageView imageview;
    private int currentimagenumber=1;
    int x,y,cx,cy,fsx,fsy,fspx,fspy;
    ArrayList<Integer> notAvailableList;
    boolean flag=true;
    Firebase ref;
    TheClass ob;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ob=new TheClass();

        PID=130;
        File folder = new File(Environment.getExternalStorageDirectory() + "/showcommerce");
        if (!folder.exists()) {
            folder.mkdir();
        }
        File folder2 = new File(Environment.getExternalStorageDirectory() + "/showcommerce/p"+PID);
        if (!folder2.exists()) {
            folder2.mkdir();
        }

        imageview=(ImageView)findViewById(R.id.image);

        Parse.initialize(this, "28rzQwSoD7MFQOOViu9awAI0giaUDK8E7ADYbXAz", "jbYQAqhT1jcRiIUrS3UwuFuFOipjv04kUYhZpkEN");

        if(checkConnection()){
            Firebase.setAndroidContext(this);
            ref=new Firebase("https://smartdemo.firebaseio.com/TV01/360/");
            ref.addChildEventListener(new ChildEventListener() {
                 @Override
                 public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                    if(snapshot.getValue().toString().equals("r")){
                        new Thread(new Task("r")).start();
                    }else{
                        new Thread(new Task("l")).start();
                    }
                 }
                 @Override
                 public void onChildMoved(DataSnapshot snapshot, String previousChildKey) {

                 }
                 @Override
                 public void onChildRemoved(DataSnapshot snapshot) {}

                 @Override
                 public void onChildChanged(DataSnapshot snapshot, String previousChildKey) {}

                 @Override
                 public void onCancelled(FirebaseError firebaseError) {}
            });
        }

        notAvailableList=new ArrayList<>(36);
        notAvailableList.clear();
        for(int i=1 ; i<=36 ; ++i){
            String FILENAME=Environment.getExternalStorageDirectory().toString() + "/showcommerce/p"+PID+"/"+PID+"_"+i+".jpg";
            File file = new File(FILENAME);
            if(file.exists()) {
                continue;
            }else{
                notAvailableList.add(i);
            }
        }

        if(notAvailableList.size()>0) {
            if (checkConnection()) {
                dialog = ProgressDialog.show(this, null, "Just a moment...", true);
                CURR_COUNT = 0;
                COUNT = notAvailableList.size();
                for (final int k : notAvailableList) {
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("product360images");
                    query.whereEqualTo("pid", PID);
                    query.whereEqualTo("position", k);
                    query.findInBackground(new FindCallback<ParseObject>() {
                        public void done(List<ParseObject> objects, ParseException e) {
                            if (e == null) {
                                startDownload(objects.get(0).getString("url"), PID + "_" + k + ".jpg");
                            } else {
                                Log.e("PARSE", "Error: " + e.getMessage());
                            }
                        }
                    });
                }
            }else{
                Toast.makeText(this,"Internet Connection unavailable!",Toast.LENGTH_LONG).show();
                imageview.setImageResource(R.drawable.noimage);
                LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(200,200);
                imageview.setLayoutParams(params);
                LinearLayout ll=(LinearLayout)findViewById(R.id.mainll);
                Button button=new Button(this);
                button.setText("Retry");
                button.setBackgroundResource(R.drawable.okbutton_selector);
                button.setTextColor(Color.WHITE);
                button.setTextSize(14);
                button.setTypeface(null, Typeface.BOLD);
                LinearLayout.LayoutParams buttonparams=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
                buttonparams.setMargins(0,20,0,0);
                button.setLayoutParams(buttonparams);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(MainActivity.this,MainActivity.class));
                        finish();
                    }
                });
                ll.addView(button);
            }
        }else{
            Bitmap bmp = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().toString()+"/showcommerce/p"+PID+"/"+PID+"_1.jpg");
            currentimagenumber=1;
            imageview.setImageBitmap(bmp);
            imageview.setOnTouchListener(this);
        }
    }

    private void toggleFullscreen(boolean fullscreen)
    {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        if (fullscreen)
        {
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }
        else
        {
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }
        getWindow().setAttributes(attrs);
        flag=!flag;
    }

    public boolean checkConnection(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo == null ) return false;
        else return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
            setContentView(R.layout.activity_main);
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_main);
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN: {
                fspx=(int)event.getX();
                fspy=(int)event.getY();
                cx=(int)event.getX();
                cy=(int)event.getY();
                x=(int)event.getX();
                y=(int)event.getY();
                return true;
            }
            case MotionEvent.ACTION_UP: {
                x=(int)event.getX();
                y=(int)event.getY();
                fsx=x;
                fsy=y;
                if(Math.abs(fsx-fspx)<=5 && Math.abs(fsy-fspy)<=5){
                    toggleFullscreen(flag);
                }
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                x=(int)event.getX();
                y=(int)event.getY();

                if(x>=cx+10){
                    cx=x;
                    moveRight();
                    return true;
                }else if(x<=cx-10){
                    cx=x;
                    moveLeft();
                    return true;
                }
            }
        }
        return false;
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
        public synchronized void send(String s){
            move(s);
        }
    }




    public void move(String s){
        if(s.equals("r")) {

            currentimagenumber--;
            if (currentimagenumber == 0) currentimagenumber = 36;
            final Bitmap bmp = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().toString() + "/showcommerce/p" + PID + "/" + PID + "_" + currentimagenumber + ".jpg");
            runOnUiThread(new Runnable(){
                public void run() {
                    imageview.setImageBitmap(bmp);
                }
            });

        }else{

            currentimagenumber++;
            if(currentimagenumber==37)currentimagenumber=1;
            final Bitmap bmp = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().toString()+"/showcommerce/p"+PID+"/"+PID+"_"+currentimagenumber+".jpg");
            runOnUiThread(new Runnable(){
                public void run() {
                    imageview.setImageBitmap(bmp);
                }
            });

        }
    }

    public void moveRight(){
        currentimagenumber--;
        if (currentimagenumber == 0) currentimagenumber = 36;
        Bitmap bmp = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().toString()+"/showcommerce/p"+PID+"/"+PID+"_"+currentimagenumber+".jpg");
        imageview.setImageBitmap(bmp);
    }

    public void moveLeft(){
        currentimagenumber++;
        if(currentimagenumber==37)currentimagenumber=1;
        Bitmap bmp = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().toString()+"/showcommerce/p"+PID+"/"+PID+"_"+currentimagenumber+".jpg");
        imageview.setImageBitmap(bmp);
    }

    private void startDownload(String url,String filename) {
        new DownloadFileAsync().execute(url, filename);
    }

    class DownloadFileAsync extends AsyncTask<String, String, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(String... aurl) {
                int count;
                try {
                    URL url = new URL(aurl[0]);
                    String filename = aurl[1];
                    URLConnection conexion = url.openConnection();
                    conexion.connect();
                    int lenghtOfFile = conexion.getContentLength();
                    InputStream input = new BufferedInputStream(url.openStream());
                    OutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory().toString() + "/showcommerce/p"+PID+"/" + filename);

                    byte data[] = new byte[512];
                    long total = 0;
                    while ((count = input.read(data)) != -1) {
                        total += count;
                        publishProgress("" + (int) ((total * 100) / lenghtOfFile));
                        output.write(data, 0, count);
                    }
                    output.flush();
                    output.close();
                    input.close();

                }
                catch(Exception e){
                    File file = new File(Environment.getExternalStorageDirectory() + "/showcommerce/p"+PID+"/" + aurl[1]);
                    file.delete();
                }
                return null;
            }

            @Override
            protected synchronized void onPostExecute(String unused) {
                CURR_COUNT++;
                if(CURR_COUNT==COUNT){
                    Bitmap bmp = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().toString()+"/showcommerce/p"+PID+"/"+PID+"_"+currentimagenumber+".jpg");
                    imageview.setImageBitmap(bmp);
                    imageview.setOnTouchListener(MainActivity.this);
                    dialog.dismiss();
                }
            }
        }
}