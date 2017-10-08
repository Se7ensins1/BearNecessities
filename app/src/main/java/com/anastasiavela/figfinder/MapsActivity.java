package com.anastasiavela.figfinder;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2052;
    private GoogleMap mMap;
    public Button errorButton;
    public Button contButton;
    private LocationManager locationManager;
    private double mLatitude, mLongitude;
    private HashMap<String[], Double[]> coordinates;
    private String mRequestURL = "https://api.yelp.com/v3/businesses/search";
    private String mAccessCode = "7wmm-8fEb734g0Zn-YZOcwTRVZwHu6AoqBUUJy_tbrI9NZgjPFcWk65m8o3m2rgvLWBJTjFUg-J_82Lm-Te7x3qnVlmHZtqt50XzKJ4Jz6L5axMeaQl7inWw8UeHWXYx";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        buttons();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        coordinates = new HashMap<>();
    }

    public void buttons() {
        contButton = (Button) findViewById(R.id.buttonCont);
        contButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent map = new Intent(MapsActivity.this, PopCont.class);
                startActivity(map);
            }
        });

        errorButton = (Button) findViewById(R.id.buttonError);
        errorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent map = new Intent(MapsActivity.this, PopError.class);
                startActivity(map);
            }
        });
    }

    /**
     * Manipulates the map once available. This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            return;
        }

        mMap.setMyLocationEnabled(true);
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
                int start = 0;
                @Override
                public void onLocationChanged(Location location) {
                    start++;
                    mLongitude = location.getLongitude();
                    mLatitude = location.getLatitude();
                    LatLng latlon = new LatLng(mLatitude, mLongitude);
                    if (start == 1) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlon, 14.6f));
                    }
                    updateListings();


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

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onMapReady(mMap);
                }
                return;
            }
        }
    }

    private void updateListings() {
        String fullurl = mRequestURL + "?latitude=" + mLatitude + "&longitude=" + mLongitude + "&limit=50" + "&sort_by=distance" + "&open_now=true";

        JsonObjectRequest request = new JsonObjectRequest(fullurl, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                storeData(response);
                Log.d("Response" , response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error", error.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "bearer " + mAccessCode);

                return headers;
            }
        };

        ApiSingleton.getInstance(this).addRequest(request, "Yelp Listings");
    }

    private void storeData(JSONObject data) {
        try {
            for (int i = 0; i < data.getJSONArray("businesses").length(); i++) {
                JSONObject restaurant = data.getJSONArray("businesses").getJSONObject(i);

                String[] labels = new String[2];
                labels[0] = restaurant.getString("id");
                labels[1] = restaurant.getString("name");
                Double[] coordinate = new Double[2];

                JSONObject coor = restaurant.getJSONObject("coordinates");
                coordinate[0] = coor.getDouble("latitude");
                coordinate[1] = coor.getDouble("longitude");
                this.coordinates.put(labels, coordinate);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for(String[] label : coordinates.keySet()) {

            String title = label[1];
            Double[] pos = coordinates.get(label);
            LatLng position = new LatLng(pos[0], pos[1]);
            mMap.addMarker(new MarkerOptions().position(position).title(title)).setTag(0);
            Log.d("Marker", "place" + pos[0] + ", " + pos[1]);
        }
    }
}