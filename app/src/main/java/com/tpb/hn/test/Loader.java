package com.tpb.hn.test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.tpb.hn.network.HNParser;

/**
 * Created by theo on 25/11/16.
 */

public class Loader extends BroadcastReceiver {
    private static final String TAG = Loader.class.getSimpleName();

    private static Loader instance;
    private boolean online = false;

    public static Loader getInstance(Context context) {
        if(instance == null) {
            instance = new Loader(context);
        }
        return instance;
    }

    private Loader(Context context) {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo netInfo = cm.getActiveNetworkInfo();
        online = netInfo != null;
        if(!online) {
            //TODO Notify network info listeners
        }
    }

    public void getIds(final String url) {
        AndroidNetworking.get(url)
                .setTag(url)
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        final int[] items = HNParser.extractIntArray(response);
                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });
    }

    public void loadItem(final int id) {

    }

    public void loadItems(final int[] ids, final boolean background) {

    }

    public void loadChildJSON(final int id) {}



    public interface idLoader {

        void idsLoaded(int[] ids);

        void idError(int code);

    }

    public interface itemLoader {

        void itemLoaded(Item item);

        void itemError(int id, int code);

    }

    public interface commentLoader {

        void commentsLoaded(String json);

        void commentError(int id, int code);

    }

    public interface networkChangeListener {

        void networkStateChange(boolean netAvailable);

    }

}
