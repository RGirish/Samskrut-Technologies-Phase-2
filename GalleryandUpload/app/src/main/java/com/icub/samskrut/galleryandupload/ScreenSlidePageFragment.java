
package com.icub.samskrut.galleryandupload;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

public class ScreenSlidePageFragment extends Fragment {

    public static final String ARG_PAGE = "page";
    private int mPageNumber;
    static Bitmap bitmap;

    public static ScreenSlidePageFragment create(int pageNumber) {
        ScreenSlidePageFragment fragment = new ScreenSlidePageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public ScreenSlidePageFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageNumber = getArguments().getInt(ARG_PAGE);
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 0) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_screen_slide_page, container, false);
        bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().toString() + "/galleryandupload/large/" + (ImagesSlideshow.TOTAL-mPageNumber) + ".jpg");
        try{
            bitmap = getResizedBitmap(bitmap, 1024);
        }catch (Exception e){}
        ((ImageView)rootView.findViewById(R.id.theImage)).setImageBitmap(bitmap);
        return rootView;
    }

    public int getPageNumber() {
        return mPageNumber;
    }

}