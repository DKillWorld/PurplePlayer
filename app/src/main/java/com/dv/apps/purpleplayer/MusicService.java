package com.dv.apps.purpleplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import java.io.IOException;
import java.util.ArrayList;

import static com.dv.apps.purpleplayer.MainActivity.userStopped;

/**
 * Created by Dhaval on 18-09-2017.
 */

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener{

    MediaPlayer mediaPlayer;
    ArrayList<Songs> songList;
    int songPosn;

    AudioManager audioManager;
    AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener;

    private static final int NOTIFY_ID = 1;
    private String songTitle;

    private final IBinder binder = new MusicBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mediaPlayer.stop();
        mediaPlayer.release();
        return false;
    }

    public class MusicBinder extends Binder{
        MusicService getService(){
            return MusicService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        songPosn = 0;
        mediaPlayer = new MediaPlayer();
        initMusicPlayer();
        getAudioFocus();
    }

    public int getAudioFocus(){

        //AudioManager Code
        if (onAudioFocusChangeListener == null) {
            onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
                @Override
                public void onAudioFocusChange(int focusChange) {
                    audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

                    switch (focusChange) {

                        case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK):
                            mediaPlayer.setVolume(0.2f, 0.2f);
                            break;

                        case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT):
                            mediaPlayer.pause();
                            userStopped = false;
                            break;

                        case (AudioManager.AUDIOFOCUS_LOSS):
                            mediaPlayer.pause();
                            userStopped = false;
                            break;

                        case (AudioManager.AUDIOFOCUS_GAIN):
                            mediaPlayer.setVolume(1f, 1f);
                            startPlayer();
                            break;

                    }

                }
            };
        }


        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int focusResult = audioManager.requestAudioFocus(onAudioFocusChangeListener , AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        return focusResult;

    }

    public void initMusicPlayer(){
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnCompletionListener(this);

    }

    public void setList(ArrayList<Songs> songList){
        this.songList = songList;
    }

    public void setSong(int songIndex){
        songPosn = songIndex;
    }

    public Songs getSong(){
        return songList.get(songPosn);
    }

    public void playSong(){
        mediaPlayer.reset();
        Songs sampleSong = songList.get(songPosn);
        songTitle = sampleSong.getTitle();
        long currentSong = sampleSong.getId();
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currentSong);

        try {
            mediaPlayer.setDataSource(getApplicationContext(), trackUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.prepareAsync();
    }

    public int getPosn(){
        return mediaPlayer.getCurrentPosition();
    }

    public int getDur(){
        return mediaPlayer.getDuration();
    }

    public boolean isPlaying(){
        return mediaPlayer.isPlaying();
    }

    public void pausePlayer(){
        mediaPlayer.pause();
    }

    public void seekTo(int posn){
        mediaPlayer.seekTo(posn);
    }

    public void startPlayer(){
        mediaPlayer.start();
    }

    public void playPrev(){
        songPosn--;
        if (songPosn == 0){
            songPosn = songList.size() - 1;
        }
        playSong();
    }

    public void playNext(){
        songPosn++;
        if (songPosn == songList.size()){
            songPosn = 0;
        }
        playSong();
    }

    public void setLooping(boolean value){
        if (value){
            mediaPlayer.setLooping(true);
        }else {
            mediaPlayer.setLooping(false);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mediaPlayer.start();

        if (DetailActivity.getInstance() != null) {
            DetailActivity.getInstance().updateViews();
        }

        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(songTitle);

        Notification notification = builder.build();
        startForeground(NOTIFY_ID, notification);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        audioManager.abandonAudioFocus(onAudioFocusChangeListener);
        stopForeground(true);
    }
}
