package com.tpb.hn.storage.permanent;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.tpb.hn.data.Item;
import com.tpb.hn.data.ItemType;
import com.tpb.hn.network.loaders.CachedItemLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by theo on 03/11/16.
 * The Cache used for Items loaded from HN it will write to DB
 */

public class ItemCache {
    private static final String TAG = ItemCache.class.getSimpleName();

    private static ItemCache instance;
    private static DB db;
    private SharedPreferences sp;

    private ArrayList<Item> items = new ArrayList<>();
    private HashMap<Item, ArrayList<Item>> kids = new HashMap<>();

    private ArrayList<Item> backgroundChunk = new ArrayList<>();

    public static ItemCache getInstance(Context context) {
        if(instance == null) {
            instance = new ItemCache(context);
        }
        return instance;
    }

    private ItemCache(Context context) {
        db = DB.getDB(context);
        sp = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        //db.loadRecentItems(null, 1000L * 3600 * 24 * 100);
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    public ArrayList<Item> getKids(Item parent) {
        return kids.get(parent);
    }

    public static void writeIds(Context context, int[] ids, String key) {
        CachedItemLoader.writeItemIds(context, ids, key);
    }

    public void insert(Item item, boolean background) {
        if(item.getType() == ItemType.COMMENT) {
            final Item parent = new Item();
            parent.setId(item.getParent());
            ArrayList<Item> siblings = kids.get(parent);
            if(siblings == null) {
                siblings = new ArrayList<>();
                kids.put(parent, siblings);
            }
            siblings.add(item);
            Collections.sort(siblings);
        } else {
            if(items.contains(item)) return;
            items.add(item);
            Collections.sort(items);
            if(background) {
                backgroundChunk.add(item);
                if(backgroundChunk.size() > 50) {
                    Log.i(TAG, "insert: Writing chunk");
                    db.writeOrUpdate(backgroundChunk.toArray(new Item[0]));
                    backgroundChunk.clear();
                }
            } else {
                db.writeOrUpdate(item);
            }
        }
    }

    public void update(Item item) {
        if(item.getType() == ItemType.COMMENT) {
            final Item parent = new Item();
            parent.setId(item.getParent());
            ArrayList<Item> siblings = kids.get(parent);
            if(siblings == null) {
                siblings = new ArrayList<>();
                kids.put(parent, siblings);
            }
            siblings.add(item);
            Collections.sort(items);
        } else {
            final int pos = Collections.binarySearch(items, item);
            if(pos > 0 && pos < items.size()) {
                items.set(pos, item);
            } else {
                insert(item, false);
            }
            db.writeOrUpdate(item);
        }
    }

    public int position(int id) {
        final Item key = new Item();
        key.setId(id);
        int pos = Collections.binarySearch(items, key);
        return Math.max(pos, -1);
    }

}
