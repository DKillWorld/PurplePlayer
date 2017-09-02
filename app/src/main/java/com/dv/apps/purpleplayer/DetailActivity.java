package com.dv.apps.purpleplayer;

import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Virtualizer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import static com.dv.apps.purpleplayer.MainActivity.looping;
import static com.dv.apps.purpleplayer.MainActivity.mediaPlayer;
import static com.dv.apps.purpleplayer.MainActivity.randomize;
import static com.dv.apps.purpleplayer.MainActivity.songCursor;

public class DetailActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    TextView textView1, textView2;
    ImageView imageView;
    ImageButton playPause, loop, next, prev, shuffle, showLyrics;
    SeekBar seekBar;

    static DetailActivity detailActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_activity);

        detailActivity = this;

        //Title and Artise Textview, Album Art ImageView
        textView1 = (TextView) findViewById(R.id.titleDetail);
        textView2 = (TextView) findViewById(R.id.artistDetail);
        imageView = (ImageView) findViewById(R.id.albumArt);
        updateViews();

        //Play Pause Button
        playPause = (ImageButton) findViewById(R.id.playPause);
        playPause.setOnClickListener(this);

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
        if (mediaPlayer.isPlaying()){
            playPause.setImageResource(R.mipmap.ic_pause);
        }
    }

    public void updateViews(){
        //Setting up Title
        textView1.setText(songCursor.getString(songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));

        //Setting up Artist
        textView2.setText(songCursor.getString(songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));

        //Setting up AlmubArt
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Long songId = songCursor.getLong(songCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
        Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, songId);
        imageView.setImageURI(albumArtUri);
        if (imageView.getDrawable() == null){
            imageView.setImageResource(R.mipmap.ic_launcher_web);
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
                if (mediaPlayer != null) {
                    seekBar.setMax(mediaPlayer.getDuration());
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
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
                String ArtName = songCursor.getString
                        (songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String SongName = songCursor.getString
                        (songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                if (qLInstalled){
                    startActivity(new Intent("com.geecko.QuickLyric.getLyrics")
                            .putExtra("TAGS", new String[]{ArtName, SongName}));
                }else {
                    Toast.makeText(detailActivity, "Lyrics are supported by QuickLyric App", Toast.LENGTH_SHORT).show();
                    installQL();
                }
                break;

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

            case R.id.loop:
                if (!looping){
                    mediaPlayer.setLooping(true);
                    loop.setBackgroundResource(R.drawable.background_button_selected);
                    Toast.makeText(getApplicationContext(), "Repeat ON!!", Toast.LENGTH_SHORT).show();
                    looping = true;
                }else{
                    mediaPlayer.setLooping(false);
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
            if (mediaPlayer != null){
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

    @Override
    protected void onStop() {
        SharedPreferences preferences = MainActivity.getInstance().getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("Cursor_Pos", songCursor.getPosition());
        editor.apply();
        super.onStop();
    }

    public void addBassTest(View view){
        BassBoost bassBoost = new BassBoost(0, mediaPlayer.getAudioSessionId());
        bassBoost.setStrength((short) 1000);
        bassBoost.setEnabled(true);
        mediaPlayer.setAuxEffectSendLevel(1.0f);
    }

    public void addVirtualizerTest(View view){
        Virtualizer virtualizer = new Virtualizer(0, mediaPlayer.getAudioSessionId());
        virtualizer.setStrength((short) 1000);
        virtualizer.setEnabled(true);
        mediaPlayer.setAuxEffectSendLevel(1.0f);
    }
}
