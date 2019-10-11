package com.lobscab.icode.lobscab;


import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.Toolbar;

import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import org.w3c.dom.Text;

import java.util.HashMap;

/**
 * Created by Icode on 3/9/2017.
 */

public class About extends AppCompatActivity {
    boolean boolVal;
    TextView abtRemote;
    FirebaseRemoteConfig remoteConfig=FirebaseRemoteConfig.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        remoteConfig.setConfigSettings(new FirebaseRemoteConfigSettings.Builder().setDeveloperModeEnabled(true).build());
        HashMap<String,Object> defaults=new HashMap<>();
        defaults.put("abt_config","");
        remoteConfig.setDefaults(defaults);
        final Task<Void> fetch=remoteConfig.fetch(0);
        fetch.addOnSuccessListener(this, new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                remoteConfig.activateFetched();
                TextView abtRemote = (TextView) findViewById(R.id.abtRemote);

                String abtText= remoteConfig.getString("abt_config");
                abtRemote.setText(abtText);
            }
        });
        setContentView(R.layout.about);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.setTitle("  About");
        // toolbar.setBackground(new ColorDrawable(Color.parseColor("#0000ff")));
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);
        // Display icon in the toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
