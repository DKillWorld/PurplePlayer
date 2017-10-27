package com.dv.apps.purpleplayer.ListFragments;


import android.content.ContentUris;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;

import com.dv.apps.purpleplayer.ListAdapters.SongAdapter;
import com.dv.apps.purpleplayer.MusicService;
import com.dv.apps.purpleplayer.R;
import com.dv.apps.purpleplayer.Songs;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class SongListFragment extends Fragment{


    ListView listView;
    SongAdapter adapter;
    ArrayList<Songs> songList;
    SearchView searchView;

    public SongListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_song_list, container, false);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listView = view.findViewById(R.id.fragment_song_list);
        listView.setFastScrollEnabled(true);
        songList = MusicService.songList;
        adapter = new SongAdapter(getActivity(), songList);
        listView.setAdapter(adapter);
        setHasOptionsMenu(true);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Songs tempSong = adapter.getItem(position);
                Uri playUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, tempSong.getId());
                Bundle bundle = new Bundle();
                bundle.putInt("Pos", songList.indexOf(tempSong));
                MediaControllerCompat.getMediaController(getActivity()).getTransportControls()
                        .playFromUri(playUri, bundle);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);

        //SearchView Code
        MenuItem item = menu.findItem(R.id.search);
        searchView = (SearchView) item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}