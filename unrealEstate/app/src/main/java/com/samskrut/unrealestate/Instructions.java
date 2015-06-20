package com.samskrut.unrealestate;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
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
                Intent mainIntent = new Intent(Instructions.this, MainActivity.class);
                Instructions.this.startActivity(mainIntent);
                Instructions.this.finish();
            }
        });
        db = openOrCreateDatabase("unrealestate.db", SQLiteDatabase.CREATE_IF_NECESSARY, null);
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

        Bundle bundle = new Bundle();
        bundle.putInt("pos", R.mipmap.instruction_1);
        ParallaxFragment parallaxFragment = new ParallaxFragment();
        parallaxFragment.setArguments(bundle);
        mAdapter.add(parallaxFragment);

        bundle = new Bundle();
        bundle.putInt("pos", R.mipmap.instruction_2);
        parallaxFragment = new ParallaxFragment();
        parallaxFragment.setArguments(bundle);
        mAdapter.add(parallaxFragment);

        bundle = new Bundle();
        bundle.putInt("pos", R.mipmap.instruction_3);
        parallaxFragment = new ParallaxFragment();
        parallaxFragment.setArguments(bundle);
        mAdapter.add(parallaxFragment);

        bundle = new Bundle();
        bundle.putInt("pos", R.mipmap.instruction_4);
        parallaxFragment = new ParallaxFragment();
        parallaxFragment.setArguments(bundle);
        mAdapter.add(parallaxFragment);

        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                prevPos = currPos;
                currPos = position;
                if(currPos == 3 && prevPos == 2){
                    TextView finishButton = (TextView) findViewById(R.id.finishButton);
                    finishButton.setText("<Finish>");
                    final Animation in = new AlphaAnimation(0.0f, 1.0f);
                    in.setDuration(500);
                    finishButton.startAnimation(in);
                }
                if(currPos == 2 && prevPos == 3){
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