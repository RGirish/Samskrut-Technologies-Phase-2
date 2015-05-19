package com.example.girish.test;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WebView webView = (WebView)findViewById(R.id.myWebView);
        webView.loadUrl("http://krpano.com/stereo3d/indiantemple/mobilevr.html");
        webView.getSettings().setJavaScriptEnabled(true);
    }
}