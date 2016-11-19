package com.tpb.hn.network.loaders;

import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.tpb.hn.data.User;
import com.tpb.hn.network.APIPaths;
import com.tpb.hn.network.HNParser;

import org.json.JSONObject;


/**
 * Created by theo on 28/10/16.
 */

public class HNUserLoader {
    private static final String TAG = HNUserLoader.class.getSimpleName();

    private HNUserLoadDone userListener;

    public HNUserLoader(HNUserLoadDone listener) {
        userListener = listener;
    }

    public void loadUser(String id) {
        Log.i(TAG, "loadUser: Url " + APIPaths.getUserPath(id));
        AndroidNetworking.get(APIPaths.getUserPath(id))
                .setTag(id)
                .setPriority(Priority.IMMEDIATE)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            final User user = HNParser.JSONToUser(response);
                            if(userListener != null) userListener.userLoaded(user);
                            Log.i(TAG, "onResponse: " + user.toString());
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
