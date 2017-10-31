package com.dv.apps.purpleplayer.Models;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.IOException;

/**
 * Created by Dhaval on 01-07-2017.
 */

public class Song {

    private String title, artist;
    private Uri image;
    private long id;
    private int duration;
    private Context context;

    public Song(Context context, String title, long id, int duration, String artist, Uri uri) {
        this.title = title;
        this.id = id;
        this.duration = duration;
        this.artist = artist;
        this.image = uri;
        this.context = context;
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

    public Uri getImage() {
        return image;
    }

    public Bitmap getImageBitmap(){
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), image);
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

