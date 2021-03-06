package com.example.findfriends;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.StrictMode;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationClickListener, LocationListener {

    private static final int MY_LOCATION_REQUEST_CODE = 1338;

    private GoogleMap mMap;
    private LocationManager locationManager;
    private boolean onezooming = false;
    private static final long MIN_TIME = 4000;
    private static final float MIN_DISTANCE = 1000;

    private DBHandler dbHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this); //You can also use LocationManager.GPS_PROVIDER and LocationManager.PASSIVE_PROVIDER
        }

        try {
            mapFragment.getMapAsync(this);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        dbHandler = new DBHandler(this, "User");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == MY_LOCATION_REQUEST_CODE) {
            if (permissions.length != 1 || permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION ) || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Please allow location!", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_LOCATION_REQUEST_CODE);
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //TODO: Locaction wird noch nicht angezeigt nach dem akzeptieren der Berechtigung, erst wenn die Activity neu aufgerufen wird
                mMap.setMyLocationEnabled(true);
                mMap.setOnMyLocationClickListener(this);

                //Reload to using new Permissions
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // TODO: Before enabling the My Location layer, you must request
            // location permission from the user.
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationClickListener(this);
        } else //No Permissions
        {
            //Request GPS Permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_LOCATION_REQUEST_CODE);
        }

        refreshLocations();
    }

    @Override
    public void onMyLocationClick(final @NonNull Location location) {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                dbHandler.refreshOwnLocation(location);
            }
        });
    }

    @Override
    public void onLocationChanged(final Location location)
    {
        if(onezooming == false)
        {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 13);
            mMap.animateCamera(cameraUpdate);
            onezooming = true;
        }


        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                dbHandler.refreshOwnLocation(location);
            }
        });

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    void refreshLocations()
    {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ArrayList<User> userlist = dbHandler.enterRoom("RaketeStart");
                final ArrayList<Marker> markerList = new ArrayList<>();

                while (true) {
                    try { Thread.sleep(5000); } catch (InterruptedException e) { e.printStackTrace(); }

                    final ArrayList<UserLocation> locations = dbHandler.getLocations(userlist);


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for(Marker m: markerList)
                                m.remove();

                            markerList.clear();


                            for(final UserLocation location : locations)
                            {
                                if(location == null) return;


                                MarkerOptions markerOptions = new MarkerOptions();
                                // Setting the position for the marker
                                markerOptions.position(new LatLng(location.latitude, location.longitude));
                                markerOptions.flat(true);
                                //markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.new_mark));

                                // Setting a new infowindow for every station
                                //CustomInfoWindow customInfoWindow = new CustomInfoWindow(this);
                                //mMap.setInfoWindowAdapter(customInfoWindow);

                                Marker m = mMap.addMarker(markerOptions);
                                m.setTitle(location.UID);
                                m.setTag(location);
                                markerList.add(m);
                            }
                        }
                    });
                }
            }
        });
    }
}




