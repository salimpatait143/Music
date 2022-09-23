package com.example.music;

import android.net.Uri;

import java.io.Serializable;

public class Track implements Serializable {

    String title;
    Uri uri;
    Uri artworkUri;
    int size;
    int duration;

    public Track(String title, Uri uri, Uri artworkUri, int size, int duration) {
        this.title = title;
        this.uri = uri;
        this.artworkUri = artworkUri;
        this.size = size;
        this.duration = duration;
    }

    public String getTitle() {
        return title;
    }

    public Uri getUri() {
        return uri;
    }

    public Uri getArtworkUri() {
        return artworkUri;
    }

    public int getSize() {
        return size;
    }

    public int getDuration() {
        return duration;
    }
}


