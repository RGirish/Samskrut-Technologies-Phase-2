package iclub.samskrut.smartdemo;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.client.Firebase;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


public class FirstActivity extends ActionBarActivity {

    public static SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.rgb(26, 183, 80)));
        actionBar.setTitle(Html.fromHtml("<font color='#ffffff'>smartDemo</font>"));

        Connection.CONNECTED=false;
        Firebase.setAndroidContext(FirstActivity.this);

        db=openOrCreateDatabase("smartdemo.db",SQLiteDatabase.CREATE_IF_NECESSARY, null);
        createTables();

        try {
            Cursor cursor = db.rawQuery("SELECT pid,uid FROM previous_session;", null);
            cursor.moveToFirst();
            int pid= cursor.getInt(0);
            String uid= cursor.getString(1);
            Intent intent=new Intent(this,SecondActivity.class);
            intent.putExtra("code",String.valueOf(pid));
            Connection.PID=pid;
            db.execSQL("DELETE FROM previous_session;");
            db.execSQL("INSERT INTO previous_session(pid) VALUES(" + Connection.PID + ");");
            if(Connection.CONNECTED)Connection.ref.child("pid").push().setValue(Connection.PID);
            cancelAlarmManager(Integer.parseInt(uid));
            startActivity(intent);
            finish();
        }catch (Exception e){}
    }

    public void cancelAlarmManager(int uid) {

        try {
            Intent intent = new Intent(this, NotifyService.class);
            intent.putExtra(NotifyService.INTENT_NOTIFY, true);
            PendingIntent displayIntent = PendingIntent.getService(this, uid, intent, PendingIntent.FLAG_NO_CREATE);
            if (displayIntent != null) {
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                alarmManager.cancel(displayIntent);
                displayIntent.cancel();
            }
        }catch (Exception e){Log.e("error", "werwerjnwernwerjnweerkjwerkjnwerkj");}
    }

    public void createTables(){
        try{
            db.execSQL("CREATE TABLE video(pid NUMBER, position NUMBER, url TEXT, videoid TEXT);");
        }catch(Exception e){}
        try{
            db.execSQL("CREATE TABLE no(pid NUMBER, no_ss NUMBER, no_360 NUMBER, no_v NUMBER);");
        }catch(Exception e){}
        try{
            db.execSQL("CREATE TABLE previous_session(pid NUMBER,uid TEXT);");
        }catch(Exception e){}
    }

    public void onClickScan(View view){
        IntentIntegrator.initiateScan(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case IntentIntegrator.REQUEST_CODE:

                IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (scanResult == null) {return;}
                final String result = scanResult.getContents();
                if (result != null) {
                    Intent intent=new Intent(this,SecondActivity.class);
                    intent.putExtra("code",result);
                    Connection.PID=Integer.parseInt(result);
                    db.execSQL("DELETE FROM previous_session;");
                    db.execSQL("INSERT INTO previous_session(pid) VALUES(" + Connection.PID + ");");
                    if(Connection.CONNECTED)Connection.ref.child("pid").push().setValue(Connection.PID);
                    startActivity(intent);
                    finish();
                }
                break;

            default:
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(!Connection.CONNECTED){
            final Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            dialog.setContentView(R.layout.dialog_connect);
            Button connect = (Button) dialog.findViewById(R.id.connectBtn);
            connect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String uniqueCode = ((TextView) dialog.findViewById(R.id.uniqueCode)).getText().toString();
                    Connection.ref = new Firebase("https://smartdemo.firebaseio.com/" + uniqueCode);
                    Connection.CONNECTED = true;
                    dialog.dismiss();
                    Toast.makeText(FirstActivity.this, "Connected to TV" + uniqueCode, Toast.LENGTH_LONG).show();
                }
            });
            dialog.show();
        }else{
            final Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_disconnect);
            Button disconnect = (Button) dialog.findViewById(R.id.disconnect);
            disconnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Connection.ref = null;
                    Connection.CONNECTED = false;
                    dialog.dismiss();
                    Toast.makeText(FirstActivity.this, "Disconnected from TV", Toast.LENGTH_LONG).show();
                }
            });
            Button cancel = (Button) dialog.findViewById(R.id.cancel);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
        return true;
    }

}