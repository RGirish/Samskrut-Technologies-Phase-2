
package com.iclub.samskrut.omnipresence;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import com.google.vrtoolkit.cardboard.CardboardActivity;
import org.rajawali3d.cardboard.RajawaliCardboardRenderer;
import org.rajawali3d.cardboard.RajawaliCardboardView;

public class MyVrView extends CardboardActivity{

    int POS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RajawaliCardboardView view = new RajawaliCardboardView(this);
        setContentView(view);
        setCardboardView(view);

        POS = getIntent().getIntExtra("pos",1);
        RajawaliCardboardRenderer renderer = new MyRenderer(this, POS);
        view.setRenderer(renderer);
        view.setSurfaceRenderer(renderer);

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)){
            finish();
        }
        else if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP)){
            Cursor cursor = ProjectList.db.rawQuery("SELECT COUNT(pos) FROM projects;", null);
            cursor.moveToFirst();
            int COUNT = cursor.getInt(0);
            cursor.close();
            if(POS+1<COUNT) {
                Intent intent = new Intent(this, MyVrView.class);
                intent.putExtra("pos", POS + 1);
                startActivity(intent);
                finish();
            }
        }
        else if ((keyCode == KeyEvent.KEYCODE_BACK)){
            super.onBackPressed();
        }
        return true;
    }

}