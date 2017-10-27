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
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.dv.apps.purpleplayer.ListAdapters.SongAdapter;
import com.dv.apps.purpleplayer.R;
import com.dv.apps.purpleplayer.Songs;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class GenreListFragment extends Fragment {


    ListView listView;
    boolean in_detail_view = false;
    SearchView searchView;

    ArrayAdapter<String> genreAdapter;
    SongAdapter songAdapter;
    ArrayList<Songs> tempSongList;

    public GenreListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_genre_list, container, false);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        listView = view.findViewById(R.id.fragment_genre_list);
        Uri uri = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI;
        final ArrayList<String> arrayList = new ArrayList<>();
        final Cursor genreCursor = getContext().getContentResolver().query(uri, null, null, null, MediaStore.Audio.Genres.DEFAULT_SORT_ORDER);
        genreCursor.moveToPosition(0);
        do {
            String albumName = genreCursor.getString(genreCursor.getColumnIndex(MediaStore.Audio.Genres.NAME));
            arrayList.add(albumName);
        }while (genreCursor.moveToNext());
        genreAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item,R.id.songName, arrayList);
        listView.setAdapter(genreAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!in_detail_view) {
                    String s = genreAdapter.getItem(position);
                    genreCursor.moveToPosition(arrayList.indexOf(s));

                    Uri uri1 = MediaStore.Audio.Genres.Members.getContentUri("external", genreAdapter.getItemId(position));

                    tempSongList = new ArrayList<Songs>();
                    Cursor songCursor = getActivity().getContentResolver().query(uri1, null, null, null, MediaStore.Audio.Genres.Members.DEFAULT_SORT_ORDER);
                    if (songCursor != null && songCursor.moveToFirst()) {
                        int songId = songCursor.getColumnIndex((MediaStore.Audio.Genres.Members.AUDIO_ID));
                        int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Genres.Members.TITLE);
                        int songDuration = songCursor.getColumnIndex(MediaStore.Audio.Genres.Members.DURATION);
                        int songArtist = songCursor.getColumnIndex(MediaStore.Audio.Genres.Members.ARTIST);
                        int songAlbumId = songCursor.getColumnIndex(MediaStore.Audio.Genres.Members.ALBUM_ID);

                        do {
                            String currentTitle = songCursor.getString(songTitle);
                            long currentId = songCursor.getLong(songId);
                            int currentDuration = songCursor.getInt(songDuration);
                            String currentArtist = songCursor.getString(songArtist);
                            long currentAlbumId = songCursor.getLong(songAlbumId);

                            Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                            Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, currentAlbumId);

                            tempSongList.add(new Songs(getActivity(), currentTitle, currentId, currentDuration, currentArtist, albumArtUri));
                        } while (songCursor.moveToNext());
                        songCursor.close();
                        songAdapter = new SongAdapter(getActivity(), tempSongList);
                    }
                    listView.setAdapter(songAdapter);
                    in_detail_view = true;
                    getActivity().invalidateOptionsMenu();
                } else {

                    Songs tempSong = songAdapter.getItem(position);
                    MediaControllerCompat.getMediaController(getActivity()).getTransportControls()
                            .playFromSearch(tempSong.getTitle(), null);
                }
            }
        });
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        MenuItem closeItem = menu.findItem(R.id.close);

        if (!in_detail_view) {
            searchItem.setVisible(true);
            searchView = (SearchView) searchItem.getActionView();
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    genreAdapter.getFilter().filter(newText);
                    return true;
                }
            });
            closeItem.setVisible(false);


        }else {
            closeItem.setVisible(true);
            searchItem.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.close:
                in_detail_view = false;
                listView.setAdapter(genreAdapter);
                genreAdapter.getFilter().filter("");
                getActivity().invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}