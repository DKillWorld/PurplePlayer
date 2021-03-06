package com.dv.apps.purpleplayer;

/**
 * Created by Dhaval on 01-07-2017.
 */

public class Songs {

    private String title, artist;
    private long id;
    private int duration, image;

    public Songs(String title, long id, int duration, String artist) {
        this.title = title;
        this.id = id;
        this.duration = duration;
        this.artist = artist;
    }

    public String getTitle(){
        return title;
    }

    public long getId(){
        return id;
    }

    public int getDuration(){
        return duration;
    }

    public String getArtist(){
        return artist;
    }

}
