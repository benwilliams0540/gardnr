package com.cu.gardnr;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

public class Utilities {
    private static SQLiteDatabase db;
    private static Plant currentPlant;
    private static android.os.Handler customHandler = new android.os.Handler();

    static public void setupReminders(Context c, String username){
        SharedPreferences preferences = c.getSharedPreferences("com.cu.gardnr", Context.MODE_PRIVATE);
        AlarmManager alarmManager = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        Intent intentAlarm1 = new Intent(c, AlarmReceiver.class);
        Intent intentAlarm2 = new Intent(c, AlarmReceiver.class);
        Intent intentAlarm3 = new Intent(c, AlarmReceiver.class);

        intentAlarm1.putExtra("reminder", 1);
        intentAlarm1.putExtra("username", username);
        intentAlarm2.putExtra("reminder", 2);
        intentAlarm2.putExtra("username", username);
        intentAlarm3.putExtra("reminder", 3);
        intentAlarm3.putExtra("username", username);

        alarmManager.cancel(PendingIntent.getBroadcast(c, 1, intentAlarm1, PendingIntent.FLAG_CANCEL_CURRENT));
        alarmManager.cancel(PendingIntent.getBroadcast(c, 2, intentAlarm2, PendingIntent.FLAG_CANCEL_CURRENT));
        alarmManager.cancel(PendingIntent.getBroadcast(c, 3, intentAlarm3, PendingIntent.FLAG_CANCEL_CURRENT));

        if (preferences.getBoolean("reminderStatus", true)) {
            Long time = new GregorianCalendar().getTimeInMillis();
            Long original;
            Calendar firstReminder = Calendar.getInstance();
            firstReminder.set(Calendar.DAY_OF_WEEK, getDay(c, 1));
            firstReminder.set(Calendar.HOUR_OF_DAY, getHour(c, 1));
            firstReminder.set(Calendar.MINUTE, 0);
            firstReminder.set(Calendar.SECOND, 0);
            original = firstReminder.getTimeInMillis();
            if ((original - time) < 0){
                firstReminder.setTimeInMillis(original+(7 * 24 * 60 * 60 * 1000));
            }

            Calendar secondReminder = Calendar.getInstance();
            secondReminder.set(Calendar.DAY_OF_WEEK, getDay(c, 2));
            secondReminder.set(Calendar.HOUR_OF_DAY, getHour(c, 2));
            secondReminder.set(Calendar.MINUTE, 0);
            secondReminder.set(Calendar.SECOND, 0);
            original = secondReminder.getTimeInMillis();
            if ((original - time) < 0){
                secondReminder.setTimeInMillis(original+(7 * 24 * 60 * 60 * 1000));
            }

            Calendar thirdReminder = Calendar.getInstance();
            thirdReminder.set(Calendar.DAY_OF_WEEK, getDay(c, 3));
            thirdReminder.set(Calendar.HOUR_OF_DAY, getHour(c, 3));
            thirdReminder.set(Calendar.MINUTE, 0);
            thirdReminder.set(Calendar.SECOND, 0);
            original = thirdReminder.getTimeInMillis();
            if ((original - time) < 0){
                thirdReminder.setTimeInMillis(original+(7 * 24 * 60 * 60 * 1000));
            }
            if (Build.VERSION.SDK_INT >= 19) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, firstReminder.getTimeInMillis(), PendingIntent.getBroadcast(c, 1, intentAlarm1, PendingIntent.FLAG_CANCEL_CURRENT));
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, secondReminder.getTimeInMillis(), PendingIntent.getBroadcast(c, 2, intentAlarm2, PendingIntent.FLAG_CANCEL_CURRENT));
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, thirdReminder.getTimeInMillis(), PendingIntent.getBroadcast(c, 3, intentAlarm3, PendingIntent.FLAG_CANCEL_CURRENT));
            }
            else {
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, firstReminder.getTimeInMillis(), 7 * 24 * 60 * 60 * 1000, PendingIntent.getBroadcast(c, 1, intentAlarm1, PendingIntent.FLAG_CANCEL_CURRENT));
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, secondReminder.getTimeInMillis(), 7 * 24 * 60 * 60 * 1000, PendingIntent.getBroadcast(c, 2, intentAlarm2, PendingIntent.FLAG_CANCEL_CURRENT));
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, thirdReminder.getTimeInMillis(), 7 * 24 * 60 * 60 * 1000, PendingIntent.getBroadcast(c, 3, intentAlarm3, PendingIntent.FLAG_CANCEL_CURRENT));
            }
        }
    }

    static private int getDay(Context c, int reminder){
        SharedPreferences preferences = c.getSharedPreferences("com.cu.gardnr", Context.MODE_PRIVATE);
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

    static private int getHour(Context c, int reminder){
        SharedPreferences preferences = c.getSharedPreferences("com.cu.gardnr", Context.MODE_PRIVATE);
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
        db = MainActivity.getDB();
        currentPlant = plant;
        customHandler.postDelayed(delete, 3500);
    }

    static public void cancelRemove(){
        customHandler.removeCallbacksAndMessages(null);
    }

    static Runnable delete = new Runnable() {
        public void run() {
            new DeletePlant().execute();
        }
    };

    static class DeletePlant extends AsyncTask<String, String, String> {
        protected String doInBackground(String... args){
            JSONParser jParser = new JSONParser();
            HashMap params = new HashMap<>();
            params.put("pid", currentPlant.getPID().toString());
            String URL = "https://people.cs.clemson.edu/~brw2/x820/gardnr/scripts/delete_plant.php";
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

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (s.equalsIgnoreCase("success")){
                Integer toRemove = currentPlant.getPID();
                db.delete("plants", "pid = ?", new String[] {toRemove.toString()});
            }

        }
    }
}
