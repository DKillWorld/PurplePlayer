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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.aesthetic.Aesthetic;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.CircleView;
import com.dv.apps.purpleplayer.ListAdapters.SongAdapter;
import com.dv.apps.purpleplayer.ListFragments.AlbumListFragment;
import com.dv.apps.purpleplayer.ListFragments.ArtistListFragment;
import com.dv.apps.purpleplayer.ListFragments.GenreListFragment;
import com.dv.apps.purpleplayer.ListFragments.PlaylistListFragment;
import com.dv.apps.purpleplayer.ListFragments.SongListFragment;
import com.dv.apps.purpleplayer.Models.Song;
import com.eftimoff.viewpagertransformers.AccordionTransformer;
import com.eftimoff.viewpagertransformers.BackgroundToForegroundTransformer;
import com.eftimoff.viewpagertransformers.CubeInTransformer;
import com.eftimoff.viewpagertransformers.CubeOutTransformer;
import com.eftimoff.viewpagertransformers.DefaultTransformer;
import com.eftimoff.viewpagertransformers.DepthPageTransformer;
import com.eftimoff.viewpagertransformers.DrawFromBackTransformer;
import com.eftimoff.viewpagertransformers.FlipHorizontalTransformer;
import com.eftimoff.viewpagertransformers.ForegroundToBackgroundTransformer;
import com.eftimoff.viewpagertransformers.RotateDownTransformer;
import com.eftimoff.viewpagertransformers.RotateUpTransformer;
import com.eftimoff.viewpagertransformers.StackTransformer;
import com.eftimoff.viewpagertransformers.TabletTransformer;
import com.eftimoff.viewpagertransformers.ZoomInTransformer;
import com.eftimoff.viewpagertransformers.ZoomOutSlideTransformer;
import com.eftimoff.viewpagertransformers.ZoomOutTranformer;
import com.github.florent37.viewanimator.ViewAnimator;
import com.github.javiersantos.piracychecker.PiracyChecker;
import com.github.javiersantos.piracychecker.enums.Display;
import com.github.javiersantos.piracychecker.enums.InstallerID;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import static com.dv.apps.purpleplayer.MusicService.PERMISSION_GRANTED;
import static com.dv.apps.purpleplayer.MusicService.userStopped;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, RewardedVideoAdListener {

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
    RewardedVideoAd rewardedVideoAd;

    SharedPreferences preferences;

    private PiracyChecker checker;;

    public static final int LISTVIEW_BACKGROUND_COLOR_DEFAULT = -1;
    public static final int PRIMARY_COLOR_DEFAULT = -15108398;
    public static final int ACCENT_COLOR_DEFAULT = -10752;

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
            LinearLayout tvMainView = findViewById(R.id.tvMainLayout);
            ViewAnimator
                    .animate(tvMainView)
                    .bounce()
                    .accelerate()
                    .duration(1000)
                    .start();

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
                    .colorPrimary(PRIMARY_COLOR_DEFAULT)
                    .colorAccent(ACCENT_COLOR_DEFAULT)
                    .textColorSecondaryInverseRes(android.R.color.white)
                    .colorNavigationBarAuto()
                    .colorStatusBarAuto()
                    .apply();
        }

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        mediaBrowserCompat = new MediaBrowserCompat(this, new ComponentName(getApplicationContext(), MusicService.class),connectionCallback, null);
//        songList = new ArrayList<Song>();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.library);
        }

        context = this;
        adapter = new SongAdapter(getApplicationContext(), songList);

        if (BuildConfig.APPLICATION_ID.equals("com.dv.apps.purpleplayer")) {
            MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");
            setupInterstitialAd();
            setupRewardedVideoAd();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(CircleView.shiftColorDown(preferences.getInt("primary_color", PRIMARY_COLOR_DEFAULT)));
        }

        //Method to setup Drawer Layout , Permissions and TabLayout
        setupDrawerLayout2();
        setupPermissions(); //This encloses setupTabLayout && Permissions

        validate();

        //Getting Views & Applying Theme
        playPauseMain = (ImageButton) findViewById(R.id.playPauseMain);
        tvMain = (TextView) findViewById(R.id.tvMain);
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
            tvMain.setText(R.string.select_song);
        }


    }

    @Override
    protected void onStart() {
        super.onStart();
        mediaBrowserCompat.connect();
    }

    public void setupInterstitialAd(){
        if (BuildConfig.APPLICATION_ID.equals("com.dv.apps.purpleplayer")) {
            if (Calendar.getInstance().getTimeInMillis() > preferences.getLong("ad_free_till", 0)) {
                interstitialAd = new InterstitialAd(this);
                interstitialAd.setAdUnitId("ca-app-pub-9589539002030859/7346365267");
                interstitialAd.loadAd(getInterstitialAdrequest());
                interstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        interstitialAd.loadAd(getInterstitialAdrequest());
                    }
                });
            }
        }
    }

    public AdRequest getInterstitialAdrequest(){
        return new AdRequest.Builder()
                .addTestDevice("DD0CDAB405F30F550CD856F507E39725")
                .build();
    }

    public void showInterstitial(){
        if (BuildConfig.APPLICATION_ID.equals("com.dv.apps.purpleplayer")) {
            if (interstitialAd != null) {
                if (interstitialAd.isLoaded()) {
                    interstitialAd.show();
                }
            }
        }
    }

    public void setupRewardedVideoAd(){
        if (BuildConfig.APPLICATION_ID.equals("com.dv.apps.purpleplayer")) {
            if (Calendar.getInstance().getTimeInMillis() > preferences.getLong("ad_free_till", 0)) {
                rewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
                rewardedVideoAd.loadAd("ca-app-pub-9589539002030859/2050180592", getInterstitialAdrequest());
                rewardedVideoAd.setRewardedVideoAdListener(this);
            }
        }
    }

    public void showRewardedVideo(){
        if (BuildConfig.APPLICATION_ID.equals("com.dv.apps.purpleplayer")) {
            if (rewardedVideoAd.isLoaded()) {
                rewardedVideoAd.show();
            }
        }
    }

    @Override
    public void onRewarded(RewardItem reward) {
        Calendar cal = Calendar.getInstance(); //current date and time
//        cal.add(Calendar.DAY_OF_MONTH, 1); //add a day
        cal.set(Calendar.HOUR_OF_DAY, 23); //set hour to last hour
        cal.set(Calendar.MINUTE, 59); //set minutes to last minute
        cal.set(Calendar.SECOND, 59); //set seconds to last second
        cal.set(Calendar.MILLISECOND, 999); //set milliseconds to last millisecond
        long millis = cal.getTimeInMillis();
        preferences.edit().putLong("ad_free_till", millis).apply();
        if (result.getDrawerItem(9) != null){
            result.removeItem(9);
        }
        new MaterialDialog.Builder(this)
                .positiveText(R.string.ok)
                .title(R.string.info)
                .content(R.string.restartApp)
                .show();
        // Reward the user.
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
//        Toast.makeText(MainActivity.this, "onRewardedVideoAdLeftApplication",
//                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdClosed() {
//        Toast.makeText(MainActivity.this, "onRewardedVideoAdClosed", Toast.LENGTH_SHORT).show();
        if (result.getDrawerItem(9) != null){
            result.removeItem(9);
        }
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int errorCode) {
//        Toast.makeText(MainActivity.this, "onRewardedVideoAdFailedToLoad", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdLoaded() {
//        Toast.makeText(MainActivity.this, "onRewardedVideoAdLoaded", Toast.LENGTH_SHORT).show();
        if (BuildConfig.APPLICATION_ID.equals("com.dv.apps.purpleplayer")){
            result.addItem(new SecondaryDrawerItem().withIdentifier(9).withName("Remove ads for a day")
                    .withIcon(R.drawable.ic_drawer_buypro).withSelectable(false));
            if (!getSupportFragmentManager().isDestroyed()) {
                new MaterialDialog.Builder(this)
                        .content(R.string.removeAdsForDay)
                        .title(R.string.info)
                        .positiveText(R.string.ok)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                showRewardedVideo();
                            }
                        })
                        .negativeText(R.string.later)
                        .show();
            }
        }
    }

    @Override
    public void onRewardedVideoAdOpened() {
    }

    @Override
    public void onRewardedVideoStarted() {
    }

    public void setupDrawerLayout2(){

        result = new DrawerBuilder()
                .withActivity(this)
                .withSelectedItem(-1)
                .addDrawerItems(
                        new PrimaryDrawerItem().withIdentifier(1).withName(R.string.songs).withIcon(R.drawable.ic_drawer_songs).withSelectable(false),
                        new PrimaryDrawerItem().withIdentifier(2).withName(R.string.albums).withIcon(R.drawable.ic_drawer_album).withSelectable(false),
                        new PrimaryDrawerItem().withIdentifier(3).withName(R.string.artists).withIcon(R.drawable.ic_drawer_artist).withSelectable(false),
                        new PrimaryDrawerItem().withIdentifier(4).withName(R.string.genres).withIcon(R.drawable.ic_drawer_genre).withSelectable(false),
                        new PrimaryDrawerItem().withIdentifier(5).withName(R.string.playlists).withIcon(R.drawable.ic_drawer_playlist).withSelectable(false),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withIdentifier(6).withName(R.string.settings).withIcon(R.drawable.ic_drawer_settings).withSelectable(false),
                        new SecondaryDrawerItem().withIdentifier(7).withName(R.string.rateUs).withIcon(R.drawable.ic_drawer_support_development).withSelectable(false),
                        new SecondaryDrawerItem().withIdentifier(8).withName(R.string.upgradeToPurplePlayerPro).withIcon(R.drawable.ic_drawer_buypro).withSelectable(false)
//                        new SecondaryDrawerItem().withIdentifier(9).withName("Remove ads for a day").withIcon(R.drawable.ic_drawer_buypro).withSelectable(false)
//                        new SecondaryDrawerItem().withIdentifier(10).withName("Radio").withIcon(R.drawable.ic_drawer_buypro).withSelectable(false)
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
                                            .content(R.string.alreadyProUser)
                                            .positiveText(R.string.ok)
                                            .title(R.string.info)
                                            .show();
                                }
                                break;
                            case 9:
                                if (BuildConfig.APPLICATION_ID.equals("com.dv.apps.purpleplayer")){
                                    showRewardedVideo();
                                }
                                break;
                            case 10:
                                Intent intent = new Intent(MainActivity.this, RadioActivity.class);
                                startActivity(intent);
                        }

                        result.getDrawerLayout().closeDrawers();
                        return true;


                    }
                })
                .build();

        if (BuildConfig.APPLICATION_ID.equals("com.dv.apps.purpleplayerpro")){
            result.removeItem(8);
        }

        actionBarToggle = new ActionBarDrawerToggle(this,result.getDrawerLayout(), R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.app_name);
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.library);
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
                        return getResources().getString(R.string.songs);
                    case 1:
                        return getResources().getString(R.string.albums);
                    case 2:
                        return getResources().getString(R.string.artists);
                    case 3:
                        return getResources().getString(R.string.genres);
                    case 4:
                        return getResources().getString(R.string.playlists);
                    default:
                        return null;
                }
            }
        };
        viewPager.setAdapter(fragmentPagerAdapter);
        setupTransitionEffect(preferences.getString("transition_effect", "Default"));
        tabLayout.setupWithViewPager(viewPager);

    }

    public void setupTransitionEffect(String value){
        if (BuildConfig.APPLICATION_ID.equals("com.dv.apps.purpleplayerpro")) {
            switch (value) {
                case "Default":
                    viewPager.setPageTransformer(true, new DefaultTransformer());
                    break;
                case "Accordion":
                    viewPager.setPageTransformer(true, new AccordionTransformer());
                    break;
                case "BackgroundToForeground":
                    viewPager.setPageTransformer(true, new BackgroundToForegroundTransformer());
                    break;
                case "CubeIn":
                    viewPager.setPageTransformer(true, new CubeInTransformer());
                    break;
                case "CubeOut":
                    viewPager.setPageTransformer(true, new CubeOutTransformer());
                    break;
                case "DepthPage":
                    viewPager.setPageTransformer(true, new DepthPageTransformer());
                    break;
                case "FlipHorizontal":
                    viewPager.setPageTransformer(true, new FlipHorizontalTransformer());
                    break;
                case "ForegroundToBackground":
                    viewPager.setPageTransformer(true, new ForegroundToBackgroundTransformer());
                    break;
                case "RotateDown":
                    viewPager.setPageTransformer(true, new RotateDownTransformer());
                    break;
                case "RotateUp":
                    viewPager.setPageTransformer(true, new RotateUpTransformer());
                    break;
                case "DrawFromBack":
                    viewPager.setPageTransformer(true, new DrawFromBackTransformer());
                    break;
                case "Stack":
                    viewPager.setPageTransformer(true, new StackTransformer());
                    break;
                case "Tablet":
                    viewPager.setPageTransformer(true, new TabletTransformer());
                    break;
                case "ZoomIn":
                    viewPager.setPageTransformer(true, new ZoomInTransformer());
                    break;
                case "ZoomOut":
                    viewPager.setPageTransformer(true, new ZoomOutTranformer());
                    break;
                case "ZoomOutSlide":
                    viewPager.setPageTransformer(true, new ZoomOutSlideTransformer());
                    break;
                default:
                    viewPager.setPageTransformer(true, new AccordionTransformer());
                    break;

            }
        }else {
            switch (value) {
                case "Default":
                    viewPager.setPageTransformer(true, new DefaultTransformer());
                    break;
                case "Accordion":
                default:
                    viewPager.setPageTransformer(true, new AccordionTransformer());
                    break;
            }
        }
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
                    Toast.makeText(this, R.string.emptyPlaylist, Toast.LENGTH_SHORT).show();
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
                showInterstitial();
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
                    Toast.makeText(this, R.string.noEqualierFound, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.close:
                return false;
            case R.id.search:
                return false;
        }
        return true;
    }

    private void validate(){
        if (BuildConfig.APPLICATION_ID.equals("com.dv.apps.purpleplayerpro")) {
            checker = new PiracyChecker(this)
//                    .enableGooglePlayLicensing("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgBT+tKXqMH4FEejIu9Zhbs6+1N/UXFPN7TK11PYzkYe5qSvQnfENkdjXfJQ55h2aAbMn1jOXXB5xQwDHyRE2VNlrGBIplIRPFfDpZ4Vl/2niCwseLbke9VetHGIgx9vROBsJs9QMWJC0/yphxPqARXNJ+uYkQg164ZXaLcAl7/7pOxucZ9DKN0lbIqwE8eysFr6gcCeVutGfn5tDya5+cFj9zMGq6ImQSaCPTcWXm4/up2HyASKVw9TYuCgvGRvVF1BrP6ifs6uXFxZvK1mYCnVHGXPhAlQjlnTMp2k8Wy/KJdgCYRYjeMfvm+Z/KOp2mLZBW5QAc6Aro4jG9Pxr+wIDAQAB")
//                    .saveResultToSharedPreferences(preferences, "valid_license")
//                    .enableSigningCertificate("ldgUxo13aF54Jsqay5L9W/S4/g0=")     google
//                    .enableSigningCertificate("uxh/RlppBQNr/6nlf3bO4UmKmNg=")
                    .enableUnauthorizedAppsCheck()
                    .enableInstallerId(InstallerID.GOOGLE_PLAY)
                    .enableInstallerId(InstallerID.AMAZON_APP_STORE)
                    .enableInstallerId(InstallerID.GALAXY_APPS)
                    .blockIfUnauthorizedAppUninstalled(preferences, "app_unauth")
                    .display(Display.ACTIVITY);
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
                    Toast.makeText(this, R.string.welcome, Toast.LENGTH_SHORT).show();
                    PERMISSION_GRANTED = true;
                    setupTabLayout();
                } else {
                    Toast.makeText(this, R.string.oneOrMorePermissionDenied, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (rewardedVideoAd != null) {
            rewardedVideoAd.destroy(this);
        }
        if (checker != null) {
            checker.destroy();
        }
    }

    @Override
    protected void onPause() {
        Aesthetic.pause(this);
        if (rewardedVideoAd != null) {
            rewardedVideoAd.pause(this);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewPager != null) {
            setupTransitionEffect(preferences.getString("transition_effect", "Default"));
        }
        Aesthetic.resume(this);
        if (rewardedVideoAd != null) {
            rewardedVideoAd.resume(this);
        }
    }
}

