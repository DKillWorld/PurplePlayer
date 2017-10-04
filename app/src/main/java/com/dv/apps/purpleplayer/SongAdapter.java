package com.dv.apps.purpleplayer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

/**
 * Created by Dhaval on 01-07-2017.
 */

public class SongAdapter extends ArrayAdapter<Songs> {

    private String songTime;
    private ArrayList<Songs> songList, backupList;
    private Songs song;
    private Context context;

    public SongAdapter(@NonNull Context context, ArrayList<Songs> songList) {
        super(context,0, songList);
        this.songList = songList;
        this.backupList = songList;
        this.context = context;
    }

    class Myholder{
        TextView textView;
        TextView textView2;
        TextView textView3;
        ImageView imageView;
        Myholder(View view) {
            textView = (TextView) view.findViewById(R.id.songName);
            textView2 = (TextView) view.findViewById(R.id.songArtist);
            textView3 = (TextView) view.findViewById(R.id.songDuration);
            imageView = (ImageView) view.findViewById(R.id.image_view);
        }
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        Myholder myholder = null;
        song = getItem(position);
        if (view == null){
            LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.list_item, parent, false);
            myholder = new Myholder(view);
            view.setTag(myholder);
        }else {
            myholder = (Myholder) view.getTag();
        }


        //Calculating HH:MM:SS from milliseconds
        int duration = song.getDuration();
        int hrs = (duration / 3600000);
        int mns = (duration / 60000) % 60000;
        int scs = (duration % 60000) / 1000;
        if (hrs == 0){
            songTime = String.format("%02d:%02d",mns, scs);
        }else {
            songTime = String.format("%02d:%02d:%02d", hrs, mns, scs);
        }

        myholder.textView.setText(song.getTitle());
        myholder.textView2.setText(song.getArtist() + "");
        myholder.textView3.setText(songTime + "");
//        myholder.imageView.setImageURI(song.getImage());
        Glide.with(context)
                .load(song.getImage())
                .apply(new RequestOptions().placeholder(R.mipmap.ic_launcher))
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(myholder.imageView);

        return view;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return myFilter;
    }

    Filter myFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint.length() == 0){
                results.values = backupList;
                results.count = backupList.size();
            }
            else if (constraint != null && constraint.toString().length() > 0){
                ArrayList<Songs> tempList = new ArrayList<Songs>();
                for (int i = 0, l = backupList.size(); i < l; i++){
                    Songs tempSong = backupList.get(i);
                    if (tempSong.getTitle().toLowerCase().contains(constraint.toString().toLowerCase())
                            || tempSong.getArtist().toLowerCase().contains(constraint.toString().toLowerCase())){
                        tempList.add(tempSong);
                    }
                }
                Log.i("songlist", "" + songList.size());
                results.count = tempList.size();
                results.values = tempList;
            }else {
                synchronized (this){
                    results.values = songList;
                    results.count = songList.size();
                }
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            songList = (ArrayList<Songs>) results.values;
            if (songList.size() > 0) {
                notifyDataSetChanged();
            }else{
                notifyDataSetInvalidated();
            }
        }
    };

    @Nullable
    @Override
    public Songs getItem(int position) {
        return songList.get(position);
    }

    @Override
    public int getCount() {
        return songList.size();
    }
}

