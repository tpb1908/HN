package com.tpb.hn.network;

import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.OkHttpResponseListener;

import java.io.IOException;

import okhttp3.Response;

/**
 * Created by theo on 27/10/16.
 */

public class Login {
    private static final String TAG = Login.class.getSimpleName();

    public Login(String username, String password) {
//        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//
//        StrictMode.setThreadPolicy(policy);
//        final OkHttpClient loginClient = new OkHttpClient.Builder()
//                //.followRedirects(false)
//                .build();
//        RequestBody body = new FormBody.Builder()
//                .add("acct", username)
//                .add("pw", password)
//                .build();
//        Request r = new Request.Builder()
//                .url("https://news.ycombinator.com/login?goto=news")
//                .post(body)
//                .build();
//        try {
//            Response res = loginClient.newCall(r).execute();
//            Log.i(TAG, "Login: " + res.body().string());
//        } catch(IOException ioe) {
//            Log.e(TAG, "Login: ", ioe);
//        }

        /*
            Once we have the cookie, we can post to https://news.ycombinator.com/vote?id=12802949&how=up&auth=CODE_HERE&goto=news
            with our cookie????
         */
        AndroidNetworking.post("https://news.ycombinator.com/login?goto=news")
                .addBodyParameter("acct", username)
                .addBodyParameter("pw", password)
                .setTag("LOGIN")
                .setPriority(Priority.IMMEDIATE)
                .build()
                .getAsOkHttpResponse(new OkHttpResponseListener() {
                    @Override
                    public void onResponse(Response response) {
                        if(response.code() == 200) //Success
                        try {
                            Log.i(TAG, "onResponse: Login response " + response.toString());
                            Log.i(TAG, "onResponse: Network response " + response.networkResponse().toString());
                            Log.i(TAG, "onResponse: Cache control " + response.cacheControl().toString());
                            Log.i(TAG, "onResponse: Body " + response.body().string());
                            Log.i(TAG, "onResponse: Response headers" + response.headers().toString());
                            //Found it, we want the prior response headerss
                            Log.i(TAG, "onResponse: Prior response headers " + response.priorResponse().headers().toString() );
                        } catch(IOException ioe) {}
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.d(TAG, "onError: Login error \n\n\n" + anError.getErrorBody());
                    }
                });

//        OkHttpClient httpclient = new OkHttpClient();
//        RequestBody body = new FormBody.Builder()
//                .add("acct", username)
//                .add("pw", password)
//                .build();
//        Request request = new Request.Builder()
//                .url("https://news.ycombinator.com/login/?goto=news")
//                .post(body)
//                .build();
//        try {
//            Response response = httpclient.newCall(request).execute();
//            Log.i(TAG, "Login: Reponse " + response.toString());
//        } catch(Exception e) {
//            Log.e(TAG, "Login: ", e);
//        }
    }


}
