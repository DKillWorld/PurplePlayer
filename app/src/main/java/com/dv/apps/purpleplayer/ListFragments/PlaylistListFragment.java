package com.dv.apps.purpleplayer.ListFragments;


import android.animation.LayoutTransition;
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
import android.widget.LinearLayout;
import android.widget.ListView;

import com.dv.apps.purpleplayer.ListAdapters.SongAdapter;
import com.dv.apps.purpleplayer.Models.Song;
import com.dv.apps.purpleplayer.MusicService;
import com.dv.apps.purpleplayer.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlaylistListFragment extends Fragment {


    ListView listView;
    boolean in_detail_view = false;
    SearchView searchView;

    ArrayAdapter<String> playlistAdapter;
    SongAdapter songAdapter;
    ArrayList<Song> tempSongList;

    public PlaylistListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_playlist_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        listView = view.findViewById(R.id.fragment_playlist_list);
        Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        String projection[] = {MediaStore.Audio.Playlists._ID, MediaStore.Audio.Playlists.NAME};
        final ArrayList<String> arrayList = new ArrayList<>();
        final Cursor playlistCursor = getContext().getContentResolver().query(uri, projection, null, null, MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER);
        if (playlistCursor != null && playlistCursor.moveToFirst()) {
            do {
                String albumName = playlistCursor.getString(playlistCursor.getColumnIndex(MediaStore.Audio.Playlists.NAME));
                arrayList.add(albumName);
            } while (playlistCursor.moveToNext());
        }
        playlistAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item,R.id.songName, arrayList);
        listView.setAdapter(playlistAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!in_detail_view) {
                    String s = playlistAdapter.getItem(position);
                    playlistCursor.moveToPosition(arrayList.indexOf(s));

                    Uri uri1 = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistCursor.getLong(0));

                    tempSongList = new ArrayList<Song>();
                    Cursor songCursor = getActivity().getContentResolver().query(uri1, null, null, null, MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER);
                    if (songCursor != null && songCursor.moveToFirst()) {
                        int songId = songCursor.getColumnIndex((MediaStore.Audio.Playlists.Members.AUDIO_ID));
                        int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Playlists.Members.TITLE);
                        int songDuration = songCursor.getColumnIndex(MediaStore.Audio.Playlists.Members.DURATION);
                        int songArtist = songCursor.getColumnIndex(MediaStore.Audio.Playlists.Members.ARTIST);
                        int songAlbumId = songCursor.getColumnIndex(MediaStore.Audio.Playlists.Members.ALBUM_ID);

                        do {
                            String currentTitle = songCursor.getString(songTitle);
                            long currentId = songCursor.getLong(songId);
                            int currentDuration = songCursor.getInt(songDuration);
                            String currentArtist = songCursor.getString(songArtist);
                            long currentAlbumId = songCursor.getLong(songAlbumId);

                            Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                            Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, currentAlbumId);

                            tempSongList.add(new Song(getActivity(), currentTitle, currentId, currentDuration, currentArtist, albumArtUri));
                        } while (songCursor.moveToNext());
                        songCursor.close();
                        songAdapter = new SongAdapter(getActivity(), tempSongList);
                    }
                    listView.setAdapter(songAdapter);
                    in_detail_view = true;
                    getActivity().invalidateOptionsMenu();
                } else {
                    Song tempSong = songAdapter.getItem(position);
                    MusicService.getInstance().setSongList(tempSongList);
                    MediaControllerCompat.getMediaController(getActivity()).getTransportControls()
                            .playFromSearch(tempSong.getTitle(), null);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        in_detail_view = false;
        listView.setAdapter(playlistAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        MenuItem closeItem = menu.findItem(R.id.close);

        if (!in_detail_view) {
            searchItem.setVisible(true);
            searchView = (SearchView) searchItem.getActionView();
            LinearLayout searchBar = (LinearLayout) searchView.findViewById(R.id.search_bar);
            searchBar.setLayoutTransition(new LayoutTransition());
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    playlistAdapter.getFilter().filter(newText);
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
                listView.setAdapter(playlistAdapter);
                playlistAdapter.getFilter().filter("");
                getActivity().invalidateOptionsMenu();
//                ContentValues cv = new ContentValues();
//                cv.put(MediaStore.Audio.Playlists.NAME, "New 21/01");
//                getActivity().getContentResolver().insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, cv);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
