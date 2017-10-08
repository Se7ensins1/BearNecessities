package com.anastasiavela.figfinder;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YelpSearchActivity extends Activity {

    String mRequestURL = "https://api.yelp.com/v3/businesses/search";
    LocationManager mLocationManager;
    double mLongitude, mLatitude;
    String mAccessCode = "7wmm-8fEb734g0Zn-YZOcwTRVZwHu6AoqBUUJy_tbrI9NZgjPFcWk65m8o3m2rgvLWBJTjFUg-J_82Lm-Te7x3qnVlmHZtqt50XzKJ4Jz6L5axMeaQl7inWw8UeHWXYx";
    ListView mListView;
    HashMap<String[], Double[]> listings;
    ArrayList<String[]> ordered;
    Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yelp_search);

        mLatitude = 27.986065;
        mLongitude = 86.922623;
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mListView = (ListView) findViewById(R.id.searchresults);
        listings = new HashMap<>();
        ordered = new ArrayList<String[]>();

        updateListings();
        mListView.setAdapter(new ResultsAdapter(this, ordered));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selected = ordered.get(i)[0];

                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("data", listings);
                intent.putExtra("selected", selected);
                intent.putExtra("latitude", mLatitude);
                intent.putExtra("longitude", mLongitude);

                startActivity(intent);
            }
        });

        searchButton = (Button)findViewById(R.id.searchbutton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateListings();
            }
        });
    }

    private void updateLocation() {
        double bestAccuracy = Double.MAX_VALUE;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location bestLocation = null;
        for (String provider : mLocationManager.getAllProviders()) {
            Location tl = mLocationManager.getLastKnownLocation(provider);
            if(tl == null) { continue; }
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
        String fullurl = (searchQuery.equals("")) ?
                mRequestURL + "?latitude=" + mLatitude + "&longitude=" + mLongitude + "&limit=50" + "&sort_by=distance" + "&open_now=true" :
                mRequestURL + "?term=" + searchQuery + "&latitude=" + mLatitude + "&longitude=" + mLongitude + "&limit=50" + "&sort_by=distance" + "&open_now=true";

        JsonObjectRequest request = new JsonObjectRequest(fullurl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("Response", response.toString());
                ordered.clear();
                listings.clear();
                try {
                    for (int i = 0; i < response.getJSONArray("businesses").length(); i++) {
                        JSONObject business = response.getJSONArray("businesses").getJSONObject(i);

                        String[] identifiers = { business.getString("id"),
                                                 business.getString("name") };

                        Log.d("hhhhhhhhhhhhhhhhh",identifiers[1]);
                        Double[] coords;
                        Double distance;
                        try {
                            coords = new Double[] { business.getJSONObject("coordinates").getDouble("latitude"),
                                                    business.getJSONObject("coordinates").getDouble("longitude") };
                            distance = Math.round(business.getDouble("distance") * 0.000621371 * 100)/100d; // meters per mile
                        } catch(JSONException e) {
                            coords = new Double[]{0., 0.};
                            distance = -1.;
                        }
                        listings.put(identifiers, coords);
                        ordered.add(new String[]{identifiers[1], distance.toString()});
                    }
                    ((ResultsAdapter)mListView.getAdapter()).notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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



    /***************************************************/

    private class ResultsAdapter extends ArrayAdapter<String> {
        private ArrayList<String[]> mItems;

        public ResultsAdapter(@NonNull Context context, ArrayList<String[]> items) {
            super(context, R.layout.element_layout);
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

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            View result = inflater.inflate(R.layout.element_layout, null);

            ((TextView)result.findViewById(R.id.title)).setText(mItems.get(position)[0]);
            ((TextView)result.findViewById(R.id.distance)).setText(((mItems.get(position)[1].equals("-1") ? "-" : mItems.get(position)[1])) + " mi.");

            return result;
        }
    }
}
