package com.anastasiavela.figfinder;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by maxmcarthur on 10/7/17.
 */

public class ApiSingleton {
    private static ApiSingleton mSingletonInstance;
    private Context mContext;
    private RequestQueue mRequestQueue;

    private ApiSingleton(Context context) {
        this.mContext = context;
    }

    public static ApiSingleton getInstance(Context context){
        if (mSingletonInstance == null) {
            mSingletonInstance = new ApiSingleton(context);
        }
        return mSingletonInstance;
    }

    public RequestQueue getRequestQueue() {
        if (this.mRequestQueue == null) {
            this.mRequestQueue = Volley.newRequestQueue(mContext);
        }
        return this.mRequestQueue;
    }

    public <T> void addRequest(Request<T> request, String tag) {
        request.setTag(tag);
        this.mRequestQueue.add(request);
    }

    public void clearRequestTag(String tag) {
        this.mRequestQueue.cancelAll(tag);
    }
}
