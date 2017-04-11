package com.cu.gardnr;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PlantActivity extends AppCompatActivity {
    private Integer pid;
    private Plant plant;
    private String imagePath = "";
    private String username;
    private static SQLiteDatabase db;
    private static Handler customHandler = new Handler();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                AlertDialog.Builder builder = new AlertDialog.Builder(PlantActivity.this);

                builder.setTitle("Are you sure you want to discard these changes?")
                        .setIcon(R.drawable.ic_delete)
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        })
                        .setPositiveButton("Discard", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                PlantActivity.super.onBackPressed();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_update, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_plant);

        pid = getIntent().getIntExtra("plant", 0);
        username = getIntent().getStringExtra("username");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        customHandler.post(loadPlant);
    }

    private Runnable loadPlant = new Runnable () {
        public void run() {
            try {
                String sqlString = "CREATE TABLE IF NOT exists plants (pid INTEGER PRIMARY KEY, image VARCHAR, username VARCHAR, name VARCHAR, location VARCHAR, light VARCHAR, water VARCHAR, notification VARCHAR)";
                db = PlantActivity.this.openOrCreateDatabase("gardnr", MODE_PRIVATE, null);
                db.execSQL(sqlString);
            } catch (Exception e){
                e.printStackTrace();
            }

            Cursor c = db.rawQuery("SELECT * FROM plants WHERE pid='" + pid + "'", null);
            int pIndex = c.getColumnIndex("pid");
            int imageIndex = c.getColumnIndex("image");
            int userIndex = c.getColumnIndex("username");
            int nameIndex = c.getColumnIndex("name");
            int locIndex = c.getColumnIndex("location");
            int lightIndex = c.getColumnIndex("light");
            int waterIndex = c.getColumnIndex("water");
            int notifIndex = c.getColumnIndex("notification");

            c.moveToFirst();
            plant = new Plant(c.getInt(pIndex), c.getString(imageIndex), c.getString(userIndex), c.getString(nameIndex), c.getString(locIndex), c.getString(lightIndex), c.getString(waterIndex), c.getString(notifIndex));
            setupUI();
        }
    };

    private void setupUI(){
        ImageView plantImage = (ImageView) findViewById(R.id.defaultPlant);
        EditText nameEditText = (EditText) findViewById(R.id.nameEditText);
        EditText locationEditText = (EditText) findViewById(R.id.locationEditText);
        EditText lightEditText = (EditText) findViewById(R.id.lightEditText);
        Spinner frequencySpinner = (Spinner) findViewById(R.id.frequencySpinner);
        CheckBox notificationCheckBox = (CheckBox) findViewById(R.id.notificationCheckBox);

        Bitmap bitmap = BitmapFactory.decodeFile(plant.getImage());
        plantImage.setImageBitmap(bitmap);

        nameEditText.setText(plant.getName());
        locationEditText.setText(plant.getLocation());
        lightEditText.setText(plant.getLight());

        List<String> frequencies = new ArrayList<String>();
        frequencies.add("1x a week");
        frequencies.add("2x a week");
        frequencies.add("3x a week");

        ArrayAdapter<String> waterAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, frequencies);
        waterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        frequencySpinner.setAdapter(waterAdapter);

        if (plant.getWater().equals("1x a week")){
            frequencySpinner.setSelection(0);
        }
        else if (plant.getWater().equals("2x a week")){
            frequencySpinner.setSelection(1);
        }
        else {
            frequencySpinner.setSelection(2);
        }

        notificationCheckBox.setChecked(plant.getNotification());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.cameraButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] sources = {"Gallery", "Camera"};
                AlertDialog.Builder builder = new AlertDialog.Builder(PlantActivity.this);

                builder.setTitle("Choose a source")
                        .setItems(sources, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int position) {
                                addPhoto(position);
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        imagePath = image.getAbsolutePath();
        return image;
    }

    private boolean checkStoragePermissions(){
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }
        else {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            else {
                return true;
            }
        }
    }

    private boolean checkCameraPermissions(){
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }
        else {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            else {
                return true;
            }
        }
    }

    private void addPhoto(int source){
        File photoFile = null;

        if (source == 0){
            if (Build.VERSION.SDK_INT < 23) {
                Intent takePictureIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(takePictureIntent, 1);
            }
            else {
                if (checkStoragePermissions()) {
                    Intent takePictureIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(takePictureIntent, 1);
                }
                else {
                    requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                }
            }
        }
        else if (source == 1) {
            if (Build.VERSION.SDK_INT < 23) {
                saveCameraPhoto();
            }
            else {
                if (checkCameraPermissions()){
                    saveCameraPhoto();
                }
                else {
                    requestPermissions(new String[]{android.Manifest.permission.CAMERA}, 2);
                }
            }
        }
    }

    private void saveGalleryPhoto(Bitmap image) {
        try {
            File photoFile = createImageFile();
            FileOutputStream fos = new FileOutputStream(photoFile);
            image.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d("", "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d("", "Error accessing file: " + e.getMessage());
        }
    }

    private void saveCameraPhoto(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        if (Build.VERSION.SDK_INT < 23) {
            try {
                photoFile = createImageFile();
                if (photoFile != null) {
                    Uri photoURI = Uri.fromFile(createImageFile());
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, 2);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                photoFile = createImageFile();
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this, "com.cu.gardnr.fileprovider", photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, 2);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent takePictureIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(takePictureIntent, 1);
            }
        }
        else if (requestCode == 2){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveCameraPhoto();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ImageView plantImage = (ImageView) findViewById(R.id.defaultPlant);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null){
            Uri selectedImage = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                plantImage.setImageBitmap(bitmap);
                saveGalleryPhoto(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            plantImage.setImageBitmap(bitmap);
        }
    }

    public void updatePlant(MenuItem menu) {
        EditText nameEditText = (EditText) findViewById(R.id.nameEditText);
        EditText locationEditText = (EditText) findViewById(R.id.locationEditText);
        EditText lightEditText = (EditText) findViewById(R.id.lightEditText);
        Spinner frequencySpinner = (Spinner) findViewById(R.id.frequencySpinner);
        CheckBox notificationCheckBox = (CheckBox) findViewById(R.id.notificationCheckBox);

        String name = nameEditText.getText().toString();
        String location = locationEditText.getText().toString();
        String light = lightEditText.getText().toString();
        String frequency = frequencySpinner.getSelectedItem().toString();
        String notification;

        if (notificationCheckBox.isChecked()){
            notification = "true";
        }
        else {
            notification = "false";
        }

        if (name.length() < 1) {
            Toast.makeText(PlantActivity.this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
        }
        else if (location.length() < 1) {
            Toast.makeText(PlantActivity.this, "Location cannot be empty", Toast.LENGTH_SHORT).show();
        }
        else if (light.length() < 1) {
            Toast.makeText(PlantActivity.this, "Light cannot be empty", Toast.LENGTH_SHORT).show();
        }
        else if (imagePath.equals("")){
            Toast.makeText(PlantActivity.this, "A photo of the plant must be set", Toast.LENGTH_SHORT).show();
        }
        else {
            ContentValues insertValues = new ContentValues();
            insertValues.put("image", imagePath);
            insertValues.put("username", username);
            insertValues.put("name", name);
            insertValues.put("location", location);
            insertValues.put("light", light);
            insertValues.put("water", frequency);
            insertValues.put("notification", notification);

            try {
                db.update("plants", insertValues, "pid = ?", new String[]{pid.toString()});
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(PlantActivity.this, "Error updating plant", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }
}
