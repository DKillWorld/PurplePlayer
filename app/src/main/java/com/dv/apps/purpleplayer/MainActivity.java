package com.dv.apps.purpleplayer;

import android.content.ContentResolver;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    MediaPlayer mediaPlayer = new MediaPlayer();
    Uri uri, songUri;
    ContentResolver contentResolver;
    ArrayList<Songs> songList;
    Button playPause, stop, next, prev, shuffle;
    int currentPosition;
    TextView tvMain;
    Cursor songCursor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        songList = new ArrayList<Songs>();

        //SongList Code
        contentResolver = getContentResolver();
        uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        songCursor = contentResolver.query(uri, null, null, null, null);

        if (songCursor != null && songCursor.moveToFirst()){
            int songId = songCursor.getColumnIndex((MediaStore.Audio.Media._ID));
            int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int songDuration = songCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);

            while (songCursor.moveToNext()){
                String currentTitle = songCursor.getString(songTitle);
                long currentId = songCursor.getLong(songId);
                int currentDuration = songCursor.getInt(songDuration);
                String currentArtist = songCursor.getString(songArtist);

                songList.add(new Songs(currentTitle, currentId, currentDuration, currentArtist));
            }
        }


        //ListView creation
        ListView listView = (ListView) findViewById(R.id.lv);
        SongAdapter adapter = new SongAdapter(getApplicationContext(), songList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                currentPosition = position;
                play(position);
            }
        });



        //Play Pause Button
        playPause = (Button) findViewById(R.id.playPause);
        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        playPause.setBackgroundResource(R.drawable.ic_play_selected);
                    } else {
                        mediaPlayer.start();
                        playPause.setBackgroundResource(R.drawable.ic_pause);
                    }
                }
            }
        });

        //Stop Button
        stop = (Button) findViewById(R.id.stop);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer != null) {
                    mediaPlayer.reset();
                    mediaPlayer.release();
                }
                mediaPlayer = null;
                playPause.setBackgroundResource(R.drawable.ic_play_selected);
            }
        });

        //Next Song Button
        next = (Button) findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPosition == (songList.size() - 1)){
                    currentPosition = -1;
                }
                play(currentPosition + 1);
                currentPosition++;
            }
        });

        //Prev Song Button
        prev = (Button) findViewById(R.id.prev);
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPosition == 0){
                    currentPosition = songList.size();
                }
                play(currentPosition - 1);
                currentPosition--;
            }
        });

        //shuffle Button
        shuffle = (Button) findViewById(R.id.shuffle);
    }




    //Play Method
    public void play(int pos) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        songCursor.moveToPosition(pos + 1);
        if (songCursor.getPosition() == songList.size() + 1){
            currentPosition = 0;
            songCursor.moveToPosition(0);
        }
        String _id = songCursor.getString(songCursor.getColumnIndex(MediaStore.Audio.Media._ID));
        songUri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, _id);
        mediaPlayer = MediaPlayer.create(getApplicationContext(),songUri);
        mediaPlayer.start();
        tvMain = (TextView) findViewById(R.id.tvMain);
        tvMain.setText(songCursor.getString(songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
        tvMain.setSelected(true);
        playPause.setBackgroundResource(R.drawable.ic_pause);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                play(currentPosition + 1);
                currentPosition++;
            }
        });
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

}
