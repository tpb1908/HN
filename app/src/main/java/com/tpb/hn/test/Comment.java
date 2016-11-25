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

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", parent=" + parent +
                ", points=" + points +
                ", author='" + author + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
