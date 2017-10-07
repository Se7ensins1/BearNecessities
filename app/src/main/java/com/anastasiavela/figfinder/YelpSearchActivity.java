package com.anastasiavela.figfinder;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YelpSearchActivity extends AppCompatActivity {

    String mRequestURL = "https://api.yelp.com/v3/businesses/search";
    LocationManager mLocationManager;
    double mLongitude, mLatitude;
    String mAccessCode = "7wmm-8fEb734g0Zn-YZOcwTRVZwHu6AoqBUUJy_tbrI9NZgjPFcWk65m8o3m2rgvLWBJTjFUg-J_82Lm-Te7x3qnVlmHZtqt50XzKJ4Jz6L5axMeaQl7inWw8UeHWXYx";
    ListView mListView;
    ArrayList<String> listings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yelp_search);

        mLatitude = 27.986065;
        mLongitude = 86.922623;
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mListView = (ListView) findViewById(R.id.searchresults);

        updateListings();
        mListView.setAdapter(new ResultsAdapter(this, listings));
    }

    private void updateLocation() {
        double bestAccuracy = Double.MAX_VALUE;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location bestLocation = null;
        for (String provider : mLocationManager.getAllProviders()) {
            Location tl = mLocationManager.getLastKnownLocation(provider);
            if(tl.getAccuracy() < bestAccuracy) {
                bestLocation = tl;
            }
        }

        this.mLongitude = bestLocation.getLongitude();
        this.mLatitude = bestLocation.getLatitude();
    }

    private void updateListings() {
        this.updateLocation();
        String searchQuery = ((EditText)findViewById(R.id.searchbar)).getText().toString();

        String fullurl = (searchQuery == "") ?
                mRequestURL + "?latitude=" + mLatitude + "&longitude=" + mLongitude :
                mRequestURL + "?term=" + searchQuery + "&latitude=" + mLatitude + "&longitude=" + mLongitude;

        JsonObjectRequest request = new JsonObjectRequest(fullurl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("Response" , response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error", error.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authentication", "bearer " + mAccessCode);

                return headers;
            }
        };

        ApiSingleton.getInstance(this).addRequest(request, "Yelp Listings");
    }


    /***************************************************/

    private class ResultsAdapter extends ArrayAdapter<String> {
        private List<String> mItems;

        public ResultsAdapter(@NonNull Context context, List<String> items) {
            super(context, android.R.layout.simple_list_item_1);
            this.mItems = items;
        }

        @Override
        public boolean isEmpty() {
            return mItems.isEmpty();
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Nullable
        @Override
        public String getItem(int position) {
            return mItems.get(position);
        }
    }
}
