package com.tpb.hn.network;

import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.OkHttpResponseListener;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by theo on 27/10/16.
 * Once we have this cookie, we can use a request with the cookie to pull down the page and vote
 */

public class Login {
    private static final String TAG = Login.class.getSimpleName();
    private final LoginListener listener;
    private final String user;
    private final String pass;

    private String cookie = "";

    public Login(LoginListener listener, String username, String password) {
        this.listener = listener;
        user = username;
        pass = password;

    }

    public void getCookie() {
                AndroidNetworking.post("https://news.ycombinator.com/login?goto=news")
                .addBodyParameter("acct", user)
                .addBodyParameter("pw", pass)
                .setTag("LOGIN")
                .setPriority(Priority.IMMEDIATE)
                .build()
                .getAsOkHttpResponse(new OkHttpResponseListener() {
                    @Override
                    public void onResponse(Response response) {
                        if(response.code() >=200 && response.code() < 400) {//Success
                            try {
//                            Log.i(TAG, "onResponse: Login response " + response.toString());
//                            Log.i(TAG, "onResponse: Network response " + response.networkResponse().toString());
//                            Log.i(TAG, "onResponse: Cache control " + response.cacheControl().toString());
//                            Log.i(TAG, "onResponse: Body " + response.body().string());
//                            Log.i(TAG, "onResponse: Response headers" + response.headers().toString());

                                //We want the prior response headers as there is a redirect
                                final String res = response.priorResponse().headers().get("set-cookie");
                                final int startPos = res.indexOf("user=") + 5;
                                final int endPos = res.indexOf(";");
                                cookie = res.substring(startPos, endPos);
                                Log.i(TAG, "onResponse: Header " + response.priorResponse().headers().toString() + "\n\nCookie " + cookie);
                                checkWithCookie(cookie, false);
                            } catch(Exception e) {
                                Log.i(TAG, "onResponse: Exception when getting header");
                                listener.response(false);
                            }
                        } else {
                            listener.response(false);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.d(TAG, "onError: Login error \n\n\n" + anError.getErrorBody());
                        listener.response(false);
                    }
                });
    }

    public void checkCookieStillValid(String cookie) {
        checkWithCookie(cookie, true);
    }

    private void checkWithCookie(final String cookie, final boolean setCookie) {
        AndroidNetworking.get("https://news.ycombinator.com")
                .setOkHttpClient(buildWithUserCookie(cookie))
                .setTag("Test")
                .setPriority(Priority.IMMEDIATE)
                .build()
                .getAsOkHttpResponse(new OkHttpResponseListener() {
                    @Override
                    public void onResponse(Response response) {
                        if(response.code() >=200 && response.code() < 400) {
                            try {
                                final boolean success = checkIfLoggedIn(response.body().string());
                                if(setCookie && success) Login.this.cookie = cookie;
                                listener.response(success);
                            } catch(Exception e) {
                                listener.response(false);
                            }
                        }
                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });
    }

    private static boolean checkForBadLogin(String body) {
        return body.contains("Bad login");
    }

    private static boolean checkIfLoggedIn(String body) {
        return body.contains("user?id=");
    }

    private static OkHttpClient buildWithUserCookie(final String cookie) {
        return new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        final Request orig = chain.request();

                        final Request authorised = orig.newBuilder()
                                .addHeader("Cookie", "user=" + cookie)
                                .build();
                        return chain.proceed(authorised);
                    }
                })
                .build();
    }

    public interface LoginListener {

        void response(boolean success);

    }

}
