package com.tpb.hn.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by theo on 18/10/16.
 */

public class Item implements Parcelable {
    private long id;
    private boolean deleted;
    private ItemType type;
    private String by;
    private long time;
    private String text;
    private boolean dead;
    private long parent;
    private long[] kids;
    private String url;
    private int score;
    private String title;
    private long[] pargs;
    private int descendants;

    public Item() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeByte(this.deleted ? (byte) 1 : (byte) 0);
        dest.writeInt(this.type == null ? -1 : this.type.ordinal());
        dest.writeString(this.by);
        dest.writeLong(this.time);
        dest.writeString(this.text);
        dest.writeByte(this.dead ? (byte) 1 : (byte) 0);
        dest.writeLong(this.parent);
        dest.writeLongArray(this.kids);
        dest.writeString(this.url);
        dest.writeInt(this.score);
        dest.writeString(this.title);
        dest.writeLongArray(this.pargs);
        dest.writeInt(this.descendants);
    }


    protected Item(Parcel in) {
        this.id = in.readLong();
        this.deleted = in.readByte() != 0;
        int tmpType = in.readInt();
        this.type = tmpType == -1 ? null : ItemType.values()[tmpType];
        this.by = in.readString();
        this.time = in.readLong();
        this.text = in.readString();
        this.dead = in.readByte() != 0;
        this.parent = in.readLong();
        this.kids = in.createLongArray();
        this.url = in.readString();
        this.score = in.readInt();
        this.title = in.readString();
        this.pargs = in.createLongArray();
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
