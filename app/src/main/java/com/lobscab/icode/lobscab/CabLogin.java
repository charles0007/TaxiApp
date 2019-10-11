package com.lobscab.icode.lobscab;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CALL_PHONE;

/**
 *
 *
 * Created by charles on 21-Mar-16.
 */

public class CabLogin extends AppCompatActivity implements View.OnClickListener {
    Button btnLogin;
    TextView regLogin,forgot;
    EditText email,password;
    SessionManagement session;
    String remail,rPass;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1001;

    private View mProgressView;
    private UserCabLoginTask mAuthTask = null;
    boolean boolVal =true;
    protected LocationManager locationManager;
    private FirebaseAuth auth;
    View mRegFormView;
    public static boolean welcomeScreenIsShown = true;
    private static final int MY_PERMISSIONS_CALL_PHONE = 1005;
    private AdView adView;
    GoogleApiClient mGoogleApiClient;
    int PLAY_SERVICES_RESOLUTION_REQUEST=9000;
    ProgressDialog pd;
    DBHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper=new DBHelper(this);
        checkPlayServices();

        session = new SessionManagement(CabLogin.this);
        welcomeScreenIsShown=false;

        auth = FirebaseAuth.getInstance();
        checkGps();
        setContentView(R.layout.cab_login);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        regLogin = (TextView) findViewById(R.id.regLogin);
        forgot = (TextView) findViewById(R.id.forgot);
        email = (EditText) findViewById(R.id.login_user);
        password = (EditText) findViewById(R.id.login_pass);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(" LobsCab ");
       // toolbar.setColsetBackground(new ColorDrawable(Color.parseColor("#0000ff")));
        setSupportActionBar(toolbar);
        // Display icon in the toolbar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.logo);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
//                String id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        MobileAds.initialize(this, "ca-app-pub-5527620381143347~2094474910");
         adView = (AdView) findViewById(R.id.adView);
//        adView.setAdSize(AdSize.SMART_BANNER);
//        adView.setAdUnitId("ca-app-pub-5527620381143347/8001407719");

//        AdRequest adRequest = new AdRequest.Builder().addTestDevice(id).build();
        AdRequest adRequest = new AdRequest.Builder().build();

        adView.loadAd(adRequest);

        regLogin.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
             //   startActivity(new Intent(CabLogin.this, CabReg.class));

                startActivity(new Intent(CabLogin.this, CabReg.class));

            }
        } );

        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CabLogin.this, ResetPasswordActivity.class));
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mRegFormView = findViewById(R.id.cab_login_form);
                mProgressView = findViewById(R.id.cab_login_progress);
                int rs = 0;
                remail = email.getText().toString();
                rPass = password.getText().toString();
                email.setError(null);password.setError(null);

                if (TextUtils.isEmpty(remail)) {
                    email.setError(getString(R.string.error_field_required));
                }
                else if (TextUtils.isEmpty(rPass)) {
                    password.setError(getString(R.string.error_field_required));
                }
                else if (rPass.length() < 6) {
                    password.setError(getString(R.string.minimum_password));
                }
                else {
                     pd = new ProgressDialog(CabLogin.this);
                    pd.setMessage("Loading...");
                        pd.show();
                    pd.setCancelable(false);
                    pd.setCanceledOnTouchOutside(false);
                    mAuthTask = new UserCabLoginTask(remail, rPass);
                    mAuthTask.execute((Void) null);
                }

            }
        });

    }

    public void checkGps(){
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(ActivityCompat.checkSelfPermission(CabLogin.this,ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED&&ActivityCompat.checkSelfPermission(CabLogin.this,android.Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(CabLogin.this, new String[]{ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);

            }
        if (ActivityCompat.checkSelfPermission(CabLogin.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling

            ActivityCompat.requestPermissions(CabLogin.this, new String[]{CALL_PHONE}, MY_PERMISSIONS_CALL_PHONE);
            // return true;
        }
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                showGPSAlertToUser();
            if (auth.getCurrentUser() != null) {
//                    session.createLoginSession(remail);
                startActivity(new Intent(CabLogin.this, MapsNavActivity.class));
                finish();
                }
            }
            else {
                if (auth.getCurrentUser() != null) {
//                    session.createLoginSession(remail);
                    startActivity(new Intent(CabLogin.this, MapsNavActivity.class));
                    finish();

                }
            }

        //return;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(CabLogin.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling

                        ActivityCompat.requestPermissions(CabLogin.this, new String[]{CALL_PHONE}, MY_PERMISSIONS_CALL_PHONE);
                        // return true;
                    }
                    else if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                        showGPSAlertToUser();
                    }
                    else {

                        if (auth.getCurrentUser() != null) {
                            if(auth.getCurrentUser().getEmail().equalsIgnoreCase(remail)){
                                session.createLoginSession(remail);

                                startActivity(new Intent(CabLogin.this, MapsNavActivity.class));
                                finish();
                            }

                        }
//
                    }
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    //  setContentView(R.layout.login);
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }

    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login_menu, menu);
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_exit:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("Exit LobsCab?");
                alertDialogBuilder
                        .setMessage("Click yes to exit!")
                        .setCancelable(false)
                        .setPositiveButton("Yes",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        finish();
                                        System.exit(0);
                                    }
                                })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
    @Override
    protected void onDestroy(){
        super.onDestroy();

    }

    private void showGPSAlertToUser(){
        AlertDialog.Builder builder = new AlertDialog.Builder(CabLogin.this);
        builder.setTitle(" GPS ACTIVATION");
        builder.setMessage("GPS is disabled in your device, Goto Settings Page To Enable GPS")
                .setCancelable(false)
                .setPositiveButton("OK",  new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try{
                            Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
                            intent.putExtra("enabled", true);
                            sendBroadcast(intent);

                        }catch(Exception rex){
                            try {String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                                if(!provider.contains("gps")){ //if gps is disabled
                                    final Intent poke = new Intent();
                                    poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
                                    poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
                                    poke.setData(Uri.parse("3"));
                                    sendBroadcast(poke);
                                }
                            }catch(Exception re){
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }}
                        Intent callGPSSettingIntent = new Intent(
                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(callGPSSettingIntent);

                    }
                });
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        try{
                            if (dbHelper.numberOfRows()>0){dialog.cancel();}
                            else{
                                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {dialog.cancel();}
                                else{
                                    finish();
                                    System.exit(0);
                                }

                            }
                        }catch (Exception ft){
                            finish();
                            System.exit(0);}

                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }


    public class UserCabLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String ruserBg,rpassBg;

        UserCabLoginTask(String userBg,String passBg) {
            ruserBg = userBg;
            rpassBg=passBg;


        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.


            try {
                //authenticate user
                auth.signInWithEmailAndPassword(ruserBg, rpassBg)
                        .addOnCompleteListener(CabLogin.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                //progressBar.setVisibility(View.GONE);
                                auth = FirebaseAuth.getInstance();
                                if (!task.isSuccessful()) {
                                    // there was an error

                                    boolVal= false;
                                } else {
                                    boolVal=true;

                                }
                            }
                        });
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

    return boolVal;
//}catch (Exception dataE){return false;}



            // TODO: register the new account here.
          //  return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;


            if (success) {
                if (auth.getCurrentUser() != null) {
                    if(auth.getCurrentUser().getEmail().equalsIgnoreCase(remail) ) {
                        if(!checkIfEmailVerified()){
                                    AlertDialog.Builder builder = new AlertDialog.Builder(CabLogin.this);
        builder.setTitle("LobsCab");
        builder.setMessage("Email not verified, verify email and try again")
                .setCancelable(true)
                .setPositiveButton("OK", null);
        AlertDialog alert = builder.create();
        alert.show();
                        }else{
                            try {
                                dbHelper.updateRegVerify(1);
                            }catch (Exception e){}
                            session.createLoginSession(remail);
                            Intent intent = new Intent(CabLogin.this, MapsNavActivity.class);
                            startActivity(intent);
                            pd.dismiss();
                            finish();
                        }

                    }else{noNetworkAuth();}
                }else{noNetworkAuth();}
            } else {
                if(isNetworkAvailable()){notAuth();}
                else{networkError();}
                pd.dismiss();
            }

        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            pd.dismiss();
        }
    }

    void notAuth(){
    AlertDialog.Builder builder = new AlertDialog.Builder(CabLogin.this);
    builder.setTitle("LobsCab");
    builder.setMessage(R.string.auth_failed)
            .setCancelable(true)
            .setPositiveButton("OK", null);
    AlertDialog alert = builder.create();
    alert.show();
}

    void noNetworkAuth(){

        mAuthTask = new UserCabLoginTask(remail,rPass);
        mAuthTask.execute((Void) null);
//

    }
    void networkError(){
        AlertDialog.Builder builder = new AlertDialog.Builder(CabLogin.this);
        builder.setMessage("Network connection error")
                .setCancelable(true)
                .setPositiveButton("OK", null);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private boolean canToggleGPS() {
        PackageManager pacman = getPackageManager();
        PackageInfo pacInfo = null;

        try {
            pacInfo = pacman.getPackageInfo("com.android.settings", PackageManager.GET_RECEIVERS);
        } catch (PackageManager.NameNotFoundException e) {
            return false; //package not found
        }

        if(pacInfo != null){
            for(ActivityInfo actInfo : pacInfo.receivers){
                //test if recevier is exported. if so, we can toggle GPS.
                if(actInfo.name.equals("com.android.settings.widget.SettingsAppWidgetProvider") && actInfo.exported){
                    return true;
                }
            }
        }

        return false; //default
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adView!=null){
            adView.resume();
        }
        checkPlayServices();
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                //Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private boolean checkIfEmailVerified()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user.isEmailVerified())
        {

            // user is verified, so you can finish this activity or send user to activity which you want.

            return true;
        }
        else
        {
            // email is not verified, so just prompt the message to the user and restart this activity.
            // NOTE: don't forget to log out the user.
            FirebaseAuth.getInstance().signOut();
            return false;
            //restart this activity

        }
    }

}


