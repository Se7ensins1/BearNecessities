package com.anastasiavela.figfinder;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DatabaseSearch extends Activity {

    private String baseURL = "https://api.mlab.com/api/1/databases/diarya/collections/businesses";
    private String apiKey = "qRE-43TB3yKdXgibhg2s3GF8z3WyRgYD";
    private Map<String, JSONObject> db;

    public DatabaseSearch(){
        String url = baseURL + "?apiKey=" + apiKey;
        this.db = new HashMap<>();
        JsonArrayRequest request = new JsonArrayRequest(JsonRequest.Method.GET, url, null, new Response.Listener<JSONArray>() {

            private Map<String, JSONObject> database;

            @Override
            public void onResponse(JSONArray response) {
                String id;
                JSONObject info;
                database = new HashMap<>();
                try {
                    for (int i = 0; i < response.length(); i++) {
                        id = response.getJSONObject(i).getString("id");
                        info = response.getJSONObject(i).getJSONObject("info");
                        database.put(id, info);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                setDb(database);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error", error.toString());
            }
        }) ;
        ApiSingleton.getInstance(this).addRequest(request, "Database Setup");
    }

    private void setDb(Map<String, JSONObject> data) {
        this.db = data;
    }

    public void addData(HashMap<String[], Double[]> data) {
        for (String[] labels : data.keySet()) {
            if (!this.db.containsKey(labels[0])) {
                /*TODO Write to database as a document*/
            }
        }
    }
}
