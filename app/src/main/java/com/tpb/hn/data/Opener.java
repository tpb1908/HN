package com.tpb.hn.data;

/**
 * Created by theo on 26/11/16.
 */

public class Opener {
    private static boolean whichToRead = false;
    private static Item itemToOpen;
    private static boolean itemRead = false;
    private static Comment commentToOpen;
    private static boolean commentRead = false;

    public static Item getItem() {
        itemRead = true;
        return itemToOpen;
    }

    public static Comment getComment() {
        commentRead = true;
        return commentToOpen;
    }

    public static void setItem(Item item) {
        itemToOpen = item;
        itemRead = false;
        whichToRead = false;
    }

    public static void setComment(Comment comment) {
        commentToOpen = comment;
        commentRead = false;
        whichToRead = true;
    }

    public static boolean mostRecent() {
        return whichToRead;
    }

}
