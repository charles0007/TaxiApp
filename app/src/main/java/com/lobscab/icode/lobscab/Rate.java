package com.lobscab.icode.lobscab;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.firebase.client.Firebase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Icode on 12/17/2017.
 */

public class Rate extends AppCompatActivity {

    RadioGroup rd_rate;
    RadioButton poor,good,vgood,excellent,rate;
    Button submit;
    EditText comment;
    Firebase reference1;
    String fulldate="",time="",date="",day="";
    String radioBtn;
    SessionManagement sessionManagement;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rate);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        sessionManagement=new SessionManagement(this);
        toolbar.setTitle("  "+"Rating ");
        // toolbar.setBackground(new ColorDrawable(Color.parseColor("#0000ff")));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        DateFormat df = new SimpleDateFormat("EEE,dd MM yyyy,HH:mm:ss");
        fulldate = df.format(Calendar.getInstance().getTime());
        day =fulldate.split(",")[0].trim();
        date=fulldate.split(",")[1].trim();
        time=fulldate.split(",")[2].trim();
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        Firebase.setAndroidContext(this);

        reference1 = new Firebase("https://oral-23700.firebaseio.com/lobscab/driver-rating/" + sessionManagement.get("username") + "_" + sessionManagement.get("plate"));

        rd_rate=(RadioGroup)findViewById(R.id.rd_rate);
        submit=(Button)findViewById(R.id.submit);
        comment=(EditText)findViewById(R.id.comment);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String commentText = comment.getText().toString();

                try {
                    rate=(RadioButton)findViewById(rd_rate.getCheckedRadioButtonId());
                    if (!commentText.equals("")) {
                        Map<String, String> map = new HashMap<String, String>();
                        Map<String, String> mapUser = new HashMap<String, String>();
                        map.put("comment", commentText);
                        map.put("user", sessionManagement.get("username"));
                        map.put("rate",rate.getText().toString());
                        map.put("read", "unread");
                        map.put("date", date);
                        map.put("day", day);
                        map.put("time", time);
                    mapUser.put("driver", sessionManagement.get("plate"));
                        reference1.push().setValue(map);
                        AlertDialog.Builder builder = new AlertDialog.Builder(Rate.this);
                        builder.setTitle("Rating & Comment");
                        builder.setMessage("Thanks for your feedback!")
                                .setCancelable(true)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                        comment.setText("");
//                    usersChat.push().setValue(mapUser);

                    }
                }catch (Exception exr) {
                    Toast.makeText(Rate.this, "Error occured, comment not sent try again", Toast.LENGTH_LONG);
                }
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
}