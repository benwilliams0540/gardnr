package com.cu.gardnr;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static SQLiteDatabase db;
    private static String username;
    private static ArrayList<Plant> plants;

    private static RecyclerView rv;
    private static LinearLayoutManager llm;
    private static PlantAdapter adapter;

    private static Handler customHandler = new Handler();

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
        SharedPreferences preferences = this.getSharedPreferences("com.cu.gardnr", Context.MODE_PRIVATE);
        preferences.edit().putString("username", username).apply();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupDatabase();
        setupUI();
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

        customHandler.post(loadUI);
    }

    private void setupUI(){
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.addPlantButton);
        rv = (RecyclerView) findViewById(R.id.rv);
        llm = new LinearLayoutManager(MainActivity.this);
        adapter = new PlantAdapter(plants);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCreatePlant();
            }
        });
        rv.setLayoutManager(llm);
    }

    static public void removePlant(Plant plant){
        Runnable undo = createDeleteRunnable(plant);
        customHandler.postDelayed(undo, 3500);
    }

    static public void cancelRemove(){
        customHandler.removeCallbacksAndMessages(null);
    }

    static private Runnable createDeleteRunnable(final Plant plant){
        Runnable delete = new Runnable() {
            public void run() {
                Integer toRemove = plant.getPID();
                db.delete("plants", "pid = ?", new String[] {toRemove.toString()});
            }
        };
        return delete;
    }

    private Runnable loadUI = new Runnable () {
        public void run() {
            rv = (RecyclerView) findViewById(R.id.rv);
            llm = new LinearLayoutManager(MainActivity.this);
            adapter = new PlantAdapter(plants);

            rv.setLayoutManager(llm);
            rv.setAdapter(adapter);

            ItemTouchHelper.Callback callback = new PlantTouchHelper(adapter, MainActivity.this);
            ItemTouchHelper helper = new ItemTouchHelper(callback);
            helper.attachToRecyclerView(rv);
        }
    };

    public void launchCreatePlant(){
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
