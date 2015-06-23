package com.samskrut.unrealestate;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class MyWebView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_web_view);
        setFullscreen(true);

        Intent intent = getIntent();
        String url = intent.getStringExtra("url");

        final ProgressDialog dialog = ProgressDialog.show(this,null,"Loading - 0%",true);

        //Set up the webview with the url extracted from the Intent and set a progress listener to update the ProgressDialog
        WebView webView = (WebView)findViewById(R.id.mwv);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                dialog.setMessage("Loading - "+progress+"%");
                if(progress==100){
                    dialog.dismiss();
                }
            }
        });
        webView.loadUrl(url);
    }

    /**
     * A function to make the app go full screen- hides the status bar
     * @param fullscreen a value of true goes full screen, false comes back from full screen
     */
    private void setFullscreen(boolean fullscreen){
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        if (fullscreen){
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }else{
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }
        getWindow().setAttributes(attrs);
    }
}