package com.dv.apps.purpleplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.dv.apps.purpleplayer.MainActivity.userStopped;

/**
 * Created by Dhaval on 18-09-2017.
 */

public class MusicService extends MediaBrowserServiceCompat implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener{

    MediaPlayer mediaPlayer;
    ArrayList<Songs> songList;
    int songPosn;

    AudioManager audioManager;
    AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener;
    BroadcastReceiver becomingNoisyReceiver;

    MediaSessionCompat mediaSessionCompat;
    PlaybackStateCompat playbackStateCompat;

    private static final int NOTIFY_ID = 1;
    private String songTitle;
    private String songArtist;

    private final IBinder binder = new MusicBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return null;
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {

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
        initMediaSession();
        getAudioFocus();
        setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);


        becomingNoisyReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //pause audio on ACTION_AUDIO_BECOMING_NOISY
                pausePlayer();
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mediaSessionCompat, intent);
        return START_STICKY;
    }



    public void setupMetadata(){
        MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();
        builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, getSong().getTitle());
        builder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, getSong().getArtist());
        builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, getSong().getImageBitmap());
        builder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, getSong().getDuration());
        mediaSessionCompat.setMetadata(builder.build());

    }

    public void setMediaPlaybackState(int state){
        PlaybackStateCompat.Builder builder = new PlaybackStateCompat.Builder();

        if (state == PlaybackStateCompat.STATE_PLAYING){
            builder.setActions(PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS | PlaybackStateCompat.ACTION_FAST_FORWARD |
                    PlaybackStateCompat.ACTION_REWIND);
        }else {
            builder.setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS | PlaybackStateCompat.ACTION_FAST_FORWARD |
                    PlaybackStateCompat.ACTION_REWIND);
        }
        builder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1);
        playbackStateCompat = builder.build();
        mediaSessionCompat.setPlaybackState(playbackStateCompat);

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

    public void initMediaSession(){
        mediaSessionCompat = new MediaSessionCompat(getApplicationContext(), "TAG");
        mediaSessionCompat.setCallback(new MediaSessionCompat.Callback(){
            @Override
            public void onSkipToNext() {
                MainActivity.getInstance().playNext();
                super.onSkipToNext();

            }

            @Override
            public void onSkipToPrevious() {
                MainActivity.getInstance().playPrev();
                super.onSkipToPrevious();
            }

            @Override
            public void onPause() {
                pausePlayer();
                super.onPause();
            }

            @Override
            public void onPlay() {
                startPlayer();
                super.onPlay();
            }

            @Override
            public void onFastForward() {
                if ((mediaPlayer.getCurrentPosition() + 15000) < mediaPlayer.getDuration()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 15000);
                }
                super.onFastForward();
            }

            @Override
            public void onRewind() {
                if ((mediaPlayer.getCurrentPosition() - 15000) > 0){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 15000);
                }
                super.onRewind();
            }
        });
        mediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS | MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS);

        Intent mediaButoonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButoonIntent.setClass(this, MediaButtonReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, mediaButoonIntent, 0);
        mediaSessionCompat.setMediaButtonReceiver(pendingIntent);

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
        songArtist = sampleSong.getArtist();
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
        setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);
        stopForeground(false);
        updateNotification();

    }

    public void seekTo(int posn){
        mediaPlayer.seekTo(posn);
    }

    public void startPlayer(){
        mediaPlayer.start();
        setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
        startForeground(NOTIFY_ID, setupNotification());
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

    private void registerBecomingNoisyReceiver() {
        //register after getting audio focus
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        playNext();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (getAudioFocus() == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            registerBecomingNoisyReceiver();
            mediaPlayer.start();
            setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);

            MainActivity.getInstance().controller.show(5000);

            MainActivity.getInstance().updateViews();
        }

        if (MainActivity.getInstance() != null){
            MainActivity.getInstance().updateViews();
            MainActivity.getInstance().updatePreferences();
        }

        if (DetailActivity.getInstance() != null) {
            DetailActivity.getInstance().updateViews();
            DetailActivity.getInstance().updateSeekbar();
        }

        mediaSessionCompat.setActive(true);
        setupMetadata();
        setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
        startForeground(NOTIFY_ID, setupNotification());
        updateNotification();

//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
//
//        builder.setContentIntent(pendingIntent)
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setTicker(songTitle)
//                .setOngoing(true)
//                .setContentTitle(songTitle)
//                .setContentText(songArtist);
//
//        Notification notification = builder.build();
//        startForeground(NOTIFY_ID, notification);


    }

    public Notification setupNotification(){
        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = MediaStyleHelper.from(getApplicationContext(), mediaSessionCompat);
        builder.setContentIntent(pendingIntent);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(getSong().getImageBitmap())
                .setColor(ContextCompat.getColor(this, android.R.color.holo_purple));
        builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_previous, "Prev", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)));
        builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_rew, "FastReve", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_REWIND)));
        if (playbackStateCompat.getState() == PlaybackStateCompat.STATE_PLAYING){
            builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_pause, "Pause", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PAUSE)));
        }else {
            builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_play, "Play", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY)));
        }
        builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_ff, "FastForw", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_FAST_FORWARD)));
        builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_next, "Next", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)));


        builder.setShowWhen(false);
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        builder.setStyle(new NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(2)
                .setMediaSession(mediaSessionCompat.getSessionToken()));
        return builder.build();

    }

    public void updateNotification(){
        if (playbackStateCompat.getState() == PlaybackStateCompat.ACTION_PAUSE) {
            startForeground(NOTIFY_ID, setupNotification());
            stopForeground(false);
        }else if(playbackStateCompat.getState() == PlaybackStateCompat.ACTION_PLAY){
            startForeground(NOTIFY_ID, setupNotification());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaSessionCompat.setActive(false);
        mediaSessionCompat.release();
        audioManager.abandonAudioFocus(onAudioFocusChangeListener);
        stopForeground(true);
    }
}
