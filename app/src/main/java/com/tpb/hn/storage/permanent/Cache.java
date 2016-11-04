package com.tpb.hn.storage.permanent;

import android.content.Context;
import android.content.SharedPreferences;

import com.tpb.hn.data.Item;
import com.tpb.hn.data.ItemType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by theo on 03/11/16.
 */

public class Cache {
    private static final String TAG = Cache.class.getSimpleName();

    private static Cache instance;
    private static DB db;
    private SharedPreferences sp;

    private ArrayList<Item> items = new ArrayList<>();
    private HashMap<Item, ArrayList<Item>> kids = new HashMap<>();


    private int[] top = new int[500];
    private int[] newstories = new int[500];
    private int[] best = new int[200];
    private int[] ask = new int[200];
    private int[] show = new int[200];
    private int[] jobs = new int[200];

    public static Cache getInstance(Context context) {
        if(instance == null) {
            instance = new Cache(context);
        }
        return instance;
    }

    private Cache(Context context) {
        db = new DB(context);
        sp = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    public ArrayList<Item> getKids(Item parent) {
        return kids.get(parent);
    }

    public void insert(Item item) {
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
            items.add(item);
            Collections.sort(items);
            db.writeOrUpdate(item);
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
                insert(item);
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
