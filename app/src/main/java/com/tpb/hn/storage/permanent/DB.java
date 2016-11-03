package com.tpb.hn.storage.permanent;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;

import com.tpb.hn.data.Item;

/**
 * Created by theo on 03/11/16.
 */

class DB extends SQLiteOpenHelper {
    private static final String TAG = DB.class.getSimpleName();

    private static final String NAME = "CACHE";
    private static final int VERSION = 1;


    public DB(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public class WriteItemTask extends AsyncTask<Item, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Item... items) {
            return null;
        }
    }


}
