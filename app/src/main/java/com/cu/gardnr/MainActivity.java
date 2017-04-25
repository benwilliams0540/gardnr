package com.cu.gardnr;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class MainActivity extends AppCompatActivity {
    private static SQLiteDatabase db;
    private static ArrayList<Plant> plants;
    private static String username;

    private static RecyclerView rv;
    private static LinearLayoutManager llm;
    private static PlantAdapter adapter;

    private static Handler customHandler = new Handler();
    private static NetworkChangeReceiver networkChangeReceiver;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
        Intent intent = new Intent(getBaseContext(), LoginActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences preferences = this.getSharedPreferences("com.cu.gardnr", Context.MODE_PRIVATE);
        if (preferences.getBoolean("firstRun", true)) {
            customHandler.postDelayed(firstTutorial, 1000);
        }

        networkChangeReceiver = new NetworkChangeReceiver();
        networkChangeReceiver.setInitialStatus(getBaseContext());

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        getBaseContext().registerReceiver(networkChangeReceiver, intentFilter);

        setupDatabase();
        new GetPlants().execute();
    }

    @Override
    public void onPause(){
        super.onPause();
        getBaseContext().unregisterReceiver(networkChangeReceiver);
    }

    @Override
    public void onResume(){
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        getBaseContext().registerReceiver(networkChangeReceiver, intentFilter);
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
        username = getIntent().getStringExtra("username");
        plants = new ArrayList<Plant>();
        Utilities.setupReminders(getBaseContext(), username);

        try {
            String sqlString = "CREATE TABLE IF NOT exists plants (pid INTEGER PRIMARY KEY, image VARCHAR, username VARCHAR, name VARCHAR, location VARCHAR, light VARCHAR, water VARCHAR, notification VARCHAR)";
            db = this.openOrCreateDatabase("gardnr", MODE_PRIVATE, null);
            db.execSQL(sqlString);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    class GetPlants extends AsyncTask<String, String, String> {
        protected String doInBackground(String... args) {
            if (networkChangeReceiver.getNetworkStatus()) {
                JSONParser jParser = new JSONParser();
                HashMap params = new HashMap<>();
                params.put("username", username);
                String URL = "https://people.cs.clemson.edu/~brw2/x820/gardnr/scripts/get_plants.php";
                JSONObject json = jParser.makeHttpRequest(URL, "POST", params);

                try {
                    int success = json.getInt("success");

                    if (success == 1) {
                        db.delete("plants", null, null);
                        JSONArray externalPlants = json.getJSONArray("plants");

                        for (int i = 0; i < externalPlants.length(); i++) {
                            JSONObject c = externalPlants.getJSONObject(i);
                            int pid = c.getInt("pid");
                            String image = c.getString("image");
                            String username = c.getString("username");
                            String name = c.getString("name");
                            String location = c.getString("location");
                            String light = c.getString("light");
                            String water = c.getString("water");
                            String notification = c.getString("notification");
                            plants.add(new Plant(pid, image, username, name, location, light, water, notification));

                            ContentValues insertValues = new ContentValues();
                            insertValues.put("pid", pid);
                            insertValues.put("image", image);
                            insertValues.put("username", username);
                            insertValues.put("name", name);
                            insertValues.put("location", location);
                            insertValues.put("light", light);
                            insertValues.put("water", water);
                            insertValues.put("notification", notification);

                            db.insert("plants", null, insertValues);
                        }
                        return "success";
                    } else {
                        return "failure";
                    }
                } catch (JSONException e) {
                    return "failure";
                }
            }
            else {
                return "failure";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (s.equalsIgnoreCase("failure")){
                loadDatabase();
            }
            else {
                customHandler.post(loadUI);
            }
        }
    }

    private void loadDatabase(){
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

    private Runnable loadUI = new Runnable () {
        public void run() {
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.addPlantButton);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchCreatePlant();
                }
            });
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

    public static SQLiteDatabase getDB() {
        return db;
    }
    public static String getUsername(){
        return username;
    }
    public static boolean getNetworkStatus() {
        return networkChangeReceiver.getNetworkStatus();
    }

    private Runnable firstTutorial = new Runnable () {
        public void run() {
            launchTutorial(null);
        }
    };

    public void launchCreatePlant(){
        if (networkChangeReceiver.getNetworkStatus()) {
            Intent intent = new Intent(getBaseContext(), AddPlantActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        }
        else {
            Toast.makeText(MainActivity.this, "Unable to add plant without an internet connection", Toast.LENGTH_LONG).show();
        }
    }
    public void launchTutorial(MenuItem menu){
        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this);
        final MaterialShowcaseView addPlant = new MaterialShowcaseView.Builder(this)
                .setMaskColour(R.color.colorPrimary)
                .setTarget(findViewById(R.id.addPlantButton))
                .setDismissText("GOT IT")
                .setContentText("To add a plant to your garden, select the add button here")
                .setDelay(250)
                .build();
        sequence.addSequenceItem(addPlant);
        final CardView cardExample = (CardView) findViewById(R.id.card_example);
        final MaterialShowcaseView plantView = new MaterialShowcaseView.Builder(this)
                .setMaskColour(R.color.colorPrimary)
                .setTarget(cardExample)
                .setDismissText("GOT IT")
                .setContentText("As you add plants, they will appear here. Selecting one will show you a detailed view of that plant")
                .withRectangleShape()
                .setDelay(250)
                .build();
        sequence.addSequenceItem(plantView);
        sequence.setOnItemDismissedListener(new MaterialShowcaseSequence.OnSequenceItemDismissedListener() {
            @Override
            public void onDismiss(MaterialShowcaseView materialShowcaseView, int i) {
                if (materialShowcaseView.equals(addPlant)){
                    if (plants.size() == 0) {
                        cardExample.setVisibility(View.VISIBLE);
                    }
                }
                if (materialShowcaseView.equals(plantView)){
                    cardExample.setVisibility(View.GONE);
                }
            }
        });
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setMaskColour(R.color.colorPrimary)
                        .setTarget(toolbar.getChildAt(1))
                        .setDismissText("GOT IT")
                        .setContentText("Reminders for watering your plants can be adjusted in the 'Settings' menu")
                        .setDelay(250)
                        .build()
        );
        sequence.start();
    }
    public void launchSettings(MenuItem menu){
        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
    }
    public void launchInfo(MenuItem menu){
        startActivity(new Intent(MainActivity.this, InfoActivity.class));
    }
}
