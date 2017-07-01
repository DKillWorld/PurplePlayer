package com.dv.apps.purpleplayer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Dhaval on 01-07-2017.
 */

public class SongAdapter extends ArrayAdapter<Songs> {

    public SongAdapter(@NonNull Context context, ArrayList<Songs> songList) {
        super(context,0, songList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        Songs songs = getItem(position);
        if (view == null){
            view = LayoutInflater.from(getContext()).inflate(R.layout.activity_list, parent, false);
        }

        TextView textView = (TextView) view.findViewById(R.id.songName);
        TextView textView2 = (TextView) view.findViewById(R.id.songId);
        TextView textView3 = (TextView) view.findViewById(R.id.songDuration);
        TextView textView4 = (TextView) view.findViewById(R.id.songArtist);

        textView.setText(songs.getTitle());
        textView2.setText(songs.getId() + "");
        textView3.setText(songs.getDuration() + "");
        textView4.setText(songs.getArtist());

        return view;
    }
}
