package com.cu.gardnr;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {
    private User user;
    private static NetworkChangeReceiver networkChangeReceiver;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        networkChangeReceiver = new NetworkChangeReceiver();
        networkChangeReceiver.setInitialStatus(getBaseContext());

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        getBaseContext().registerReceiver(networkChangeReceiver, intentFilter);
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

    class AddUser extends AsyncTask<String, String, String> {
        protected String doInBackground(String... args){
            if (networkChangeReceiver.getNetworkStatus()) {
                JSONParser jParser = new JSONParser();
                HashMap params = new HashMap<>();
                params.put("username", user.getUsername());
                params.put("password", user.getPassword());

                String URL = "https://people.cs.clemson.edu/~brw2/x820/gardnr/scripts/add_user.php";
                JSONObject json = jParser.makeHttpRequest(URL, "POST", params);

                try {
                    int success = json.getInt("success");
                    if (success == 1) {
                        return "success";
                    } else {
                        return "failure";
                    }
                } catch (JSONException e) {
                    return "failure";
                }
            }
            else {
                return "networkFailure";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (s.equalsIgnoreCase("success")){
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                intent.putExtra("username", user.getUsername());
                startActivity(intent);
            }
            else if (s.equalsIgnoreCase("failure")){
                Toast.makeText(SignupActivity.this, "Username is already taken, please try another", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(SignupActivity.this, "Unable to add user without internet connection", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void signup(View view){
        EditText unField = (EditText) findViewById(R.id.unField);
        EditText pwField = (EditText) findViewById(R.id.pwField);
        EditText pw2Field = (EditText) findViewById(R.id.pw2Field);
        String username = unField.getText().toString();
        String password = pwField.getText().toString();
        String confirm = pw2Field.getText().toString();

        if (username.equals("")){
            Toast.makeText(SignupActivity.this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.equals("")){
            Toast.makeText(SignupActivity.this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirm)){
            Toast.makeText(SignupActivity.this, "Passwords must match", Toast.LENGTH_SHORT).show();
            return;
        }

        user = new User(username, password);
        new AddUser().execute();
    }
}
