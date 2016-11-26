package com.tpb.hn.data;

/**
 * Created by theo on 25/11/16.
 */

public class Comment extends Item{
    private static final String TAG = Comment.class.getSimpleName();

    private String text;
    private String children;
    private int descendants;
    private long time;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getChildren() {
        return children;
    }

    public void setChildren(String children) {
        this.children = children;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getDescendants() {
        return descendants;
    }

    public void setDescendants(int descendants) {
        this.descendants = descendants;
    }


    @Override
    public String toString() {
        return "Comment{" +
                "text='" + text + '\'' +
                ", children='" + children + '\'' +
                ", descendants=" + descendants +
                ", time=" + time +
                '}';
    }
}
