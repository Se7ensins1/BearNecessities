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
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2052;
    private GoogleMap mMap;
    private LocationManager locationManager;
    private boolean mStartedFromList = false;
    private double mLatitude, mLongitude;
    private String mSelected;
    private Double[] mSelectedLocation;
    private HashMap<String[], Double[]> coordinates;
    private String mRequestURL = "https://api.yelp.com/v3/businesses/search";
    private String mAccessCode = "7wmm-8fEb734g0Zn-YZOcwTRVZwHu6AoqBUUJy_tbrI9NZgjPFcWk65m8o3m2rgvLWBJTjFUg-J_82Lm-Te7x3qnVlmHZtqt50XzKJ4Jz6L5axMeaQl7inWw8UeHWXYx";
    private HashMap<String, LatLng> idsToCo;
    private HashMap<String, String> idsToStatus;
    private HashMap<String, String> idsToName;
    private int markerCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        coordinates = new HashMap<>();
        idsToCo = new HashMap<>();
        idsToStatus = new HashMap<>();
        idsToName = new HashMap<>();

        mSelected = "";
        if(getIntent().getSerializableExtra("data") != null) {
            mStartedFromList = true;
            mLatitude = getIntent().getDoubleExtra("latitude", 0.0);
            mLongitude = getIntent().getDoubleExtra("longitude", 0.0);
            coordinates = (HashMap<String[], Double[]>)getIntent().getSerializableExtra("data");
            mSelected = getIntent().getStringExtra("selected");
            for (String[] k : coordinates.keySet()) {
                if(k[1].equals(mSelected)) {
                    mSelectedLocation = coordinates.get(k);
                }
                idsToCo.put(k[0], new LatLng(coordinates.get(k)[0], coordinates.get(k)[1]));
            }
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
                    LatLng latlon;
                    if(!mStartedFromList) {
                        latlon = new LatLng(mLatitude, mLongitude);
                    } else {
                        latlon = new LatLng(mSelectedLocation[0], mSelectedLocation[1]);
                    }
                    if (start == 1) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlon, 14.6f));
                        Log.d("Start", "is" + start);
                        updateListings();
                    }
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
            }
        }
    }

    private void updateListings() {
        String fullurl = (mStartedFromList) ? mRequestURL + "?latitude=" + mSelectedLocation[0] + "&longitude=" + mSelectedLocation[1] + "&limit=15" + "&sort_by=distance" + "&open_now=true" :
                mRequestURL + "?latitude=" + mLatitude + "&longitude=" + mLongitude + "&limit=15" + "&sort_by=distance" + "&open_now=true";
        coordinates.clear();
        JsonObjectRequest request = new JsonObjectRequest(fullurl, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d("Response", response.toString());
                storeData(response);
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
        int x=5;// retry count
        request.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 48,
                x, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        ApiSingleton.getInstance(this).addRequest(request, "Yelp Listings");
    }

    private void storeData(JSONObject data) {
        if (data != null) {
            try {
                for (int i = 0; i < data.getJSONArray("businesses").length(); i++) {
                    JSONObject restaurant = data.getJSONArray("businesses").getJSONObject(i);

                    String id = restaurant.getString("id");
                    String name = restaurant.getString("name");

                    JSONObject co = restaurant.getJSONObject("coordinates");
                    LatLng pos = new LatLng(co.getDouble("latitude"), co.getDouble("longitude"));
                    idsToCo.put(id, pos);
                    idsToName.put(id, name);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.d("HELLO", "Reached end of store data with length " + idsToCo.keySet().size());
        dbSearch();
    }

    private void dbSearch() {
        String baseURL = "https://api.mlab.com/api/1/databases/diarya/collections/businesses";
        String apiKey = "qRE-43TB3yKdXgibhg2s3GF8z3WyRgYD";

        for (String id : idsToCo.keySet()) {
            String url = baseURL + "?q={'_id':'" + id + "'}&apiKey=" + apiKey;

            JsonArrayRequest request = new JsonArrayRequest(JsonRequest.Method.GET, url, null, new Response.Listener<JSONArray>() {

                @Override
                public void onResponse(JSONArray response) {
                    markerCount++;
                    try {
                        String info;
                        String id;
                        for (int i = 0; i < response.length(); i++) {
                            info = response.getJSONObject(i).getString("bathroom");
                            id = response.getJSONObject(i).getString("_id");
                            idsToStatus.put(id, info);
                        }
                        if (markerCount == 15) {
                            addColorMarker();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("Error", error.toString());
                }
            }) ;
            int x=5;// retry count
            request.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 48,
                    x, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            ApiSingleton.getInstance(this).addRequest(request, "Database Setup");
        }
    }

    private void addColorMarker() {
        for (String id: idsToCo.keySet()) {
            String status = idsToStatus.get(id);
            String name = idsToName.get(id);
            LatLng pos = idsToCo.get(id);
            if (name == null) {
                name = id;
            }
            if (name.equals(mSelected)) {
                mMap.addMarker(new MarkerOptions().position(pos).title(name).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))).setTag(0);
            } else if (status == null) {
                //write to db
                mMap.addMarker(new MarkerOptions().position(pos).title(name).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))).setTag(0);
            } else if (status.equals("Free")) {
                mMap.addMarker(new MarkerOptions().position(pos).title(name).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))).setTag(0);
            } else if (status.equals("Purchase")) {
                mMap.addMarker(new MarkerOptions().position(pos).title(name).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))).setTag(0);
            } else if (status.equals("None")) {
                mMap.addMarker(new MarkerOptions().position(pos).title(name).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))).setTag(0);
            }
        }
    }
}