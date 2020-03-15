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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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

    private FAFDatabase faf = new FAFDatabase();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        //requestPermission();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this); //You can also use LocationManager.GPS_PROVIDER and LocationManager.PASSIVE_PROVIDER
        }

        try {
            mapFragment.getMapAsync(this);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        AsyncTask.execute(new Runnable() {
            ArrayList<User> userlist;
            String android_id = Settings.Secure.getString(MainActivity.this.getContentResolver(), Settings.Secure.ANDROID_ID);

            @Override
            public void run() {

                faf.connect("meiner.ml", 2345);
                String ans = faf.get("RaketeStart");
                userlist = deserialize(ans);
                if(userlist == null) userlist = new ArrayList<>();

                User ownUser = new User();
                ownUser.UID = android_id;
                ownUser.Username = "Test";

                if (userlist.contains(ownUser) == false) {
                    userlist.add(ownUser);
                    faf.update("RaketeStart", serialize(userlist));
                    System.out.println(serialize(userlist));
                }

                faf.close();

                while (true) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    faf.connect("meiner.ml", 2345);

                    for (User user : userlist) {
                        String loc = faf.get(user.UID);
                    }
                    faf.close();
                }
            }
        });
    }

    public static String serialize(ArrayList dataList) {

        try (ByteArrayOutputStream bo = new ByteArrayOutputStream();
             ObjectOutputStream so = new ObjectOutputStream(bo)) {
            so.writeObject(dataList);
            so.flush();
            return Base64.getEncoder().encodeToString(bo.toByteArray());
        }
        catch (IOException e) {

        }
        return null;
    }

    public static ArrayList deserialize(String dataStr) {

        byte[] b = Base64.getDecoder().decode(dataStr);
        ByteArrayInputStream bi = new ByteArrayInputStream(b);
        ObjectInputStream si;
        try {
            si = new ObjectInputStream(bi);
            return ArrayList.class.cast(si.readObject());
        }
        catch (IOException | ClassNotFoundException e) {
            //throw new SerializationException("Error during deserialization", e);
            return new ArrayList();
        }
    }

    // Unsued for now
    public boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermission() {

        if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_LOCATION_REQUEST_CODE);
        } else {
            // TODO Make it only appear once
                Toast.makeText(this, "Permission granted!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == MY_LOCATION_REQUEST_CODE) {
            if (permissions.length != 1 || permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION ) || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Please allow location!", Toast.LENGTH_LONG).show();
                requestPermission();
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

    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        // TODO: Implement
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


        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                String android_id = Settings.Secure.getString(MainActivity.this.getContentResolver(),
                        Settings.Secure.ANDROID_ID);

                Parcel p = Parcel.obtain();
                location.writeToParcel(p, 0);
                final byte[] b = p.marshall();      //now you've got bytes
                p.recycle();


                faf.connect("meiner.ml", 2345);
                faf.update(android_id, b.toString());
                faf.close();
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
}




