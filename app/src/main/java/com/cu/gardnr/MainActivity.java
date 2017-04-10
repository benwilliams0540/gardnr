package com.cu.gardnr;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.GregorianCalendar;

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

        setupUI();
        setupReminders();
    }

    private void setupUI(){
        rv = (RecyclerView) findViewById(R.id.rv);
        llm = new LinearLayoutManager(MainActivity.this);

        ItemTouchHelper.Callback callback = new PlantTouchHelper(adapter, MainActivity.this);
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(rv);

        Runnable loadUI = createLoadThread();
        customHandler.post(loadUI);
        setupReminders();
    }

    private void setupReminders(){
        Long time = new GregorianCalendar().getTimeInMillis()+5000;
        Intent intentAlarm = new Intent(this, AlarmReceiver.class);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, PendingIntent.getBroadcast(this, 1, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
    }

    private void setupReminders(){
        Intent intentAlarm = new Intent(this, AlarmReceiver.class);
        Long time = new GregorianCalendar().getTimeInMillis() + 15000;
        Log.i("set time", "" + time);

        // create the object
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        //set the alarm for particular time
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, PendingIntent.getBroadcast(this, 1, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
    }

    static public void removePlant(Plant plant){
        Runnable undo = createDeleteThread(plant);
        customHandler.postDelayed(undo, 4000);
    }

    static public void cancelRemove(){
        customHandler.removeCallbacksAndMessages(null);
    }

    static private Runnable createLoadThread() {
        Runnable loadUI = new Runnable() {
            public void run() {
                adapter = new PlantAdapter(plants);

                rv.setLayoutManager(llm);
                rv.setAdapter(adapter);
            }
        };
        return loadUI;
    }

    static private Runnable createDeleteThread(final Plant plant){
        Runnable delete = new Runnable() {
            public void run() {
                Integer toRemove = plant.getPID();
                db.delete("plants", "pid = ?", new String[] {toRemove.toString()});
            }
        };
        return delete;
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
