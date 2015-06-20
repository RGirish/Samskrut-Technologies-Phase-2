
package com.samskrut.unrealestate;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.crashlytics.android.Crashlytics;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseCrashReporting;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import java.util.List;

public class Login extends AppCompatActivity {

    public static String USERNAME="";
    public static SQLiteDatabase db;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);
        setContentView(R.layout.activity_login);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setFullScreen(true);

        try{ParseCrashReporting.enable(this);}catch (Exception e){}
        Parse.initialize(this, "Sq2yle2ei4MmMBXAChjGksJDqlwma3rjarvoZCsk", "vMw4I2I0fdSD1frBohAvWCaXZYqLaHZ8ljnwqavg");
        db = openOrCreateDatabase("unrealestate.db",SQLiteDatabase.CREATE_IF_NECESSARY, null);
        createTables();

        Cursor cursor = db.rawQuery("SELECT username FROM session;", null);
        try{
            cursor.moveToFirst();
            USERNAME = cursor.getString(0);
            if(!(USERNAME.equals("NONE"))){
                try{
                    db.execSQL("CREATE TABLE "+USERNAME+"_projects(pos NUMBER, name TEXT, desc TEXT, url TEXT, username TEXT, timestamp TEXT);");
                }catch(Exception e){}
                try{
                    db.execSQL("CREATE TABLE "+USERNAME+"_projects_temp(pos NUMBER, name TEXT, desc TEXT, url TEXT, username TEXT, timestamp TEXT);");
                }catch(Exception e){}
                startActivity(new Intent(Login.this,Splash.class));
                finish();
            }
        }catch(Exception e){}

        EditText editText = (EditText)findViewById(R.id.password);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                hideKeyboard();
                onClickLogin(null);
                return true;
            }
        });

    }

    public void downloadLoginDetails(final String username, final String password) {
        db.execSQL("DELETE FROM login;");
        final ParseQuery<ParseObject> query = ParseQuery.getQuery("Login");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    for (ParseObject ob : objects) {
                        db.execSQL("INSERT INTO login VALUES('" + ob.getString("username") + "','" + ob.getString("password") + "');");
                    }

                    Cursor c = db.rawQuery("SELECT username FROM login WHERE username='" + username + "' AND password='" + password + "';", null);
                    try{
                        c.moveToFirst();
                        USERNAME = c.getString(0);
                        c.close();
                        dialog.dismiss();
                        try{
                            db.execSQL("CREATE TABLE "+USERNAME+"_projects(pos NUMBER, name TEXT, desc TEXT, url TEXT, username TEXT, timestamp TEXT);");
                        }catch(Exception exc){}
                        try{
                            db.execSQL("CREATE TABLE "+USERNAME+"_projects_temp(pos NUMBER, name TEXT, desc TEXT, url TEXT, username TEXT, timestamp TEXT);");
                        }catch(Exception ex){}
                        db.execSQL("DELETE FROM session;");
                        db.execSQL("INSERT INTO session VALUES('" + username + "');");
                        startActivity(new Intent(Login.this, Splash.class));
                        finish();

                    }catch (Exception ee){
                        Log.e(ee.getMessage(),ee.toString());
                        dialog.dismiss();
                        Toast.makeText(Login.this,"Username and Password do not match!",Toast.LENGTH_LONG).show();
                        ((EditText)findViewById(R.id.password)).setText("");
                        ((EditText)findViewById(R.id.username)).setText("");
                    }

                } else {
                    Log.e("PARSE", "Error: " + e.getMessage());
                }
            }
        });
    }

    public void onClickLogin(View v){

        dialog = ProgressDialog.show(this,null,"Please wait...",true);

        String username = ((EditText)findViewById(R.id.username)).getText().toString();
        String password = ((EditText)findViewById(R.id.password)).getText().toString();

        if(username.equals("") || password.equals("")){
            Toast.makeText(Login.this, "Don't leave the fields empty!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            return;
        }

        Cursor c = db.rawQuery("SELECT username FROM login WHERE username='" + username + "' AND password='" + password + "';", null);
        try{
            c.moveToFirst();
            USERNAME = c.getString(0);
            dialog.dismiss();
            c.close();
            try{
                db.execSQL("CREATE TABLE "+USERNAME+"_projects(pos NUMBER, name TEXT, desc TEXT, url TEXT, username TEXT, timestamp TEXT);");
            }catch(Exception e){}
            try{
                db.execSQL("CREATE TABLE "+USERNAME+"_projects_temp(pos NUMBER, name TEXT, desc TEXT, url TEXT, username TEXT, timestamp TEXT);");
            }catch(Exception e){}

            db.execSQL("DELETE FROM session;");
            db.execSQL("INSERT INTO session VALUES('"+USERNAME+"');");
            startActivity(new Intent(Login.this, Splash.class));
            finish();
        }catch (Exception e){
            if(checkConnection()){
                downloadLoginDetails(username,password);
            }else{
                dialog.dismiss();
                findViewById(R.id.firstTime).setVisibility(View.VISIBLE);
                Toast.makeText(this,"Username and Password do not match!",Toast.LENGTH_LONG).show();
                ((EditText)findViewById(R.id.username)).setText("");
                ((EditText)findViewById(R.id.password)).setText("");
                findViewById(R.id.username).requestFocus();
            }
        }
    }

    public void createTables() {
        try{
            db.execSQL("CREATE TABLE session(username TEXT);");
        }catch(Exception e){}
        try{
            db.execSQL("CREATE TABLE login(username TEXT,password TEXT);");
        }catch(Exception e){}
    }

    public boolean checkConnection(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo == null ) return false;
        else return true;
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    void setFullScreen(boolean fullscreen) {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        if (fullscreen) {
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }
        else{
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }
        getWindow().setAttributes(attrs);
    }

}