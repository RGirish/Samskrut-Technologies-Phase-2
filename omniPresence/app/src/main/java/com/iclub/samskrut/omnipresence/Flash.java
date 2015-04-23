package com.iclub.samskrut.omnipresence;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;

public class Flash extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mainIntent = new Intent(Flash.this, ProjectList.class);
                Flash.this.startActivity(mainIntent);
                Flash.this.finish();
            }
        }, 500);

    }

}