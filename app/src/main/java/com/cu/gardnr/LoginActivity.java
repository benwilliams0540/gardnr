package com.cu.gardnr;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {
    SQLiteDatabase db;

    ArrayList<User> allUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupDatabase();
    }

    private void setupDatabase(){
        allUsers = new ArrayList<User>();
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
            allUsers.add(new User(c.getString(uid), c.getString(pid)));
            c.moveToNext();
        }
    }

    public void login(View view){
        EditText usernameField = (EditText) findViewById(R.id.usernameField);
        EditText passwordField = (EditText) findViewById(R.id.passwordField);
        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();

        for (int i = 0; i < allUsers.size(); i++) {
            if (allUsers.get(i).getUsername().equals(username)) {
                if (allUsers.get(i).checkPassword(password)) {
                    Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                }
            }
        }
    }
    public void launchSignup(View view){
        setContentView(R.layout.activity_signup);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }
    public void signup(View view){
        EditText unField = (EditText) findViewById(R.id.unField);
        EditText pwField = (EditText) findViewById(R.id.pwField);
        EditText pw2Field = (EditText) findViewById(R.id.pw2Field);
        String username = unField.getText().toString();
        String password = pwField.getText().toString();
        String confirm = pw2Field.getText().toString();

        if (!password.equals(confirm)){
            Toast.makeText(LoginActivity.this, "Passwords must match", Toast.LENGTH_SHORT).show();
        }
        else {
            ContentValues insertValues = new ContentValues();
            insertValues.put("username", username);
            insertValues.put("password", password);
            try {
                db.insertOrThrow("users", null, insertValues);
                allUsers.add(new User(username, password));
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            } catch (Exception e){
                Toast.makeText(LoginActivity.this, "Username already taken", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
