package com.joy.player.service;

import android.os.Parcel;
import android.os.Parcelable;

public class MusicFavTtrack implements Parcelable {


    public static final Creator<MusicFavTtrack> CREATOR = new Creator<MusicFavTtrack>() {
        @Override
        public MusicFavTtrack createFromParcel(Parcel source) {
            return new MusicFavTtrack(source);
        }

        @Override
        public MusicFavTtrack[] newArray(int size) {
            return new MusicFavTtrack[size];
        }
    };
    public String id;
    public String name;
    public String artist;
    public String url;
    public String duration;


    public MusicFavTtrack(String id, String name,String artist, String url,String duration ) {
        this.id = id;
        this.name = name;
        this.artist = artist;
        this.url = url;
        this.duration = duration;
    }

    public MusicFavTtrack(Parcel in) {
        id = in.readString();
        name = in.readString();
        artist = in.readString();
        url = in.readString();
        duration = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(artist);
        dest.writeString(url);
        dest.writeString(duration);

    }

}
