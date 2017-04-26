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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class LoginActivity extends AppCompatActivity {
    private SQLiteDatabase db;
    private ArrayList<User> users;

    private Toolbar toolbar;

    private NetworkChangeReceiver networkChangeReceiver;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences preferences = this.getSharedPreferences("com.cu.gardnr", Context.MODE_PRIVATE);
        preferences.edit().putBoolean("firstRun", false).apply();
        if (preferences.getBoolean("firstRun", true)){
            Handler customHandler = new Handler();
            customHandler.postDelayed(firstTutorial, 1000);
        }

        networkChangeReceiver = new NetworkChangeReceiver();
        networkChangeReceiver.setInitialStatus(getBaseContext());

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        getBaseContext().registerReceiver(networkChangeReceiver, intentFilter);

        setupDatabase();
        new GetUsers().execute();
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
        Intent intent = new Intent(getBaseContext(), LoginActivity.class);
        startActivity(intent);
    }

    private void setupDatabase(){
        users = new ArrayList<User>();

        try {
            String sqlString = "CREATE TABLE IF NOT exists users (username VARCHAR PRIMARY KEY, password VARCHAR)";
            db = this.openOrCreateDatabase("gardnr", MODE_PRIVATE, null);
            db.execSQL(sqlString);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    class GetUsers extends AsyncTask<String, String, String> {
        protected String doInBackground(String... args) {
            Log.i("Network status", "" + networkChangeReceiver.getNetworkStatus());
            if (networkChangeReceiver.getNetworkStatus()) {
                JSONParser jParser = new JSONParser();
                HashMap params = new HashMap<>();
                String URL = "https://people.cs.clemson.edu/~brw2/x820/gardnr/scripts/get_users.php";
                JSONObject json = jParser.makeHttpRequest(URL, "GET", params);

                try {
                    int success = json.getInt("success");

                    if (success == 1) {
                        db.delete("users", null, null);
                        JSONArray externalUsers = json.getJSONArray("users");

                        for (int i = 0; i < externalUsers.length(); i++) {
                            JSONObject c = externalUsers.getJSONObject(i);
                            String username = c.getString("username");
                            String password = c.getString("password");
                            users.add(new User(username, password));

                            ContentValues insertValues = new ContentValues();
                            insertValues.put("username", username);
                            insertValues.put("password", password);

                            db.insert("users", null, insertValues);
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
                Log.i("Info", "Unable to load from external DB");
                loadDatabase();
            }
        }
    }

    private void loadDatabase() {
        Cursor c = db.rawQuery("SELECT * FROM users", null);
        int uid = c.getColumnIndex("username");
        int pid = c.getColumnIndex("password");

        c.moveToFirst();
        for (int i = 0; i < c.getCount(); i++){
            users.add(new User(c.getString(uid), c.getString(pid)));
            c.moveToNext();
        }
    }

    private Runnable firstTutorial = new Runnable () {
        public void run() {
            launchTutorial(null);
        }
    };

    public void login(View view){
        EditText usernameField = (EditText) findViewById(R.id.usernameField);
        EditText passwordField = (EditText) findViewById(R.id.passwordField);
        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();

        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUsername().equals(username)) {
                if (users.get(i).checkPassword(password)) {
                    Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    intent.putExtra("username", username);
                    startActivity(intent);
                    return;
                }
            }
        }
        Toast.makeText(LoginActivity.this, "Username or password is incorrect, please try again", Toast.LENGTH_SHORT).show();
        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        intent.putExtra("username", "brw2");
        startActivity(intent);
    }
    public void launchSignup(View view){
        startActivity(new Intent(LoginActivity.this, SignupActivity.class));
    }
    public void launchTutorial(MenuItem menu){
        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this);
        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setMaskColour(R.color.colorPrimary)
                        .setTarget(findViewById(R.id.signupButton))
                        .setDismissText("GOT IT")
                        .setContentText("To create a profile, select the 'SIGN UP' button")
                        .withRectangleShape()
                        .setDelay(250)
                        .build()
        );

        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setMaskColour(R.color.colorPrimary)
                        .setTarget(findViewById(R.id.informationLayout))
                        .setDismissText("GOT IT")
                        .setContentText("If you already have a profile, you can enter your information here and select 'LOGIN'")
                        .withRectangleShape()
                        .setDelay(250)
                        .build()
        );
        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setMaskColour(R.color.colorPrimary)
                        .setTarget(toolbar.getChildAt(1))
                        .setDismissText("GOT IT")
                        .setContentText("If you need to view a tutorial again, simply select the help icon at any time")
                        .withRectangleShape()
                        .setDelay(250)
                        .build()
        );
        sequence.start();
    }
}
