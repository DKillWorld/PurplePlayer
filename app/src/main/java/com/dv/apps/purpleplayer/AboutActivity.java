package com.dv.apps.purpleplayer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.aesthetic.Aesthetic;
import com.afollestad.materialdialogs.color.CircleView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import static com.dv.apps.purpleplayer.MainActivity.PRIMARY_COLOR_DEFAULT;
import static com.dv.apps.purpleplayer.R.id.adView;


//Icon credit = psdblast.com
public class AboutActivity extends AppCompatActivity {

    ImageButton emailButton, fBPageButton;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Aesthetic.attach(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("About");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        textView = (TextView) findViewById(R.id.version_about);
        textView.setText("v " + BuildConfig.VERSION_NAME);

        fBPageButton = (ImageButton) findViewById(R.id.fb_message_icon);
        fBPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent fbIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/840546956122931"));
                    startActivity(fbIntent);
                }catch (Exception e){
                    Intent fbIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/thepurpleplayer"));
                    startActivity(fbIntent);
                }

            }
        });

        emailButton = (ImageButton) findViewById(R.id.contact_icon);
        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto: dkillworld@gmail.com"));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback about Purple Player");
                startActivity(Intent.createChooser(emailIntent, "Send feedback"));
            }
        });

        if (BuildConfig.APPLICATION_ID.equals("com.dv.apps.purpleplayer")) {
            AdView adView = (AdView) findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice("DD0CDAB405F30F550CD856F507E39725")
                    .build();
            adView.loadAd(adRequest);
            boolean isTestDevice = adRequest.isTestDevice(this);
            if (isTestDevice) {
                Toast.makeText(this, "Loaded on Test Device", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                this.finish();
                break;
        }
        return true;
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
