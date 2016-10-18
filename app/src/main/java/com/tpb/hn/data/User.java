package com.tpb.hn.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by theo on 18/10/16.
 */

public class User implements Parcelable {
    private long id;
    private short delay;
    private long created;
    private int karma;
    private String about;
    private int[] submitted;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeInt(this.delay);
        dest.writeLong(this.created);
        dest.writeInt(this.karma);
        dest.writeString(this.about);
        dest.writeIntArray(this.submitted);
    }

    public User() {
    }

    protected User(Parcel in) {
        this.id = in.readLong();
        this.delay = (short) in.readInt();
        this.created = in.readLong();
        this.karma = in.readInt();
        this.about = in.readString();
        this.submitted = in.createIntArray();
    }

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
}
