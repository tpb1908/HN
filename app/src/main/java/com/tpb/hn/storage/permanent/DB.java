package com.tpb.hn.storage.permanent;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;

import com.tpb.hn.data.Item;
import com.tpb.hn.network.HNParser;

import org.json.JSONObject;

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

    DB(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE +
                "(" +
                KEY_ID + " INTEGER PRIMARY KEY, " +
                KEY_LAST_UPDATE + " INTEGER, " +
                KEY_JSON + " VARCHAR " +
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

    void loadRecentItems() {

    }

    public interface DBCallback {

        boolean writeComplete(boolean success, Item item);

    }

    private class WriteItemTask extends AsyncTask<Item, Void, Boolean> {
        private DBCallback callback;

        WriteItemTask() {}

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
                final boolean success = db.insertWithOnConflict(TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE) != -1;
                allSuccessful &= success;
                if(callback != null) callback.writeComplete(success, i);
                values.clear();
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            return allSuccessful;
        }
    }


}
