package com.tpb.hn.network.loaders;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.StringRequestListener;
import com.tpb.hn.Util;
import com.tpb.hn.data.Comment;
import com.tpb.hn.data.Item;
import com.tpb.hn.network.APIPaths;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by theo on 25/11/16.
 */

public class Loader extends BroadcastReceiver {
    private static final String TAG = Loader.class.getSimpleName();
    public static int ERROR_NOT_IN_CACHE = 100;
    public static int ERROR_TIMEOUT = 200;
    public static int ERROR_PARSING = 300;
    public static int ERROR_NETWORK_CHANGE = 400;
    public static int ERROR_UNKNOWN = 0;
    public static int ERROR_PDF = 500;
    private static Loader instance;
    private static HashMap<String, ArrayList<WeakReference<TextLoader>>> listeners = new HashMap<>();
    private SharedPreferences prefs;
    private boolean online = false;
    private ArrayList<WeakReference<NetworkChangeListener>> mNetworkListeners = new ArrayList<>();
    private DB db;

    private Loader(Context context) {
        prefs = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        db = DB.getDB(context);
    }

    public static Loader getInstance(Context context) {
        if(instance == null) {
            instance = new Loader(context);
            instance.online = Util.isNetworkAvailable(context);
            context.registerReceiver(instance, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
        return instance;
    }

    public void removeReceiver(Context context) {
        context.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive: Network change");
        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo netInfo = cm.getActiveNetworkInfo();
        online = netInfo != null;
        for(int i = 0; i < mNetworkListeners.size(); i++) {
            if(mNetworkListeners.get(i).get() == null) {
                mNetworkListeners.remove(i);
                i--;
            } else {
                mNetworkListeners.get(i).get().networkStateChange(online);
            }
        }
    }

    public void addNetworkListener(NetworkChangeListener listener) {
        mNetworkListeners.add(new WeakReference<>(listener));
    }

    public void removeNetworkListener(NetworkChangeListener listener) {
        mNetworkListeners.remove(new WeakReference<>(listener));
    }

    public void cancelBackgroundLoading() {
        AndroidNetworking.forceCancel("BG");
    }

    public void getIds(String key, final idLoader loader) {
        final String url;
        switch(key.toLowerCase()) {
            case "top":
                url = APIPaths.getTopPath();
                break;
            case "best":
                url = APIPaths.getBestPath();
                break;
            case "ask":
                url = APIPaths.getAskPath();
                break;
            case "new":
                url = APIPaths.getNewPath();
                break;
            case "show":
                url = APIPaths.getShowPath();
                break;
            case "job":
                url = APIPaths.getJobPath();
                break;
            default:
                url = "";
        }
        Log.d(TAG, "getIds: " + url);
        if(online) {
            AndroidNetworking.get(url)
                    .setTag(url)
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsString(new StringRequestListener() {
                        @Override
                        public void onResponse(String response) {
                            final int[] ids = Parser.extractIntArray(response);
                            Log.i(TAG, "onResponse: ids " + Arrays.toString(ids));
                            loader.idsLoaded(ids);
                            Util.putIntArrayInPrefs(prefs.edit(), url, ids);
                        }

                        @Override
                        public void onError(ANError anError) {
                            if(!online) {
                                loader.idError(ERROR_NETWORK_CHANGE);
                            } else {
                                loader.idError(ERROR_UNKNOWN);
                            }
                        }
                    });
        } else {
            final int[] ids = Util.getIntArrayFromPrefs(prefs, url);
            if(ids.length > 0) {
                loader.idsLoaded(ids);
            } else {
                loader.idError(ERROR_NOT_IN_CACHE);
            }
        }
    }

    public void removeListeners() {
        listeners.clear();
    }

    public void saveItem(final Item item, final Context context, final ItemSaveListener saveListener) {
        networkLoadArticle(item.getUrl(), true, new TextLoader() {
            @Override
            public void textLoaded(JSONObject result) {
                Log.i(TAG, "textLoaded: save success");
                try {
                    final String MERCURY = result.getString("content");
                    db.writeItem(item, true, MERCURY);
                    saveListener.itemSaved(item);
                } catch(JSONException jse) {
                    saveListener.saveError(item, ERROR_PARSING);
                    Log.e(TAG, "textLoaded: for save of " + item.getTitle(), jse);
                }
            }

            @Override
            public void textError(String url, int code) {
                saveListener.saveError(item, code);
            }
        });
        final WebView wv = new WebView(context);
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.i(TAG, "onPageFinished: Saved page finished for " + item.getTitle());
            }
        });
        wv.loadUrl(item.getUrl());

    }

    public void loadItem(final int id, final ItemLoader loader) {
        if(online) {
            networkLoadItem(id, loader);
        } else {
            cacheLoadItem(id, loader);
        }
    }

    private void networkLoadItem(final int id, final ItemLoader loader) {
        AndroidNetworking.get(APIPaths.getItemPath(id))
                .setTag(id)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            final Item item = Parser.parseItem(response);
                            loader.itemLoaded(item);
                            db.writeItem(item);
                        } catch(JSONException e) {
                            loader.itemError(id, ERROR_PARSING);
                        } catch(Exception e) {
                            loader.itemError(id, ERROR_UNKNOWN);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        loader.itemError(id, ERROR_NETWORK_CHANGE);
                    }
                });

    }

    private void cacheLoadItem(final int id, ItemLoader loader) {
        db.readItem(id, loader);
    }

    public void loadItems(final int[] ids, final boolean background, ItemLoader loader) {
        if(online) {
            networkLoadItems(ids, background, loader);
        } else if(!background) {
            cacheLoadItems(ids, loader);
        }
    }

    private void networkLoadItems(final int[] ids, final boolean background, final ItemLoader loader) {
        for(int i : ids) {
            final int id = i;
            AndroidNetworking.get(APIPaths.getItemPath(id))
                    .setTag(background ? "BG" : id)
                    .setPriority(background ? Priority.MEDIUM : Priority.HIGH)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                final Item item = Parser.parseItem(response);
                                loader.itemLoaded(item);
                                db.writeItem(item);
                            } catch(JSONException jse) {
                                Log.e(TAG, "onResponse: ", jse);
                                loader.itemError(id, ERROR_PARSING);
                            } catch(Exception e) {
                                loader.itemError(id, ERROR_UNKNOWN);
                            }
                        }

                        @Override
                        public void onError(ANError anError) {

                            loader.itemError(id, ERROR_NETWORK_CHANGE);
                        }
                    });
        }
    }

    private void cacheLoadItems(final int[] ids, ItemLoader loader) {
        db.readItems(ids, loader);
    }

    public void loadChildren(final int id, CommentLoader loader) {
        if(online) {
            networkLoadChildren(id, loader);
        } else {
            cacheLoadChildren(id);
        }
    }

    private void networkLoadChildren(final int id, final CommentLoader loader) {
        AndroidNetworking.get(APIPaths.getAlgoliaItemPath(id))
                .setTag(id)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            final Comment comment = Parser.parseComment(response);
                            loader.commentsLoaded(comment);
                        } catch(JSONException e) {
                            Log.e(TAG, "onResponse: ", e);
                            loader.commentError(id, ERROR_PARSING);
                        } catch(Exception e) {
                            loader.commentError(id, ERROR_UNKNOWN);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        loader.commentError(id, ERROR_NETWORK_CHANGE);
                    }
                });
    }

    public void loadArticle(final Item item, boolean forImmediateUse, TextLoader loader) {
        if(online) {
            networkLoadArticle(item.getUrl(), forImmediateUse, loader);
        } else {
            cacheLoadArticle(item.getId(), loader);
        }
    }

    private void networkLoadArticle(final String url, boolean forImmediateUse, final TextLoader loader) {
        if(url.endsWith(".pdf")) {
            loader.textError(url, ERROR_PDF);
        } else {
            if(listeners.get(url) == null)
                listeners.put(url, new ArrayList<WeakReference<TextLoader>>());
            listeners.get(url).add(new WeakReference<>(loader));

            Log.i(TAG, "networkLoadArticle: " + listeners.get(url).toString());
            AndroidNetworking.get(APIPaths.getMercuryParserPath(url))
                    .setTag(url)
                    .setOkHttpClient(APIPaths.MERCURY_CLIENT)
                    .setPriority(forImmediateUse ? Priority.IMMEDIATE : Priority.MEDIUM)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if(response.has("content") && !response.get("content").equals("<div></div>")) {
                                    Log.i(TAG, "onResponse: " + listeners.get(url));
                                    if(listeners.get(url) != null) {
                                        for(WeakReference<TextLoader> loader : listeners.get(url)) {
                                            if(loader.get() == null) {
                                                listeners.get(url).remove(loader);
                                            } else {
                                                loader.get().textLoaded(response);
                                            }
                                        }
                                        listeners.get(url).remove(new WeakReference<>(loader));
                                    }
                                }
                            } catch(JSONException jse) {
                                for(WeakReference<TextLoader> loader : listeners.get(url)) {
                                    loader.get().textError(url, ERROR_PARSING);
                                }
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            for(WeakReference<TextLoader> loader : listeners.get(url)) {
                                if(loader.get() != null)  loader.get().textError(url, ERROR_NETWORK_CHANGE);
                            }
                        }
                    });
        }

    }

    public void redirectThroughMercury(String url, TextLoader loader) {
        if(online) {
            networkLoadArticle(url, true, loader);
        } else {
            loader.textError(url, ERROR_NOT_IN_CACHE);
        }
    }

    private void cacheLoadArticle(final int id, final TextLoader loader) {
        db.readItem(id, new ItemLoader() {
            @Override
            public void itemLoaded(Item item) {
                if(item.getParsedText() != null && item.getParsedText().length() > 5) {
                    try {
                        final JSONObject jse = new JSONObject();
                        jse.put("content", item.getParsedText());
                        loader.textLoaded(jse);
                    } catch(JSONException jse) {
                        Log.e(TAG, "itemLoaded: ", jse);
                    }
                } else {
                    Log.i(TAG, "itemLoaded: not in cache");
                    loader.textError("", ERROR_NOT_IN_CACHE);
                }
            }

            @Override
            public void itemError(int id, int code) {
                Log.i(TAG, "itemError: " + code);
                loader.textError("", code);
            }
        });
    }

    private void cacheLoadChildren(final int id) {
    }

    public interface idLoader {

        void idsLoaded(int[] ids);

        void idError(int code);

    }

    public interface ItemLoader {

        void itemLoaded(Item item);

        void itemError(int id, int code);

    }

    public interface CommentLoader {

        void commentsLoaded(Comment rootComment);

        void commentError(int id, int code);

    }

    public interface TextLoader {

        void textLoaded(JSONObject result);

        void textError(String url, int code);

    }

    public interface ItemSaveListener {

        void itemSaved(Item item);

        void saveError(Item item, int code);

    }

    public interface NetworkChangeListener {

        void networkStateChange(boolean netAvailable);

    }

    private static class DB extends SQLiteOpenHelper {
        private static final String TAG = DB.class.getSimpleName();
        private static final String NAME = "CACHE";
        private static final int VERSION = 1;
        private static DB instance;


        private static final String TABLE = "CACHE";
        private static final String KEY_ID = "ID";
        private static final String KEY_SAVED = "SAVED";
        private static final String KEY_JSON = "JSON";
        private static final String KEY_MERCURY = "MERCURY";

        private DB(Context context) {
            super(context, NAME, null, VERSION);
        }

        static DB getDB(Context context) {
            if(instance == null) {
                instance = new DB(context);
            }
            return instance;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            final String CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE +
                    "( " +
                    KEY_ID + " INTEGER PRIMARY KEY, " +
                    KEY_SAVED + " BOOLEAN, " +
                    KEY_JSON + " VARCHAR, " +
                    KEY_MERCURY + " VARCHAR " +
                    ");";
            db.execSQL(CREATE);

        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        }

        void readItem(final int id, final ItemLoader loader) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    final SQLiteDatabase db = DB.this.getReadableDatabase();
                    final String QUERY = "SELECT * FROM " + TABLE + " WHERE " + KEY_ID  + " = ? LIMIT 1";
                    final Cursor cursor = db.rawQuery(QUERY, new String[] { Integer.toString(id) });
                    final Handler handler = new Handler(Looper.getMainLooper());
                    if(cursor.moveToFirst()) {
                        final String JSON = cursor.getString(cursor.getColumnIndex(KEY_JSON));
                        try {
                            final Item item = Parser.parseItem(new JSONObject(JSON));
                            item.setParsedText(cursor.getString(cursor.getColumnIndex(KEY_MERCURY)));
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    loader.itemLoaded(item);
                                }
                            });

                        } catch(JSONException jse) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    loader.itemError(id, ERROR_PARSING);
                                }
                            });

                        }
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                loader.itemError(id, ERROR_NOT_IN_CACHE);
                            }
                        });
                    }
                    cursor.close();
                }
            });
        }

        void readItems(final int[] ids, final ItemLoader loader) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    final SQLiteDatabase db = DB.this.getReadableDatabase();
                    final String QUERY = "SELECT * FROM " + TABLE + " WHERE " + KEY_ID + buildWhereIn(ids.length);
                    final Cursor cursor = db.rawQuery(QUERY, intArToStrAr(ids));
                    final Handler handler = new Handler(Looper.getMainLooper());
                    if(cursor.moveToFirst()) {
                        do {
                            final String JSON = cursor.getString(cursor.getColumnIndex(KEY_JSON));
                            final int id = cursor.getInt(cursor.getColumnIndex(KEY_ID));
                            try {
                                final Item item = Parser.parseItem(new JSONObject(JSON));
                                item.setParsedText(cursor.getString(cursor.getColumnIndex(KEY_MERCURY)));
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        loader.itemLoaded(item);
                                    }
                                });
                            } catch(JSONException jse) {
                                Log.e(TAG, "run: Loading from db " + JSON, jse);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        loader.itemError(id, ERROR_PARSING);
                                    }
                                });

                            }
                        } while(cursor.moveToNext());
                    } else {
                        Log.e(TAG, "run: ", new Exception("Couldn't find any of the ids"));
                        //TODO- Handle multiple errors
                    }

                    cursor.close();
                }
            });
        }

        void writeItem(Item item) {
            writeItem(item, false, "");
        }

        void writeItem(final Item item, final boolean saved, final String mercury) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    DB.this.getWritableDatabase().insertWithOnConflict(TABLE,
                            null,
                            itemToCV(item, saved, mercury),
                            SQLiteDatabase.CONFLICT_REPLACE);
                }
            });
        }

        private ContentValues itemToCV(Item item, boolean saved, String mercury) {
            final ContentValues cv = new ContentValues();
            cv.put(KEY_ID, item.getId());
            cv.put(KEY_SAVED, saved);
            try {
                final String JSON = Parser.itemJSON(item);
                cv.put(KEY_JSON, JSON);
            } catch(JSONException jse) {
                cv.put(KEY_JSON, "{}");
            }
            cv.put(KEY_MERCURY, mercury);
            return cv;
        }

        private String buildWhereIn(int size) {
            Log.i(TAG, "buildWhereIn: Building where for " + size);
            final StringBuilder builder = new StringBuilder();
            if(size > 0) {
                String spacer = " ";
                builder.append(" IN (");
                while(size > 0) {
                    builder.append(spacer);
                    builder.append("?");
                    spacer = ", ";
                    size--;
                }
                builder.append(" )");
            }
            return builder.toString();
        }

        private String[] intArToStrAr(int[] a) {
            final String[] s = new String[a.length];
            for(int i = 0; i < a.length; i++) {
                s[i] = Integer.toString(a[i]);
            }
            return s;
        }

    }

}
