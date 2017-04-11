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
import android.support.design.widget.FloatingActionButton;
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
import java.util.Calendar;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity {
    SharedPreferences preferences;
    public static String username;
    private static SQLiteDatabase db;
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
        preferences = this.getSharedPreferences("com.cu.gardnr", Context.MODE_PRIVATE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupDatabase();
        setupUI();
        setupReminders();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        finish();
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
    }

    private void setupDatabase(){
        plants = new ArrayList<Plant>();

        try {
            String sqlString = "CREATE TABLE IF NOT exists plants (pid INTEGER PRIMARY KEY, image VARCHAR, username VARCHAR, name VARCHAR, location VARCHAR, light VARCHAR, water VARCHAR, notification VARCHAR)";
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
        int notifIndex = c.getColumnIndex("notification");

        c.moveToFirst();
        for (int i = 0; i < c.getCount(); i++){
            plants.add(new Plant(c.getInt(pIndex), c.getString(imageIndex), c.getString(userIndex), c.getString(nameIndex), c.getString(locIndex), c.getString(lightIndex), c.getString(waterIndex), c.getString(notifIndex)));
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

    private void setupReminders(){
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intentAlarm1 = new Intent(this, AlarmReceiver.class);
        Intent intentAlarm2 = new Intent(this, AlarmReceiver.class);
        Intent intentAlarm3 = new Intent(this, AlarmReceiver.class);

        intentAlarm1.putExtra("reminder", 1);
        intentAlarm1.putExtra("username", username);
        intentAlarm2.putExtra("reminder", 2);
        intentAlarm2.putExtra("username", username);
        intentAlarm3.putExtra("reminder", 3);
        intentAlarm3.putExtra("username", username);

        if (preferences.getBoolean("reminderStatus", true)) {
            Log.i("Reminders", "set");
            Calendar firstReminder = Calendar.getInstance();
            firstReminder.set(Calendar.DAY_OF_WEEK, getDay(1));
            firstReminder.set(Calendar.HOUR_OF_DAY, getHour(1));
            firstReminder.set(Calendar.MINUTE, 0);
            firstReminder.set(Calendar.SECOND, 0);

            Log.i("First", "" + firstReminder.getTimeInMillis());

            Calendar secondReminder = Calendar.getInstance();
            secondReminder.set(Calendar.DAY_OF_WEEK, getDay(2));
            secondReminder.set(Calendar.HOUR_OF_DAY, getHour(2));
            secondReminder.set(Calendar.MINUTE, 0);
            secondReminder.set(Calendar.SECOND, 0);
            Log.i("Second", "" + secondReminder.getTimeInMillis());

            Calendar thirdReminder = Calendar.getInstance();
            thirdReminder.set(Calendar.DAY_OF_WEEK, getDay(3));
            thirdReminder.set(Calendar.HOUR_OF_DAY, getHour(3));
            thirdReminder.set(Calendar.MINUTE, 0);
            thirdReminder.set(Calendar.SECOND, 0);
            Log.i("Third", "" + thirdReminder.getTimeInMillis());

            Long time = new GregorianCalendar().getTimeInMillis();
            Log.i("Current", "" + time);
            Log.i("First diff", "" + (time - firstReminder.getTimeInMillis()));
            Log.i("Second diff", "" + (time - secondReminder.getTimeInMillis()));
            Log.i("Third diff", "" + (time - thirdReminder.getTimeInMillis()));

            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, firstReminder.getTimeInMillis(), 24*60*60*1000, PendingIntent.getBroadcast(this, 1, intentAlarm1, PendingIntent.FLAG_UPDATE_CURRENT));
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, secondReminder.getTimeInMillis(), 24*60*60*1000, PendingIntent.getBroadcast(this, 1, intentAlarm2, PendingIntent.FLAG_UPDATE_CURRENT));
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, thirdReminder.getTimeInMillis(), 24*60*60*1000, PendingIntent.getBroadcast(this, 1, intentAlarm3, PendingIntent.FLAG_UPDATE_CURRENT));
//            alarmManager.set(AlarmManager.RTC_WAKEUP, firstReminder.getTimeInMillis(), PendingIntent.getBroadcast(this, 1, intentAlarm1, PendingIntent.FLAG_UPDATE_CURRENT));
//            alarmManager.set(AlarmManager.RTC_WAKEUP, secondReminder.getTimeInMillis(), PendingIntent.getBroadcast(this, 2, intentAlarm2, PendingIntent.FLAG_UPDATE_CURRENT));
//            alarmManager.set(AlarmManager.RTC_WAKEUP, thirdReminder.getTimeInMillis(), PendingIntent.getBroadcast(this, 3, intentAlarm3, PendingIntent.FLAG_UPDATE_CURRENT));
        }
        else {
            Log.i("Reminders", "cancelled");
            alarmManager.cancel(PendingIntent.getBroadcast(this, 1, intentAlarm1, PendingIntent.FLAG_UPDATE_CURRENT));
            alarmManager.cancel(PendingIntent.getBroadcast(this, 2, intentAlarm2, PendingIntent.FLAG_UPDATE_CURRENT));
            alarmManager.cancel(PendingIntent.getBroadcast(this, 3, intentAlarm3, PendingIntent.FLAG_UPDATE_CURRENT));
        }
    }

    private int getDay(int reminder){
        String day;
        if (reminder == 1){
            day = preferences.getString("day1", "Monday");
        }
        else if (reminder == 2){
            day = preferences.getString("day2", "Monday");
        }
        else {
            day = preferences.getString("day3", "Monday");
        }

        switch (day) {
            case "Sunday":
                return 1;
            case "Monday":
                return 2;
            case "Tuesday":
                return 3;
            case "Wednesday":
                return 4;
            case "Thursday":
                return 5;
            case "Friday":
                return 6;
            case "Saturday":
                return 7;
        }

        return 1;
    }

    private int getHour(int reminder){
        String time;
        if (reminder == 1){
            time = preferences.getString("time1", "01:00 AM");
        }
        else if (reminder == 2){
            time = preferences.getString("time2", "01:00 AM");
        }
        else {
            time = preferences.getString("time3", "01:00 AM");
        }

        switch (time) {
            case "12:00 AM":
                return 0;
            case "01:00 AM":
                return 1;
            case "02:00 AM":
                return 2;
            case "03:00 AM":
                return 3;
            case "04:00 AM":
                return 4;
            case "05:00 AM":
                return 5;
            case "06:00 AM":
                return 6;
            case "07:00 AM":
                return 7;
            case "08:00 AM":
                return 8;
            case "09:00 AM":
                return 9;
            case "10:00 AM":
                return 10;
            case "11:00 AM":
                return 11;
            case "12:00 PM":
                return 12;
            case "01:00 PM":
                return 13;
            case "02:00 PM":
                return 14;
            case "03:00 PM":
                return 15;
            case "04:00 PM":
                return 16;
            case "05:00 PM":
                return 17;
            case "06:00 PM":
                return 18;
            case "07:00 PM":
                return 19;
            case "08:00 PM":
                return 20;
            case "09:00 PM":
                return 21;
            case "10:00 PM":
                return 22;
            case "11:00 PM":
                return 23;
        }

        return 0;
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
        Intent intent = new Intent(getBaseContext(), AddPlantActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
    }
    public void launchSettings(MenuItem menu){
        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
    }
    public void launchInfo(MenuItem menu){
        startActivity(new Intent(MainActivity.this, MainActivity.class));
    }
}
