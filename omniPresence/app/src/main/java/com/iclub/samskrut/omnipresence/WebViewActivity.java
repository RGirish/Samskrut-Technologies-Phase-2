package com.iclub.samskrut.omnipresence;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;


public class WebViewActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_web_view);

        WebView mwebview = (WebView) findViewById(R.id.my_webview);
        mwebview.loadUrl("file:///android_asset/test.html");
        mwebview.getSettings().setJavaScriptEnabled(true);

    }

}