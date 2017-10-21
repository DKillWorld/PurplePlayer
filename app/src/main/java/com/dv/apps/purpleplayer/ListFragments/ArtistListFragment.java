package com.dv.apps.purpleplayer.ListFragments;


import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.media.session.MediaControllerCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.dv.apps.purpleplayer.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ArtistListFragment extends Fragment {

    ListView listView;

    public ArtistListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_artist_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listView = view.findViewById(R.id.fragment_artist_list);
        Uri uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
        ArrayList<String> arrayList = new ArrayList<>();
        Cursor albumCursor = getContext().getContentResolver().query(uri, null, null, null, MediaStore.Audio.Artists.DEFAULT_SORT_ORDER);
        albumCursor.moveToPosition(0);
        do {
            String albumName = albumCursor.getString(albumCursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST));
            arrayList.add(albumName);
        }while (albumCursor.moveToNext());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item,R.id.songName, arrayList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MediaControllerCompat.getMediaController(getActivity()).getTransportControls().play();
            }
        });
    }
}
