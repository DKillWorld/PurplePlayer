package com.dv.apps.purpleplayer;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Virtualizer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, MediaPlayer.OnPreparedListener
        , MediaPlayer.OnCompletionListener, MediaController.MediaPlayerControl{

//    static MediaPlayer mediaPlayer;
    static Cursor songCursor;
    static BassBoost bassBoost;
    static Virtualizer virtualizer;
    Uri uri, songUri;
    ContentResolver contentResolver;
    static ArrayList<Songs> songList;
    SongAdapter adapter;
    ImageButton playPause, loop, next, prev, shuffle, playPauseMain;
    TextView tvMain;
    static boolean randomize = false;
    static boolean looping = false;
    static boolean userStopped = true;
    SeekBar seekBar;
    SearchView searchView;
    AudioManager audioManager;
    int focusResult;
    SharedPreferences preferences;

    static MainActivity mainActivity;

    //TEST THINGS
    public static MusicService musicService;
    private Intent playIntent;
    private boolean musicBound = false;
    private ServiceConnection musicConnection;

    MusicController controller;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean("Theme_Key", false)){
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        songList = new ArrayList<Songs>();
        setupPermissions();
        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");

//        preferences = getPreferences(MODE_PRIVATE);
//        if ((preferences != null) && (songCursor != null)){
//            songCursor.moveToPosition(preferences.getInt("Cursor_Pos", 0));
//            if (songCursor.getPosition() == songList.size()){
//                songCursor.moveToPosition(0);
//            }
//            String _id = songCursor.getString(songCursor.getColumnIndex(MediaStore.Audio.Media._ID));
//            songUri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, _id);
//            musicService.mediaPlayer = new MediaPlayer();
//            musicService.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//            try {
//                musicService.mediaPlayer.setDataSource(getApplicationContext(), songUri);
//                musicService.mediaPlayer.prepare();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            musicService.mediaPlayer.setOnCompletionListener(this);
//
//            tvMain = (TextView) findViewById(R.id.tvMain);
//            tvMain.setText(songCursor.getString(songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
//            tvMain.setSelected(true);
//            tvMain.setOnClickListener(this);
//
//            randomize = preferences.getBoolean("Shuffle_Status", false);
//            looping = preferences.getBoolean("Loop_Status", false);
//
//        }

        mainActivity = this;

        playPauseMain = (ImageButton) findViewById(R.id.playPauseMain);
        playPauseMain.setOnClickListener(this);

        tvMain = (TextView) findViewById(R.id.tvMain);
        tvMain.setSelected(true);
        tvMain.setOnClickListener(this);

        musicConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
                musicService = binder.getService();
                musicService.setList(songList);
                musicBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                musicBound = false;
            }
        };

        setController();

    }

    public void updateViews(){
        if (musicService != null) {
            tvMain.setText(musicService.getSong().getTitle());
            if (musicService.isPlaying()){
                playPauseMain.setImageResource(R.drawable.ic_pause_white_24dp);
            }else {
                playPauseMain.setImageResource(R.drawable.ic_play_arrow_white_24dp);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (playIntent == null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);

        }
    }

    public void setController(){
        controller = new MusicController(this);
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.root_main));
        controller.setEnabled(true);
    }

    private void playNext(){
        musicService.playNext();
        controller.show();
    }

    private void playPrev(){
        musicService.playPrev();
        controller.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (musicService != null) {
            if (musicService.isPlaying()) {
                playPauseMain.setImageResource(R.drawable.ic_pause_white_24dp);
            } else {
                playPauseMain.setImageResource(R.drawable.ic_play_arrow_white_24dp);
            }
        }
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
            songCursor.close();
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
                int pos2 = songList.indexOf(tempSong);
                musicService.setSong(songList.indexOf(tempSong));
                musicService.playSong();
                controller.show();
//                play(songList.indexOf(tempSong));
            }
        });
    }

    //Play Method
    public void play(int pos) {
        musicService.songPosn = pos;
        musicService.playSong();
        if (DetailActivity.getInstance() != null) {
            DetailActivity.getInstance().updateViews();
        }

        /**
         * Old code to Play Song, Stored for reference
         * mediaPlayer = MediaPlayer.create(getApplicationContext(), songUri);   //Old Code to play song
         * mediaPlayer.start();   //Old Code to play song
         *
         */



        playPauseMain.setImageResource(R.drawable.ic_pause_white_24dp);

        musicService.mediaPlayer.setOnCompletionListener(this);
        updatePreferences();
    }

    //Updating SharedPreferences Once file is changed
    private void updatePreferences() {
        preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("Cursor_Pos", songCursor.getPosition());
        editor.putBoolean("Shuffle_Status", randomize);
        editor.putBoolean("Loop_Status", looping);
        editor.apply();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.next:
                if (randomize){
                    musicService.songPosn = getRandom();
                    musicService.playSong();
                }else {
                    playNext();
                }
                break;

            case R.id.prev:
                playPrev();
                break;

            case R.id.tvMain:
                Intent intent = new Intent(this, DetailActivity.class);
                startActivity(intent);
                break;

            case R.id.playPauseMain:
                if (musicService != null) {
                    if (musicService.isPlaying()) {
                        musicService.pausePlayer();
                        playPauseMain.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                        userStopped = true;
                    } else {
                        musicService.startPlayer();
                        if (musicService.isPlaying()) {
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
                Intent sIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(sIntent);
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
        stopService(playIntent);
        musicService = null;

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    //permissionHandler
    public void setupPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                getSongs();
            }else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
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
                    finish();
                }
            }
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (focusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
        }

        if (DetailActivity.getInstance() != null){
            DetailActivity.getInstance().playPause.setImageResource(R.mipmap.ic_pause);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (randomize){
            play(getRandom());
        } else {
            songCursor.moveToPosition(songCursor.getPosition() + 1);
            play(songCursor.getPosition());
        }

    }

    @Override
    public void start() {
        musicService.startPlayer();
    }

    @Override
    public void pause() {
        musicService.pausePlayer();
    }

    @Override
    public int getDuration() {
        if (musicService != null && musicBound){
            return musicService.getDur();
        }
        else return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (musicService != null && musicBound && musicService.isPlaying()){
                return musicService.getPosn();
        }
        else return 0;
    }

    @Override
    public void seekTo(int pos) {
        musicService.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        if (musicService != null && musicBound){
            return musicService.isPlaying();
        }
        else return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
}

