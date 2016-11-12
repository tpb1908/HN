package com.tpb.hn.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by theo on 18/10/16.
 */

public class Item implements Parcelable, Comparable<Item> {
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
    private boolean viewed;

    private long lastUpdated;

    public Item() {
        lastUpdated = System.currentTimeMillis();
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
        lastUpdated = System.currentTimeMillis();
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
        lastUpdated = System.currentTimeMillis();
        this.time = time;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        lastUpdated = System.currentTimeMillis();
        this.text = text;
    }

    public boolean isDead() {
        return dead;
    }

    public void setDead(boolean dead) {
        lastUpdated = System.currentTimeMillis();
        this.dead = dead;
    }

    public int getParent() {
        return parent;
    }

    public void setParent(int parent) {
        lastUpdated = System.currentTimeMillis();
        this.parent = parent;
    }

    public int[] getKids() {
        return kids;
    }

    public void setKids(int[] kids) {
        lastUpdated = System.currentTimeMillis();
        this.kids = kids;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        lastUpdated = System.currentTimeMillis();
        this.url = url;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        lastUpdated = System.currentTimeMillis();
        this.score = score;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        lastUpdated = System.currentTimeMillis();
        this.title = title;
    }

    public int[] getParts() {
        return parts;
    }

    public void setParts(int[] parts) {
        lastUpdated = System.currentTimeMillis();
        this.parts = parts;
    }

    public int getDescendants() {
        return descendants;
    }

    public void setDescendants(int descendants) {
        lastUpdated = System.currentTimeMillis();
        this.descendants = descendants;
    }

    public boolean isViewed() {
        return viewed;
    }

    public void setViewed(boolean viewed) {
        this.viewed = viewed;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    //</editor-fold>

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Item) {
            final Item i = (Item) obj;
            return id == i.id &&
                    (text != null && i.text != null && text.equals(i.text)) &&
                    (kids != null && i.kids != null && kids.length == i.kids.length) &&
                    score == i.score &&
                    parts.length == i.parts.length &&
                    descendants == i.descendants;
        }
        return false;
    }

    public String getFormattedTitle() {
        if(url != null && url.endsWith(".pdf") && !title.contains("pdf")) {
            return title + "[pdf]";
        } else {
            return title;
        }
    }

    public String getFormattedInfo() {
        String info = score + " points | ";
        if(descendants > 0) {
            info +=  descendants + " comments | ";
        }
        return info + Formatter.timeAgo(time);
    }

    public String getFormattedURL() {
        try {
            return "(" + new URL(url).getHost() + ")";
        } catch(MalformedURLException mue) {
            return "";
        }
    }

    public String getFormattedBy() {
        return "By " + by;
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
        dest.writeByte(this.viewed ? (byte) 1 : (byte) 0);
        dest.writeLong(this.lastUpdated);
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
        this.viewed = in.readByte() != 0;
        this.lastUpdated = in.readLong();
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

    @Override
    public int compareTo(@NonNull Item item) {
        return id == item.id ? 0 : id > item.id ? 1 : -1;
    }

    public static Comparator<Item> comparator = new Comparator<Item>() {
        @Override
        public int compare(Item item, Item t1) {
            return item.id == t1.id ? 0 : item.id > t1.id ? 1 : -1;
        }
    };

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", deleted=" + deleted +
                ", type=" + type +
                ", by='" + by + '\'' +
                ", time=" + time +
                ", text='" + text + '\'' +
                ", dead=" + dead +
                ", parent=" + parent +
                ", kids=" + Arrays.toString(kids) +
                ", url='" + url + '\'' +
                ", score=" + score +
                ", title='" + title + '\'' +
                ", parts=" + Arrays.toString(parts) +
                ", descendants=" + descendants +
                ", viewed=" + viewed +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
