package com.dv.apps.purpleplayer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class AboutActivity extends AppCompatActivity {

    ImageButton imageButton;
    TextView textView;
    AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textView = (TextView) findViewById(R.id.version_about);
        textView.setText("v " + BuildConfig.VERSION_NAME);

        imageButton = (ImageButton) findViewById(R.id.contact_icon);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent fbIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile/100002257422329"));
                    startActivity(fbIntent);
                }catch (Exception e){
                    Intent fbIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/dkillworld"));
                    startActivity(fbIntent);
                }
            }
        });

        adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("DD0CDAB405F30F550CD856F507E39725")
                .build();
        adView.loadAd(adRequest);
        boolean isTestDevice = adRequest.isTestDevice(this);
        if (isTestDevice){
            Toast.makeText(this, "Loaded on Test Device", Toast.LENGTH_SHORT).show();
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
}
