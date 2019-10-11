package com.lobscab.icode.lobscab;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.HashMap;

public class SessionManagement {
    // Shared Preferences
    SharedPreferences pref,pref_reg;

    // Editor for Shared preferences
    Editor editor;
    public static MapsNavActivity rmapNav=new MapsNavActivity();

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "LobsCabPref";

    // All Shared Preferences Keys
    private static final String IS_LOGIN = "IsLoggedIn";

    // User name (make variable public to access from outside)
    public static final String KEY_NAME ="User" ;
    DBHelper dbHelper;


    // Constructor
    public SessionManagement(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();

    }

    public void set(String set,String name){
        // Storing name in pref
        editor.putString(set, name);
        // commit changes
        editor.commit();
    }

    public String get(String name){
        return  pref.getString(name, null);
    }

    /**
     * Create login session
     * */
    public void createLoginSession(String name){
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);
        editor.putString("username",name
                .replace(".","_")
                .replace("#","_")
                .replace("$","_")
                .replace("[","_")
                .replace("]","_")
                .split("_")[0]);

        // Storing name in pref
        editor.putString(KEY_NAME, name);
        // commit changes
        editor.commit();
    }

    /**
     * Clear session details
     * */
    public void logoutUser(){
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

        // After logout redirect user to Loing Activity
        Intent i = new Intent(_context, CabLogin.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Staring Login Activity
        _context.startActivity(i);
    }



}