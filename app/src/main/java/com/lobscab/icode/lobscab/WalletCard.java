package com.lobscab.icode.lobscab;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.HashMap;

/**
 * Created by Icode on 12/11/2017.
 */

public class WalletCard extends AppCompatActivity {
    FirebaseRemoteConfig remoteConfig;
    String weburl;
    WebView webView;
    TextView walletTxt;
    ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wallet_card);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(" Make Payment  ");
        walletTxt=(TextView)findViewById(R.id.walletTxt);
        // toolbar.setBackground(new ColorDrawable(Color.parseColor("#0000ff")));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        //REMOTE CONFIG
        weburl="";
        pd=new ProgressDialog(this);
        pd.setMessage("Loading...");
        pd.show();
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
        remoteConfig=FirebaseRemoteConfig.getInstance();
        remoteConfig.setConfigSettings(new FirebaseRemoteConfigSettings.Builder().setDeveloperModeEnabled(true).build());
        HashMap<String, Object> defaults = new HashMap<>();
        defaults.put("rider_payment_link", weburl);
        remoteConfig.setDefaults(defaults);
        webView=(WebView)findViewById(R.id.webView);
        webView.setWebViewClient(new MyBrowser());
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);

        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        final Task<Void> fetch = remoteConfig.fetch(0);
        fetch.addOnSuccessListener(this, new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                remoteConfig.activateFetched();
                weburl = remoteConfig.getString("rider_payment_link");
                if(!weburl.isEmpty()) {
                    webView.setVisibility(View.VISIBLE);
                    walletTxt.setVisibility(View.GONE);
                    webView.loadUrl(weburl);
                }else {
                    webView.setVisibility(View.GONE);
                    walletTxt.setVisibility(View.VISIBLE);}
                pd.dismiss();
//                Toast.makeText(WalletCard.this,"Remote: "+weburl,Toast.LENGTH_LONG).show();
            }
        });




    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private class MyBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}
