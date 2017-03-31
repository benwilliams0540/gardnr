package com.cu.gardnr;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ArrayList plantNames;
    ArrayList plantLocations;
    ArrayList plantImages;

    ListView plantList;
    CustomListAdapter plantAdapter;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loadDatabase();

        plantAdapter = new CustomListAdapter(this, plantNames, plantImages);
        plantList = (ListView) findViewById(R.id.plantList);
        plantList.setAdapter(plantAdapter);

    }

    public void loadDatabase(){
        plantNames = new ArrayList<String>();
        plantLocations = new ArrayList<String>();
        plantImages = new ArrayList<Integer>();


    }

    public void launchSettings(MenuItem menu){
        startActivity(new Intent(MainActivity.this, MainActivity.class));
    }

    public void launchInfo(MenuItem menu){
        startActivity(new Intent(MainActivity.this, MainActivity.class));
    }
}
