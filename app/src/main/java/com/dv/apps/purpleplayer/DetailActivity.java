package com.dv.apps.purpleplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

import static com.dv.apps.purpleplayer.MusicService.looping;
import static com.dv.apps.purpleplayer.MusicService.randomize;
import static com.dv.apps.purpleplayer.MusicService.userStopped;

public class DetailActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    TextView textView1, textView2;
    ImageView imageView;
    ImageButton playPause, loop, next, prev, shuffle, showLyrics;
    SeekBar seekBar;

    Handler seekHandler;

    private MediaBrowserCompat mediaBrowserCompat;
    private MediaBrowserCompat.ConnectionCallback connectionCallback = new MediaBrowserCompat.ConnectionCallback(){
        @Override
        public void onConnected() {
            super.onConnected();
            MediaSessionCompat.Token token = mediaBrowserCompat.getSessionToken();
            MediaControllerCompat mediaControllerCompat = null;
            try {
                mediaControllerCompat = new MediaControllerCompat(DetailActivity.this, token);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            MediaControllerCompat.setMediaController(DetailActivity.this, mediaControllerCompat);
            buildTransportControls();
        }
    };
    private MediaControllerCompat.Callback mediaControllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            if (state.getState() == PlaybackStateCompat.STATE_PLAYING){
                playPause.setImageResource(R.mipmap.ic_pause);
            }else if (state.getState() == PlaybackStateCompat.STATE_PAUSED
                    | state.getState() == PlaybackStateCompat.STATE_STOPPED){
                playPause.setImageResource(R.mipmap.ic_launcher);
            }
            updateSeekbar();
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);

            //Setting up Title
            textView1.setText(metadata.getDescription().getTitle());

            //Setting up Artist
            textView2.setText(metadata.getDescription().getSubtitle());

            //Setting up AlmubArt

            //Old code for ImageLoading
//            imageView.setImageURI(musicService.getSong().getImage());
//            if (imageView.getDrawable() == null) {
//                imageView.setImageResource(R.mipmap.ic_launcher_web);
//            }
            Glide.with(getApplicationContext())
                    .load(metadata.getDescription().getIconUri())
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(30, 0)))
                    .apply(new RequestOptions().placeholder(imageView.getDrawable()).error(R.mipmap.ic_launcher_web))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageView);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//        if (sharedPreferences.getBoolean("Theme_Key", false)){
//            setTheme(R.style.AppTheme);
//        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_activity);

        mediaBrowserCompat = new MediaBrowserCompat(this, new ComponentName(this, MusicService.class), connectionCallback, null);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, android.R.color.transparent)));
        getWindow().getDecorView().setBackgroundResource(R.mipmap.background_list);

    }

    public void buildTransportControls(){
        final MediaControllerCompat mediaControllerCompat = MediaControllerCompat.getMediaController(DetailActivity.this);
        MediaMetadataCompat mediaMetadataCompat = mediaControllerCompat.getMetadata();
        PlaybackStateCompat playbackStateCompat = mediaControllerCompat.getPlaybackState();
        mediaControllerCompat.registerCallback(mediaControllerCallback);

        //Title and Artise Textview, Album Art ImageView
        textView1 = (TextView) findViewById(R.id.titleDetail);
        textView1.setText(MediaControllerCompat.getMediaController(this).getMetadata().getDescription().getTitle());

        textView2 = (TextView) findViewById(R.id.artistDetail);
        textView2.setText(MediaControllerCompat.getMediaController(this).getMetadata().getDescription().getSubtitle());

        imageView = (ImageView) findViewById(R.id.albumArt);
        Glide.with(getApplicationContext())
                .load(MediaControllerCompat.getMediaController(this).getMetadata().getDescription().getIconUri())
                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(30, 0)))
                .apply(new RequestOptions().placeholder(imageView.getDrawable()).error(R.mipmap.ic_launcher_web))
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView);

        //Play Pause Button
        playPause = (ImageButton) findViewById(R.id.playPause);
        if (MediaControllerCompat.getMediaController(this).getPlaybackState().getState() == PlaybackStateCompat.STATE_PAUSED ||
                MediaControllerCompat.getMediaController(this).getPlaybackState().getState() == PlaybackStateCompat.STATE_NONE ||
                MediaControllerCompat.getMediaController(this).getPlaybackState().getState() == PlaybackStateCompat.STATE_STOPPED){
            playPause.setImageResource(R.mipmap.ic_launcher);
        }else playPause.setImageResource(R.mipmap.ic_pause);
        playPause.setOnClickListener(this);

        //Loop Button
        loop = (ImageButton) findViewById(R.id.loop);
        if (looping){
            loop.setBackgroundResource(R.drawable.background_button_selected);
        }
        loop.setOnClickListener(this);

        //shuffle Button
        shuffle = (ImageButton) findViewById(R.id.shuffle);
        if (randomize){
            shuffle.setBackgroundResource(R.drawable.background_button_selected);
        }
        shuffle.setOnClickListener(this);

        showLyrics = (ImageButton) findViewById(R.id.showLyrics);
        showLyrics.setOnClickListener(this);

        //Seekbar
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(this);
        updateSeekbar();

        //Next Song Button
        next = (ImageButton) findViewById(R.id.next);
        next.setOnClickListener(this);

        //Prev Song Button
        prev = (ImageButton) findViewById(R.id.prev);
        prev.setOnClickListener(this);


    }

    //Seekbar Mechanism
    public void updateSeekbar() {
        seekBar.setProgress((int) MediaControllerCompat.getMediaController(DetailActivity.this).getPlaybackState().getPosition());
        seekBar.setMax((int) MediaControllerCompat.getMediaController(DetailActivity.this).getMetadata().getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
        if (seekHandler == null){
            seekHandler = new Handler();
        }
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaBrowserCompat.isConnected()){
                    if (MediaControllerCompat.getMediaController(DetailActivity.this).getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING ||
                            MediaControllerCompat.getMediaController(DetailActivity.this).getPlaybackState().getState() == PlaybackStateCompat.STATE_FAST_FORWARDING) {
                        int current = (int) MediaControllerCompat.getMediaController(DetailActivity.this).getPlaybackState().getPosition();
                        long timeDelta = SystemClock.elapsedRealtime() - MediaControllerCompat.getMediaController(DetailActivity.this)
                                .getPlaybackState().getLastPositionUpdateTime();
                        current += timeDelta * MediaControllerCompat.getMediaController(DetailActivity.this).getPlaybackState().getPlaybackSpeed();
                        if (current > MediaControllerCompat.getMediaController(DetailActivity.this).getMetadata().getLong(MediaMetadataCompat.METADATA_KEY_DURATION)) {

                        }
                        seekBar.setProgress(current);
                        seekHandler.postDelayed(this, 1000);
                    }
                }
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.showLyrics:
                boolean qLInstalled = isQLInstalled(getApplicationContext());
                String ArtName = MediaControllerCompat.getMediaController(this).getMetadata().getDescription().getSubtitle().toString();
                String SongName = MediaControllerCompat.getMediaController(this).getMetadata().getDescription().getTitle().toString();
                if (qLInstalled){
                    startActivity(new Intent("com.geecko.QuickLyric.getLyrics")
                            .putExtra("TAGS", new String[]{ArtName, SongName}));
                }else {
                    installQL();
                }
                break;

            case R.id.playPause:
                if (MediaControllerCompat.getMediaController(this).getPlaybackState().getState() == PlaybackStateCompat.STATE_PAUSED ||
                        MediaControllerCompat.getMediaController(this).getPlaybackState().getState() == PlaybackStateCompat.STATE_STOPPED ||
                        MediaControllerCompat.getMediaController(this).getPlaybackState().getState() == PlaybackStateCompat.STATE_NONE){
                    MediaControllerCompat.getMediaController(this).getTransportControls().play();
                    userStopped = false;
                } else {
                    MediaControllerCompat.getMediaController(this).getTransportControls().pause();
                    userStopped = true;
                }
                break;

            case R.id.loop:
                if (!looping){
                    loop.setBackgroundResource(R.drawable.background_button_selected);
                    MediaControllerCompat.getMediaController(this).getTransportControls().setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE);
                    Toast.makeText(getApplicationContext(), "Repeat ON!!", Toast.LENGTH_SHORT).show();
                }else{
                    loop.setBackgroundResource(R.drawable.background_buttons);
                    MediaControllerCompat.getMediaController(this).getTransportControls().setRepeatMode(PlaybackStateCompat.REPEAT_MODE_NONE);
                    Toast.makeText(getApplicationContext(), "Repeat OFF!!", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.shuffle:
                if (!randomize){
                    shuffle.setBackgroundResource(R.drawable.background_button_selected);
                    MediaControllerCompat.getMediaController(this).getTransportControls().setShuffleModeEnabled(true);
                    Toast.makeText(getApplicationContext(), "Shuffle ON!!", Toast.LENGTH_SHORT).show();
                }else{
                    shuffle.setBackgroundResource(R.drawable.background_buttons);
                    MediaControllerCompat.getMediaController(this).getTransportControls().setShuffleModeEnabled(false);
                    Toast.makeText(getApplicationContext(), "Shuffle OFF!!", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.next:
                MediaControllerCompat.getMediaController(this).getTransportControls().skipToNext();
                break;

            case R.id.prev:
                MediaControllerCompat.getMediaController(this).getTransportControls().skipToPrevious();
                break;
        }
    }

    private static boolean isQLInstalled(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo("com.geecko.QuickLyric", PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }
    }

    private void installQL(){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=com.geecko.QuickLyric"));
        startActivity(intent);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser){
            if (mediaBrowserCompat.isConnected()){
                MediaControllerCompat.getMediaController(this).getTransportControls().seekTo(progress);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.settingsDetail:
//                Intent sIntent = new Intent(this, SettingsActivity.class);
//                startActivity(sIntent);
                Toast.makeText(this, "Under Development !!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.equilizerDetail:
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
            case android.R.id.home:
                this.finish();
                break;
        }

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mediaBrowserCompat.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mediaBrowserCompat.disconnect();
    }
}
