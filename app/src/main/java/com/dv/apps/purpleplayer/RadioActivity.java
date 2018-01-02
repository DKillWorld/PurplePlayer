package com.dv.apps.purpleplayer;

import android.content.ComponentName;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.aesthetic.Aesthetic;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.dv.apps.purpleplayer.ListAdapters.RadioStationAdapter;
import com.dv.apps.purpleplayer.Models.RadioStation;
import com.dv.apps.purpleplayer.Utils.OnSwipeTouchListener;

import java.util.ArrayList;

import static com.dv.apps.purpleplayer.MusicService.userStopped;


public class RadioActivity extends AppCompatActivity implements View.OnClickListener {

    ImageButton playPauseMain;
    TextView tvMain;

    ListView listView;

    private MediaBrowserCompat mediaBrowserCompat;
    private MediaBrowserCompat.ConnectionCallback connectionCallback = new MediaBrowserCompat.ConnectionCallback(){
        @Override
        public void onConnected() {
            super.onConnected();
            MediaSessionCompat.Token token = mediaBrowserCompat.getSessionToken();
            MediaControllerCompat mediaControllerCompat = null;
            try {
                mediaControllerCompat = new MediaControllerCompat(RadioActivity.this, token);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            MediaControllerCompat.setMediaController(RadioActivity.this, mediaControllerCompat);
            buildTransportControls();
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
        setContentView(R.layout.activity_radio);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Radio");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mediaBrowserCompat = new MediaBrowserCompat(this, new ComponentName(getApplicationContext(), MusicService.class),connectionCallback, null);

        playPauseMain = (ImageButton) findViewById(R.id.playPauseMain);
        tvMain = (TextView) findViewById(R.id.tvMain);
        listView = findViewById(R.id.fragment_radiostation_list);
    }

    public void buildTransportControls(){
        final MediaControllerCompat mediaControllerCompat = MediaControllerCompat.getMediaController(RadioActivity.this);
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
        tvMain.setOnTouchListener(new OnSwipeTouchListener(this){

            @Override
            public void onSwipeRight() {
                MaterialDialog dialog = new MaterialDialog.Builder(RadioActivity.this)
                        .positiveText(R.string.ok)
                        .customView(R.layout.tag_editor_activity, true)
                        .show();
                final EditText editText = dialog.getCustomView().findViewById(R.id.editText);
                dialog.getBuilder().onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        String playLink = editText.getText().toString();
                        Bundle bundle = new Bundle();
                        bundle.putInt("Pos", 0);
                        MediaControllerCompat.getMediaController(RadioActivity.this).getTransportControls()
                                .playFromUri(Uri.parse(playLink), bundle);
                    }
                });
            }
        });
        tvMain.setOnClickListener(this);
        if (mediaControllerCompat.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
            tvMain.setText(mediaControllerCompat.getMetadata().getDescription().getTitle());
        }else {
            tvMain.setText(R.string.select_song);
        }

        final ArrayList<RadioStation> stationList = new ArrayList<RadioStation>(20);
        stationList.add(new RadioStation(this, "Radio",0,"Radio", "http://titan.shoutca.st:8892//stream"));
        final RadioStationAdapter adapter = new RadioStationAdapter(this, stationList );

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                MusicService.getInstance().setSongList(songList);
                RadioStation tempRadiostation = adapter.getItem(position);
                Uri playUri = tempRadiostation.getStationUri();
                Bundle bundle = new Bundle();
                bundle.putInt("Pos", stationList.indexOf(tempRadiostation));
                MediaControllerCompat.getMediaController(RadioActivity.this).getTransportControls()
                        .playFromUri(playUri, bundle);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.tvMain:
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
    protected void onStart() {
        super.onStart();
        if (!mediaBrowserCompat.isConnected()) {
            mediaBrowserCompat.connect();
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
