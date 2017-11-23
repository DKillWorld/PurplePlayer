package com.dv.apps.purpleplayer;

import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;

import com.afollestad.materialdialogs.color.CircleView;
import com.afollestad.materialdialogs.color.ColorChooserDialog;

import static com.dv.apps.purpleplayer.MainActivity.PRIMARY_COLOR_DEFAULT;


public class SettingsActivity extends AppCompatActivity implements ColorChooserDialog.ColorCallback, SharedPreferences.OnSharedPreferenceChangeListener {

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Settings");
        }

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(preferences.getInt("primary_color", PRIMARY_COLOR_DEFAULT)));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(CircleView.shiftColorDown(preferences.getInt("primary_color", PRIMARY_COLOR_DEFAULT)));
        }
    }


    @Override
    public void onColorSelection(@NonNull ColorChooserDialog colorChooserDialog, @ColorInt int i) {
        String dialogueTag = colorChooserDialog.tag();
        if (dialogueTag.equals("Secondary")){
            preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("list_background", i).apply();
        }else {
            preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("primary_color", i).apply();
        }
        onSharedPreferenceChanged(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()), "primary_color");

    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog colorChooserDialog) {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(sharedPreferences.getInt("primary_color", PRIMARY_COLOR_DEFAULT)));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(CircleView.shiftColorDown(sharedPreferences.getInt("primary_color", PRIMARY_COLOR_DEFAULT)));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        preferences.unregisterOnSharedPreferenceChangeListener(this);
    }
}
