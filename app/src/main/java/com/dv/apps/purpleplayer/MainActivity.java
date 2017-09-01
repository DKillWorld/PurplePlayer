package com.dv.apps.purpleplayer;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    MediaPlayer mediaPlayer = new MediaPlayer();
    Uri uri, songUri;
    ContentResolver contentResolver;
    ArrayList<Songs> songList;
    SongAdapter adapter;
    ImageButton playPause, loop, next, prev, shuffle, playPauseMain;
    TextView tvMain;
    Cursor songCursor;
    boolean randomize = false;
    boolean looping = false;
    SeekBar seekBar;
    SearchView searchView;

    static MainActivity mainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        songList = new ArrayList<Songs>();
        setupPermissions();

        mainActivity = this;

        playPauseMain = (ImageButton) findViewById(R.id.playPauseMain);
        playPauseMain.setOnClickListener(this);
    }

    public static MainActivity getInstance(){
        return mainActivity;
    }

    //songList Code
    public void getSongs() {
        contentResolver = getContentResolver();
        uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        songCursor = contentResolver.query(uri, null, selection, null,MediaStore.Audio.Media.TITLE);

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

                songList.add(new Songs(currentTitle, currentId, currentDuration, currentArtist, albumArtUri));
            } while (songCursor.moveToNext());
        }

        //ListView creation
        ListView listView = (ListView) findViewById(R.id.lv);
        listView.setFastScrollEnabled(true);
        adapter = new SongAdapter(getApplicationContext(), songList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Songs tempSong = adapter.getItem(position);
                play(songList.indexOf(tempSong));
            }
        });
    }

    //Play Method
    public void play(int pos) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        songCursor.moveToPosition(pos);
        if (songCursor.getPosition() == songList.size()) {
            songCursor.moveToPosition(0);
        }
        String _id = songCursor.getString(songCursor.getColumnIndex(MediaStore.Audio.Media._ID));
        songUri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, _id);
        mediaPlayer = MediaPlayer.create(getApplicationContext(), songUri);
        mediaPlayer.start();
        tvMain = (TextView) findViewById(R.id.tvMain);
        tvMain.setText(songCursor.getString(songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
        tvMain.setSelected(true);
        playPauseMain.setImageResource(R.drawable.ic_pause_white_24dp);
        startNotification();

        tvMain.setOnClickListener(this);



        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (randomize){
                    play(getRandom());
                } else {
                    songCursor.moveToPosition(songCursor.getPosition() + 1);
                    play(songCursor.getPosition());
                }

            }
        });
    }

//Notofication
    public void startNotification(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(songCursor.getString(songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)) + "")
                .setContentText(songCursor.getString(songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)) + "")
                .setContentIntent(contentIntent)
                .build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.playPause:
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        playPause.setImageResource(R.mipmap.ic_launcher);
                    } else {
                        mediaPlayer.start();
                        if (mediaPlayer.isPlaying()) {
                            playPause.setImageResource(R.mipmap.ic_pause);
                        }
                    }
                }
                break;

            case R.id.next:
                if (randomize){
                    play(getRandom());
                }else {
                    if (songCursor.getPosition() == (songList.size() - 1)) {
                        songCursor.moveToFirst();
                    }
                    songCursor.moveToPosition(songCursor.getPosition() + 1);
                    play(songCursor.getPosition());
                }
                break;

            case R.id.prev:
                if (songCursor.getPosition() == 0 ) {
                    songCursor.moveToPosition(songList.size());
                }
                songCursor.moveToPosition(songCursor.getPosition() - 1);
                play(songCursor.getPosition());
                break;

            case R.id.loop:
                if (!looping){
                    mediaPlayer.setLooping(true);
//                    loop.setBackgroundResource(R.drawable.background_button_selected);
                    looping = true;
                }else{
                    mediaPlayer.setLooping(false);
//                    loop.setBackgroundResource(R.drawable.background_buttons);
                    looping = false;
                }
                break;

            case R.id.shuffle:
                if (!randomize){
                    randomize = true;
//                    shuffle.setBackgroundResource(R.drawable.background_button_selected);
                    Toast.makeText(getApplicationContext(), "Random ON!!", Toast.LENGTH_SHORT).show();
                }else{
                    randomize = false;
//                    shuffle.setBackgroundResource(R.drawable.background_buttons);
                    Toast.makeText(getApplicationContext(), "Random OFF!!", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.tvMain:
                Intent intent = new Intent(this, DetailActivity.class);
                startActivity(intent);
                break;

            case R.id.playPauseMain:
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        playPauseMain.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                    } else {
                        mediaPlayer.start();
                        if (mediaPlayer.isPlaying()) {
                            playPauseMain.setImageResource(R.drawable.ic_pause_white_24dp);
                        }
                    }
                }
                break;
        }
    }

    //get random song when randomize/shuffle ON
    public int getRandom(){
        return (int) (Math.random() * songList.size());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.setting_menu:
                Toast.makeText(this, "Under Construction!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.about_menu:
                Intent aIntent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(aIntent);
                break;
            case R.id.equilizer:
                Intent bIntent = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
                if (bIntent.resolveActivity(getPackageManager()) != null){
                    startActivity(bIntent);
                }else {
                    Toast.makeText(this, "No Equilizer Found !!", Toast.LENGTH_SHORT).show();
                }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
        }
        mediaPlayer = null;

    }

    //permissionHandler
    public void setupPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED){
                getSongs();
            }else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 1);
            }
        }else {
            getSongs();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Welcome!!", Toast.LENGTH_SHORT).show();
                    getSongs();
                } else {
                    Toast.makeText(this, "One or more permission is denied !!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            if (mediaPlayer != null) {
                mediaPlayer.seekTo(progress);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}

