package com.iclub.samskrut.omnipresence;

import android.os.Bundle;
import android.os.Handler;

import com.google.vrtoolkit.cardboard.CardboardActivity;
import org.rajawali3d.cardboard.RajawaliCardboardRenderer;
import org.rajawali3d.cardboard.RajawaliCardboardView;

public class MyVrView extends CardboardActivity{

    SettingsContentObserver mSettingsContentObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RajawaliCardboardView view = new RajawaliCardboardView(this);
        setContentView(view);
        setCardboardView(view);

        RajawaliCardboardRenderer renderer = new MyRenderer(this, getIntent().getIntExtra("pos",1));
        view.setRenderer(renderer);
        view.setSurfaceRenderer(renderer);

        mSettingsContentObserver = new SettingsContentObserver(this,new Handler());
        getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, mSettingsContentObserver );

    }
}