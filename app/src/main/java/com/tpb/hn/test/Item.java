package com.tpb.hn.test;

import com.tpb.hn.data.Formatter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

/**
 * Created by theo on 25/11/16.
 */

public class Item {
    private static final String TAG = Item.class.getSimpleName();

    private int id;
    private boolean deleted;
    private String by;
    private long time;
    private String text;
    private boolean dead;
    private int parent;
    private int[] kids;
    private String url;
    private int score;
    private int[] parts;
    private int descendants;

    private boolean viewed;
    private boolean isNew;
    private boolean parsedText;
    private String commentJSON;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getBy() {
        return by;
    }

    public void setBy(String by) {
        this.by = by;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isDead() {
        return dead;
    }

    public void setDead(boolean dead) {
        this.dead = dead;
    }

    public int getParent() {
        return parent;
    }

    public void setParent(int parent) {
        this.parent = parent;
    }

    public int[] getKids() {
        return kids;
    }

    public void setKids(int[] kids) {
        this.kids = kids;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int[] getParts() {
        return parts;
    }

    public void setParts(int[] parts) {
        this.parts = parts;
    }

    public int getDescendants() {
        return descendants;
    }

    public void setDescendants(int descendants) {
        this.descendants = descendants;
    }

    public boolean isViewed() {
        return viewed;
    }

    public void setViewed(boolean viewed) {
        this.viewed = viewed;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public boolean isParsedText() {
        return parsedText;
    }

    public void setParsedText(boolean parsedText) {
        this.parsedText = parsedText;
    }

    public String getCommentJSON() {
        return commentJSON;
    }

    public void setCommentJSON(String commentJSON) {
        this.commentJSON = commentJSON;
    }

    public String getFormattedURL() {
        try {
            return "(" + new URL(url).getHost() + ")";
        } catch(MalformedURLException mue) {
            return "(Unknown)";
        }
    }

    public String getInfo() {
        String info = score + " points | ";
        if(descendants > 0) {
            info += descendants + " comments | ";
        }
        return info + Formatter.timeAgo(time);
    }

    public String getFormattedBy() {
        return "By " + by;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Item) {
            final Item i = (Item) obj;
            return id == i.id &&
                    (text != null && i.text != null && text.equals(i.text)) &&
                    (kids != null && i.kids != null && kids.length == i.kids.length) &&
                    score == i.score &&
                    (parts != null && i.parts != null && parts.length == i.parts.length) &&
                    descendants == i.descendants &&
                    deleted == i.deleted;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", deleted=" + deleted +
                ", by='" + by + '\'' +
                ", time=" + time +
                ", text='" + text + '\'' +
                ", dead=" + dead +
                ", parent=" + parent +
                ", kids=" + Arrays.toString(kids) +
                ", url='" + url + '\'' +
                ", score=" + score +
                ", parts=" + Arrays.toString(parts) +
                ", descendants=" + descendants +
                ", viewed=" + viewed +
                ", isNew=" + isNew +
                ", parsedText=" + parsedText +
                ", commentJSON='" + commentJSON + '\'' +
                '}';
    }
}
