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
    private static final String TAG = HNParser.class.getSimpleName();

    private static final String KEY_ID = "id";
    private static final String KEY_BY = "by";
    private static final String KEY_DESCENDANTS = "descendants";
    private static final String KEY_KIDS = "kids";
    private static final String KEY_SCORE = "score";
    private static final String KEY_TIME = "time";
    private static final String KEY_CREATED_AT = "created_at_i";
    private static final String KEY_TITLE = "title";
    private static final String KEY_TYPE = "type";
    private static final String KEY_URL = "url";
    private static final String KEY_TEXT = "text";
    private static final String KEY_PARENT = "parent";
    private static final String KEY_ASK_TITLE = "Ask HN:";

    private static final String KEY_AUTHOR = "author";
    private static final String KEY_POINTS = "points";
    private static final String KEY_PARENT_ID = "parent_id";
    private static final String KEY_CHILDREN = "children";

    private static final String KEY_DEAD = "dead";
    private static final String KEY_DELETED = "deleted";
    private static final String KEY_DELAY = "delay";
    private static final String KEY_CREATED = "created";
    private static final String KEY_KARMA = "karma";
    private static final String KEY_ABOUT = "about";
    private static final String KEY_SUBMITTED = "submitted";


    public static Item JSONToItem(JSONObject obj) throws JSONException {
        final Item item = new Item();
        item.setId(obj.getInt(KEY_ID));
        item.setTime(obj.getLong(KEY_TIME));
        if(obj.has(KEY_TITLE)) item.setTitle(obj.getString(KEY_TITLE));
        if(item.getTitle() != null && item.getTitle().contains(KEY_ASK_TITLE)) {
            item.setType(ItemType.ASK);
        } else {
            item.setType(getType(obj.getString(KEY_TYPE)));
        }
        if(obj.has(KEY_BY)) item.setBy(obj.getString(KEY_BY));
        if(obj.has(KEY_SCORE)) item.setScore(obj.getInt(KEY_SCORE));
        if(obj.has(KEY_DESCENDANTS)) item.setDescendants(obj.getInt(KEY_DESCENDANTS));
        if(obj.has(KEY_URL)) item.setUrl(obj.getString(KEY_URL));
        if(obj.has(KEY_KIDS)) item.setKids(extractIntArray(obj.getJSONArray(KEY_KIDS)));
        if(obj.has(KEY_TEXT)) item.setText(obj.getString(KEY_TEXT));
        if(obj.has(KEY_PARENT)) item.setParent(obj.getInt(KEY_PARENT));
        if(obj.has(KEY_DELETED)) item.setDeleted(obj.getBoolean(KEY_DELETED));
        return item;
    }

    public static Item commentJSONToItem(JSONObject obj) throws JSONException {

        final Item item = new Item();
        item.setId(obj.getInt(KEY_ID));
        if(obj.has(KEY_CREATED_AT)) item.setTime(obj.getLong(KEY_CREATED_AT));
        if(obj.has(KEY_TITLE)) item.setTitle(obj.getString(KEY_TITLE));
        item.setType(ItemType.COMMENT);

        if(obj.has(KEY_AUTHOR)) item.setBy(obj.getString(KEY_AUTHOR));
        if(obj.has(KEY_POINTS) && !obj.getString(KEY_POINTS).equals("null"))
            item.setScore(obj.getInt(KEY_POINTS));

        if(obj.has(KEY_URL)) item.setUrl(obj.getString(KEY_URL));
        if(obj.has(KEY_TEXT)) item.setText(obj.getString(KEY_TEXT));
        if(obj.has(KEY_PARENT_ID) && !obj.getString(KEY_PARENT_ID).equals("null"))
            item.setParent(obj.getInt(KEY_PARENT_ID));

        if(obj.has(KEY_CHILDREN)) item.setCommentJSON(obj.getString(KEY_CHILDREN));


        return item;
    }

    public static String getCommentJSON(JSONObject obj) throws JSONException {
        return obj.getString(KEY_CHILDREN);
    }


    public static JSONObject itemToJSON(Item item) {
        final JSONObject object = new JSONObject();
        try {
            object.put(KEY_ID, item.getId());
            object.put(KEY_TYPE, item.getType());
            object.put(KEY_PARENT_ID, item.getParent());
            object.put(KEY_POINTS, item.getScore());
            object.put(KEY_DEAD, item.isDead());
            object.put(KEY_DELETED, item.isDeleted());

            if(item.getBy() != null) object.put(KEY_BY, item.getBy());
            if(item.getTime() != 0) object.put(KEY_TIME, item.getTime());
            if(item.getText() != null) object.put(KEY_TEXT, item.getText());
            if(item.getParent() != 0) object.put(KEY_PARENT, item.getParent());
            if(item.getCommentJSON() != null) object.put(KEY_CHILDREN, item.getCommentJSON());
            if(item.getUrl() != null) object.put(KEY_URL, item.getUrl());
            if(item.getTitle() != null) object.put(KEY_TITLE, item.getTitle());

        } catch(JSONException je) {

        }
        return object;
    }

    public static User JSONToUser(JSONObject obj) throws JSONException {
        final User user = new User();
        user.setId(obj.getString(KEY_ID));
        if(obj.has(KEY_ABOUT)) user.setAbout(obj.getString(KEY_ABOUT));
        if(obj.has(KEY_DELAY)) user.setDelay((byte) obj.getInt(KEY_DELAY));
        if(obj.has(KEY_SUBMITTED)) {
            user.setSubmitted(extractIntArray(obj.getJSONArray(KEY_SUBMITTED)));
        } else {
            user.setSubmitted(new int[] {});
        }
        user.setCreated(obj.getLong(KEY_CREATED));
        user.setKarma(obj.getInt(KEY_KARMA));

        return user;
    }

    public static Item[] parseComments(String json) throws JSONException {
        final JSONArray carray = new JSONArray(json);

        final Item[] children = new Item[carray.length()];
        for(int i = 0; i < carray.length(); i++) {
            children[i] = commentJSONToItem(carray.getJSONObject(i));
        }
        return children;
    }

    public static JSONArray toJSONArray(int[] array) {
        final JSONArray jsonArray = new JSONArray();
        for(int i : array) {
            jsonArray.put(i);
        }
        return jsonArray;
    }

    public static int[] extractIntArray(JSONArray array) {
        final int[] kids = new int[array.length()];
        for(int i = 0; i < array.length(); i++) {
            kids[i] = array.optInt(i);
        }
        return kids;
    }

    public static int[] extractIntArray(String array) {
        final String[] items = array.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\s", "").split(",");
        //Log.i(TAG, "extractIntArray: Extracting int array " + Arrays.toString(items));
        final int[] results = new int[items.length];

        for(int i = 0; i < items.length; i++) {
            try {
                results[i] = Integer.parseInt(items[i]);
            } catch(NumberFormatException nfe) {
                results[i] = -1;
                //TODO: write something here if you need to recover from formatting errors
            }
        }
        //Log.i(TAG, "extractIntArray: " + Arrays.toString(results));
        return results;
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
