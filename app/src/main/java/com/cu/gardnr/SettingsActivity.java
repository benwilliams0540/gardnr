package com.cu.gardnr;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;

public class SettingsActivity extends AppCompatActivity  {
    SharedPreferences preferences;
    private String username;
    private boolean reminderStatus;

    private String day1;
    private String time1;

    private String day2;
    private String time2;

    private String day3;
    private String time3;


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        loadPreferences();
        setupUI();
    }

    private void loadPreferences(){
        preferences = this.getSharedPreferences("com.cu.gardnr", Context.MODE_PRIVATE);
        username = preferences.getString("username", "default");
        reminderStatus = preferences.getBoolean("reminderStatus", true);

        day1 = preferences.getString("day1", "Monday");
        time1 = preferences.getString("time1", "01:00 AM");

        day2 = preferences.getString("day2", "Monday");
        time2 = preferences.getString("time2", "01:00 AM");

        day3 = preferences.getString("day3", "Monday");
        time3 = preferences.getString("time3", "01:00 AM");
    }

    private void setupUI(){
        final String[] days = getResources().getStringArray(R.array.days);
        final String[] times = getResources().getStringArray(R.array.times);

        ArrayAdapter<String> dayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, days);
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, times);

        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        CheckBox reminderCheckBox = (CheckBox) findViewById(R.id.notificationCheckBox);
        reminderCheckBox.setChecked(reminderStatus);
        reminderCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                preferences.edit().putBoolean("reminderStatus", isChecked).apply();
            }
        });

        Spinner spinner1day = (Spinner) findViewById(R.id.spinner1day);
        Spinner spinner1time = (Spinner) findViewById(R.id.spinner1time);
        Spinner spinner2day = (Spinner) findViewById(R.id.spinner2day);
        Spinner spinner2time = (Spinner) findViewById(R.id.spinner2time);
        Spinner spinner3day = (Spinner) findViewById(R.id.spinner3day);
        Spinner spinner3time = (Spinner) findViewById(R.id.spinner3time);

        spinner1day.setAdapter(dayAdapter);
        spinner1time.setAdapter(timeAdapter);
        spinner2day.setAdapter(dayAdapter);
        spinner2time.setAdapter(timeAdapter);
        spinner3day.setAdapter(dayAdapter);
        spinner3time.setAdapter(timeAdapter);

        spinner1day.setSelection(findDay(day1));
        spinner1time.setSelection(findTime(time1));
        spinner2day.setSelection(findDay(day2));
        spinner2time.setSelection(findTime(time2));
        spinner3day.setSelection(findDay(day3));
        spinner3time.setSelection(findTime(time3));

        spinner1day.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                preferences.edit().putString("day1", days[position]).apply();
                Log.i("Set day1", preferences.getString("day1", "FAILED"));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });
        spinner1time.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                preferences.edit().putString("time1", times[position]).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });

        spinner2day.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                preferences.edit().putString("day2", days[position]).apply();
                Log.i("Set day2", preferences.getString("day2", "FAILED"));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });
        spinner2time.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                preferences.edit().putString("time2", times[position]).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });

        spinner3day.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                preferences.edit().putString("day3", days[position]).apply();
                Log.i("Set day3", preferences.getString("day3", "FAILED"));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });
        spinner3time.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                preferences.edit().putString("time3", times[position]).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });
    }

    private int findDay(String day){
        String[] days = getResources().getStringArray(R.array.days);
        for (int i = 0; i < days.length; i++){
            if (days[i].equals(day)){
                return i;
            }
        }

        return 0;
    }

    private int findTime(String time){
        final String[] times = getResources().getStringArray(R.array.times);
        for (int i = 0; i < times.length; i++){
            if (times[i].equals(time)){
                return i;
            }
        }

        return 0;
    }
}
