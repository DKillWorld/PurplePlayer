package com.dv.apps.purpleplayer;

import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;

import com.afollestad.aesthetic.Aesthetic;
import com.afollestad.aesthetic.AutoSwitchMode;
import com.afollestad.materialdialogs.color.CircleView;
import com.afollestad.materialdialogs.color.ColorChooserDialog;

import static com.dv.apps.purpleplayer.MainActivity.PRIMARY_COLOR_DEFAULT;


public class SettingsActivity extends AppCompatActivity implements ColorChooserDialog.ColorCallback {

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Aesthetic.attach(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Settings");
        }

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

    }


    @Override
    public void onColorSelection(@NonNull ColorChooserDialog colorChooserDialog, @ColorInt int i) {
        String dialogueTag = colorChooserDialog.tag();
        if (dialogueTag.equals("Secondary")){
            Aesthetic.get()
                    .colorAccent(i)
                    .apply();
        }else {
            Aesthetic.get()
                    .colorPrimary(i)
                    .colorStatusBarAuto()
                    .colorNavigationBarAuto()
                    .apply();
            preferences.edit().putInt("primary_color", i).apply();
        }
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog colorChooserDialog) {

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
