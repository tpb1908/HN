package com.tpb.hn.content;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.tpb.hn.R;
import com.tpb.hn.network.APIPaths;

/**
 * Created by theo on 17/10/16.
 */

public class Content extends AppCompatActivity {
    private static final String TAG = Content.class.toString();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);
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
