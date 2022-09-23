package com.example.music;

import android.media.MediaPlayer;

public class MyMediaPlayer {
    static MediaPlayer instance;
    public static MediaPlayer getInstance(){
        if (instance==null){
            instance=new MediaPlayer();
        }
        return instance;

    }
    public static int CurrentIndex=-1;
}
