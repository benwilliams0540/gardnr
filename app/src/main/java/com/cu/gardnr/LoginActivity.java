package com.cu.gardnr;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class LoginActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private SharedPreferences preferences;
    private SQLiteDatabase db;
    private ArrayList<User> users;

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

        preferences = this.getSharedPreferences("com.cu.gardnr", Context.MODE_PRIVATE);
//        if (preferences.getBoolean("signedUp", true)){
//            preferences.edit().putBoolean("signedUp", false).apply();
//            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
//        }
        setupDatabase();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState){
        super.onPostCreate(savedInstanceState);

        if (preferences.getBoolean("firstRun", true)){
            Handler customHandler = new Handler();
            customHandler.postDelayed(firstTutorial, 1000);
        }
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

        Cursor c = db.rawQuery("SELECT * FROM users", null);
        int uid = c.getColumnIndex("username");
        int pid = c.getColumnIndex("password");

        c.moveToFirst();
        for (int i = 0; i < c.getCount(); i++){
            users.add(new User(c.getString(uid), c.getString(pid)));
            c.moveToNext();
        }
    }

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
                }
            }
        }
        Toast.makeText(LoginActivity.this, "Username or password is incorrect, please try again", Toast.LENGTH_SHORT).show();
//        Intent intent = new Intent(getBaseContext(), MainActivity.class);
//        intent.putExtra("username", "default");
//        startActivity(intent);
    }

    private Runnable firstTutorial = new Runnable () {
        public void run() {
            launchTutorial(null);
        }
    };

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

    public void launchSignup(View view){
        startActivity(new Intent(LoginActivity.this, SignupActivity.class));
    }
}
