package com.dv.apps.purpleplayer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Virtualizer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import static com.dv.apps.purpleplayer.MainActivity.bassBoost;
import static com.dv.apps.purpleplayer.MainActivity.looping;
import static com.dv.apps.purpleplayer.MainActivity.musicService;
import static com.dv.apps.purpleplayer.MainActivity.randomize;
import static com.dv.apps.purpleplayer.MainActivity.virtualizer;

public class DetailActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    TextView textView1, textView2;
    ImageView imageView;
    ImageButton playPause, loop, next, prev, shuffle, showLyrics;
    SeekBar seekBar;

    boolean BASS_BOOST_ATTACHED = false;
    boolean VIRTUALIZER_ATTACHED = false;


    static DetailActivity detailActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean("Theme_Key", false)){
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_activity);

        detailActivity = this;

        //Title and Artise Textview, Album Art ImageView
        textView1 = (TextView) findViewById(R.id.titleDetail);
        textView2 = (TextView) findViewById(R.id.artistDetail);
        imageView = (ImageView) findViewById(R.id.albumArt);

        //Play Pause Button
        playPause = (ImageButton) findViewById(R.id.playPause);
        playPause.setOnClickListener(this);
        updateViews();

        //Loop Button
        loop = (ImageButton) findViewById(R.id.loop);
        loop.setOnClickListener(this);

        //shuffle Button
        shuffle = (ImageButton) findViewById(R.id.shuffle);
        shuffle.setOnClickListener(this);

        showLyrics = (ImageButton) findViewById(R.id.showLyrics);
        showLyrics.setOnClickListener(this);

        //Seekbar
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(this);
        updateSeekbar();

        //Next Song Button
        next = (ImageButton) findViewById(R.id.next);
        next.setOnClickListener(MainActivity.getInstance());

        //Prev Song Button
        prev = (ImageButton) findViewById(R.id.prev);
        prev.setOnClickListener(MainActivity.getInstance());

        //Setting Loop and Shuffle Button to correct State
        if (looping){
            loop.setBackgroundResource(R.drawable.background_button_selected);
        }
        if (randomize){
            shuffle.setBackgroundResource(R.drawable.background_button_selected);
        }
        if (musicService.isPlaying()){
            playPause.setImageResource(R.mipmap.ic_pause);
        }

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, android.R.color.transparent)));
        getWindow().getDecorView().setBackgroundResource(R.mipmap.background_list);

    }


    public void updateViews(){
        //Setting up Title
        textView1.setText(musicService.getSong().getTitle());

        //Setting up Artist
        textView2.setText(musicService.getSong().getArtist());

        //Setting up AlmubArt
        imageView.setImageURI(musicService.getSong().getImage());
        if (imageView.getDrawable() == null){
            imageView.setImageResource(R.mipmap.ic_launcher_web);
        }

        if (musicService.isPlaying()){
            playPause.setImageResource(R.mipmap.ic_pause);
        }
        if (!musicService.isPlaying()){
            playPause.setImageResource(R.mipmap.ic_launcher);
        }

    }

    public static DetailActivity getInstance(){
        return detailActivity;
    }

    //Seekbar Mechanism
    public void updateSeekbar() {
        seekBar.setProgress(0);
        final Handler seekHandler = new Handler();
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (musicService.isPlaying()) {
                    seekBar.setMax(musicService.getDur());
                    seekBar.setProgress(musicService.getPosn());
                    seekHandler.postDelayed(this, 1000);
                }
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.showLyrics:
                boolean qLInstalled = isQLInstalled();
                String ArtName = musicService.getSong().getArtist();
                String SongName = musicService.getSong().getTitle();
                if (qLInstalled){
                    startActivity(new Intent("com.geecko.QuickLyric.getLyrics")
                            .putExtra("TAGS", new String[]{ArtName, SongName}));
                }else {
                    installQL();
                }
                break;

            case R.id.playPause:
                if (musicService.isPlaying()) {
                    musicService.pausePlayer();
                    playPause.setImageResource(R.mipmap.ic_launcher);
                } else {
                    musicService.getDur();
                    if (musicService.getDur() == 0) {
                        musicService.playSong();
                        playPause.setImageResource(R.mipmap.ic_pause);
                    }else {
                        musicService.startPlayer();
                        playPause.setImageResource(R.mipmap.ic_pause);
                    }
                }
                break;

            case R.id.loop:
                if (!looping){
                    musicService.setLooping(true);
                    loop.setBackgroundResource(R.drawable.background_button_selected);
                    Toast.makeText(getApplicationContext(), "Repeat ON!!", Toast.LENGTH_SHORT).show();
                    looping = true;
                }else{
                    musicService.setLooping(false);
                    loop.setBackgroundResource(R.drawable.background_buttons);
                    Toast.makeText(getApplicationContext(), "Repeat OFF!!", Toast.LENGTH_SHORT).show();
                    looping = false;
                }
                break;

            case R.id.shuffle:
                if (!randomize){
                    randomize = true;
                    shuffle.setBackgroundResource(R.drawable.background_button_selected);
                    Toast.makeText(getApplicationContext(), "Shuffle ON!!", Toast.LENGTH_SHORT).show();
                }else{
                    randomize = false;
                    shuffle.setBackgroundResource(R.drawable.background_buttons);
                    Toast.makeText(getApplicationContext(), "Shuffle OFF!!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private static boolean isQLInstalled() {
        PackageManager pm = DetailActivity.getInstance().getPackageManager();
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
            if (musicService != null){
                musicService.seekTo(progress);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public void addBassTest(View view){
        if (!BASS_BOOST_ATTACHED) {
            bassBoost = new BassBoost(0, musicService.mediaPlayer.getAudioSessionId());
            bassBoost.setStrength((short) 1000);
            bassBoost.setEnabled(true);
            musicService.mediaPlayer.setAuxEffectSendLevel(1.0f);
            BASS_BOOST_ATTACHED = true;
        }else {
            bassBoost.setEnabled(false);
            BASS_BOOST_ATTACHED = false;
        }
    }

    public void addVirtualizerTest(View view){
        if (!VIRTUALIZER_ATTACHED) {
            virtualizer = new Virtualizer(0, musicService.mediaPlayer.getAudioSessionId());
            virtualizer.setStrength((short) 1000);
            virtualizer.setEnabled(true);
            musicService.mediaPlayer.setAuxEffectSendLevel(1.0f);
            VIRTUALIZER_ATTACHED = true;
        }else {
            virtualizer.setEnabled(false);
            VIRTUALIZER_ATTACHED = false;
        }
    }
}
