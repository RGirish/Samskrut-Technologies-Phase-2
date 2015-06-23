
package com.samskrut.unrealestate;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import java.io.InputStream;

import io.fabric.sdk.android.services.common.SafeToast;

public class MainActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"#ffffff\">unrealEstate</font>"));
        displayEverything();
    }

    /**
     * To dynamically add all the views to the screen for the listview.
     */
    public void displayEverything(){

        LinearLayout mainll = (LinearLayout)findViewById(R.id.mainll);
        mainll.removeAllViews();

        //Iterate through the projects table and for each item, add a view to the linearlayout
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

    /**
     * Set up the actionbar icon for logout
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * To clear the session when the user clicks on the logout button and go back to the login activity.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.actionLogout:
                SQLiteDatabase db = openOrCreateDatabase("unrealestate.db", SQLiteDatabase.CREATE_IF_NECESSARY, null);
                db.execSQL("DELETE FROM session;");
                db.close();
                Intent mainIntent = new Intent(this, Login.class);
                startActivity(mainIntent);
                Toast.makeText(MainActivity.this, "Logged out!", Toast.LENGTH_LONG).show();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}