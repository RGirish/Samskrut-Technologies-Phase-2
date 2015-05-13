
package com.iclub.samskrut.omnipresence;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.KeyEvent;
import com.google.vrtoolkit.cardboard.CardboardActivity;
import org.rajawali3d.cardboard.RajawaliCardboardRenderer;
import org.rajawali3d.cardboard.RajawaliCardboardView;

public class MyVrView extends CardboardActivity{

    int projectPos;
    int pos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RajawaliCardboardView view = new RajawaliCardboardView(this);
        setContentView(view);
        setCardboardView(view);
        projectPos = getIntent().getIntExtra("projectPos",0);
        pos = getIntent().getIntExtra("pos",0);
        RajawaliCardboardRenderer renderer = new MyRenderer(this, projectPos,pos);
        view.setRenderer(renderer);
        view.setSurfaceRenderer(renderer);

        ProjectList.tts.stop();
        Cursor cursor = ProjectList.db.rawQuery("SELECT tts FROM subProjects WHERE projectPos=" + projectPos + " AND pos=" + pos + ";", null);
        cursor.moveToFirst();
        if(android.os.Build.VERSION.SDK_INT >= 21){
            ProjectList.tts.speak(cursor.getString(0), TextToSpeech.QUEUE_FLUSH, null, null);
        }else{
            ProjectList.tts.speak(cursor.getString(0), TextToSpeech.QUEUE_FLUSH, null);
        }
        cursor.close();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP)){
            //goes to the next image
            Cursor cursor2 = ProjectList.db.rawQuery("SELECT COUNT(pos) FROM subProjects WHERE projectPos=" + projectPos + ";", null);
            cursor2.moveToFirst();
            int COUNT_2 = cursor2.getInt(0);
            cursor2.close();
            if(pos + 1 < COUNT_2) {
                Intent intent = new Intent(this, MyVrView.class);
                intent.putExtra("projectPos", projectPos);
                intent.putExtra("pos", pos + 1);
                startActivity(intent);
                finish();
            }else{
                Cursor cursor1 = ProjectList.db.rawQuery("SELECT COUNT(pos) FROM projects;", null);
                cursor1.moveToFirst();
                int COUNT_1 = cursor1.getInt(0);
                cursor1.close();
                if(projectPos + 1 < COUNT_1) {
                    Intent intent = new Intent(this, MyVrView.class);
                    intent.putExtra("projectPos", projectPos + 1);
                    intent.putExtra("pos", 0);
                    startActivity(intent);
                    finish();
                }
            }
        }
        else{
            //goes back
            super.onBackPressed();
        }
        return true;
    }
}