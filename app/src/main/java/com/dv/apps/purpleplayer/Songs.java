package com.dv.apps.purpleplayer;

/**
 * Created by Dhaval on 01-07-2017.
 */

public class Songs {

    private String title;
    private long id;
    private int duration;

    public Songs(String title, long id, int duration) {
        this.title = title;
        this.id = id;
        this.duration = duration;
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

}
