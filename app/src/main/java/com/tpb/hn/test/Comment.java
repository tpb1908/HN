package com.tpb.hn.test;

/**
 * Created by theo on 25/11/16.
 */

public class Comment {
    private static final String TAG = Comment.class.getSimpleName();

    private int id;
    private int parent;
    private int points;
    private String author;
    private String text;
    private String children;
    private int descendants;
    private long time;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getParent() {
        return parent;
    }

    public void setParent(int parent) {
        this.parent = parent;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

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
        return "CommentWrapper{" +
                "id=" + id +
                ", parent=" + parent +
                ", points=" + points +
                ", author='" + author + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
