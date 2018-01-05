package com.dv.apps.purpleplayer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.afollestad.aesthetic.Aesthetic;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;

/**
 * Created by Dhaval on 02-09-2017.
 */

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    SharedPreferences preferences;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_main);

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        preferences.registerOnSharedPreferenceChangeListener(this);

        Preference accentColorPreference = findPreference("accent_color");
        accentColorPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new ColorChooserDialog.Builder(getActivity(), R.string.accent_color_select)
                        .titleSub(R.string.accent_color_select)  // title of dialog when viewing shades of a color
                        .doneButton(R.string.md_done_label)  // changes label of the done button
                        .cancelButton(R.string.md_cancel_label)  // changes label of the cancel button
                        .backButton(R.string.md_back_label)  // changes label of the back button
                        .tag("Secondary")
                        .accentMode(true)
                        .preselect(Aesthetic.get().colorAccent().blockingFirst())
                        .dynamicButtonColor(true)  // defaults to true, false will disable changing action buttons' color to currently selected color
                        .show((FragmentActivity) getActivity());
                return true;
            }
        });

        Preference primaryColorPreference = findPreference("primary_color");
        primaryColorPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new ColorChooserDialog.Builder(getActivity(), R.string.primary_color_select)
                        .titleSub(R.string.primary_color_select)  // title of dialog when viewing shades of a color
                        .doneButton(R.string.md_done_label)  // changes label of the done button
                        .cancelButton(R.string.md_cancel_label)  // changes label of the cancel button
                        .backButton(R.string.md_back_label)  // changes label of the back button
                        .tag("Primary")
                        .preselect(Aesthetic.get().colorPrimary().blockingFirst())
                        .dynamicButtonColor(true)  // defaults to true, false will disable changing action buttons' color to currently selected color
                        .show((FragmentActivity) getActivity());
                return true;
            }
        });

//        Preference playbackSpeed = findPreference("playback_speed");
//        playbackSpeed.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//            @Override
//            public boolean onPreferenceClick(Preference preference) {
//                new MaterialDialog.Builder(getActivity())
//                        .title("Set Playback Speed")
//                        .content("Set Speed")
//                        .inputType(InputType.TYPE_CLASS_NUMBER)
//                        .input("Speed in float", "1.0", new MaterialDialog.InputCallback() {
//                            @Override
//                            public void onInput(MaterialDialog dialog, CharSequence input) {
//                                float speed = Float.parseFloat(input.toString());
//                            }
//                        }).show();
//                return true;
//            }
//        });

//        CheckBoxPreference baseTheme = (CheckBoxPreference) findPreference("BaseTheme");
//        baseTheme.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//            @Override
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                if (newValue.equals("true")){
//                    Aesthetic.get().activityTheme(R.style.AppThemeDark).apply();
//                }else {
//                    Aesthetic.get().activityTheme(R.style.AppThemeDark).apply();
//                }
//                return true;
//            }
//        });

        Preference faq = findPreference("FAQ");
        faq.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                        .positiveText(R.string.ok)
                        .title(R.string.faq)
                        .content("Q. Is this project open-source? \nA: Shhh! Not Yet. \n\n" +
                                "Q. How can I see Now Playing songs list? \nA: Just swipe down on albumart, as easy as that.\n\n" +
                                "Q. How can I create my playlist? \nA: You can only play created playlists. Support for creating new playlist will be added soon ! \n\n" +
                                "Q. After changing app primary color, all songs disappeared. What to do? \nA: This happens when you change from dark actionbar theme to light or vice-versa. However, a quick restart will fix this issue. \n\n" +
                                "Q. I found a bug. Where/How to report? \nA: Shoot an email describing the bug and steps to reproduce it.\n\n" +
                                "Q. Why should I upgrade to Purple Player Pro? \nA: Its ad-free and it gets feature updates first.\n\n" +
                                "Q. I want to help translating app to my local language. How to? \nA: Thanks for interest. Soon, you will be able to help in translating the app.")
                        .show();
                 return true;
            }
        });

        Preference rateUs = findPreference("Rate_Us");
        rateUs.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (BuildConfig.APPLICATION_ID.equals("com.dv.apps.purpleplayer")){
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("market://details?id=com.dv.apps.purpleplayer"));
                    startActivity(intent);
                }else {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("market://details?id=com.dv.apps.purpleplayerpro"));
                    startActivity(intent);
                }
                return true;
            }
        });

        Preference buyPro = findPreference("Buy_Pro");
        buyPro.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (BuildConfig.APPLICATION_ID.equals("com.dv.apps.purpleplayer")){
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("market://details?id=com.dv.apps.purpleplayerpro"));
                    startActivity(intent);
                }else {
                    new MaterialDialog.Builder(getActivity())
                            .content(R.string.alreadyProUser)
                            .positiveText(R.string.ok)
                            .title(R.string.info)
                            .show();
                }
                return true;
            }
        });

        Preference translationHelp = findPreference("Translation_Help");
        translationHelp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent aIntent = new Intent(getActivity(), AboutActivity.class);
                startActivity(aIntent);
                Toast.makeText(getActivity(), "Thanks for showing interest. \nContact Us using Email for further instructions.", Toast.LENGTH_LONG).show();
                return true;
            }
        });

        CheckBoxPreference useAlbumArtBackground = (CheckBoxPreference) findPreference("Use_Root_Background");

        ListPreference transition = (ListPreference) findPreference("transition_effect");
        transition.setSummary(preferences.getString("transition_effect", "Default"));


    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePreference(findPreference(key), key);
    }

    private void updatePreference(Preference preference, String key) {
        if (preference == null) return;
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            listPreference.setSummary(listPreference.getEntry());
        }
    }

    @Override
    public void onDestroy() {
        preferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }
}
