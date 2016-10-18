package com.tpb.hn;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.tpb.hn.Network.APIPaths;

/**
 * Created by theo on 17/10/16.
 */

public class Main extends AppCompatActivity {
    private static final String TAG = Main.class.toString();
    private static final String URL = "https://hacker-news.firebaseio.com/v0/";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidNetworking.initialize(getApplicationContext());

        AndroidNetworking.get(APIPaths.getNewStoriesPath())
                .setTag("test")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, "onResponse: String request listener " + response);
                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });




    }
}
