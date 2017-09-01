package com.dv.apps.purpleplayer;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import static com.dv.apps.purpleplayer.MainActivity.looping;
import static com.dv.apps.purpleplayer.MainActivity.mediaPlayer;
import static com.dv.apps.purpleplayer.MainActivity.randomize;

public class DetailActivity extends AppCompatActivity implements View.OnClickListener{

    TextView textView1, textView2;
    ImageButton playPause, loop, next, prev, shuffle, showLyrics;
    SeekBar seekBar;

    static DetailActivity detailActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_activity);

        detailActivity = this;

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

        //Next Song Button
        next = (ImageButton) findViewById(R.id.next);
        next.setOnClickListener(MainActivity.getInstance());

        //Prev Song Button
        prev = (ImageButton) findViewById(R.id.prev);
        prev.setOnClickListener(MainActivity.getInstance());

        //Seekbar
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(MainActivity.getInstance());
        updateSeekbar();

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

    public static DetailActivity getInstance(){
        return detailActivity;
    }

    //Seekbar Mechanism
    public void updateSeekbar() {
        seekBar.setMax(MainActivity.getInstance().mediaPlayer.getDuration());
        seekBar.setProgress(0);
        final Handler seekHandler = new Handler();
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (MainActivity.getInstance().mediaPlayer != null) {
                    seekBar.setProgress(MainActivity.getInstance().mediaPlayer.getCurrentPosition());
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
                String ArtName = MainActivity.getInstance().songCursor.getString
                        (MainActivity.getInstance().songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String SongName = MainActivity.getInstance().songCursor.getString
                        (MainActivity.getInstance().songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                if (qLInstalled){
                    startActivity(new Intent("com.geecko.QuickLyric.getLyrics")
                            .putExtra("TAGS", new String[]{ArtName, SongName}));
                }else {
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
}
