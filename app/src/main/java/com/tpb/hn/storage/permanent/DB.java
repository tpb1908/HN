package com.tpb.hn.storage.permanent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;

import com.tpb.hn.data.Item;
import com.tpb.hn.network.HNParser;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by theo on 03/11/16.
 */

class DB extends SQLiteOpenHelper {
    private static final String TAG = DB.class.getSimpleName();

    private static final String NAME = "CACHE";
    private static final int VERSION = 1;

    private static final String TABLE = "ITEMS";
    private static final String KEY_ID = "ID";
    private static final String KEY_LAST_UPDATE = "LAST_UPDATE";
    private static final String KEY_JSON = "JSON";
    private static final String KEY_PERMANENT = "PERMANENT";
    private static final String KEY_WEB_TEXT = "TEXT";

    DB(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE +
                "(" +
                KEY_ID + " INTEGER PRIMARY KEY, " +
                KEY_LAST_UPDATE + " INTEGER, " +
                KEY_JSON + " VARCHAR, " +
                KEY_WEB_TEXT + " VARCHAR, " +
                KEY_PERMANENT + " BOOLEAN ence" +
                ");";
        db.execSQL(CREATE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    void writeOrUpdate(DBCallback callback, Item item) {
        new WriteItemTask(callback).doInBackground(item);
    }

    void writeOrUpdate(Item item) {
        new WriteItemTask().doInBackground(item);
    }

    void writeOrUpdate(DBCallback callback, Item... items) {
        new WriteItemTask(callback).doInBackground(items);
    }

    void writeOrUpdate(Item... items) {
        new WriteItemTask().doInBackground(items);
    }

    void loadRecentItems(int count) {
        //final String QUERY = "SELECT * FROM " + TABLE + " LIMIT 10 ";
    }

    void loadRecentItems(DBCallback callback, long timescale) {
        new LoadItemsTask(callback).doInBackground(100L, timescale);

    }

    public interface DBCallback {

        void writeComplete(boolean success, Item item);

        void loadComplete(boolean success, ArrayList<Item> items);

    }

    private class LoadItemsTask extends AsyncTask<Long, Void, Boolean> {
        private DBCallback callback;

        LoadItemsTask(DBCallback callback) {
            this.callback = callback;
        }

        @Override
        protected Boolean doInBackground(Long... params) {
            final SQLiteDatabase db = DB.this.getReadableDatabase();
            Cursor cursor;
            String QUERY;
            long timescale, bound;
            if(params.length == 1) {
                bound = params[0];

            }
            if(params.length == 2) {
                bound = params[0];
                timescale = System.currentTimeMillis() - params[1];
                QUERY = "SELECT * FROM " + TABLE; // + " WHERE " + KEY_LAST_UPDATE + " > ?";
                cursor = db.rawQuery(QUERY, new String[] {}); // Long.toString(timescale)});

                final ArrayList<Item> items = new ArrayList<>();

                if(cursor.moveToFirst()) {
                    do {
                        final String s = cursor.getString(cursor.getColumnIndex(KEY_JSON));
                        try {
                            final Item i = HNParser.JSONToItem(new JSONObject(s));
                            items.add(i);
                        } catch(Exception e) {
                            Log.e(TAG, "loadRecentItems: Exception parsing ", e);
                        }
                    } while(cursor.moveToNext());
                }
                cursor.close();
                Log.i(TAG, "doInBackground: Items loaded " + items.size() + " " + items.toString());
                //callback.loadComplete(true, items);
            }

            return null;
        }
    }

    private class WriteItemTask extends AsyncTask<Item, Void, Boolean> {
        private DBCallback callback;

        WriteItemTask() {
        }

        WriteItemTask(DBCallback callback) {
            this.callback = callback;
        }

        @Override
        protected Boolean doInBackground(Item... items) {
            final SQLiteDatabase db = DB.this.getWritableDatabase();
            final ContentValues values = new ContentValues();
            boolean allSuccessful = true;
            db.beginTransaction();
            for(Item i : items) {
                final JSONObject JSON = HNParser.itemToJSON(i);
                values.put(KEY_ID, i.getId());
                values.put(KEY_LAST_UPDATE, i.getLastUpdated());
                values.put(KEY_JSON, JSON.toString());
                values.put(KEY_PERMANENT, false);
                final boolean success = db.insertWithOnConflict(TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE) != -1;
                allSuccessful &= success;
                //if(callback != null) callback.writeComplete(success, i);
                values.clear();
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            return allSuccessful;
        }
    }


}
