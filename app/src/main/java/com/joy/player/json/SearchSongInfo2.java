package com.joy.player.json;

import android.os.Parcel;
import android.os.Parcelable;

public class SearchSongInfo2 implements Parcelable {

    private String name;
    private String id;
    private String artist;
    private String url;
    private String duration;

    protected SearchSongInfo2(Parcel in) {
        name = in.readString();
        id = in.readString();
        artist = in.readString();
        url = in.readString();
        duration = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(id);
        dest.writeString(artist);
        dest.writeString(url);
        dest.writeString(duration);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SearchSongInfo2> CREATOR = new Creator<SearchSongInfo2>() {
        @Override
        public SearchSongInfo2 createFromParcel(Parcel in) {
            return new SearchSongInfo2(in);
        }

        @Override
        public SearchSongInfo2[] newArray(int size) {
            return new SearchSongInfo2[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
