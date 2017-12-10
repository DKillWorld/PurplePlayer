package com.dv.apps.purpleplayer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.afollestad.aesthetic.Aesthetic;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;

/**
 * Created by Dhaval on 02-09-2017.
 */

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_main);

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

        Preference faq = findPreference("FAQ");
        faq.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Toast.makeText(getActivity(), "FAQ section is under Development. \nMeanwhile use \"Send Feedback\" to get Help. ", Toast.LENGTH_SHORT).show();
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
                            .content("You are already a Pro User !!")
                            .positiveText("OK")
                            .title("Info")
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

    }


}
