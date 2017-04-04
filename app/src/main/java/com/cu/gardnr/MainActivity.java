package com.cu.gardnr;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    SQLiteDatabase db;
    private String username;
    private ArrayList<Plant> plants;

    private CustomListAdapter plantAdapter;
    private ListView plantList;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        username = getIntent().getStringExtra("username");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupDatabase();
    }

    private void setupDatabase(){
        plants = new ArrayList<Plant>();

        try {
            String sqlString = "CREATE TABLE IF NOT exists plants (pid INTEGER PRIMARY KEY, image VARCHAR, username VARCHAR, name VARCHAR, location VARCHAR, light VARCHAR, water VARCHAR)";
            db = this.openOrCreateDatabase("gardnr", MODE_PRIVATE, null);
            db.execSQL(sqlString);
        } catch (Exception e){
            e.printStackTrace();
        }

        Cursor c = db.rawQuery("SELECT * FROM plants WHERE username='" + username + "'", null);
        int pIndex = c.getColumnIndex("pid");
        int imageIndex = c.getColumnIndex("image");
        int userIndex = c.getColumnIndex("username");
        int nameIndex = c.getColumnIndex("name");
        int locIndex = c.getColumnIndex("location");
        int lightIndex = c.getColumnIndex("light");
        int waterIndex = c.getColumnIndex("water");

        c.moveToFirst();
        for (int i = 0; i < c.getCount(); i++){
            plants.add(new Plant(c.getInt(pIndex), c.getString(imageIndex), c.getString(userIndex), c.getString(nameIndex), c.getString(locIndex), c.getString(lightIndex), c.getString(waterIndex)));
            c.moveToNext();
        }

//        plants.add(new Plant(0, R.drawable.plant, "brw2", "Hydrangea", "Front porch", "Sunny", "Once a day"));
//        plants.add(new Plant(1, R.drawable.plant, "brw2", "Tulip", "Back porch", "Sunny", "Once a day"));

        plantAdapter = new CustomListAdapter(this, plants);
        plantList = (ListView) findViewById(R.id.plantList);
        plantList.setAdapter(plantAdapter);
    }

    public void launchCreatePlant(View view){
        Intent intent = new Intent(getBaseContext(), CreatePlantActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
    }
    public void launchSettings(MenuItem menu){
        startActivity(new Intent(MainActivity.this, MainActivity.class));
    }
    public void launchInfo(MenuItem menu){
        startActivity(new Intent(MainActivity.this, MainActivity.class));
    }
}
