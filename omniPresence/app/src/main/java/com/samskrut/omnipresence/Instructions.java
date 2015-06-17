package com.samskrut.omnipresence;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.xgc1986.parallaxPagerTransformer.ParallaxPagerTransformer;

public class Instructions extends AppCompatActivity {

    SQLiteDatabase db;
    ViewPager mPager;
    ParallaxAdapter mAdapter;
    int prevPos=0,currPos=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parallax);
        TextView finishButton = (TextView) findViewById(R.id.finishButton);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(Instructions.this, Countdown.class);
                Instructions.this.startActivity(mainIntent);
                Instructions.this.finish();
            }
        });
        db = openOrCreateDatabase("omniPresence.db", SQLiteDatabase.CREATE_IF_NECESSARY, null);
        displayInstructionsImages();
        setFullscreen(true);
    }

    public void displayInstructionsImages(){
        mPager = (ViewPager) findViewById(R.id.pager);

        mPager.setBackgroundColor(0xFF000000);
        ParallaxPagerTransformer pt = new ParallaxPagerTransformer((R.id.image));
        pt.setBorder(20);
        mPager.setPageTransformer(false, pt);
        mAdapter = new ParallaxAdapter(getSupportFragmentManager());
        mAdapter.setPager(mPager);

        int count = 0;
        Cursor cursor = db.rawQuery("SELECT pos FROM splash WHERE username='"+Login.USERNAME+"' ORDER BY pos;", null);
        try{
            cursor.moveToPosition(1);
            while(true){
                int pos = cursor.getInt(0);
                Bundle bundle = new Bundle();
                bundle.putInt("pos", pos);
                ParallaxFragment parallaxFragment = new ParallaxFragment();
                parallaxFragment.setArguments(bundle);
                mAdapter.add(parallaxFragment);
                count++;
                cursor.moveToNext();
                if(cursor.isAfterLast()){
                    break;
                }
            }
        }catch (Exception e){}
        cursor.close();

        final int count2 = count;
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                if(position == count2-1) {
                    TextView finishButton = (TextView) findViewById(R.id.finishButton);
                    finishButton.setText("<Finish>");
                    final Animation in = new AlphaAnimation(0.0f, 1.0f);
                    in.setDuration(500);
                    finishButton.startAnimation(in);
                }
                prevPos = currPos;
                currPos = position;
                if(currPos == count2-2 && prevPos == count2-1){
                    final TextView finishButton = (TextView) findViewById(R.id.finishButton);
                    final Animation out = new AlphaAnimation(1.0f, 0.0f);
                    out.setDuration(500);
                    finishButton.startAnimation(out);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finishButton.setText("");
                        }
                    }, 450);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
        mPager.setAdapter(mAdapter);
    }

    private void setFullscreen(boolean fullscreen) {
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