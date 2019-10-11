package com.lobscab.icode.lobscab;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static com.lobscab.icode.lobscab.MapsNavActivity.ruser;


public class Chat extends AppCompatActivity {
    LinearLayout layout;
    RelativeLayout layout_2;
    ImageView sendButton;
    EditText messageArea;
    ScrollView scrollView;
    Button ride_accept,ride_decline;
    Firebase reference1, reference2,usersChat;
    String fulldate="",time="",date="",day="";
    DatabaseReference nRootRef= FirebaseDatabase.getInstance().getReference();
    DatabaseReference dblobscab=nRootRef.child("lobscab");
    DatabaseReference nNotifys=dblobscab.child("Notifications");
    ProgressDialog pgd;
    boolean start;
    String userName,message,mess_date;
    static int firstClick=0;
    String gpsLat,gpsLng;
    DatabaseReference nriders=dblobscab.child("riders");
    FirebaseRemoteConfig remoteConfig=FirebaseRemoteConfig.getInstance();
    String address,city,state,knownName,country,postalCode;
    SessionManagement sessionManagement;
    sendNotifyTask mAuthTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        start=true;
        setContentView(R.layout.activity_chat);

        sessionManagement=new SessionManagement(this);
        pgd = new ProgressDialog(Chat.this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("  "+sessionManagement.get("chatWithUni")+" - "+sessionManagement.get("chatWith"));
        // toolbar.setBackground(new ColorDrawable(Color.parseColor("#0000ff")));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        layout = (LinearLayout) findViewById(R.id.layout1);
        layout_2 = (RelativeLayout)findViewById(R.id.layout2);
        sendButton = (ImageView)findViewById(R.id.sendButton);
        messageArea = (EditText)findViewById(R.id.messageArea);
        scrollView = (ScrollView)findViewById(R.id.scrollView);
        ride_accept = (Button) findViewById(R.id.ride_accept);
        ride_decline = (Button) findViewById(R.id.ride_decline);
        layout = (LinearLayout) findViewById(R.id.layout1);
        DateFormat df = new SimpleDateFormat("EEE,dd MM yyyy,HH:mm:ss");
         fulldate = df.format(Calendar.getInstance().getTime());
         day =fulldate.split(",")[0].trim();
         date=fulldate.split(",")[1].trim();
         time=fulldate.split(",")[2].trim();
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        Firebase.setAndroidContext(this);

        gpsLat=sessionManagement.get("gpsLat");
        gpsLng=sessionManagement.get("gpsLng");

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(Chat.this, Locale.getDefault());

        try {addresses = geocoder.getFromLocation(Double.parseDouble(gpsLat), Double.parseDouble(gpsLng), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            if(addresses!=null && addresses.size()>0) {
                address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                 city = addresses.get(0).getLocality();
                 state = addresses.get(0).getAdminArea();
                 country = addresses.get(0).getCountryName();
                 postalCode = addresses.get(0).getPostalCode();
                 knownName = addresses.get(0).getFeatureName(); // Only

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception rt){}

        remoteConfig.setConfigSettings(new FirebaseRemoteConfigSettings.Builder().setDeveloperModeEnabled(true).build());
        HashMap<String,Object> defaults=new HashMap<>();
        defaults.put("emailTo","lobscab@gmail.com");
        defaults.put("subject","Lobscab Chat");


        reference1 = new Firebase("https://oral-23700.firebaseio.com/lobscab/driver-chat/" + sessionManagement.get("username") + "_" + sessionManagement.get("chatWithUni"));
//        reference2 = new Firebase("https://oral-23700.firebaseio.com/lobscab/driver-chat/" + sessionManagement.get("chatWithUni") + "_" + sessionManagement.get("username"));
       // usersChat = new Firebase("https://oral-23700.firebaseio.com/lobscab/users-chat/" + sessionManagement.get("username);

        ride_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pgd.show();
                pgd.setCancelable(false);
                pgd.setCanceledOnTouchOutside(false);
                String rplateNumBg,user_rider,phone_num;
                rplateNumBg = sessionManagement.get("chatWithUni");user_rider=sessionManagement.get("username");
                    phone_num = sessionManagement.get("num");

                try {

                    String mainKey2 = "rider--__"+user_rider +"--__"+ rplateNumBg;
                    DatabaseReference newNotifys = nNotifys.child(mainKey2);
                    DatabaseReference dbriderLat = newNotifys.child("rider_lat");
                    DatabaseReference dbriderLng = newNotifys.child("rider_lng");
                    DatabaseReference dbriderDate = newNotifys.child("date");
                    DatabaseReference dbriderDay = newNotifys.child("day");
                    DatabaseReference dbriderTIme = newNotifys.child("time");
                    DatabaseReference dbriderNum = newNotifys.child("rider_num");

                    dbriderLat.setValue(gpsLat);
                    dbriderLng.setValue(gpsLng);
                    dbriderDate.setValue(date);
                    dbriderDay.setValue(day);
                    dbriderTIme.setValue(time);
                    dbriderNum.setValue(phone_num);
                }catch (Exception exr){}
                mAuthTask = new sendNotifyTask(sessionManagement.get("chatWith"),
                        sessionManagement.get("chatWithUni"),
                        sessionManagement.get("username"),
                        "Rider wants to take a ride with you,click to view riders location ",
                        "Notification",gpsLat,gpsLng,user_rider);
                mAuthTask.execute((Void) null);
                pgd.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(Chat.this);
                builder.setTitle(" LobsCab ");
                builder.setMessage("Driver will reach you soon!,Thanks for using Lobscab")
                        .setCancelable(false)
                        .setPositiveButton("OK",  new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                        finish();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();

            }
        });

        ride_decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    finish();
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageArea.getText().toString();
                try {
                    if (!messageText.equals("")) {
                        Map<String, String> map = new HashMap<String, String>();
                        Map<String, String> mapUser = new HashMap<String, String>();
                        map.put("message", messageText);
                        map.put("user", sessionManagement.get("uniqueUsername"));
                        map.put("read", "unread");
                        map.put("date", date);
                        map.put("day", day);
                        map.put("time", time);
                        mapUser.put("driver", sessionManagement.get("chatWithUni"));
                        mAuthTask = new sendNotifyTask(sessionManagement.get("chatWith"),
                                sessionManagement.get("chatWithUni"),sessionManagement.get("username"),
                                messageText,"Message","","",sessionManagement.get("username"));
                        mAuthTask.execute((Void) null);
                        reference1.push().setValue(map);
//                        reference2.push().setValue(map);

//                    usersChat.push().setValue(mapUser);
                        messageArea.setText("");
                        if(firstClick==0){
                                    DatabaseReference newrider = nriders.child(ruser);
                                    DatabaseReference dbriderAddress = newrider.child("address");
                                    DatabaseReference dbriderCity = newrider.child("city");
                                    DatabaseReference dbriderState = newrider.child("state");
                                    DatabaseReference dbriderCountry = newrider.child("country");
                                    DatabaseReference dbriderPostal = newrider.child("postalCode");
                                    DatabaseReference dbriderKnownName = newrider.child("knownName");
                                    dbriderAddress.setValue(address);
                                    dbriderCity.setValue(city);
                                    dbriderState.setValue(state);
                                    dbriderCountry.setValue(country);
                                    dbriderPostal.setValue(postalCode);
                                    dbriderKnownName.setValue(knownName);

                        firstClick=1;
                        }
                    }
                }catch (Exception exr) {
                    Toast.makeText(Chat.this, "Error occured, message not sent try again", Toast.LENGTH_LONG);
                }
            }
        });


            reference1.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                   try{
                    Map map = dataSnapshot.getValue(Map.class);
                     message = map.get("message").toString();
                     userName = map.get("user").toString();
                     mess_date = map.get("date").toString();

//                       dbHelper.insertChat(message,userName,mess_date);
                       try{
                                  if (userName.equals(sessionManagement.get("uniqueUsername")) && mess_date.trim().equals(date.trim())) {
                                      addMessageBox("You:-\n" + message, 1);
                                  } else if (mess_date.trim().equals(date.trim())) {
                                      addMessageBox(sessionManagement.get("chatWith") + ":-\n" + message, 2);
                                  }

                       }catch (Exception cxp){}

                }catch (Exception exr){}
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });

    }

    public void addMessageBox(String message, int type){
        TextView textView = new TextView(Chat.this);
        textView.setText(message);

        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.weight = 1.0f;

        if(type == 1) {
            lp2.gravity = Gravity.RIGHT;
            textView.setBackgroundResource(R.drawable.bubble_in);
        }
        else{
            lp2.gravity = Gravity.LEFT;
            textView.setBackgroundResource(R.drawable.bubble_out);
        }
        textView.setLayoutParams(lp2);
        layout.addView(textView);
        scrollView.fullScroll(View.FOCUS_DOWN);
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

    @Override
    protected void onResume() {

        super.onResume();
    }

    protected void sendEmail(String emailTo,String subject,String emailTxt) {

//        String[] TO = {""};
        String[] CC = {""};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, emailTo);
//        emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, emailTxt);

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            finish();

        } catch (android.content.ActivityNotFoundException ex) {
//            Toast.makeText(Chat.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }


    public class sendNotifyTask extends AsyncTask<Void, Void, Boolean> {
        private String recipient,title, message,plate,type,rlat,rlog,user;

        sendNotifyTask(String nrecipient,String nplate,String ntitle,String nmessage,String ntype,String nrlat,String nrlog,String nuser){
            recipient=nrecipient;
            title=ntitle;
            message=nmessage;
            plate=nplate;
            type=ntype;
            rlat=nrlat;
            rlog=nrlog;
            user=nuser;

        }
        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody body = new FormBody.Builder()
                        .add("Message",message)
                        .add("Rider",user)
                        .add("carUser",recipient)
                        .add("Title",title)
                        .add("carPlate",plate)
                        .add("Type",type)
                        .add("Lat",rlat)
                        .add("Log",rlog)
                        .build();
                Request request = new Request.Builder()
                        .url("http://donationearners.net/cab_token/lobscab_get_data.php")
                        .post(body)
                        .build();
                try {
                    client.newCall(request).execute();
                } catch (IOException e) {

                    e.printStackTrace();
                    return false;
                }
                // TODO: register the new account here.
                return true;
            }catch (Exception er){
                return false;
            }

        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {
                Toast.makeText(Chat.this, "Success Sent", Toast.LENGTH_SHORT).show();
            } else {

                Toast.makeText(Chat.this, "Failed Sent", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled () {
            mAuthTask = null;

        }
    }
}