package com.iclub.samskrut.omnipresence;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.crashlytics.android.Crashlytics;

public class Flash extends AppCompatActivity {

    int pos=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);
        setContentView(R.layout.flash);

        setFullscreen(true);

        /*final TextView skip = (TextView)findViewById(R.id.skip);
        skip.setText("<Skip>");
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(Flash.this, Countdown.class);
                Flash.this.startActivity(mainIntent);
                Flash.this.finish();
            }
        });

        final TextView conti = (TextView)findViewById(R.id.conti);
        conti.setText("<Continue>");
        conti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pos == 0) {
                    (findViewById(R.id.flash)).setBackgroundResource(R.drawable.flash2);
                    skip.setVisibility(View.GONE);
                    LinearLayout.LayoutParams params= (LinearLayout.LayoutParams)conti.getLayoutParams();
                    params.setMargins(0,0,0,0);
                    conti.setLayoutParams(params);
                } else if (pos == 1) {
                    (findViewById(R.id.flash)).setBackgroundResource(R.drawable.flash3);
                } else if (pos == 2) {
                    (findViewById(R.id.flash)).setBackgroundResource(R.drawable.flash4);
                } else if (pos == 3) {
                    (findViewById(R.id.flash)).setBackgroundResource(R.drawable.flash5);
                } else if (pos == 4) {
                    (findViewById(R.id.flash)).setBackgroundResource(R.drawable.flash6);
                    conti.setText("<Finish>");
                } else if (pos == 5) {
                    Intent mainIntent = new Intent(Flash.this, Countdown.class);
                    Flash.this.startActivity(mainIntent);
                    Flash.this.finish();
                }
                pos++;
            }
        });*/

        Intent intent = new Intent(this,ProjectList.class);
        startActivity(intent);
        finish();

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