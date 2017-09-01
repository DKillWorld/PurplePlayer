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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Play Pause Button
        playPause = (ImageButton) findViewById(R.id.playPause);
        playPause.setOnClickListener(MainActivity.getInstance());

        //Stop Button
        loop = (ImageButton) findViewById(R.id.loop);
        loop.setOnClickListener(MainActivity.getInstance());

        //Next Song Button
        next = (ImageButton) findViewById(R.id.next);
        next.setOnClickListener(MainActivity.getInstance());

        //Prev Song Button
        prev = (ImageButton) findViewById(R.id.prev);
        prev.setOnClickListener(MainActivity.getInstance());

        //shuffle Button
        shuffle = (ImageButton) findViewById(R.id.shuffle);
        shuffle.setOnClickListener(MainActivity.getInstance());

        //Seekbar
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(MainActivity.getInstance());
        updateSeekbar();

        showLyrics = (ImageButton) findViewById(R.id.showLyrics);
        showLyrics.setOnClickListener(this);


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
