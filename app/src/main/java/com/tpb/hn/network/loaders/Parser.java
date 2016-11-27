package com.tpb.hn.network.loaders;

import com.tpb.hn.data.Comment;
import com.tpb.hn.data.Item;
import com.tpb.hn.data.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by theo on 25/11/16.
 */

public class Parser {
    private static final String TAG = Parser.class.getSimpleName();

    private static final String KEY_ID = "id";
    private static final String KEY_BY = "by";
    private static final String KEY_DESCENDANTS = "descendants";
    private static final String KEY_KIDS = "kids";
    private static final String KEY_SCORE = "score";
    private static final String KEY_TIME = "time";
    private static final String KEY_CREATED_AT = "created_at";
    private static final String KEY_CREATED_AT_I = "created_at_i";
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

    private static final String KEY_NULL = "null";

    public static Item parseItem(JSONObject obj) throws JSONException {
        final Item item = new Item();
        item.setId(obj.getInt(KEY_ID));
        item.setTime(obj.getLong(KEY_TIME));
        if(obj.has(KEY_TITLE)) {
            item.setTitle(obj.getString(KEY_TITLE));
        }
        if(obj.getString(KEY_TYPE).equals("comment")) item.setComment(true);
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

    public static Comment parseComment(JSONObject obj) throws JSONException {
        final Comment comment = new Comment();
        comment.setId(obj.getInt(KEY_ID));
        if(obj.has(KEY_PARENT_ID)) comment.setParent(obj.getInt(KEY_PARENT_ID));
        if(obj.has(KEY_CREATED_AT_I)) comment.setTime(obj.getInt(KEY_CREATED_AT_I));

        comment.setText(obj.getString(KEY_TEXT));
        comment.setBy(obj.getString(KEY_AUTHOR));
        comment.setChildren(obj.getString(KEY_CHILDREN));
        return comment;
    }

    public static User parseUser(JSONObject obj) throws JSONException {
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

    public static String itemJSON(Item item) throws JSONException{
        final JSONObject obj = new JSONObject();
        obj.put(KEY_ID, item.getId());
        obj.put(KEY_TIME, item.getTime());
        obj.put(KEY_TITLE, item.getTitle());
        obj.put(KEY_TYPE, item.isComment() ? "comment" : "other");
        obj.put(KEY_BY, item.getBy());
        obj.put(KEY_SCORE, item.getScore());
        obj.put(KEY_DEAD, item.getDescendants());
        obj.put(KEY_URL, item.getUrl());

        if(item.getKids() != null) obj.put(KEY_KIDS, new JSONArray(item.getKids()));
        obj.put(KEY_TEXT, item.getText());
        obj.put(KEY_PARENT_ID, item.getParent());
        obj.put(KEY_DELETED, item.isDeleted());

        return obj.toString();
    }

    private static int[] extractIntArray(JSONArray array) {
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


    public static Comment getDeadComment(JSONObject obj) throws JSONException {
        final Comment dead = new Comment();
        if(obj.has(KEY_ID)) dead.setId(obj.getInt(KEY_ID));
        if(obj.has(KEY_PARENT)) dead.setParent(obj.getInt(KEY_PARENT));
        if(obj.has(KEY_PARENT_ID)) dead.setParent(obj.getInt(KEY_PARENT_ID));
        if(obj.has(KEY_BY)) dead.setBy(obj.getString(KEY_BY));
        dead.setDead(true);
        dead.setText("[Dead item]");
        return dead;
    }


}
