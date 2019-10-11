package com.lobscab.icode.lobscab;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;

import android.os.AsyncTask;
import android.os.Bundle;

import android.provider.Settings;
import android.support.v4.app.ActivityCompat;

import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

//import com.google.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
//import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CALL_PHONE;

public class MapsNavActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        LocationListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnInfoWindowLongClickListener,
        GoogleMap.InfoWindowAdapter,
        ClusterManager.OnClusterItemClickListener<Person>,
         ClusterManager.OnClusterInfoWindowClickListener<Person>,
        ClusterManager.OnClusterClickListener<Person>,
        ClusterManager.OnClusterItemInfoWindowClickListener<Person>{


    private GoogleMap mMap;

    protected LocationManager locationManager;
    protected LocationListener locationListener;
    protected Context context;
    ArrayList<HashMap<String, String>> driverList;
static int Resume=1;
    String provider;
    private static final int MY_PERMISSIONS_CALL_PHONE = 1005;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1001;

    DatabaseReference nDrivers;
    static String gpsLat, gpsLog;

    static int movement = 0;
    Marker mPerth, onChange;
    String towers,towersNetwork;
    Location locLastKnown,locLastKnownNetwork,locNet;
    Double getLat, getLog;
    Marker lst,placeMarker;
    private View mProgressView;
    private View mRegMapView;
    public static String rplateNum, ruser;
    SessionManagement session;
    private FirebaseAuth auth;
    public static int placeUsed=1;
    public int initial_zoom=15;
String num,carUser,carPlt;
    private AdView adView;
    static boolean justStarted=true;
    FirebaseRemoteConfig remoteConfig;
    private ClusterManager<Person> mClusterManager;
    private Random mRandom = new Random(1984);
   static List<String> lstArr = new ArrayList<String>();
    boolean zoom_control=true,keepSyn=false,isNetworkEnabled;
    ProgressDialog pd;
    DBHelper dbHelper;
    DrawerLayout drawer;
    DatabaseReference nRootRef= FirebaseDatabase.getInstance().getReference();
    DatabaseReference dblobscab=nRootRef.child("lobscab");
    DatabaseReference nriders=dblobscab.child("riders");
    String phone="";
    sendRegTask mAuthTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper=new DBHelper(this);
        remoteConfig=FirebaseRemoteConfig.getInstance();
        //  FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        auth = FirebaseAuth.getInstance();
        session = new SessionManagement(MapsNavActivity.this);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

         nRootRef = FirebaseDatabase.getInstance().getReference();
         dblobscab = nRootRef.child("lobscab");

         nDrivers = dblobscab.child("drivers");


        remoteConfig.setConfigSettings(new FirebaseRemoteConfigSettings.Builder().setDeveloperModeEnabled(true).build());
        HashMap<String,Object> defaults=new HashMap<>();
        defaults.put("initial_zoom",15);
        defaults.put("zoom_control",false);
        defaults.put("keepSyn",true);
        remoteConfig.setDefaults(defaults);
        final Task<Void> fetch=remoteConfig.fetch(0);
        fetch.addOnSuccessListener(this, new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                remoteConfig.activateFetched();
                initial_zoom= (int)remoteConfig.getLong("initial_zoom");
                zoom_control= remoteConfig.getBoolean("zoom_control");
                keepSyn= remoteConfig.getBoolean("keepSyn");

            }
        });

        checkGps();

        setContentView(R.layout.activity_map_nav);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        nDrivers.keepSynced(keepSyn);
//        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
//        toolbar.setBackground(new ColorDrawable(Color.TRANSPARENT));
//        actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#550000ff")));
//        toolbar.setAlpha(0);
        setSupportActionBar(toolbar);

        String id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        MobileAds.initialize(this, "ca-app-pub-5527620381143347~2094474910");
        adView = (AdView) findViewById(R.id.adView);

        AdRequest adRequest = new AdRequest.Builder().build();

        adView.loadAd(adRequest);

        pd = new ProgressDialog(MapsNavActivity.this);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);

       // mProgressView = headerView.findViewById(R.id.cab_map_progress);

        mRegMapView = headerView;

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_nav);
        mapFragment.getMapAsync(this);


         drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        Criteria crit = new Criteria();
        towers = locationManager.getBestProvider(crit, false);
        locLastKnown = locationManager.getLastKnownLocation(towers);

        PlaceAutocompleteFragment places = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        AutocompleteFilter autocompleteFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(Place.TYPE_COUNTRY)
                .setCountry("NG")
                .build();

        places.setFilter(autocompleteFilter);


        try {
            Thread.sleep(500);
        } catch (Exception e) {
        }
        ruser = auth.getCurrentUser().getEmail()
                .replace(".", "_")
                .replace("#", "_")
                .replace("$", "_")
                .replace("[", "_")
                .replace("]", "_")
                .split("@")[0];

        session.set("uniqueUsername",ruser);
        DatabaseReference newrider = nriders.child(ruser);
        DatabaseReference dbriderPhone = newrider.child("Phone");

//Register Token
        mAuthTask = new sendRegTask();
        mAuthTask.execute((Void) null);
        dbriderPhone.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    phone = dataSnapshot.getValue().toString();
                    session.set("num",phone);
                    try{
                    if(dbHelper.numberOfRowsReg()<1) {
                        dbHelper.insertReg(auth.getCurrentUser().getEmail(), "", 1, phone);
                    }else{
                        dbHelper.updateReg(auth.getCurrentUser().getEmail(), phone);
                    }}catch (Exception ex){}

                }catch (Exception er){}
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        try {
            Thread.sleep(500);
        } catch (Exception e) {
        }


        TextView driverUser = (TextView) headerView.findViewById(R.id.driver_user);
        driverUser.setText(ruser);


        places.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                String locationPlace = place.getName().toString();
                List<Address> addressList = null;

                if (locationPlace != null || !locationPlace.equals("")) {

                    Geocoder geocoder = new Geocoder(MapsNavActivity.this);
                    try {
                        addressList = geocoder.getFromLocationName(locationPlace, 1);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (!addressList.equals(null) && !addressList.equals("")) {
                        if (onChange != null) {
                            onChange.remove();
                        }
                        if (placeMarker != null) {
                            placeMarker.remove();
                        }

                        Address address = addressList.get(0);
                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                        placeMarker= mMap.addMarker(new MarkerOptions().position(latLng).title(ruser).snippet("Current Location")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_current_loc)));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,initial_zoom));
                        placeUsed=2;

                    }
                }


            }

            @Override
            public void onError(Status status) {

                Toast.makeText(getApplicationContext(),status.toString(),Toast.LENGTH_SHORT).show();

            }
        });


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        try {
            LatLng sydneyDB = new LatLng(
                    Double.parseDouble(session.get("gpsLat")),
                    Double.parseDouble(session.get("gpsLat")));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydneyDB, 10));

        } catch (Exception r) {
            //Toast.makeText(MapsNavActivity.this, r.toString(), Toast.LENGTH_SHORT).show();
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if(justStarted){
            if ( mPerth!= null) {
                mPerth.remove();
            }
            try {
                nDrivers.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                         // Result will be holded Here
                        int i = 0, indexi = 0;
                        int loopi = 0;
                        for (DataSnapshot dsp : dataSnapshot.getChildren()) {

                            String username = "", phone = "", driverGpsLat = "", driverGpsLog = "", plate = "", carDesc = "",
                                    verify = "true";
                            i++;
                            String key = dsp.getKey();
                            String val = dsp.getValue().toString();
                            String actualVal = val.replace("{", "").replace("}", "");
                            String[] separatedActual = actualVal.split(",");
                            for (int uval = 0; uval < separatedActual.length; uval++) {
                                String[] actValArr = separatedActual[uval].split("=");
                                String keyVal1 = actValArr[0];
                                String val1 = actValArr[actValArr.length - 1];

                                if (keyVal1.trim().equals("GpsLat")) {
                                    driverGpsLat = val1;
                                    continue;
                                } else if (keyVal1.trim().equals("GpsLog")) {
                                    driverGpsLog = val1;
                                    continue;
                                } else if (keyVal1.trim().equals("Full Name")) {
                                    username = val1;

                                    continue;
                                } else if (keyVal1.trim().equals("Plate Number")) {
                                    plate = val1;
                                    continue;
                                } else if (keyVal1.trim().equals("Phone Number")) {
                                    phone = val1;
                                    continue;
                                } else if (keyVal1.trim().equals("Cab Description")) {
                                    carDesc = val1;
                                    continue;
                                } else if (keyVal1.trim().equals("Verification")) {
                                    verify = val1;
                                    if (verify.equals("false")) {
                                        break;
                                    }
                                }

                            }
                            double gpsLatDrv, gpsLogDrv;
                            try {
                                gpsLatDrv = Double.parseDouble(driverGpsLat);
                                gpsLogDrv = Double.parseDouble(driverGpsLog);
                            } catch (Exception doubleExp) {
                                gpsLatDrv = 0.0;
                                gpsLogDrv = 0.0;
                            }
                            if (gpsLatDrv == 0.0 || gpsLogDrv == 0.0 || username.equals("")) {
                                continue;
                            }
                            //  if (!(driverList.contains(mapList.get("Plate Number")))){
                            LatLng PERTH = new LatLng(gpsLatDrv, gpsLogDrv);

                            if (verify.equals("true") && !lstArr.contains(plate)) {

                                mClusterManager.addItem(new Person(PERTH,username, R.drawable.logo_sixr,plate + ","+phone+ "," + carDesc,username));
                                loopi = loopi + 1;
                                lstArr.add(plate);

                            }
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                justStarted=false;
            } catch (Exception t) {
                justStarted=true;
            }

        }
    if(locationManager.isProviderEnabled(locationManager.GPS_PROVIDER)){


    if ( locLastKnown!= null) {

        LatLng sydney = new LatLng(locLastKnown.getLatitude(), locLastKnown.getLongitude());
        String strgetLatitude=Double.toString(locLastKnown.getLatitude()),
                strgetLongitude=Double.toString(locLastKnown.getLongitude());

        try {
            if(dbHelper.numberOfRows()>0) {
                if (!strgetLatitude.equals("") && !strgetLongitude.equals("") &&(!strgetLatitude.isEmpty() && !strgetLongitude.isEmpty())) {
                    dbHelper.updateGPSByUser(strgetLatitude, strgetLongitude);
                    session.set("gpsLat",strgetLatitude);
                    session.set("gpsLng",strgetLongitude);
                }

            }else {
                if (!strgetLatitude.equals("") && !strgetLongitude.equals("") &&(!strgetLatitude.isEmpty() && !strgetLongitude.isEmpty())) {
                    dbHelper.insertGPS(strgetLatitude, strgetLongitude);
                    session.set("gpsLat",strgetLatitude);
                    session.set("gpsLng",strgetLongitude);
                }

            }
        }catch (Exception rew){
            if (!strgetLatitude.equals("") && !strgetLongitude.equals("") &&(!strgetLatitude.isEmpty() && !strgetLongitude.isEmpty())) {
                dbHelper.insertGPS(strgetLatitude, strgetLongitude);
                session.set("gpsLat",strgetLatitude);
                session.set("gpsLng",strgetLongitude);
            }
        }

        lst = mMap.addMarker(new MarkerOptions().position(sydney).title(ruser)
                .snippet("this is me").visible(false));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, initial_zoom));

    }else{ try {
        LatLng sydneyDB = new LatLng(
                Double.parseDouble(session.get("gpsLat")),
                Double.parseDouble(session.get("gpsLat")));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydneyDB, 10));
    }catch (Exception r){}
    }
    }else{ try{
        LatLng sydneyDB = new LatLng(
                Double.parseDouble(session.get("gpsLat")),
                Double.parseDouble(session.get("gpsLat")));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydneyDB, 10));
    }catch (Exception r){}
    }
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.setMyLocationEnabled(true);
        mMap.setInfoWindowAdapter(this);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnInfoWindowLongClickListener(this);

        startDemo();
        mMap.setMaxZoomPreference(20);
        mMap.setMinZoomPreference(10);
        locationManager.requestLocationUpdates(towers,0, 0, this);

    }

    @Override
    public void onLocationChanged(Location locat) {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

            if(justStarted){
                if ( mPerth!= null) {
                    mPerth.remove();
                }
            //    if(mClusterManager!=null){mClusterManager.clearItems();}
//                try {
//                    dblobscab.child("drivers").addValueEventListener(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot dataSnapshot) {
//                            List<String> lst = new ArrayList<String>(); // Result will be holded Here
//                            int i = 0, indexi = 0;
//                            HashMap<String, String> mapList = new HashMap<String, String>();
//                            HashMap<String, String> mapListcheck = new HashMap<String, String>();
//                            long df = dataSnapshot.getChildrenCount();
//                            String dist = "";
//                            double distance = 0.0;
//
//                            int loopi = 0;
//                            for (DataSnapshot dsp : dataSnapshot.getChildren()) {
//                                String username = "", phone = "", driverGpsLat = "", driverGpsLog = "", plate = "", carDesc = "",
//                                        verify = "true";
//                                i++;
//                                String key = dsp.getKey();
//                                String val = dsp.getValue().toString();
//                                String actualVal = val.replace("{", "").replace("}", "");
//                                String[] separatedActual = actualVal.split(",");
//                                for (int uval = 0; uval < separatedActual.length; uval++) {
//                                    String[] actValArr = separatedActual[uval].split("=");
//                                    String keyVal1 = actValArr[0];
//                                    String val1 = actValArr[actValArr.length - 1];
//
//                                    if (keyVal1.trim().equals("GpsLat")) {
//                                        driverGpsLat = val1;
//                                        continue;
//                                    } else if (keyVal1.trim().equals("GpsLog")) {
//                                        driverGpsLog = val1;
//                                        continue;
//                                    } else if (keyVal1.trim().equals("Full Name")) {
//                                        username = val1;
//
//                                        continue;
//                                    } else if (keyVal1.trim().equals("Plate Number")) {
//                                        plate = val1;
//                                        continue;
//                                    } else if (keyVal1.trim().equals("Phone Number")) {
//                                        phone = val1;
//                                        continue;
//                                    } else if (keyVal1.trim().equals("Cab Description")) {
//                                        carDesc = val1;
//                                        continue;
//                                    } else if (keyVal1.trim().equals("Verification")) {
//                                        verify = val1;
//                                        if (verify.equals("false")) {
//                                            break;
//                                        }
//                                    }
//
//                                }
//                                double gpsLatDrv, gpsLogDrv;
//                                try {
//                                    gpsLatDrv = Double.parseDouble(driverGpsLat);
//                                    gpsLogDrv = Double.parseDouble(driverGpsLog);
//                                } catch (Exception doubleExp) {
//                                    gpsLatDrv = 0.0;
//                                    gpsLogDrv = 0.0;
//                                }
//                                if (gpsLatDrv == 0.0 || gpsLogDrv == 0.0 || username.equals("")) {
//                                    continue;
//                                }
//                                //  if (!(driverList.contains(mapList.get("Plate Number")))){
//                                LatLng PERTH = new LatLng(gpsLatDrv, gpsLogDrv);
//
//                                if (verify.equals("true")) {
////                                    mPerth = mMap.addMarker(new MarkerOptions()
////                                            .position(PERTH)
////                                            .title(username)
////                                            .snippet(plate + "," + phone + "," + carDesc)
////                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.locate_car)));
////                                    mPerth.setTag(loopi);
//
//
////                                    mClusterManager.addItem(new Person(PERTH,"Benin", R.drawable.logo_sixr));
//
//                                    loopi = loopi + 1;
//                                }
//                            }
//
//
//                        }
//
//                        @Override
//                        public void onCancelled(DatabaseError databaseError) {
//
//                        }
//                    });
//                    justStarted=false;
//                } catch (Exception t) {
//                    justStarted=true;
//                }

            }
        if (locat != null) {
            getLat = locat.getLatitude();
            getLog = locat.getLongitude();
            String strGetLat=Double.toString(getLat),strGetLng=Double.toString(getLog);
            LatLng newLoc = new LatLng(getLat, getLog);
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                try {
                    if(dbHelper.numberOfRows()>0){
                        if(!strGetLat.equals("") && !strGetLng.equals("") && (!strGetLat.isEmpty() && !strGetLng.isEmpty() )) {
                            dbHelper.updateGPSByUser(strGetLat, strGetLng);
                            session.set("gpsLat",strGetLat);
                            session.set("gpsLng",strGetLng);
                        }

                    }else{
                        if(!strGetLat.equals("") && !strGetLng.equals("") && (!strGetLat.isEmpty() && !strGetLng.isEmpty() )) {
                            dbHelper.insertGPS(strGetLat, strGetLng);
                            session.set("gpsLat",strGetLat);
                            session.set("gpsLng",strGetLng);
                        }
                    }
                }catch (Exception rew){
                    if(!strGetLat.equals("") && !strGetLng.equals("") && (!strGetLat.isEmpty() && !strGetLng.isEmpty() )) {
                        dbHelper.insertGPS(strGetLat, strGetLng);
                        session.set("gpsLat",strGetLat);
                        session.set("gpsLng",strGetLng);
                    }
                }
                if (lst != null) {
                    lst.remove();
                }
                if (onChange != null) {
                    onChange.remove();
                }
                onChange = mMap.addMarker(new MarkerOptions().position(newLoc).title(ruser)
                        .snippet("this  is me").visible(false));
                if(movement==0 || movement==120 ){
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && placeUsed == 1) {
                    try {
                        if(dbHelper.numberOfRows()>0){
                            if(!strGetLat.equals("") && !strGetLng.equals("") && (!strGetLat.isEmpty() && !strGetLng.isEmpty() )) {
                                dbHelper.updateGPSByUser(strGetLat, strGetLng);
                                session.set("gpsLat", strGetLat);
                                session.set("gpsLng", strGetLng);
                            }
                        }else{
                            if(!strGetLat.equals("") && !strGetLng.equals("") && (!strGetLat.isEmpty() && !strGetLng.isEmpty() )) {
                                dbHelper.insertGPS(strGetLat, strGetLng);
                                session.set("gpsLat", strGetLat);
                                session.set("gpsLng", strGetLng);
                            }
                        }
                    }catch (Exception rew){
                        if(!strGetLat.equals("") && !strGetLng.equals("") && (!strGetLat.isEmpty() && !strGetLng.isEmpty() )) {
                            dbHelper.insertGPS(strGetLat, strGetLng);
                            session.set("gpsLat", strGetLat);
                            session.set("gpsLng", strGetLng);
                        }
                    }
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLoc, initial_zoom));
                }
                if (movement==120){movement=0;}

            }
            movement=movement+1;
            }else{ try{
                LatLng sydneyDB = new LatLng(
                        Double.parseDouble(session.get("gpsLat")),
                        Double.parseDouble(session.get("gpsLat")));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydneyDB, 10));
                
            }catch (Exception r){}
            }
//            else if(isNetworkEnabled && locat==null){
//                locationManager.requestLocationUpdates(
//                        LocationManager.NETWORK_PROVIDER,
//                        500,
//                        1, this);
//
//                if (locationManager != null) {
//                    if (onChange != null) {
//                        onChange.remove();
//                    }
//
//                    locNet  = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//
//                    if (locNet != null) {
//                        LatLng newLocNet = new LatLng(locNet.getLatitude(), locNet.getLongitude());
//                        onChange = mMap.addMarker(new MarkerOptions().position(newLocNet).title(ruser)
//                                .snippet("this  is me").visible(false));
//                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLocNet, 10));
//                    }
//                }
//
//            }

        }else{ try{
            LatLng sydneyDB = new LatLng(
                    Double.parseDouble(session.get("gpsLat")),
                    Double.parseDouble(session.get("gpsLat")));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydneyDB, 10));
        }catch (Exception r){}
        }
        mMap.setMaxZoomPreference(20);
        mMap.setMinZoomPreference(10);
        locationManager.requestLocationUpdates(towers,0, 0, this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.nav, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
          if (id == R.id.nav_profile) {

              startActivity(new Intent(MapsNavActivity.this, Profile.class));
             // startActivity(new Intent(MapsNavActivity.this, Profile.class));
        }else if (id == R.id.nav_wallet) {
//              startActivity(new Intent(MapsNavActivity.this, Profile.class));
              AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
              alertDialogBuilder
                      .setMessage("Card payment will be available soon!")
                      .setCancelable(true)
                      .setPositiveButton("Ok",
                              new DialogInterface.OnClickListener() {
                                  public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                  }
                              });
              AlertDialog alertDialog = alertDialogBuilder.create();
              alertDialog.show();
          }
          else if (id == R.id.nav_exit) {
              exitCode();
          }
          else if (id == R.id.nav_about) {
              startActivity(new Intent(MapsNavActivity.this, About.class));
        }else if (id == R.id.nav_rate) {
              rating();

          }


        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        justStarted=true;
        if (adView!=null){
            adView.resume();
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            showGPSAlertToUser();
        }
            locationManager.requestLocationUpdates(towers,500, 1, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.removeUpdates(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
      //  if(locationManager!=null){
            locationManager.removeUpdates(this);
      //  }
    }

    public void setUpMap() {

        getMap().setMapType(GoogleMap.MAP_TYPE_HYBRID);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        getMap().setMyLocationEnabled(true);
        getMap().setTrafficEnabled(true);
        getMap().setIndoorEnabled(true);
        getMap().setBuildingsEnabled(true);
        getMap().getUiSettings().setZoomControlsEnabled(true);
    }


    public void signOut() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Logout from LobsCab?");
        alertDialogBuilder
                .setMessage("Click yes to logout!")
                .setCancelable(true)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                auth.signOut();session.logoutUser();
                                finish();
                            }
                        })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void exitCode(){
    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
    alertDialogBuilder.setTitle("Exit LobsCab?");
    alertDialogBuilder
            .setMessage("Click yes to exit!")
            .setCancelable(true)
            .setPositiveButton("Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            finish();
                            // moveTaskToBack(true);
                            // android.os.Process.killProcess(android.os.Process.myPid());
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
}

    private void showGPSAlertToUser(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsNavActivity.this);
        builder.setTitle(" LobsCab GPS ACTIVATION");
        builder.setMessage("GPS is disabled in your device, Goto Settings Page To Enable GPS")
                .setCancelable(true)
                .setPositiveButton("OK",  new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try{
                            Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
                            intent.putExtra("enabled", true);
                            sendBroadcast(intent);
                        }catch(Exception rex){
                            Intent callGPSSettingIntent = new Intent(
                                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(callGPSSettingIntent);
                        }
                        try{
                            String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
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
                        }
                    }
                });
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){

                     if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || dbHelper.numberOfRows()>0){dialog.cancel();}
                                else{
                            finish();
                            System.exit(0);
                        }
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

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
    public void onInfoWindowClick(Marker marker) {
      return;
    }

    @Override
    public void onInfoWindowLongClick(Marker marker) {
       return;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
//        return null;
        return prepareInfoView(marker);
    }

    private View prepareInfoView(Marker marker){
        //prepare InfoView programmatically
        LinearLayout infoView = new LinearLayout(MapsNavActivity.this);
        LinearLayout.LayoutParams infoViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        infoView.setOrientation(LinearLayout.HORIZONTAL);
        infoView.setLayoutParams(infoViewParams);

        ImageView infoImageView = new ImageView(MapsNavActivity.this);
        //Drawable drawable = getResources().getDrawable(R.mipmap.ic_launcher);
        Drawable drawable = getResources().getDrawable(R.drawable.logo_sixr);
        infoImageView.setImageDrawable(drawable);
        infoView.addView(infoImageView);

        LinearLayout subInfoView = new LinearLayout(MapsNavActivity.this);
        LinearLayout.LayoutParams subInfoViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        subInfoView.setOrientation(LinearLayout.VERTICAL);
        subInfoView.setLayoutParams(subInfoViewParams);
 String username,plate,phone,car;
        username=marker.getTitle();
        String[] str=marker.getSnippet().toString().split(",");
        plate=str[0].toUpperCase();
        car=str[str.length-1];
        //phone=str[str.length-2];

        TextView name = new TextView(MapsNavActivity.this);

        name.setText("Name: " + username);

        TextView plateText = new TextView(MapsNavActivity.this);
        plateText.setText("Plate Num: " + plate);
//        TextView phoneText = new TextView(MapsNavActivity.this);
//        phoneText.setText("Phone: " + phone);
        TextView carText = new TextView(MapsNavActivity.this);
        carText.setText("Car: " + car);
        subInfoView.addView(name);
        subInfoView.addView(plateText);
//        subInfoView.addView(phoneText);
        subInfoView.addView(carText);

        infoView.addView(subInfoView);

        return infoView;
    }



    private class PersonRenderer extends DefaultClusterRenderer<Person> {
        private final IconGenerator mIconGenerator = new IconGenerator(getApplicationContext());
        private final IconGenerator mClusterIconGenerator = new IconGenerator(getApplicationContext());
        private final ImageView mImageView;
        private final ImageView mClusterImageView;
        private final int mDimension;

        public PersonRenderer() {
            super(getApplicationContext(), getMap(), mClusterManager);

            View multiProfile = getLayoutInflater().inflate(R.layout.multi_profile, null);
            mClusterIconGenerator.setContentView(multiProfile);
            mClusterImageView = (ImageView) multiProfile.findViewById(R.id.image);

            mImageView = new ImageView(getApplicationContext());
            mDimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
            mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
            int padding = (int) getResources().getDimension(R.dimen.custom_profile_padding);
            mImageView.setPadding(padding, padding, padding, padding);
            mIconGenerator.setContentView(mImageView);
        }

        @Override
        protected void onBeforeClusterItemRendered(Person person, MarkerOptions markerOptions) {
            // Draw a single person.
            // Set the info window to show their name.
            mImageView.setImageResource(person.profilePhoto);
            Bitmap icon = mIconGenerator.makeIcon();
            //write your info code here
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(person.name);
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<Person> cluster, MarkerOptions markerOptions) {
            // Draw multiple people.
            // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
            List<Drawable> profilePhotos = new ArrayList<Drawable>(Math.min(4, cluster.getSize()));
            int width = mDimension;
            int height = mDimension;

            for (Person p : cluster.getItems()) {
                // Draw 4 at most.
                if (profilePhotos.size() == 4) break;
                Drawable drawable = getResources().getDrawable(p.profilePhoto);
                drawable.setBounds(0, 0, width, height);
                profilePhotos.add(drawable);
            }
            MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
            multiDrawable.setBounds(0, 0, width, height);

            mClusterImageView.setImageDrawable(multiDrawable);
            Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 1;
        }
    }

    @Override
    public boolean onClusterClick(Cluster<Person> cluster) {
        // Show a toast with some info when the cluster is clicked.
//        String firstName = cluster.getItems().iterator().next().name;
//        Toast.makeText(this, cluster.getSize() + " (including " + firstName + ")", Toast.LENGTH_SHORT).show();

        // Zoom in the cluster. Need to create LatLngBounds and including all the cluster items
        // inside of bounds, then animate to center of the bounds.

        // Create the builder to collect all essential cluster items for the bounds.
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (ClusterItem item : cluster.getItems()) {
            builder.include(item.getPosition());
        }
        // Get the LatLngBounds
        final LatLngBounds bounds = builder.build();

        // Animate camera to the bounds
        try {
            getMap().animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public void onClusterInfoWindowClick(Cluster<Person> cluster) {
//        Toast.makeText(getApplicationContext(),"onClusterInfoWindowClick",Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onClusterItemClick(Person item) {
        // Does nothing, but you could go into the user's profile page, for example.
        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(Person item) {
       markerInfoClick(item);
    }

    private void markerInfoClick(Person marker) {

                String[] phNum = marker.getSnippet().split(",");
                num=phNum[phNum.length-2];
                  carPlt=phNum[0];
                    carUser=marker.getTitle();
        if(ruser != null){
            session.set("username",ruser
                    .replace(".","_")
                .replace("#","_")
                .replace("$","_")
                .replace("[","_")
                .replace("]","_")
                .split("_")[0]);

                }


                AlertDialog.Builder builder = new AlertDialog.Builder(MapsNavActivity.this);
                builder.setTitle("Negotiate with "+marker.getTitle());
                builder.setCancelable(true)
                        .setPositiveButton(" Call ", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (ActivityCompat.checkSelfPermission(MapsNavActivity.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                    // TODO: Consider calling

                                    ActivityCompat.requestPermissions(MapsNavActivity.this, new String[]{CALL_PHONE}, MY_PERMISSIONS_CALL_PHONE);
                                    // return true;
                                }
                                Intent callIntent = new Intent(Intent.ACTION_CALL);
                                callIntent.setData(Uri.parse("tel:" + num));
                                startActivity(callIntent);

                            }
                        });
//        builder.setNegativeButton(" Negotiate ", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//                session.set("chatWithUni",carPlt);
//                session.set("chatWith",carUser);
//                try {
//                    if(dbHelper.numberOfRowsChatWit()<1){
//                        dbHelper.insertChatWit(ruser
//                                .replace(".","_")
//                                .replace("#","_")
//                                .replace("$","_")
//                                .replace("[","_")
//                                .replace("]","_")
//                                .split("_")[0],carUser,carPlt);
//                    }else{  dbHelper.updateChatWit(ruser
//                            .replace(".","_")
//                            .replace("#","_")
//                            .replace("$","_")
//                            .replace("[","_")
//                            .replace("]","_")
//                            .split("_")[0],carUser,carPlt);}
//                }catch (Exception er){
//
//                }
//                startActivity(new Intent(MapsNavActivity.this,Chat.class));
//
//            }
//        });
//        setNeutralButton

        builder.setNegativeButton(" Rate Driver", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                session.set("plate",carPlt);
                session.set("driver",carUser);
                try {
                    if(dbHelper.numberOfRowsChatWit()<1){
                        dbHelper.insertChatWit(ruser
                                .replace(".","_")
                                .replace("#","_")
                                .replace("$","_")
                                .replace("[","_")
                                .replace("]","_")
                                .split("_")[0],carUser,carPlt);
                    }else{  dbHelper.updateChatWit(ruser
                            .replace(".","_")
                            .replace("#","_")
                            .replace("$","_")
                            .replace("[","_")
                            .replace("]","_")
                            .split("_")[0],carUser,carPlt);}
                }catch (Exception er){

                }
                startActivity(new Intent(MapsNavActivity.this,Rate.class));
            }
        });
//        builder.setNegativeButton(" Free Call ", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//                //free call here
//
//            }
//        });

//                builder.setNegativeButton("Cancel",
//                        new DialogInterface.OnClickListener(){
//                            public void onClick(DialogInterface dialog, int id){
//                                dialog.cancel();
//                            }
//                        });
                AlertDialog alert = builder.create();
                alert.show();


                //}
            }

    //    @Override
    protected void startDemo() {
        mClusterManager = new ClusterManager<Person>(this, getMap());
        mClusterManager.setRenderer(new PersonRenderer());
        getMap().setOnCameraIdleListener(mClusterManager);
        getMap().setOnMarkerClickListener(mClusterManager);
        getMap().setOnInfoWindowClickListener(mClusterManager);
        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterInfoWindowClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setOnClusterItemInfoWindowClickListener(this);
        try {
            Thread.sleep(500);
        } catch (Exception e) {
        }
        mClusterManager.cluster();
    }


    protected GoogleMap getMap() {
        return mMap;
    }

    public void checkGps(){
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(ActivityCompat.checkSelfPermission(MapsNavActivity.this,ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED&&ActivityCompat.checkSelfPermission(MapsNavActivity.this,android.Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
        {

            ActivityCompat.requestPermissions(MapsNavActivity.this, new String[]{ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);

        }else {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                showGPSAlertToUser();
            }
            else if (ActivityCompat.checkSelfPermission(MapsNavActivity.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling

                ActivityCompat.requestPermissions(MapsNavActivity.this, new String[]{CALL_PHONE}, MY_PERMISSIONS_CALL_PHONE);
                // return true;
            }

        }
        //return;
    }


    public void rating(){
        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
        Intent intent=null;
        try {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName));

        } catch (android.content.ActivityNotFoundException anfe) {
            intent= new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));
        }
        if(intent!=null)
            startActivity(intent);
    }


    public class sendRegTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody body = new FormBody.Builder()
                        .add("Token",session.get("Token"))
                        .add("User",ruser)
                        .build();

                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url("http://donationearners.net/cab_token/lobscab_tokenphp.php")
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
                Toast.makeText(MapsNavActivity.this, "Success Sent", Toast.LENGTH_SHORT).show();
            } else {

                Toast.makeText(MapsNavActivity.this, "Failed Sent", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled () {
            mAuthTask = null;

        }
    }
}
