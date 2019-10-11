package com.lobscab.icode.lobscab;

/**
 * Created by Icode on 5/16/2017.
 */
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Profile extends AppCompatActivity {

    private Button btnChangeEmail, btnChangePassword,
            changeEmail, changePassword, sendEmail, remove, signOut;

    private EditText oldEmail, newEmail, password, newPassword;
    private TextView profile_name,profile_num,profile_email;
    private ProgressBar progressBar;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    SessionManagement session;
    DBHelper dbHelper;
    DatabaseReference nRootRef= FirebaseDatabase.getInstance().getReference();
    DatabaseReference dblobscab=nRootRef.child("lobscab");
    DatabaseReference nriders=dblobscab.child("riders");
    String phone="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper=new DBHelper(this);
        setContentView(R.layout.profile_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("  "+"Setting");
       // toolbar.setBackground(new ColorDrawable(Color.parseColor("#0000ff")));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        //get firebase auth instance
        auth = FirebaseAuth.getInstance();
        session=new SessionManagement(Profile.this);
        MobileAds.initialize(this, "ca-app-pub-5527620381143347~2094474910");
        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.SMART_BANNER);
        adView.setAdUnitId("ca-app-pub-5527620381143347/8001407719");

//        AdRequest adRequest = new AdRequest.Builder().addTestDevice(id).build();
        AdRequest adRequest = new AdRequest.Builder().build();

        adView.loadAd(adRequest);


        //get current user
        final FirebaseUser userDetails = FirebaseAuth.getInstance().getCurrentUser();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser userDetails = firebaseAuth.getCurrentUser();
                if (userDetails == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(Profile.this, CabLogin.class));
                    finish();
                }
            }
        };

        btnChangeEmail = (Button) findViewById(R.id.change_email_button);
        btnChangePassword = (Button) findViewById(R.id.change_password_button);


        changeEmail = (Button) findViewById(R.id.changeEmail);
        changePassword = (Button) findViewById(R.id.changePass);
        sendEmail = (Button) findViewById(R.id.send);
        remove = (Button) findViewById(R.id.remove);

        profile_name = (TextView) findViewById(R.id.profile_name);
        profile_email = (TextView) findViewById(R.id.profile_email);
        profile_num = (TextView) findViewById(R.id.profile_num);
        String[] userArr=null;
        try {
             userArr = dbHelper.getDetailsReg().split("--__")[0]
                    .replace(".", "_")
                    .replace("#", "_")
                    .replace("$", "_")
                    .replace("[", "_")
                    .replace("]", "_")
                    .split("@");
        }catch (Exception r){

            userArr=userDetails.getEmail()
                    .replace(".","_")
                    .replace("#","_")
                    .replace("$","_")
                    .replace("[","_")
                    .replace("]","_")
                    .split("@");

        }

        DatabaseReference newrider = nriders.child(userArr[0]);
        DatabaseReference dbriderPhone = newrider.child("Phone");

try {
    if (session.get("num").isEmpty() || session.get("num") == null) {
        dbriderPhone.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    phone = dataSnapshot.getValue().toString();
                    profile_num.setText(phone);
                    session.set("num",phone);
//                    Toast.makeText(Profile.this,"DataChange "+phone,Toast.LENGTH_LONG).show();
                    try {
                        if (dbHelper.numberOfRowsReg() < 1) {
                            dbHelper.insertReg(userDetails.getEmail(), "", 1, phone);
                        } else {
                            dbHelper.updateReg(userDetails.getEmail(), phone);
                        }
                    } catch (Exception ex) {
                    }

                } catch (Exception er) {
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}catch (Exception xp){}

        try {
            Thread.sleep(500);
        } catch (Exception e) {
        }
profile_name.setText(userArr[0]);
        String[] prEmail=null;
        try {
            prEmail=dbHelper.getDetailsReg().split("--__");
            profile_email.setText(prEmail[0]);
            if(profile_num.getText().toString().isEmpty()){profile_num.setText(prEmail[3]); }

        }catch (Exception rt){
            profile_email.setText(userDetails.getEmail());

        }



        oldEmail = (EditText) findViewById(R.id.old_email);
        newEmail = (EditText) findViewById(R.id.new_email);
        password = (EditText) findViewById(R.id.password);
        newPassword = (EditText) findViewById(R.id.newPassword);

        oldEmail.setVisibility(View.GONE);
        newEmail.setVisibility(View.GONE);
        password.setVisibility(View.GONE);
        newPassword.setVisibility(View.GONE);
        changeEmail.setVisibility(View.GONE);
        changePassword.setVisibility(View.GONE);
        sendEmail.setVisibility(View.GONE);
        remove.setVisibility(View.GONE);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        btnChangeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oldEmail.setVisibility(View.GONE);
                newEmail.setVisibility(View.VISIBLE);
                password.setVisibility(View.GONE);
                newPassword.setVisibility(View.GONE);
                changeEmail.setVisibility(View.VISIBLE);
                changePassword.setVisibility(View.GONE);
                sendEmail.setVisibility(View.GONE);
                remove.setVisibility(View.GONE);
            }
        });

        changeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                if (userDetails != null && !newEmail.getText().toString().trim().equals("")) {
                    userDetails.updateEmail(newEmail.getText().toString().trim())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
                                        builder.setMessage("Email address is updated. Please sign in with new email id!")
                                                .setCancelable(false)
                                                .setPositiveButton("OK",  new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        signOut();
                                                    }
                                                });
                                        AlertDialog alert = builder.create();
                                        alert.show();


                                        progressBar.setVisibility(View.GONE);
                                    } else {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
                                        builder.setMessage("Failed to update email!")
                                                .setCancelable(false)
                                                .setPositiveButton("OK",  new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        progressBar.setVisibility(View.GONE);
                                                    }
                                                });
                                        AlertDialog alert = builder.create();
                                        alert.show();

                                    }
                                }
                            });
                } else if (newEmail.getText().toString().trim().equals("")) {
                    newEmail.setError("Enter email");
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oldEmail.setVisibility(View.GONE);
                newEmail.setVisibility(View.GONE);
                password.setVisibility(View.GONE);
                newPassword.setVisibility(View.VISIBLE);
                changeEmail.setVisibility(View.GONE);
                changePassword.setVisibility(View.VISIBLE);
                sendEmail.setVisibility(View.GONE);
                remove.setVisibility(View.GONE);
            }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                if (userDetails != null && !newPassword.getText().toString().trim().equals("")) {
                    if (newPassword.getText().toString().trim().length() < 6) {
                        newPassword.setError("Password too short, enter minimum 6 characters");
                        progressBar.setVisibility(View.GONE);
                    } else {
                        userDetails.updatePassword(newPassword.getText().toString().trim())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
                                            builder.setMessage("Password is updated, sign in with new password!")
                                                    .setCancelable(false)
                                                    .setPositiveButton("OK",  new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            signOut();
                                                            progressBar.setVisibility(View.GONE);
                                                        }
                                                    });
                                            AlertDialog alert = builder.create();
                                            alert.show();
                                        } else {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
                                            builder.setMessage("Failed to update password!")
                                                    .setCancelable(false)
                                                    .setPositiveButton("OK",  new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {

                                                            progressBar.setVisibility(View.GONE);
                                                        }
                                                    });
                                            AlertDialog alert = builder.create();
                                            alert.show();
                                          //  Toast.makeText(Profile.this, "Failed to update password!", Toast.LENGTH_SHORT).show();
                                          //  progressBar.setVisibility(View.GONE);
                                        }
                                    }
                                });
                    }
                } else if (newPassword.getText().toString().trim().equals("")) {
                    newPassword.setError("Enter password");
                    progressBar.setVisibility(View.GONE);
                }
            }
        });



    }

    //sign out method
    public void signOut() {
        auth.signOut();session.logoutUser();
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
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
}