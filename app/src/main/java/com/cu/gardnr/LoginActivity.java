package com.cu.gardnr;

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
    private SQLiteDatabase db;
    private ArrayList<User> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupDatabase();
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
        Toast.makeText(LoginActivity.this, "Username or password is incorrect - logging in anyways", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        intent.putExtra("username", "default");
        startActivity(intent);
    }

    public void launchSignup(View view){
        startActivity(new Intent(LoginActivity.this, SignupActivity.class));
    }
}
