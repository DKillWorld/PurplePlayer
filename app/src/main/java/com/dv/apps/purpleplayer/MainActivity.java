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
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dv.apps.purpleplayer.ListAdapters.SongAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.Random;

import static com.dv.apps.purpleplayer.MusicService.userStopped;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, MediaController.MediaPlayerControl, SharedPreferences.OnSharedPreferenceChangeListener{

    Cursor songCursor;
    Uri uri, songUri;
    ContentResolver contentResolver;
    static ArrayList<Songs> songList;
    SongAdapter adapter;
    ImageButton playPause, loop, next, prev, shuffle, playPauseMain;
    TextView tvMain;
    static boolean randomize = false;
    static boolean looping = false;
    SeekBar seekBar;
    SearchView searchView;
    SharedPreferences preferences;

    DrawerLayout drawerlayout;
    ListView drawerList;
    ActionBarDrawerToggle actionBarToggle;

    static MainActivity mainActivity;

    //TEST THINGS
    public static MusicService musicService;
    private Intent playIntent;
    private boolean musicBound = false;
    private ServiceConnection musicConnection;

    InterstitialAd interstitialAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//        if (sharedPreferences.getBoolean("Theme_Key", false)){
//            setTheme(R.style.AppTheme);
//        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        songList = new ArrayList<Songs>();
        setupPermissions();
        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");
        setupInterstitialAd();

        //Method to setup Drawer Layout
        setupDrawerLayout();

        preferences = getPreferences(MODE_PRIVATE);

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
                musicService.setSong(preferences.getInt("songPosn", 0));
                musicBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                musicBound = false;
            }
        };
    }

    public void updateViews(){
        if (musicService != null && songList.size() != 0) {
            tvMain.setText(musicService.getSong().getTitle());
            if (musicService.isPlaying()){
                playPauseMain.setImageResource(R.drawable.ic_pause_white_24dp);
            }
            if (!musicService.isPlaying()){
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

    public void playNext(){
        if (randomize){
            musicService.songPosn = getRandom();
            musicService.playSong();
        }else {
            musicService.playNext();
        };
    }

    public void playPrev(){
        musicService.playPrev();
    }

    public static MainActivity getInstance(){
        return mainActivity;
    }

    //songList Code
    public ArrayList<Songs> getSongs() {
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
                musicService.setSong(songList.indexOf(tempSong));
                musicService.playSong();
            }
        });

        getPreferences();
        updateViews();

        return songList;
    }

    //Updating SharedPreferences Once file is changed
    public void updatePreferences() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("songPosn", songList.indexOf(musicService.getSong()));
        editor.putBoolean("Shuffle_Status", randomize);
        editor.putBoolean("Loop_Status", looping);
        editor.apply();
    }

    public void getPreferences(){

        if (preferences != null){
            randomize = preferences.getBoolean("Shuffle_Status", false);
            looping = preferences.getBoolean("Loop_Status", false);

        }
    }

    public void setupInterstitialAd(){
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId("ca-app-pub-9589539002030859/7346365267");
        interstitialAd.loadAd(getInterstitialAdrequest());
        interstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                interstitialAd.loadAd(getInterstitialAdrequest());
            }
        });
    }

    public AdRequest getInterstitialAdrequest(){
        return new AdRequest.Builder()
                .addTestDevice("DD0CDAB405F30F550CD856F507E39725")
                .build();
    }

    public void setupDrawerLayout(){
        drawerlayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.drawer_list);
        final String s[] = {"Songs", "Albums", "Artists"};
        drawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, s));
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, "Clicked " + s[position], Toast.LENGTH_SHORT).show();
//                if (interstitialAd.isLoaded()){
//                    interstitialAd.show();
//                }
//                switch (position){
//                    case 0:
//                        Collections.sort(songList, new Comparator<Songs>() {
//                            @Override
//                            public int compare(Songs o1, Songs o2) {
//                                return o1.getTitle().compareTo(o2.getTitle());
//                            }
//                        });
//
//                    case 2:
//                        Collections.sort(songList, new Comparator<Songs>() {
//                            @Override
//                            public int compare(Songs o1, Songs o2) {
//                                return o1.getArtist().compareTo(o2.getArtist());
//                            }
//                        });
//                }
            }
        });
        actionBarToggle = new ActionBarDrawerToggle(this,drawerlayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                Toast.makeText(getApplicationContext(), "Under Development !!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        drawerlayout.setDrawerListener(actionBarToggle);;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        actionBarToggle.syncState();
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
                if (songList.size() != 0) {
                    Intent intent = new Intent(this, DetailActivity.class);
                    startActivity(intent);
                }else Toast.makeText(this, "No Songs Found !!", Toast.LENGTH_SHORT).show();
                break;

            case R.id.playPauseMain:
                if (songList.size() != 0) {
                    if (musicService.isPlaying()) {
                        musicService.pausePlayer();
                        playPauseMain.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                        userStopped = true;
                    } else {
                        musicService.getDur();
                        if (musicService.getDur() == 0) {
                            musicService.playSong();
                            playPauseMain.setImageResource(R.drawable.ic_pause_white_24dp);
                        } else {
                            musicService.startPlayer();
                            playPauseMain.setImageResource(R.drawable.ic_pause_white_24dp);
                        }
                        userStopped = false;
                    }
                }else Toast.makeText(this, "No Songs Found !!", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    //get random song when randomize/shuffle ON
    public int getRandom(){
        return new Random().nextInt(songList.size());
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
        if (actionBarToggle.onOptionsItemSelected(item)){
            return true;
        }
        switch (item.getItemId()) {
            case R.id.setting_menu:
//                Intent sIntent = new Intent(MainActivity.this, SettingsActivity.class);
//                startActivity(sIntent);
                Toast.makeText(this, "Under Development !!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.about_menu:
                Intent aIntent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(aIntent);
                break;
            case R.id.equilizer:
                Intent bIntent = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
                bIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
                bIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, musicService.mediaPlayer.getAudioSessionId());
                if (bIntent.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(bIntent, 100);
                }else {
                    Toast.makeText(this, "No Equalizer Found !!", Toast.LENGTH_SHORT).show();
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
    protected void onResume() {
        super.onResume();
        updateViews();
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
        if (musicService != null && musicBound) {
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
        return musicService.mediaPlayer.getAudioSessionId();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Toast.makeText(mainActivity, "changed", Toast.LENGTH_SHORT).show();
        if (key.equals("sort")){
            switch (sharedPreferences.getString(key, "Titles")){
                case "Titles":
                    break;
                case "Artists":
                    break;
                case "Albums":
                    break;
            }
        }
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        actionBarToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarToggle.onConfigurationChanged(newConfig);
    }
}

