package com.lobscab.icode.lobscab;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.client.Firebase;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Icode on 3/9/2017.
 */

public class CabReg extends AppCompatActivity {
    Button btnCabreg;
    TextView loginCabreg;
    EditText inputEmail,inputPass,inputRepass,inputPhone;
    private FirebaseAuth auth;
    private View mProgressView;
    private View mRegFormView;
    private UserCabRegTask mAuthTask = null;
   boolean boolVal;
    StringRequest request;
    ProgressDialog pd;
    String regexception;
    DBHelper dbHelper;
    String phone;
    DatabaseReference nRootRef= FirebaseDatabase.getInstance().getReference();
    DatabaseReference dblobscab=nRootRef.child("lobscab");
    DatabaseReference nriders=dblobscab.child("riders");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper=new DBHelper(this);
        setContentView(R.layout.cab_reg);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("  Reistration");
       // toolbar.setBackground(new ColorDrawable(Color.parseColor("#0000ff")));
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);
        // Display icon in the toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        MobileAds.initialize(this, "ca-app-pub-5527620381143347~2094474910");
        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.SMART_BANNER);
        adView.setAdUnitId("ca-app-pub-5527620381143347/8001407719");

//        AdRequest adRequest = new AdRequest.Builder().addTestDevice(id).build();
        AdRequest adRequest = new AdRequest.Builder().build();

        adView.loadAd(adRequest);

        auth = FirebaseAuth.getInstance();

        inputEmail = (EditText) findViewById(R.id.reg_user);
        inputPass = (EditText) findViewById(R.id.reg_pass);
        inputRepass = (EditText) findViewById(R.id.reg_repass);
        inputPhone = (EditText) findViewById(R.id.reg_phone);


        btnCabreg = (Button) findViewById(R.id.btnCabreg);
        loginCabreg = (TextView) findViewById(R.id.loginCabreg);

        loginCabreg.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                startActivity(new Intent(CabReg.this, CabLogin.class));
            }
        } );

        btnCabreg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                mRegFormView = findViewById(R.id.cab_reg_form);
                mProgressView = findViewById(R.id.cab_reg_progress);

                int rs = 0;
                String remail,rpass,repass;

                remail = inputEmail.getText().toString();
                rpass = inputPass.getText().toString();
                repass = inputRepass.getText().toString();
                phone = inputPhone.getText().toString();

                inputEmail.setError(null);
                inputPhone.setError(null);
                inputPass.setError(null);
                inputRepass.setError(null);

                if (TextUtils.isEmpty(remail)) {
                    inputEmail.setError(getString(R.string.error_field_required));
                }
                else if (TextUtils.isEmpty(phone)) {
                    inputPhone.setError(getString(R.string.error_field_required));
                }
                else if (TextUtils.isEmpty(rpass)) {
                    inputPass.setError(getString(R.string.error_field_required));
                }
                else if (TextUtils.isEmpty(repass)) {
                    inputRepass.setError(getString(R.string.error_field_required));
                }else if (phone.length()<11 || phone.length()>15) {
                    inputPhone.setError("Wrong phone number ");
                }
                else if (rpass.length() < 6) {
                    inputPass.setError(getString(R.string.minimum_password));
                }
                else if (!rpass.equals(repass)) {
                    inputRepass.setError("Password mismerge");
                }
                 else {
//                    showProgress(true);
                    pd = new ProgressDialog(CabReg.this);
                    pd.setMessage("Loading...");
                    pd.show();
                    pd.setCancelable(false);
                    pd.setCanceledOnTouchOutside(false);
                    mAuthTask = new UserCabRegTask(remail.trim(),rpass.trim());
                    mAuthTask.execute((Void) null);


                }

            }
        });
    }


    public class UserCabRegTask extends AsyncTask<Void, Void, Boolean> {

     private final String remailBg,rpassBg;

        UserCabRegTask(String emailBg,String passBg) {
            remailBg = emailBg;
            rpassBg=passBg;



        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.


try {
    //create user

    auth.createUserWithEmailAndPassword(remailBg, rpassBg)
            .addOnCompleteListener(CabReg.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                   // Toast.makeText(SignupActivity.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                    final String user=remailBg
                            .replace(".","_")
                            .replace("#","_")
                            .replace("$","_")
                            .replace("[","_")
                            .replace("]","_")
                            .split("@")[0];
//                    String url = "https://oral-23700.firebaseio.com/users.json";
//

                    if (!task.isSuccessful()) {
                        regexception=task.getException().getMessage();
                        if(regexception==null || regexception.equals("") || regexception.isEmpty()){
                            regexception="Registration failed, try again";
                        }
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                          regexception="Registration failed, User with this email already exist.";
                        }
                        boolVal= false;
                    } else {
                        sendVerificationEmail(remailBg,rpassBg);

                        boolVal= true;
                    }
                }
            });

// Simulate network access.
    Thread.sleep(2000);
} catch (InterruptedException e) {
    return false;
}


            // TODO: register the new account here.
            return boolVal;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {
//
//                RequestQueue rQueue = Volley.newRequestQueue(CabReg.this);
//                rQueue.add(request);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                pd.dismiss();



            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(CabReg.this);
                builder.setMessage(regexception)
                        .setCancelable(true)
                        .setPositiveButton("OK", null);
                AlertDialog alert = builder.create();
                alert.show();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            pd.dismiss();
        }
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

    private void sendVerificationEmail(final String email, final String pass) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // email sent

                            try {
                                dbHelper.insertReg(email,pass,0,phone);

                                DatabaseReference newrider = nriders.child(email
                                        .replace(".", "_")
                                        .replace("#", "_")
                                        .replace("$", "_")
                                        .replace("[", "_")
                                        .replace("]", "_")
                                        .split("@")[0]);
                                DatabaseReference dbriderPhone = newrider.child("Phone");
                                dbriderPhone.setValue(phone);
                            }catch (Exception exr){}
                            // after email is sent just logout the user and finish this activity
                            FirebaseAuth.getInstance().signOut();
                            AlertDialog.Builder builder = new AlertDialog.Builder(CabReg.this);
                            builder.setMessage("Registration successful!, verification email will be send to you shortly")
                                    .setCancelable(false)
                                    .setPositiveButton("OK",  new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            startActivity(new Intent(CabReg.this, CabLogin.class));
                                            finish();
                                        }
                                    });
                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                        else
                        {
                            // email not sent, so display message and restart the activity or do whatever you wish to do
                            AlertDialog.Builder builder = new AlertDialog.Builder(CabReg.this);
                            builder.setMessage("verification not sent, try again")
                                    .setCancelable(false)
                                    .setPositiveButton("OK",  new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {

                                        }
                                    });
                            AlertDialog alert = builder.create();
                            alert.show();
                            //restart this activity
                            overridePendingTransition(0, 0);
                            finish();
                            overridePendingTransition(0, 0);
                            startActivity(getIntent());

                        }
                    }
                });
    }





}
