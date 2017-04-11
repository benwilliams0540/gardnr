package com.cu.gardnr;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class AddPlantActivity extends AppCompatActivity {
    private SharedPreferences preferences;
    private SQLiteDatabase db;
    private String username;
    private String imagePath;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                AlertDialog.Builder builder = new AlertDialog.Builder(AddPlantActivity.this);
                builder.setTitle("Are you sure you want to discard this draft?")
                        .setIcon(R.drawable.ic_delete)
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                return;
                            }
                        })
                        .setPositiveButton("Discard", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                AddPlantActivity.super.onBackPressed();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AddPlantActivity.this);
        builder.setTitle("Are you sure you want to discard this draft?")
                .setIcon(R.drawable.ic_delete)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        return;
                    }
                })
                .setPositiveButton("Discard", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AddPlantActivity.super.onBackPressed();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_plant);
        username = getIntent().getStringExtra("username");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        imagePath = "";
        setupUI();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState){
        super.onPostCreate(savedInstanceState);

        preferences = this.getSharedPreferences("com.cu.gardnr", Context.MODE_PRIVATE);
        if (preferences.getBoolean("firstRun", true)){
            Handler customHandler = new Handler();
            customHandler.postDelayed(firstTutorial, 1000);
            preferences.edit().putBoolean("firstRun", false).apply();
        }
    }

    private void setupUI(){
        Spinner frequencySpinner = (Spinner) findViewById(R.id.frequencySpinner);
        String[] frequencies = getResources().getStringArray(R.array.frequencies);

        ArrayAdapter<String> waterAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, frequencies);
        waterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        frequencySpinner.setAdapter(waterAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.cameraButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] sources = {"Gallery", "Camera"};
                AlertDialog.Builder builder = new AlertDialog.Builder(AddPlantActivity.this);

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

    private Runnable firstTutorial = new Runnable () {
        public void run() {
            launchTutorial(null);
        }
    };

    public void launchTutorial(MenuItem menu){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        LinearLayout nameLayout = (LinearLayout) findViewById(R.id.nameLayout);
        LinearLayout locationLayout = (LinearLayout) findViewById(R.id.locationLayout);
        LinearLayout lightLayout = (LinearLayout) findViewById(R.id.lightLayout);
        LinearLayout waterLayout = (LinearLayout) findViewById(R.id.waterLayout);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.cameraButton);

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this);
        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setMaskColour(R.color.colorPrimary)
                        .setTarget(nameLayout)
                        .setDismissText("GOT IT")
                        .setContentText("Set the plant name here (e.g. 'Hydrangea')")
                        .withRectangleShape()
                        .setDelay(250)
                        .build()
        );

        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setMaskColour(R.color.colorPrimary)
                        .setTarget(locationLayout)
                        .setDismissText("GOT IT")
                        .setContentText("Set the plant's physical location here (e.g. 'Front porch')")
                        .withRectangleShape()
                        .setDelay(250)
                        .build()
        );
        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setMaskColour(R.color.colorPrimary)
                        .setTarget(lightLayout)
                        .setDismissText("GOT IT")
                        .setContentText("Enter the plant's light requirements here (e.g. 'Moderate shade')")
                        .withRectangleShape()
                        .setDelay(250)
                        .build()
        );
        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setMaskColour(R.color.colorPrimary)
                        .setTarget(waterLayout)
                        .setDismissText("GOT IT")
                        .setContentText("Enter the plant's watering frequency here")
                        .withRectangleShape()
                        .setDelay(250)
                        .build()
        );
        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setMaskColour(R.color.colorPrimary)
                        .setTarget(fab)
                        .setDismissText("GOT IT")
                        .setContentText("Select a photo for the plant from either the gallery or camera here")
                        .setDelay(250)
                        .build()
        );
        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setMaskColour(R.color.colorPrimary)
                        .setTarget(toolbar.getChildAt(2))
                        .setDismissText("GOT IT")
                        .setContentText("When you are finished entering the plant's information, select the check mark to add it to your garden!")
                        .setDelay(250)
                        .build()
        );
        sequence.start();
    }
    public void createPlant(MenuItem menu){
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
            Toast.makeText(AddPlantActivity.this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
        }
        else if (location.length() < 1) {
            Toast.makeText(AddPlantActivity.this, "Location cannot be empty", Toast.LENGTH_SHORT).show();
        }
        else if (light.length() < 1) {
            Toast.makeText(AddPlantActivity.this, "Light cannot be empty", Toast.LENGTH_SHORT).show();
        }
        else if (imagePath.equals("")){
            Toast.makeText(AddPlantActivity.this, "A photo of the plant must be set", Toast.LENGTH_SHORT).show();
        }
        else {
            try {
                String sqlString = "CREATE TABLE IF NOT exists plants (pid INTEGER PRIMARY KEY, image VARCHAR, username VARCHAR, name VARCHAR, location VARCHAR, light VARCHAR, water VARCHAR, notification VARCHAR)";
                db = this.openOrCreateDatabase("gardnr", MODE_PRIVATE, null);
                db.execSQL(sqlString);
            } catch (Exception e) {
                e.printStackTrace();
            }

            ContentValues insertValues = new ContentValues();
            insertValues.put("image", imagePath);
            insertValues.put("username", username);
            insertValues.put("name", name);
            insertValues.put("location", location);
            insertValues.put("light", light);
            insertValues.put("water", frequency);
            insertValues.put("notification", notification);

            try {
                db.insertOrThrow("plants", null, insertValues);
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(AddPlantActivity.this, "Error adding plant", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }
}
