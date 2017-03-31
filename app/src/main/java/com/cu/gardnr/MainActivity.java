package com.cu.gardnr;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {
    ListView plantList;
    ArrayAdapter plantAdapter;

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

        String[] test = {"Plant 1", "Plant 2"};

        plantList = (ListView) findViewById(R.id.plantList);
        plantAdapter = (new ArrayAdapter<String>(
                this, R.layout.mylist,
                R.id.Itemname,test));


    }

    public void launchSettings(MenuItem menu){
        startActivity(new Intent(MainActivity.this, MainActivity.class));
    }

    public void launchInfo(MenuItem menu){
        startActivity(new Intent(MainActivity.this, MainActivity.class));
    }
}
