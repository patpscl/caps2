package com.parkpal;


import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parkpal.classes.BackgroundDetectedActivitiesService;
import com.parkpal.classes.Constants;
import com.parkpal.classes.ParkingLocations;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;


public class MapFragment extends Fragment implements OnMapReadyCallback{

    GoogleMap mMap;
    MapView mMapView;
    View mView;
    private Marker myMarker;
    boolean isInVehicle;
    boolean isInsideParking;
    private static final int MY_PERMISSION_REQUEST_CODE = 7192;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 300193;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static int UPDATE_INTERVAL = 500;
    private static int FASTEST_INTERVAL = 300;
    private static int DISPLACEMENT = 10;

    DatabaseReference geoRef;
    FirebaseUser currentFirebaseUser;
    DatabaseReference parkingRef;
    GeoFire geoFire;
    Marker mCurrent;
    long tStart;
    long tEnd;
    long tDelta;
    double elapsedSeconds;
    String currentParkID;
    private Double latitude;
    private Double longtitude;
    private String parkName;
    private String circlingTime;

    /////FusedLocationProviderClient
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int MY_PERMISSION_REQUEST_FINE_LOCATION = 101;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private boolean updatesOn = false;


    ///////////////////////ACTIVITY RECOGNITION///////////////////////////
    private String TAG = MainActivity.class.getSimpleName();
    BroadcastReceiver broadcastReceiver;
    ////////////////////////////////////////////////////////////
    public MapFragment() {

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpLocation();

        geoRef = FirebaseDatabase.getInstance().getReference("userLocation");
        geoFire = new GeoFire(geoRef);
        currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
        parkingRef = FirebaseDatabase.getInstance().getReference("parkingLocations");

        //activity recog
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Constants.BROADCAST_DETECTED_ACTIVITY)) {
                    int type = intent.getIntExtra("type", -1);
                    int confidence = intent.getIntExtra("confidence", 0);
                    handleUserActivity(type, confidence);
                }
            }
        };







    }
    private void handleUserActivity(int type, int confidence) {
        String label = getString(R.string.activity_unknown);
        //int icon = R.drawable.ic_still;

        switch (type) {
            case DetectedActivity.IN_VEHICLE: {
                label = getString(R.string.activity_in_vehicle);
                //icon = R.drawable.ic_driving;
                String text = "User Activity: "+label+"\nConfidence: "+confidence;
                Toast.makeText(getActivity(),
                        text,
                        Toast.LENGTH_SHORT)
                        .show();
                isInVehicle = true;
                break;
            }
            case DetectedActivity.ON_FOOT: {
                label = getString(R.string.activity_on_foot);

                //icon = R.drawable.ic_walking;
                //String text = "User Activity: "+label+"\nConfidence: "+confidence;

                if(isInsideParking)
                {
                    tEnd  = System.currentTimeMillis();
                    tDelta = tEnd - tStart;
                    elapsedSeconds = tDelta / 1000.0;
                    Toast.makeText(getActivity(),
                            "Nakapark ka na"+elapsedSeconds,
                            Toast.LENGTH_SHORT)
                            .show();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("parkingLocations");
                    ref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                                if(String.valueOf(dsp.child("parkingID").getValue(String.class)).equals(currentParkID))
                                {
                                    DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("parkingLocations").child(dsp.getKey()).child("density");
                                    ref1.child(currentFirebaseUser.getUid()).setValue(elapsedSeconds);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
                break;
            }
            case DetectedActivity.STILL: {
                label = getString(R.string.activity_still);
                String text = "User Activity: "+label+"\nConfidence: "+confidence;
                Toast.makeText(getActivity(),
                        text,
                        Toast.LENGTH_SHORT)
                        .show();
                break;
            }
            case DetectedActivity.UNKNOWN: {
                label = getString(R.string.activity_unknown);
                String text = "User Activity: "+label+"\nConfidence: "+confidence;
                Toast.makeText(getActivity(),
                        text,
                        Toast.LENGTH_SHORT)
                        .show();
                break;
            }
        }
        Log.e(TAG, "User activity: " + label + ", Confidence: " + confidence);
        if (confidence > Constants.CONFIDENCE) {
            String text = "User Activity: "+label+"\nConfidence: "+confidence;
            Toast.makeText(getActivity(),
                    text,
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }
    public void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(), new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION

            }, MY_PERMISSION_REQUEST_CODE);
            setUpLocation();
        } else {
            createLocationRequest();
            displayLocation();
            startLocationUpdates();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        createLocationRequest();
                        displayLocation();
                        startLocationUpdates();
                }
                break;
        }
    }
    private void displayLocation() {
        //fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        mLastLocation = location;
                        final double latitute = mLastLocation.getLatitude();
                        final double longtitute = mLastLocation.getLongitude();

                        geoFire.setLocation(currentFirebaseUser.getUid(), new GeoLocation(latitute, longtitute),
                                new GeoFire.CompletionListener() {
                                    @Override
                                    public void onComplete(String key, DatabaseError error) {
                                        mCurrent = mMap.addMarker(new MarkerOptions().position(new LatLng(latitute,longtitute)).title("You").icon(BitmapDescriptorFactory.fromResource(R.drawable.man)));
                                        if(mCurrent!=null)
                                        {
                                            mCurrent.remove();
                                            mCurrent = mMap.addMarker(new MarkerOptions().position(new LatLng(latitute,longtitute)).title("You").icon(BitmapDescriptorFactory.fromResource(R.drawable.man)));
                                           // mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitute,longtitute),18.0f));
                                        }
                                        else{
                                            new MaterialStyledDialog.Builder(getActivity())
                                                    .setTitle("Oh no!").setPositiveText("Exit")
                                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                                    @Override
                                                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                                        getActivity().finish();

                                                                    }
                                                                }
                                                    ).setIcon(R.drawable.logo)

                                                    .setDescription("Cannot get your current location. Please enable location permission for ParkPal.")
                                                    .show();

                                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(14.555338, 121.023233),11.0f));
                                        }

                                    }
                                });

                        Log.d("PARKPAL", String.format("Your location was changed: %f / %f", latitute, longtitute));
                    }
                    else{
                        Toast.makeText(getActivity(),"Cannot get your location",Toast.LENGTH_LONG);
                    }
                }
            });
        }
        else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_FINE_LOCATION);
                }
            }
    }
    private void createLocationRequest() {
        /////////////////////////
        locationRequest = new LocationRequest();
        locationRequest.setInterval(3000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        /////////////////////////
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_map, container, false);

        return mView;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        SupportMapFragment fm =(SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.map1);
        fm.getMapAsync(this);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //start: night time and day time style for maps
        Calendar rightNow = Calendar.getInstance();
        int currentHour = rightNow.get(Calendar.HOUR_OF_DAY);
        boolean nighttime = false;
        int eighteen = 18;
        int six = 6;
        if (currentHour >= eighteen || currentHour <= six) {
            nighttime = true;
        }
        try {
            boolean success = false;
            if(nighttime){
                success = googleMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                                getActivity(), R.raw.nighttime));
            }
            else {
                success = googleMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                                getActivity(), R.raw.mapstyle));
            }
            if (!success) {
                Log.e("MapsActivity", "Styles parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("MapsActivity", "Can't find style. Error: ", e);
        }
        //end: night time and day time style for maps


        //set default camera to makati
        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(14.555338, 121.023233),10.0f));

        parkingRef.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot dsp : dataSnapshot.getChildren()) {

                            String parkId;
                            latitude = Double.valueOf(dsp.child("lat").getValue(Double.class));
                            longtitude = Double.valueOf(dsp.child("long").getValue(Double.class));
                            parkName  = String.valueOf(dsp.child("parkName").getValue(String.class));
                            circlingTime = String.valueOf(dsp.child("averageCirclingTime").getValue(Integer.class));
                            parkId  = String.valueOf(dsp.child("parkingID").getValue(String.class));
                            LatLng Parking = new LatLng(latitude,longtitude );

                            mMap.addCircle(new CircleOptions().center(Parking).radius(50).strokeColor(Color.parseColor("#00ff33")).fillColor(0x22025551).strokeWidth(5.0f));
                            myMarker = mMap.addMarker(new MarkerOptions().position(Parking).title(parkName).icon(BitmapDescriptorFactory.fromResource(R.drawable.commercial)));

                            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                @Override
                                public boolean onMarkerClick(Marker marker) {
                                    //Toast.makeText(getContext(), marker.getTitle(), Toast.LENGTH_SHORT).show();// display toast
                                    for (DataSnapshot dsp : dataSnapshot.getChildren()) {

                                        parkName  = String.valueOf(dsp.child("parkName").getValue(String.class));
                                        circlingTime = String.valueOf(dsp.child("averageCirclingTime").getValue(Integer.class));
                                        if(marker.getTitle().equals(parkName)){
                                            new MaterialStyledDialog
                                                    .Builder(getActivity())
                                                    .setTitle(marker.getTitle()).setPositiveText("Exit")
                                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                                    @Override
                                                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                                                    }
                                                                }
                                                    ).setIcon(R.drawable.logo)
                                                    .setDescription("Average Circling Time: " + circlingTime)
                                                    .show();
                                        }
                                    }
                                    return true;
                                }
                            });

                            //mMap.moveCamera(CameraUpdateFactory.newLatLng(Parking));
                            //mMap.animateCamera( CameraUpdateFactory.zoomTo( 20.0f ) );

                            GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(Parking.latitude,Parking.longitude),0.05f);
                            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                                @Override
                                public void onKeyEntered(String key, GeoLocation location) {
                                    currentParkID = parkId;
                                    sendNotification("PARKPAL", String.format("Entered fence",key));
                                    if(isInVehicle)
                                    {
                                        tStart = System.currentTimeMillis();
                                        isInsideParking = true;
                                    }
                                }

                                @Override
                                public void onKeyExited(String key) {
                                    isInsideParking = false;
                                    sendNotification("PARKPAL", String.format("Exited fence",key));
                                    stopTracking();

                                }

                                @Override
                                public void onKeyMoved(String key, GeoLocation location) {
                                    startTracking();
                                    sendNotification("PARKPAL", String.format("moved inside fence",key));
                                }

                                @Override
                                public void onGeoQueryReady() {

                                }

                                @Override
                                public void onGeoQueryError(DatabaseError error) {
                                    Log.e("ERROR",""+error);
                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );

    }
    private void sendNotification(String parkpal, String content) {
        Notification.Builder builder = new Notification.Builder(getActivity()).setSmallIcon(R.mipmap.ic_launcher).setContentTitle(parkpal).setContentText(content);
        NotificationManager manager = (NotificationManager)getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(getActivity(), DrawerActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(getActivity(), 0, intent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(contentIntent);
        Notification notification  = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults |= Notification.DEFAULT_SOUND;

        manager.notify(new Random().nextInt(),notification);

    }
    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    for (Location location : locationResult.getLocations()) {
                        if (location != null) {


                            mLastLocation = location;
                            final double latitute = mLastLocation.getLatitude();
                            final double longtitute = mLastLocation.getLongitude();

                            geoFire.setLocation(currentFirebaseUser.getUid(), new GeoLocation(latitute, longtitute),
                                    new GeoFire.CompletionListener() {
                                        @Override
                                        public void onComplete(String key, DatabaseError error) {
                                            //mCurrent = mMap.addMarker(new MarkerOptions().position(new LatLng(latitute,longtitute)).title("You").icon(BitmapDescriptorFactory.fromResource(R.drawable.man)));
                                            if(mCurrent!=null)
                                            {
                                                mCurrent.remove();
                                                mCurrent = mMap.addMarker(new MarkerOptions().position(new LatLng(latitute,longtitute)).title("You").icon(BitmapDescriptorFactory.fromResource(R.drawable.man)));
                                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitute,longtitute),18.0f));
                                            }
                                            else{
                                                new MaterialStyledDialog.Builder(getActivity())
                                                        .setTitle("Oh no!").setPositiveText("Exit")
                                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                                        @Override
                                                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                                            getActivity().finish();

                                                                        }
                                                                    }
                                                        ).setIcon(R.drawable.logo)

                                                        .setDescription("Cannot get your current location. Please enable location permission for ParkPal.")
                                                        .show();

                                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(14.555338, 121.023233),11.0f));
                                            }

                                        }
                                    });


                        }
                    }
                }
            };
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);

        }
        else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_FINE_LOCATION);
            }
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver,
                new IntentFilter(Constants.BROADCAST_DETECTED_ACTIVITY));
    }
    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
    }
    private void startTracking() {
        Intent intent = new Intent(getActivity(), BackgroundDetectedActivitiesService.class);
        getActivity().startService(intent);
    }
    private void stopTracking() {
        Intent intent = new Intent(getActivity(), BackgroundDetectedActivitiesService.class);
        getActivity().stopService(intent);
    }

}
