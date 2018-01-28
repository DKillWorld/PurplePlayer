package com.dv.apps.purpleplayer.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.dv.apps.purpleplayer.MusicService;

public class SleepTimerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent stopIntent = new Intent(context, MusicService.class);
        context.stopService(stopIntent);
        Toast.makeText(context, "Stopped Music Playback by Sleep Timer", Toast.LENGTH_SHORT).show();
    }
}
