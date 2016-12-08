package com.tpb.hn.network;

import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.tpb.hn.Analytics;
import com.tpb.hn.data.User;
import com.tpb.hn.helpers.APIPaths;

import org.json.JSONObject;


/**
 * Created by theo on 28/10/16.
 */

public class UserLoader {
    private static final String TAG = UserLoader.class.getSimpleName();

    private final HNUserLoadDone userListener;

    public UserLoader(HNUserLoadDone listener) {
        userListener = listener;
    }

    public void loadUser(String id) {
        if(Analytics.VERBOSE) Log.i(TAG, "loadUser: Url " + APIPaths.getUserPath(id));
        AndroidNetworking.get(APIPaths.getUserPath(id))
                .setTag(id)
                .setPriority(Priority.IMMEDIATE)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            final User user = Parser.parseUser(response);
                            if(userListener != null) userListener.userLoaded(user);
                        } catch(Exception e) {
                            Log.e(TAG, "onResponse: ", e);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e(TAG, "onError: ", anError);
                    }
                });
    }

    public interface HNUserLoadDone {
        void userLoaded(User user);
    }

}
