package com.dv.apps.purpleplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.aesthetic.Aesthetic;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import jp.wasabeef.picasso.transformations.BlurTransformation;
import jp.wasabeef.picasso.transformations.ColorFilterTransformation;

import static com.dv.apps.purpleplayer.MusicService.looping;
import static com.dv.apps.purpleplayer.MusicService.randomize;
import static com.dv.apps.purpleplayer.MusicService.userStopped;

public class DetailActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {

    TextView textView1, textView2;
    ImageView imageView, rootBackground;
    ImageButton playPause, loop, next, prev, shuffle, showLyrics;
    int currentPrimaryColor;
    SeekBar seekBar;

    Handler seekHandler;

    SharedPreferences preferences;

    ShareActionProvider shareActionProvider;

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
                playPause.setImageResource(R.mipmap.ic_play);
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

//            Glide.with(getApplicationContext())
//                    .load(metadata.getDescription().getIconUri())
//                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(30, 0)))
//                    .apply(new RequestOptions().placeholder(imageView.getDrawable()).error(R.mipmap.ic_launcher_web))
//                    .transition(DrawableTransitionOptions.withCrossFade())
//                    .into(imageView);

            Picasso.with(getApplicationContext())
                    .load(metadata.getDescription().getIconUri())
                    .transform(new jp.wasabeef.picasso.transformations.RoundedCornersTransformation(20, 0))
                    .placeholder(R.mipmap.ic_launcher_web)
                    .into(imageView);

            if (preferences.getBoolean("Use_Root_Background", false)) {
//                Glide.with(getApplicationContext())
//                        .load(metadata.getDescription().getIconUri())
//                        .apply(new RequestOptions().placeholder(rootBackground.getDrawable()).error(R.mipmap.background_list))
//                        .apply(RequestOptions.bitmapTransform(new BlurTransformation(30)))
//                        .transition(DrawableTransitionOptions.withCrossFade())
//                        .into(rootBackground);

                Picasso.with(getApplicationContext())
                        .load(metadata.getDescription().getIconUri())
                        .fit()
                        .error(new ColorDrawable(currentPrimaryColor))
                        .placeholder(rootBackground.getDrawable())
                        .transform(new BlurTransformation(getApplicationContext(), 20))
                        .into(rootBackground);
            }else{
//                Glide.with(getApplicationContext())
//                        .load(R.mipmap.background_list)
//                        .transition(DrawableTransitionOptions.withCrossFade())
//                        .into(rootBackground);
//                if  (Build.VERSION.SDK_INT >= 21 && (rootBackground.getDrawable() != null))  {
//                    rootBackground.setColorFilter(Aesthetic.get().colorPrimary().blockingFirst(), PorterDuff.Mode.OVERLAY);
//                }

                Picasso.with(getApplicationContext())
                        .load(R.mipmap.background_list)
                        .fit()
                        .transform(new ColorFilterTransformation(currentPrimaryColor))
                        .into(rootBackground);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Aesthetic.attach(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_activity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Now Playing");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        currentPrimaryColor = Aesthetic.get().colorPrimary().blockingFirst();

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        preferences.registerOnSharedPreferenceChangeListener(this);

        mediaBrowserCompat = new MediaBrowserCompat(this, new ComponentName(this, MusicService.class), connectionCallback, null);

//        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, android.R.color.transparent)));
//        getWindow().getDecorView().setBackgroundResource(R.mipmap.background_list);
//        getWindow().getDecorView().getBackground().setColorFilter(new ColorDrawable(preferences.getInt("primary_color", PRIMARY_COLOR_DEFAULT)).getColor(), PorterDuff.Mode.ADD);

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
//        Glide.with(getApplicationContext())
//                .load(MediaControllerCompat.getMediaController(this).getMetadata().getDescription().getIconUri())
//                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(30, 0)))
//                .apply(new RequestOptions().placeholder(imageView.getDrawable()).error(R.mipmap.ic_launcher_web))
//                .transition(DrawableTransitionOptions.withCrossFade())
//                .into(imageView);

        Picasso.with(getApplicationContext())
                .load(MediaControllerCompat.getMediaController(this).getMetadata().getDescription().getIconUri())
                .transform(new jp.wasabeef.picasso.transformations.RoundedCornersTransformation(20, 0))
                .placeholder(R.mipmap.ic_launcher_web)
                .into(imageView);

        rootBackground = findViewById(R.id.root_background);
        if (preferences.getBoolean("Use_Root_Background", false)) {
//            Glide.with(getApplicationContext())
//                    .load(MediaControllerCompat.getMediaController(this).getMetadata().getDescription().getIconUri())
//                    .apply(RequestOptions.bitmapTransform(new BlurTransformation(30)))
//                    .transition(DrawableTransitionOptions.withCrossFade())
//                    .into(rootBackground);

            Picasso.with(getApplicationContext())
                    .load(MediaControllerCompat.getMediaController(this).getMetadata().getDescription().getIconUri())
                    .error(new ColorDrawable(currentPrimaryColor))
                    .placeholder(rootBackground.getDrawable())
                    .fit()
                    .transform(new BlurTransformation(getApplicationContext(), 20))
                    .into(rootBackground);
        }else{
//            Glide.with(getApplicationContext())
//                    .load(R.mipmap.background_list)
//                    .transition(DrawableTransitionOptions.withCrossFade())
//                    .into(rootBackground);
//            if  (Build.VERSION.SDK_INT >= 21 && (rootBackground.getDrawable() != null))  {
//                rootBackground.setColorFilter(Aesthetic.get().colorPrimary().blockingFirst(), PorterDuff.Mode.OVERLAY);
//            }

            Picasso.with(getApplicationContext())
                    .load(R.mipmap.background_list)
                    .fit()
                    .transform(new ColorFilterTransformation(currentPrimaryColor))
                    .into(rootBackground);
        }

        //Play Pause Button
        playPause = (ImageButton) findViewById(R.id.playPause);
        if (MediaControllerCompat.getMediaController(this).getPlaybackState().getState() == PlaybackStateCompat.STATE_PAUSED ||
                MediaControllerCompat.getMediaController(this).getPlaybackState().getState() == PlaybackStateCompat.STATE_NONE ||
                MediaControllerCompat.getMediaController(this).getPlaybackState().getState() == PlaybackStateCompat.STATE_STOPPED){
            playPause.setImageResource(R.mipmap.ic_play);
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
                //boolean to check if use QuickLyric OR https://api.lyrics.ovh/v1/
                boolean useQuickLyric = preferences.getBoolean(getString(R.string.key_use_quicklyric), false);

                String ArtName = (String) MediaControllerCompat.getMediaController(this).getMetadata().getDescription().getSubtitle();
                String SongName = (String) MediaControllerCompat.getMediaController(this).getMetadata().getDescription().getTitle();
                if ((ArtName == null) || (SongName == null)) {
                    Toast.makeText(this, "Nothing is playing !!", Toast.LENGTH_SHORT).show();
                    break;
                }
                if (useQuickLyric) {
                    boolean qLInstalled = isQLInstalled(getApplicationContext());
                    if (qLInstalled) {
                        startActivity(new Intent("com.geecko.QuickLyric.getLyrics")
                                .putExtra("TAGS", new String[]{ArtName, SongName}));
                    } else {
                        installQL();
                    }
                } else {
                    String artistAndSongName[] = {ArtName, SongName};
                    FetchLyricsTask fetchLyricsTask = new FetchLyricsTask();
                    fetchLyricsTask.execute(artistAndSongName);
                }

                break;

            case R.id.playPause:
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
                if ((MusicService.getInstance().songList != null) && (MusicService.getInstance().songList.size() != 0)) {
                    MediaControllerCompat.getMediaController(this).getTransportControls().skipToNext();
                }else {
                    Toast.makeText(this, "Empty playlist !! \nSelect a Song", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.prev:
                if ((MusicService.getInstance().songList != null) && (MusicService.getInstance().songList.size() != 0)) {
                    MediaControllerCompat.getMediaController(this).getTransportControls().skipToPrevious();
                }else {
                    Toast.makeText(this, "Empty playlist !! \nSelect a Song", Toast.LENGTH_SHORT).show();
                }
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

//        MenuItem item = menu.findItem(R.id.share_action_provider);
//        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
//        if (shareActionProvider != null){
//            shareActionProvider.setShareIntent(new Intent(Intent.ACTION_SEND).setType("audio/*").putExtra(Intent.EXTRA_STREAM, "Test"));
//        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.settingsDetail:
                Intent sIntent = new Intent(this, SettingsActivity.class);
                startActivity(sIntent);
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }


    @Override
    protected void onDestroy() {
        preferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        Aesthetic.pause(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        Aesthetic.resume(this);
        super.onResume();

    }

    /**
     * Created by Dhaval on 31-10-2017.
     * To get lyrics by lyrics.ovh
     */

    private class FetchLyricsTask extends AsyncTask<String, Integer, String> {

        HttpURLConnection urlConnection;
        MaterialDialog progressDialog, lyricsDialog;
        String artName, songName;
        int requestCode;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new MaterialDialog.Builder(DetailActivity.this)
                    .title("Checking for Lyrics")
                    .progress(true, 0)
                    .content("Please Wait")
                    .show();
        }

        @Override
        protected String doInBackground(String... args) {

            StringBuilder result = new StringBuilder();

            artName = args[0]; //Used to fire up Quicklyric
            songName = args[1]; //Used to fire up QuickLyric

            Uri lyricsBaseUri = Uri.parse("https://api.lyrics.ovh/v1/");
            Uri lyricsUri = lyricsBaseUri.buildUpon().appendPath(args[0]).appendPath(args[1]).build();

            try {
                URL url = new URL(lyricsUri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                requestCode = urlConnection.getResponseCode();
                if (requestCode == 200) {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                }else {
                    result = null;
                }

            }catch( Exception e) {
                e.printStackTrace();
            }
            finally {
                urlConnection.disconnect();
            }


            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            if (result == null || result.length() == 0){
                lyricsDialog = new MaterialDialog.Builder(DetailActivity.this)
                        .title("Oops !!")
                        .content("No Lyrics Found. \nMake sure you are connected to network and metadata is correct.")
                        .negativeText("Damn It !!")
                        .positiveText("QuickLyric")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                boolean qLInstalled = isQLInstalled(getApplicationContext());
                                if (qLInstalled) {
                                    startActivity(new Intent("com.geecko.QuickLyric.getLyrics")
                                            .putExtra("TAGS", new String[]{artName, songName}));
                                } else {
                                    installQL();
                                }
                            }
                        })
                        .show();
            }else {

                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.has("lyrics")) {
                        String s = jsonObject.getString("lyrics");
                        lyricsDialog = new MaterialDialog.Builder(DetailActivity.this)
                                .title("Lyrics")
                                .content(s)
                                .contentGravity(GravityEnum.CENTER)
                                .positiveText("QuickLyric")
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                        boolean qLInstalled = isQLInstalled(getApplicationContext());
                                        if (qLInstalled) {
                                            startActivity(new Intent("com.geecko.QuickLyric.getLyrics")
                                                    .putExtra("TAGS", new String[]{artName, songName}));
                                        } else {
                                            installQL();
                                        }
                                    }
                                })
                                .neutralText("Great")
                                .show();
                    } else {
                        Toast.makeText(DetailActivity.this, "No Lyrics Found !!", Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
