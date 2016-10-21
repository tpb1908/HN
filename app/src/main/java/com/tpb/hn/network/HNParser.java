package com.tpb.hn.network;

import com.tpb.hn.data.Item;
import com.tpb.hn.data.ItemType;
import com.tpb.hn.data.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by theo on 21/10/16.
 */

public class HNParser {
    private static final String KEY_ID = "id";
    private static final String KEY_BY = "by";
    private static final String KEY_DESCENDANTS = "descendants";
    private static final String KEY_KIDS = "kids";
    private static final String KEY_SCORE = "score";
    private static final String KEY_TIME = "time";
    private static final String KEY_TITLE = "title";
    private static final String KEY_TYPE = "type";
    private static final String KEY_URL = "url";
    private static final String KEY_TEXT = "text";
    private static final String KEY_ASK_TITLE = "Ask HN:";

    private static final String KEY_DELAY = "delay";
    private static final String KEY_CREATED = "created";
    private static final String KEY_KARMA = "karma";
    private static final String KEY_ABOUT = "about";
    private static final String KEY_SUBMITTED = "submitted";

    public static Item JSONToItem(JSONObject obj) throws JSONException{
        final Item item = new Item();
        item.setId(obj.getInt(KEY_ID));
        item.setBy(obj.getString(KEY_BY));
        item.setDescendants(obj.getInt(KEY_DESCENDANTS));
        item.setKids(extractIntArray(obj.getJSONArray(KEY_KIDS)));
        item.setScore(obj.getInt(KEY_SCORE));
        item.setTime(obj.getLong(KEY_TIME));
        item.setTitle(obj.getString(KEY_TITLE));
        if(item.getTitle().contains(KEY_ASK_TITLE)) {
            item.setType(ItemType.ASK);
        } else {
            item.setType(getType(obj.getString(KEY_TYPE)));
        }
        item.setUrl(obj.getString(KEY_URL));

        if(item.getType() == ItemType.COMMENT ||
                item.getType() == ItemType.ASK  ||
                item.getType() == ItemType.JOB) {
            item.setText(obj.getString(KEY_TEXT));
        }
        return item;
    }

    public static User JSONToUser(JSONObject obj) throws JSONException {
        final User user =  new User();
        user.setId(obj.getString(KEY_ID));
        user.setAbout(obj.getString(KEY_ABOUT));
        user.setDelay((byte)obj.getInt(KEY_DELAY));
        user.setCreated(obj.getLong(KEY_CREATED));
        user.setKarma(obj.getInt(KEY_KARMA));
        user.setSubmitted(extractIntArray(obj.getJSONArray(KEY_SUBMITTED)));
        return user;
    }

    private static int[] extractIntArray(JSONArray array) {
        final int[] kids = new int[array.length() + 1];
        for(int i = 0; i < array.length(); i++) {
            kids[i] = array.optInt(i);
        }
        return kids;
    }

    private static ItemType getType(String type) {
        switch(type.toLowerCase()) {
            case "story":
                return ItemType.STORY;
            case "job":
                return ItemType.JOB;
            case "comment":
                return ItemType.COMMENT;
            case "poll":
                return ItemType.POLL;
            case "pollopt":
                return ItemType.POLLOPT;
        }
        return null;
    }

}
