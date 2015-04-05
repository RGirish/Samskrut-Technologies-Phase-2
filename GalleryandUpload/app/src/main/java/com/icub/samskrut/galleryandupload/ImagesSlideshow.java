package com.icub.samskrut.galleryandupload;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import java.io.File;
import java.io.FileOutputStream;

public class ImagesSlideshow extends ActionBarActivity {

    ProgressDialog dialog;
    static int TOTAL=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images_slideshow);

        Intent intent = getIntent();
        TOTAL = intent.getIntExtra("total", 0);
        int page = TOTAL - intent.getIntExtra("page", 0);

        final ViewPager mPager = (ViewPager) findViewById(R.id.slideshowViewPager);
        final ViewPager.SimpleOnPageChangeListener mPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(final int position) {
                File file = new File(Environment.getExternalStorageDirectory().toString() + "/galleryandupload/large/" + (TOTAL-position) + ".jpg");
                if(!file.exists()){
                    dialog = ProgressDialog.show(ImagesSlideshow.this, null, "Downloading Image...", true);
                    final ParseQuery<ParseObject> query = ParseQuery.getQuery("PhotoTable");
                    query.addDescendingOrder("createdAt");
                    query.setSkip(position);
                    query.getFirstInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject parseObject, ParseException e) {
                            try {
                                ParseFile myFile = parseObject.getParseFile("pic");
                                byte[] data = myFile.getData();
                                String fn = (TOTAL-position) + ".jpg";
                                writeFile(data, fn);
                                dialog.dismiss();
                                ImagesSlideshow.this.recreate();
                            } catch (Exception ex) {
                            }
                        }
                    });


                }
            }
        };
        mPager.setOnPageChangeListener(mPageChangeListener);
        PagerAdapter mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(page);
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            return ScreenSlidePageFragment.create(position);
        }

        @Override
        public int getCount() {
            return TOTAL;
        }
    }


    public void writeFile(byte[] data, String fileName) {
        try {
            FileOutputStream out = new FileOutputStream(Environment.getExternalStorageDirectory() + "/galleryandupload/large/"+fileName);
            out.write(data);
            out.close();
        }catch(Exception e){
            Log.e("WriteFile Imslidehow",e.getMessage());
        }
    }
}