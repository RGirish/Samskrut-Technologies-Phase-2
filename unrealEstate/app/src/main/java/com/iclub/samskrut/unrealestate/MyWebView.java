package com.iclub.samskrut.unrealestate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.Toast;

public class MyWebView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setFullscreen(true);

        Intent intent = getIntent();
        String url = intent.getStringExtra("url");

        Toast.makeText(this,url,Toast.LENGTH_LONG).show();

        /*WebView webView = (WebView)findViewById(R.id.mwv);
        webView.loadUrl(url);
        webView.getSettings().setJavaScriptEnabled(true);*/
    }

    private void setFullscreen(boolean fullscreen)
    {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        if (fullscreen){
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }else{
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }
        getWindow().setAttributes(attrs);
    }
}