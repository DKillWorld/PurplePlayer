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
import android.widget.ImageView;
import android.widget.ListView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.dv.apps.purpleplayer.ListAdapters.SongAdapter;
import com.dv.apps.purpleplayer.R;
import com.dv.apps.purpleplayer.Songs;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class AlbumListFragment extends Fragment {


    boolean in_detail_view = false;

    ListView listView;
    ImageView imageView;
    SearchView searchView;

    ArrayAdapter<String> albumAdapter;
    SongAdapter songAdapter;
    ArrayList<Songs> tempSongList;

    public AlbumListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_album_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        listView = view.findViewById(R.id.fragment_album_list);
        imageView = view.findViewById(R.id.fragment_album_image);
        Uri uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        final ArrayList<String> arrayList = new ArrayList<>();
        final Cursor albumCursor = getContext().getContentResolver().query(uri, null, null, null, MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);
        albumCursor.moveToPosition(0);
        do {
            String albumName = albumCursor.getString(albumCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM));
            arrayList.add(albumName);
        }while (albumCursor.moveToNext());
        albumAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item,R.id.songName, arrayList);
        listView.setAdapter(albumAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!in_detail_view) {
                    String s = albumAdapter.getItem(position);
                    albumCursor.moveToPosition(arrayList.indexOf(s));

                    Uri uri1 = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    String selection = MediaStore.Audio.Albums.ALBUM + " = ?";
                    String selectrionArgs[] = {albumCursor.getString(albumCursor.getColumnIndex((MediaStore.Audio.Albums.ALBUM)))};

                    tempSongList = new ArrayList<Songs>();
                    Cursor songCursor = getActivity().getContentResolver().query(uri1, null, selection, selectrionArgs, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
                    if (songCursor != null && songCursor.moveToFirst()) {
                        int songId = songCursor.getColumnIndex((MediaStore.Audio.Media._ID));
                        int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                        int songDuration = songCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
                        int songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                        int songAlbumId = songCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ID);

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
                    imageView.setVisibility(View.VISIBLE);
                    listView.setAdapter(songAdapter);
                    in_detail_view = true;
                    getActivity().invalidateOptionsMenu();
                } else {

                    Songs tempSong = songAdapter.getItem(position);
                    MediaControllerCompat.getMediaController(getActivity()).getTransportControls()
                            .playFromSearch(tempSong.getTitle(), null);
                }
                Glide.with(getActivity()).load(ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"),
                        albumCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ID)))
                        .apply(new RequestOptions().centerCrop())
                        .apply(new RequestOptions().placeholder(R.mipmap.ic_launcher_web)).
                        into(imageView);




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
                    albumAdapter.getFilter().filter(newText);
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
                listView.setAdapter(albumAdapter);
                albumAdapter.getFilter().filter("");
                imageView.setVisibility(View.GONE);
                getActivity().invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
