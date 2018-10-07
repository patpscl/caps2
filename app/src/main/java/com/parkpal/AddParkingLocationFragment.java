package com.parkpal;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import java.sql.Time;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class AddParkingLocationFragment extends Fragment implements OnMapReadyCallback {
    MapView mapView;
    GoogleMap map;
    LatLng addressCoord;
    String address;
    String timeFrom;
    String timeTo;
    Button confirm;
    Float initialAmt;
    Float consecAmt;
    Marker marker;
    Bundle bundle;
    private DatabaseReference myRef;
    private FirebaseDatabase mFirebaseDatabase;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        View v = inflater.inflate(R.layout.fragment_addparkinglocation, container, false);
        mapView = (MapView) v.findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this); //this is important

        bundle = getArguments();
        if(bundle != null)
        {
            addressCoord= getLatLongFromGivenAddress(bundle.getString("address"));
            address = bundle.getString("address");
            timeFrom = bundle.getString("timeFrom");
            timeTo = bundle.getString("timeTo");
            consecAmt = Float.parseFloat(bundle.getString("consecAmt"));
            initialAmt = Float.parseFloat(bundle.getString("initalAmt"));
        }

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        confirm = (Button) view.findViewById(R.id.confirm);
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        myRef = mFirebaseDatabase.getReference();
        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentFirebaseUser.getUid();
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myRef.child("privateParking").child(uid).setValue("true");
                String key =  mFirebaseDatabase.getReference("privateParking").child(uid).push().getKey();
                myRef.child("privateParking").child(uid).child(key).child("availabilityFrom").setValue(timeFrom);
                myRef.child("privateParking").child(uid).child(key).child("availabilityTo").setValue(timeTo);
                myRef.child("privateParking").child(uid).child(key).child("initalAmt").setValue(initialAmt);
                myRef.child("privateParking").child(uid).child(key).child("consecAmt").setValue(consecAmt);
                myRef.child("privateParking").child(uid).child(key).child("address").setValue(address);
                myRef.child("privateParking").child(uid).child(key).child("lat").setValue(marker.getPosition().latitude);
                myRef.child("privateParking").child(uid).child(key).child("long").setValue(marker.getPosition().longitude);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.mainContent, new ManageFragment())
                        .commit();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setZoomControlsEnabled(true);


        map.moveCamera(CameraUpdateFactory.newLatLngZoom(addressCoord, 20.0f));

        marker=map.addMarker(new MarkerOptions().position(addressCoord)
                .title("Draggable Marker")
                .snippet("Long press and move the marker if needed.")
                .draggable(true));
        map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

            @Override
            public void onMarkerDrag(Marker arg0) {
                // TODO Auto-generated method stub
                Log.d("Marker", "Dragging");
            }

            @Override
            public void onMarkerDragEnd(Marker arg0) {
                // TODO Auto-generated method stub
                LatLng markerLocation = marker.getPosition();
                Toast.makeText(getActivity(), markerLocation.toString(), Toast.LENGTH_LONG).show();
                Log.d("Marker", "finished");
            }

            @Override
            public void onMarkerDragStart(Marker arg0) {
                // TODO Auto-generated method stub
                Log.d("Marker", "Started");

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    public static LatLng getLatLongFromGivenAddress(String youraddress) {

        double lat = 0;
        double lng = 0;
        youraddress = youraddress.replace(' ','+');

        String uri = "https://maps.google.com/maps/api/geocode/json?address=" +
                youraddress + "&key=AIzaSyB1dmPRrmswb2QFmKM6D5YEI3TqQPM0jiQ";
        HttpGet httpGet = new HttpGet(uri);
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(stringBuilder.toString());

            lng = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                    .getJSONObject("geometry").getJSONObject("location")
                    .getDouble("lng");

            lat = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                    .getJSONObject("geometry").getJSONObject("location")
                    .getDouble("lat");


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new LatLng(lat, lng);

    }
}
