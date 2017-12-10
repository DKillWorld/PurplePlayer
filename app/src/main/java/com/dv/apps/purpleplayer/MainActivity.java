package com.dv.apps.purpleplayer;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.MediaMetadataRetriever;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.aesthetic.Aesthetic;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.CircleView;
import com.dv.apps.purpleplayer.ListAdapters.SongAdapter;
import com.dv.apps.purpleplayer.ListFragments.AlbumListFragment;
import com.dv.apps.purpleplayer.ListFragments.ArtistListFragment;
import com.dv.apps.purpleplayer.ListFragments.GenreListFragment;
import com.dv.apps.purpleplayer.ListFragments.PlaylistListFragment;
import com.dv.apps.purpleplayer.ListFragments.SongListFragment;
import com.dv.apps.purpleplayer.Models.Song;
import com.github.javiersantos.piracychecker.PiracyChecker;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.io.File;
import java.util.ArrayList;

import static com.dv.apps.purpleplayer.MusicService.PERMISSION_GRANTED;
import static com.dv.apps.purpleplayer.MusicService.userStopped;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Context context;
    ArrayList<Song> songList;
    SongAdapter adapter;
    ImageButton playPauseMain;
    TextView tvMain;
    SearchView searchView;

    DrawerLayout drawerlayout;
    TabLayout tabLayout;
    ListView drawerList;
    ActionBarDrawerToggle actionBarToggle;
    ViewPager viewPager;
    Drawer result;

    InterstitialAd interstitialAd;

    SharedPreferences preferences;

    private PiracyChecker checker;;

    public static final int LISTVIEW_BACKGROUND_COLOR_DEFAULT = -1;
    public static final int PRIMARY_COLOR_DEFAULT = -14575885;

    //TEST THINGS
    private MediaBrowserCompat mediaBrowserCompat;
    private MediaBrowserCompat.ConnectionCallback connectionCallback = new MediaBrowserCompat.ConnectionCallback(){
        @Override
        public void onConnected() {
            super.onConnected();
            MediaSessionCompat.Token token = mediaBrowserCompat.getSessionToken();
            MediaControllerCompat mediaControllerCompat = null;
            try {
                mediaControllerCompat = new MediaControllerCompat(MainActivity.this, token);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            MediaControllerCompat.setMediaController(MainActivity.this, mediaControllerCompat);
            buildTransportControls();

            //GetIntent to play From File managers
            if (Intent.ACTION_VIEW.equals(getIntent().getAction()))
            {
                MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                mediaMetadataRetriever.setDataSource(getApplicationContext(), getIntent().getData());
                String songName = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                if (songName == null) {
                    File file = new File(getIntent().getData().getPath());
                    String temp = file.getName();
                    songName = temp.substring(0, temp.lastIndexOf("."));
                }
                MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().playFromSearch(songName, null);
            }
        }
    };
    private MediaControllerCompat.Callback mediaControllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            if (state.getState() == PlaybackStateCompat.STATE_PLAYING){
                playPauseMain.setImageResource(R.drawable.ic_pause_white_24dp);
            }else if (state.getState() == PlaybackStateCompat.STATE_PAUSED
                    | state.getState() == PlaybackStateCompat.STATE_STOPPED){
                playPauseMain.setImageResource(R.drawable.ic_play_arrow_white_24dp);
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            tvMain.setText(metadata.getDescription().getTitle());

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Aesthetic.attach(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_main);
        setSupportActionBar(toolbar);

        if (Aesthetic.isFirstTime()){
            Aesthetic.get()
                    .activityTheme(R.style.AppTheme)
                    .colorPrimaryRes(android.R.color.holo_blue_dark)
                    .colorNavigationBarAuto()
                    .colorStatusBarAuto()
                    .apply();
        }

        mediaBrowserCompat = new MediaBrowserCompat(this, new ComponentName(getApplicationContext(), MusicService.class),connectionCallback, null);
//        songList = new ArrayList<Song>();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Library");
        }

        context = this;
        adapter = new SongAdapter(getApplicationContext(), songList);

        if (BuildConfig.APPLICATION_ID.equals("com.dv.apps.purpleplayer")) {
            MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");
            setupInterstitialAd();
        }

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(CircleView.shiftColorDown(preferences.getInt("primary_color", PRIMARY_COLOR_DEFAULT)));
        }

        //Method to setup Drawer Layout , Permissions and TabLayout
        setupDrawerLayout2();
        setupPermissions(); //This encloses setupTabLayout && Permissions

        //Getting Views & Applying Theme
        playPauseMain = (ImageButton) findViewById(R.id.playPauseMain);
        tvMain = (TextView) findViewById(R.id.tvMain);

        authenticate(); //Checking for license and Piracy
    }

    public void buildTransportControls(){
        final MediaControllerCompat mediaControllerCompat = MediaControllerCompat.getMediaController(MainActivity.this);
        MediaMetadataCompat mediaMetadataCompat = mediaControllerCompat.getMetadata();
        PlaybackStateCompat playbackStateCompat = mediaControllerCompat.getPlaybackState();
        mediaControllerCompat.registerCallback(mediaControllerCallback);

        playPauseMain.setOnClickListener(this);
        if (mediaControllerCompat.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING){
            playPauseMain.setImageResource(R.drawable.ic_pause_white_24dp);
        }else if (mediaControllerCompat.getPlaybackState().getState() == PlaybackStateCompat.STATE_PAUSED
                | mediaControllerCompat.getPlaybackState().getState() == PlaybackStateCompat.STATE_STOPPED){
            playPauseMain.setImageResource(R.drawable.ic_play_arrow_white_24dp);
        }

        tvMain.setSelected(true);
        tvMain.setOnClickListener(this);
        if (mediaControllerCompat.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
            tvMain.setText(mediaControllerCompat.getMetadata().getDescription().getTitle());
        }else {
            tvMain.setText("Select a Song");
        }


    }

    @Override
    protected void onStart() {
        super.onStart();
        mediaBrowserCompat.connect();
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

    public void showInterstitial(){
        if (BuildConfig.APPLICATION_ID.equals("com.dv.apps.purpleplayer")) {
            if (interstitialAd.isLoaded()) {
                interstitialAd.show();
            }
        }
    }

    public void setupDrawerLayout2(){

        result = new DrawerBuilder()
                .withActivity(this)
                .withSelectedItem(-1)
                .addDrawerItems(
                        new PrimaryDrawerItem().withIdentifier(1).withName("Songs").withIcon(R.drawable.ic_drawer_songs).withSelectable(false),
                        new PrimaryDrawerItem().withIdentifier(2).withName("Albums").withIcon(R.drawable.ic_drawer_album).withSelectable(false),
                        new PrimaryDrawerItem().withIdentifier(3).withName("Artists").withIcon(R.drawable.ic_drawer_artist).withSelectable(false),
                        new PrimaryDrawerItem().withIdentifier(4).withName("Genres").withIcon(R.drawable.ic_drawer_genre).withSelectable(false),
                        new PrimaryDrawerItem().withIdentifier(5).withName("Playlists").withIcon(R.drawable.ic_drawer_playlist).withSelectable(false),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withIdentifier(6).withName("Settings").withIcon(R.drawable.ic_drawer_settings).withSelectable(false),
                        new SecondaryDrawerItem().withIdentifier(7).withName("Rate Us ").withIcon(R.drawable.ic_drawer_support_development).withSelectable(false),
                        new SecondaryDrawerItem().withIdentifier(8).withName("Upgrade to Purple Player Pro").withIcon(R.drawable.ic_drawer_buypro).withSelectable(false)
                )
                .withTranslucentStatusBar(true)
                .withDisplayBelowStatusBar(true)
                .withActionBarDrawerToggle(true)
                .withStickyHeader(R.layout.drawer_header)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        // do something with the clicked item :D
                        int selection = (int) drawerItem.getIdentifier();

                        switch (selection){
                            case 1:
                                viewPager.setCurrentItem(0, true);
                                break;
                            case 2:
                                viewPager.setCurrentItem(1, true);
                                break;
                            case 3:
                                viewPager.setCurrentItem(2, true);
                                break;
                            case 4:
                                viewPager.setCurrentItem(3, true);
                                break;
                            case 5:
                                viewPager.setCurrentItem(4, true);
                                break;
                            case 6:
                                Intent sIntent = new Intent(MainActivity.this, SettingsActivity.class);
                                startActivity(sIntent);
                                break;
                            case 7:
                                if (BuildConfig.APPLICATION_ID.equals("com.dv.apps.purpleplayer")){
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse("market://details?id=com.dv.apps.purpleplayer"));
                                    startActivity(intent);
                                }else {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse("market://details?id=com.dv.apps.purpleplayerpro"));
                                    startActivity(intent);
                                }

                                break;
                            case 8:
                                if (BuildConfig.APPLICATION_ID.equals("com.dv.apps.purpleplayer")){
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse("market://details?id=com.dv.apps.purpleplayerpro"));
                                    startActivity(intent);
                                }else {
                                    new MaterialDialog.Builder(MainActivity.this)
                                            .content("You are already a Pro User !!")
                                            .positiveText("OK")
                                            .title("Info")
                                    .show();
                                }

                                break;
                        }

                        result.getDrawerLayout().closeDrawers();
                        return true;


                    }
                })
                .build();


        actionBarToggle = new ActionBarDrawerToggle(this,result.getDrawerLayout(), R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Purple Player");
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Library");
                }
            }
        };

        result.getDrawerLayout().setDrawerListener(actionBarToggle);;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        actionBarToggle.syncState();


    }

    public void setupTabLayout(){
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.viewpager);
        FragmentPagerAdapter fragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int i) {
                Fragment fragment;
                switch (i) {
                    case 0:
                        fragment = new SongListFragment();
                        break;
                    case 1:
                        fragment = new AlbumListFragment();
                        break;
                    case 2:
                        fragment = new ArtistListFragment();
                        break;
                    case 3:
                        fragment = new GenreListFragment();
                        break;
                    case 4:
                        fragment = new PlaylistListFragment();
                        break;
                    default:
                        fragment = new SongListFragment();
                }
                return fragment;
            }

            @Override
            public int getCount() {
                return 5;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position){
                    case 0:
                        return "Songs";
                    case 1:
                        return "Albums";
                    case 2:
                        return "Artists";
                    case 3:
                        return "Genres";
                    case 4:
                        return "Playlists";
                    default:
                        return null;
                }
            }
        };
        viewPager.setAdapter(fragmentPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.tvMain:
                Intent intent = new Intent(this, DetailActivity.class);
                startActivity(intent);
                showInterstitial();
                break;

            case R.id.playPauseMain:
                if ((MusicService.getInstance().songList != null) && (MusicService.getInstance().songList.size() != 0)) {
                    if (MediaControllerCompat.getMediaController(this).getPlaybackState().getState() == PlaybackStateCompat.STATE_PAUSED ||
                            MediaControllerCompat.getMediaController(this).getPlaybackState().getState() == PlaybackStateCompat.STATE_STOPPED ||
                            MediaControllerCompat.getMediaController(this).getPlaybackState().getState() == PlaybackStateCompat.STATE_NONE) {
                        MediaControllerCompat.getMediaController(this).getTransportControls().play();
                        userStopped = false;
                    } else {
                        MediaControllerCompat.getMediaController(this).getTransportControls().pause();
                        userStopped = true;
                    }
                }else {
                    Toast.makeText(this, "Empty playlist !! \nSelect a Song", Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarToggle.onOptionsItemSelected(item)){
            return true;
        }
        switch (item.getItemId()) {
            case R.id.setting_menu:
                Intent sIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(sIntent);
                break;
            case R.id.about_menu:
                Intent aIntent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(aIntent);
                showInterstitial();
                break;
            case R.id.equilizer:
                Intent bIntent = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
                bIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
                bIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION,
                        MediaControllerCompat.getMediaController(this).getExtras().getInt("AudioSessionId"));
                if (bIntent.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(bIntent, 100);
                }else {
                    Toast.makeText(this, "No Equalizer Found !!", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.close:
                return false;
            case R.id.search:
                return false;
        }
        return true;
    }

    private void authenticate(){
        if (BuildConfig.APPLICATION_ID.equals("com.dv.apps.purpleplayerpro")) {
            checker = new PiracyChecker(this)
                    .enableGooglePlayLicensing("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgBT+tKXqMH4FEejIu9Zhbs6+1N/UXFPN7TK11PYzkYe5qSvQnfENkdjXfJQ55h2aAbMn1jOXXB5xQwDHyRE2VNlrGBIplIRPFfDpZ4Vl/2niCwseLbke9VetHGIgx9vROBsJs9QMWJC0/yphxPqARXNJ+uYkQg164ZXaLcAl7/7pOxucZ9DKN0lbIqwE8eysFr6gcCeVutGfn5tDya5+cFj9zMGq6ImQSaCPTcWXm4/up2HyASKVw9TYuCgvGRvVF1BrP6ifs6uXFxZvK1mYCnVHGXPhAlQjlnTMp2k8Wy/KJdgCYRYjeMfvm+Z/KOp2mLZBW5QAc6Aro4jG9Pxr+wIDAQAB")
                    .saveResultToSharedPreferences(preferences, "valid_license");
            checker.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mediaBrowserCompat.disconnect();
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

    //permissionHandler
    public void setupPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                PERMISSION_GRANTED = true;
                setupTabLayout();
            }else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }else {
            PERMISSION_GRANTED = true;
            setupTabLayout();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Welcome!!", Toast.LENGTH_SHORT).show();
                    PERMISSION_GRANTED = true;
                    setupTabLayout();
                } else {
                    Toast.makeText(this, "One or more permission is denied !!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (checker != null) {
            checker.destroy();
        }
    }

    @Override
    protected void onPause() {
        Aesthetic.pause(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Aesthetic.resume(this);
    }

}

