package com.dv.apps.purpleplayer.ListFragments;


import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.dv.apps.purpleplayer.ListAdapters.SongAdapter;
import com.dv.apps.purpleplayer.Models.Song;
import com.dv.apps.purpleplayer.MusicService;
import com.dv.apps.purpleplayer.R;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class SongListFragment extends Fragment{


    ListView listView;
    public SongAdapter adapter;
    ArrayList<Song> songList;
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
        setHasOptionsMenu(true);
        this.songList = getSongs();

        adapter = new SongAdapter(getActivity(), this.songList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MusicService.getInstance().setSongList(songList);
                Song tempSong = adapter.getItem(position);
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

    public ArrayList<Song> getSongs(){
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String projection[] = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Albums.ALBUM_ID,
                MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.YEAR};
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        songList = new ArrayList<>();
        Cursor songCursor = getActivity().getContentResolver().query(uri, projection, selection, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (songCursor != null && songCursor.moveToFirst()) {
            int songId = songCursor.getColumnIndex((MediaStore.Audio.Media._ID));
            int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int songDuration = songCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int songAlbumId = songCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ID);
            int songData = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int songYear = songCursor.getColumnIndex(MediaStore.Audio.Media.YEAR);

            do {
                String currentTitle = songCursor.getString(songTitle);
                long currentId = songCursor.getLong(songId);
                int currentDuration = songCursor.getInt(songDuration);
                String currentArtist = songCursor.getString(songArtist);
                long currentAlbumId = songCursor.getLong(songAlbumId);
                String currentData = songCursor.getString(songData);
                long currentYear = songCursor.getLong(songYear);

                Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, currentAlbumId);

                songList.add(new Song(getActivity(), currentTitle, currentId, currentDuration, currentArtist, albumArtUri));
            } while (songCursor.moveToNext());
            songCursor.close();
        }
        MusicService.getInstance().setGlobalSongList(songList);
        return songList;
    }
}
