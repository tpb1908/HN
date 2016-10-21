package com.tpb.hn.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by theo on 18/10/16.
 */

public class Item implements Parcelable {
    private int id;
    private boolean deleted;
    private ItemType type;
    private String by;
    private long time;
    private String text;
    private boolean dead;
    private int parent;
    private int[] kids;
    private String url;
    private int score;
    private String title;
    private int[] parts;
    private int descendants;

    public Item() {
    }

    //<editor-fold desc="Getters and setters">
    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public ItemType getType() {
        return type;
    }

    public void setType(ItemType type) {
        this.type = type;
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

    public long getParent() {
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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
    //</editor-fold>

    public String getFormattedInfo() {
        return score + " points |" + kids.length + " comments | " + Formatter.timeAgo(time);
    }

    public String getFormattedURL() {
        try {
            return new URL(url).getHost();
        } catch(MalformedURLException mue) {
            return "Unknown";
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeByte(this.deleted ? (byte) 1 : (byte) 0);
        dest.writeInt(this.type == null ? -1 : this.type.ordinal());
        dest.writeString(this.by);
        dest.writeLong(this.time);
        dest.writeString(this.text);
        dest.writeByte(this.dead ? (byte) 1 : (byte) 0);
        dest.writeInt(this.parent);
        dest.writeIntArray(this.kids);
        dest.writeString(this.url);
        dest.writeInt(this.score);
        dest.writeString(this.title);
        dest.writeIntArray(this.parts);
        dest.writeInt(this.descendants);
    }


    protected Item(Parcel in) {
        this.id = in.readInt();
        this.deleted = in.readByte() != 0;
        int tmpType = in.readInt();
        this.type = tmpType == -1 ? null : ItemType.values()[tmpType];
        this.by = in.readString();
        this.time = in.readLong();
        this.text = in.readString();
        this.dead = in.readByte() != 0;
        this.parent = in.readInt();
        this.kids = in.createIntArray();
        this.url = in.readString();
        this.score = in.readInt();
        this.title = in.readString();
        this.parts = in.createIntArray();
        this.descendants = in.readInt();
    }

    public static final Creator<Item> CREATOR = new Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel source) {
            return new Item(source);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };
}
