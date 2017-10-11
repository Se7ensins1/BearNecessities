package com.anastasiavela.figfinder;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.*;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class ConfirmMap extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    public LocationManager locationManager;
    public LatLng latlon;
    public Double longitude;
    public Double latitude;
    public Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }



    public void setMap(GoogleMap googleMap, Boolean current, String address) {
        mMap = googleMap;

        if (current) {
            getLocationFromCurrent();
        }
        else if (!current && address != null) {
            getLocationFromAddress(address);
        }
    }

    public void getLocationFromAddress(String strAddress){
        try {
            Geocoder coder = new Geocoder(this);
            List<Address> address = coder.getFromLocationName(strAddress,5);
            if (address == null) {
                throw new IOException("No address found");
            }
            Address location = address.get(0);

            latlon = new LatLng(location.getLatitude(), location.getLongitude());

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlon, 15.2f));
            mMap.addMarker(new MarkerOptions().position(latlon).title(strAddress));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getLocationFromCurrent() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);

        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(android.location.Location location) {
                    mMap.clear();

                    MarkerOptions mp = new MarkerOptions();
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    latlon = new LatLng(latitude, longitude);
                    mp.position(latlon);
                    mp.title("my position");
                    mMap.addMarker(mp);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlon, 16));
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
            });
        }
        else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(android.location.Location location) {
                    double lon = location.getLongitude();
                    double lat = location.getLatitude();
                    latlon = new LatLng(lat, lon);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlon, 15.2f));
//                    Geocoder geo = new Geocoder(getApplicationContext());
//                    try {
//                        List<android.location.Address> addressList = geo.getFromLocation(lat, lon, 1);
//                        String str = addressList.get(0).getLocality() + ",";
//                        str += addressList.get(0).getCountryName();
//                        mMap.addMarker(new MarkerOptions().position(latlon).title(str));
//
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
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
            });
        }

        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {



            @Override
            public void onMyLocationChange(android.location.Location arg0) {
                // TODO Auto-generated method stub

                mMap.addMarker(new MarkerOptions().position(new LatLng(arg0.getLatitude(), arg0.getLongitude())).title("It's Me!"));
            }
        });
    }

}