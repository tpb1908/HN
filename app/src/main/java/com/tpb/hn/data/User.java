package com.tpb.hn.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.tpb.hn.helpers.Formatter;

import java.util.Arrays;

/**
 * Created by theo on 18/10/16.
 */

public class User implements Parcelable {
    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
    private String id;
    private byte delay;
    private long created;
    private int karma;
    private String about;
    private int[] submitted;

    public User() {
    }

    private User(Parcel in) {
        this.id = in.readString();
        this.delay = in.readByte();
        this.created = in.readLong();
        this.karma = in.readInt();
        this.about = in.readString();
        this.submitted = in.createIntArray();
    }

    //<editor-fold desc="Getters and setters">
    public int[] getSubmitted() {
        return submitted;
    }

    public void setSubmitted(int[] submitted) {
        this.submitted = submitted;
    }

    public byte getDelay() {
        return delay;
    }

    public void setDelay(byte delay) {
        this.delay = delay;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public int getKarma() {
        return karma;
    }

    public void setKarma(int karma) {
        this.karma = karma;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getId() {
        return id;
    }
    //</editor-fold>s console (text)

    public void setId(String id) {
        this.id = id;
    }

    public String getInfo() {
        return submitted.length + " posts | " + karma + " karma | " + (created > 5 * 60 ? "in " + Formatter.timeAgo(created) : "since just now");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeByte(this.delay);
        dest.writeLong(this.created);
        dest.writeInt(this.karma);
        dest.writeString(this.about);
        dest.writeIntArray(this.submitted);
    }

    @Override
    public String toString() {
        return "USER{" +
                "id='" + id + '\'' +
                ", delay=" + delay +
                ", created=" + created +
                ", karma=" + karma +
                ", about='" + about + '\'' +
                ", submitted=" + Arrays.toString(submitted) +
                '}';
    }
}
