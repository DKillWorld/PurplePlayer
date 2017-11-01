package com.dv.apps.purpleplayer;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;

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

        Preference listBackground = findPreference("list_background");
        listBackground.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new ColorChooserDialog.Builder(getActivity(), R.string.app_name)
                        .titleSub(R.string.app_name)  // title of dialog when viewing shades of a color
                        .doneButton(R.string.md_done_label)  // changes label of the done button
                        .cancelButton(R.string.md_cancel_label)  // changes label of the cancel button
                        .backButton(R.string.md_back_label)  // changes label of the back button
                        .tag("Secondary")
                        .dynamicButtonColor(true)  // defaults to true, false will disable changing action buttons' color to currently selected color
                        .show((FragmentActivity) getActivity());
                return true;
            }
        });

        Preference primaryColorPreference = findPreference("primary_color");
        primaryColorPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new ColorChooserDialog.Builder(getActivity(), R.string.app_name)
                        .titleSub(R.string.app_name)  // title of dialog when viewing shades of a color
                        .doneButton(R.string.md_done_label)  // changes label of the done button
                        .cancelButton(R.string.md_cancel_label)  // changes label of the cancel button
                        .backButton(R.string.md_back_label)  // changes label of the back button
                        .tag("Primary")
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

    }


}
