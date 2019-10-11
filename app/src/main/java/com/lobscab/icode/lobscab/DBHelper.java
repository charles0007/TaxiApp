package com.lobscab.icode.lobscab;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "DBlobscabdriver.db";

    public static final String LOBSCAB_TABLE_NAME = "lobscab";
    public static final String LOBSCAB_TABLE_CHAT = "lobscabChat";
    public static final String LOBSCAB_TABLE_CHATWIT = "lobscabChatWit";
    public static final String LOBSCAB_TABLE_REG = "lobscabReg";
    public static final String LOBSCAB_TABLE_DRIVER = "lobscabDriver";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_ID = "id";

    public static final String COLUMN_LAT= "lat";
    public static final String COLUMN_LOG = "log";
    public static final String COLUMN_EMAIL = "Email";
    public static final String COLUMN_PASSWORD = "Password";
    public static final String COLUMN_VERIFY = "verify";
    public static final String COLUMN_PHONE= "phone";


    private HashMap hp;



    public DBHelper(Context context)
    {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "CREATE TABLE " + LOBSCAB_TABLE_NAME +
                        " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_LAT + " TEXT NOT NULL, " +
                        COLUMN_LOG + " TEXT NOT NULL); "
        );
        db.execSQL(
                "CREATE TABLE " + LOBSCAB_TABLE_CHAT +
                        " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "user TEXT NOT NULL, " +
                        "message TEXT NOT NULL, " +
                       "date TEXT NOT NULL); "
        );
        db.execSQL(
                "CREATE TABLE " + LOBSCAB_TABLE_CHATWIT +
                        " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "user TEXT NOT NULL, " +
                        "chatWithUni TEXT NOT NULL, " +
                        "chatWith TEXT NOT NULL); "
        );
        db.execSQL(
                "CREATE TABLE " + LOBSCAB_TABLE_REG +
                        " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_EMAIL + " TEXT NOT NULL, " +
                        COLUMN_PASSWORD + " TEXT NOT NULL, " +
                        COLUMN_PHONE + " TEXT NOT NULL, " +
                        COLUMN_VERIFY + " INTEGER NOT NULL); "
        );
        db.execSQL(
                "CREATE TABLE " + LOBSCAB_TABLE_DRIVER +
                        " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_LAT + " TEXT NOT NULL, " +
                        COLUMN_LOG + " TEXT NOT NULL); "
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS lobscab");
        db.execSQL("DROP TABLE IF EXISTS "+LOBSCAB_TABLE_CHAT);
        db.execSQL("DROP TABLE IF EXISTS "+LOBSCAB_TABLE_CHATWIT);
        db.execSQL("DROP TABLE IF EXISTS "+LOBSCAB_TABLE_REG);
        db.execSQL("DROP TABLE IF EXISTS "+LOBSCAB_TABLE_DRIVER);
        onCreate(db);
    }

    public boolean insertGPS ( final String lat,final String log)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        //contentValues.put("username", username);
        contentValues.put(COLUMN_LAT, lat);
        contentValues.put(COLUMN_LOG, log);
        db.insert("lobscab", null, contentValues);
        return true;
    }

    public boolean insertChat(String messageText,String user,String date){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("message", messageText);
        contentValues.put("user", user);
        contentValues.put("date", date);

        db.insert(LOBSCAB_TABLE_CHAT, null, contentValues);
        return true;
    }
    public boolean insertChatWit(String user,String chatWit,String chatWitUni){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("user", user);
        contentValues.put("chatWituni", chatWitUni);
        contentValues.put("chatWit", chatWit);

        db.insert(LOBSCAB_TABLE_CHATWIT, null, contentValues);
        return true;
    }
    public boolean insertReg ( String email, String password,final int verify,final String phone)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        //contentValues.put("username", username);
        contentValues.put(COLUMN_EMAIL, email);
        contentValues.put(COLUMN_PASSWORD, password);
        contentValues.put(COLUMN_VERIFY, verify);
        contentValues.put(COLUMN_PHONE, phone);
        db.insert(LOBSCAB_TABLE_REG, null, contentValues);
        return true;
    }
    public boolean insertDriver ( String lat, String log)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        //contentValues.put("username", username);
        contentValues.put("lat", lat);
        contentValues.put("log", log);
        db.insert(LOBSCAB_TABLE_DRIVER, null, contentValues);
        return true;
    }
    public Cursor getDataById(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from lobscab where id="+id+"", null );
        return res;
    }



    public Cursor getByUser(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from lobscab order by id desc limit 1", null );
        return res;
    }


    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, LOBSCAB_TABLE_NAME);
        return numRows;
    }
    public int numberOfRowsReg(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, LOBSCAB_TABLE_REG);
        return numRows;
    }
    public int numberOfRowsChatWit(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, LOBSCAB_TABLE_CHATWIT);
        return numRows;
    }

    public boolean updateGPS (Integer id, String lat, String log)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("lat", lat);
        contentValues.put("log", log);

        db.update("lobscab", contentValues, "id = ? ", new String[]{Integer.toString(id)});
        return true;
    }
    public boolean updateChatWit (String user, String chatWitUni,String chatWit)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("user", user);
        contentValues.put("chatWit", chatWit);
        contentValues.put("chatWitUni", chatWitUni);

        db.update(LOBSCAB_TABLE_CHATWIT, contentValues, " = ? ", new String[]{""});
        return true;
    }
    public boolean updateGPSByUser ( String lat, String log)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_LAT, lat);
        contentValues.put(COLUMN_LOG, log);

        db.update("lobscab", contentValues, " = ? ", new String[]{""});
        return true;
    }

    public String getDetailsByChatWit() {
        //ArrayList<String> array_list = new ArrayList<String>();
        String array_list;
        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from "+LOBSCAB_TABLE_CHATWIT+" order by id desc limit 1", null );
        res.moveToFirst();
        array_list=res.getString(res.getColumnIndex("user"))+"--__"+
                res.getString(res.getColumnIndex("chatWit"))+"--__"+
                res.getString(res.getColumnIndex("chatWitUni"));
        return array_list;
    }

    public String getGPSDetailsByUser() {
        //ArrayList<String> array_list = new ArrayList<String>();
        String array_list;
        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from lobscab order by id desc limit 1", null );
        res.moveToFirst();
        array_list=res.getString(res.getColumnIndex(COLUMN_LAT))+"-"+res.getString(res.getColumnIndex(COLUMN_LOG));
        return array_list;
    }

    public boolean updateReg ( String email,final String phone)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("Email", email);
//        contentValues.put("Password", pass);
        contentValues.put(COLUMN_PHONE, phone);
        db.update(LOBSCAB_TABLE_REG, contentValues, " = ? ", new String[]{""});
        return true;
    }

    public boolean updateRegVerify (final int verify)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_VERIFY, verify);

        db.update(LOBSCAB_TABLE_REG, contentValues, " = ? ", new String[]{""});
        return true;
    }
    public String getDetailsReg() {
        //ArrayList<String> array_list = new ArrayList<String>();
        String array_list;
        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from "+LOBSCAB_TABLE_REG+" order by id desc limit 1", null );
        res.moveToFirst();
        array_list=res.getString(res.getColumnIndex(COLUMN_EMAIL))+"--__"+res.getString(res.getColumnIndex(COLUMN_PASSWORD))
                +"--__"+res.getInt(res.getColumnIndex(COLUMN_VERIFY))+"--__"+res.getString(res.getColumnIndex(COLUMN_PHONE));
        return array_list;
    }

    public boolean updateDriverByUser ( String lat, String log)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("lat", lat);
        contentValues.put("log", log);

        db.update(LOBSCAB_TABLE_DRIVER, contentValues, " = ? ", new String[]{""});
        return true;
    }

    public ArrayList<String> getDriverDetailsByUser() {
        ArrayList<String> array_list = new ArrayList<String>();
//        String array_list;
        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from "+LOBSCAB_TABLE_DRIVER+" order by id desc", null );
        res.moveToNext();
        while(res.isAfterLast() == false){

            array_list.add(res.getString(res.getColumnIndex(COLUMN_LAT))+"--__"+res.getString(res.getColumnIndex(COLUMN_LOG)));
            res.moveToNext();

        }
        return array_list;
    }

    public boolean updateChatByUser ( String message, String user,String date)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("message", message);
        contentValues.put("user", user);
        contentValues.put("date", date);

        db.update(LOBSCAB_TABLE_CHAT, contentValues, " = ? ", new String[]{""});
        return true;
    }

    public ArrayList<String> getChatDetailsByUser() {
        //ArrayList<String> array_list = new ArrayList<String>();
//        String array_list;
        ArrayList<String> array_list = new ArrayList<String>();
        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from "+LOBSCAB_TABLE_CHAT+" order by id desc", null );
        res.moveToNext();
        while(res.isAfterLast() == false){

            array_list.add(res.getString(res.getColumnIndex("user"))+"--__"+
                    res.getString(res.getColumnIndex("message"))+"--__"+
                    res.getString(res.getColumnIndex("date")));

            res.moveToNext();
        }
//        array_list=res.getString(res.getColumnIndex("user"))+"-"+res.getString(res.getColumnIndex("message")+"-"+res.getString(res.getColumnIndex("date"));
        return array_list;
    }

    public Integer deleteGPS (Integer id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(LOBSCAB_TABLE_NAME,
                "id = ? ",
                new String[] { Integer.toString(id) });
    }



}




