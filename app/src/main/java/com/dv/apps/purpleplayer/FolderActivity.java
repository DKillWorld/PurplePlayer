package com.dv.apps.purpleplayer;

import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.afollestad.aesthetic.Aesthetic;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FolderActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    ListView listView;
    ArrayList<String> folderList;
    ArrayAdapter<String> adapter;

    private List<String> item = null;
    private List<String> path = null;
    private String root="/";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Aesthetic.attach(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle("Folders");

        listView = findViewById(R.id.folder_listview);
        listView.setOnItemClickListener(this);

        getDirectory("/");
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


    private void getDirectory(String dirPath){

        item = new ArrayList<String>();
        path = new ArrayList<String>();



        File f = new File(dirPath);
        File[] files = f.listFiles();

        if(!dirPath.equals(root)){

            item.add(root);
            path.add(root);

            item.add("../");
            path.add(f.getParent());

        }

        for(int i=0; i < files.length; i++){

            File file = files[i];
            path.add(file.getPath());

            if(file.isDirectory()) {
                item.add(file.getName() + "/");
            }else {
                item.add(file.getName());
            }
        }

        adapter = new ArrayAdapter<String>(this, R.layout.list_item, R.id.songName, item);
        listView.setAdapter(adapter);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        File file = new File(path.get(position));
        adapter.notifyDataSetChanged();

        if (file.isDirectory()){
            if(file.canRead()) {
                getDirectory(path.get(position));
            }else {
                new MaterialDialog.Builder(this)
                        .title("[" + file.getName() + "] folder can't be read!")
                        .positiveText("OK")
                        .show();
            }
        }else {
            if (file.getName().endsWith(".mp3") || file.getName().endsWith(".MP3")){
                MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                mediaMetadataRetriever.setDataSource(file.getPath());
                String songName = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                if (songName == null) {
                    File file2 = new File(getIntent().getData().getPath());
                    String temp = file.getName();
                    songName = temp.substring(0, temp.lastIndexOf("."));
                }

                MusicService.getInstance().mediaSessionCompat.getController().getTransportControls().playFromSearch(songName, null);
            }
        }

    }

}