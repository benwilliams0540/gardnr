package com.cu.gardnr;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.jar.Manifest;

public class CreatePlantActivity extends AppCompatActivity {
    SQLiteDatabase db;
    private String username;
    private String imagePath;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_plant);
        username = getIntent().getStringExtra("username");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imagePath = "";
        setupUI();
    }

    private void setupUI(){
        Spinner waterSpinner = (Spinner) findViewById(R.id.waterSpinner);
        waterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_SHORT);
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0){

            }
        });

        List<String> frequencies = new ArrayList<String>();
        frequencies.add("1x a week");
        frequencies.add("2x a week");
        frequencies.add("3x a week");

        ArrayAdapter<String> waterAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, frequencies);
        waterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        waterSpinner.setAdapter(waterAdapter);
    }

    public void addPhoto(View view){
        if (Build.VERSION.SDK_INT < 23) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 1);
        }
        else {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
            else {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1);
            }
        }
    }

    public void takePhoto(View view){
        if (Build.VERSION.SDK_INT < 23) {
            savePhoto();
        }
        else {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
            }
            else {
                savePhoto();
            }
        }
    }

    private void savePhoto(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (Build.VERSION.SDK_INT < 23) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.cu.gardnr.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, 2);
            }
        }
        else {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA}, 3);
            }
            else {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this,
                            "com.cu.gardnr.fileprovider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, 2);
                }
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        imagePath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1);
            }
        }
        else if (requestCode == 2){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                savePhoto();
            }
        }
        else if (requestCode == 3){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this,
                            "com.cu.gardnr.fileprovider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, 2);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ImageView plantImage = (ImageView) findViewById(R.id.plantImage);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null){
            Uri selectedImage = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                plantImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
//            Bundle extras = data.getExtras();
//            Bitmap bitmap = (Bitmap) extras.get("data");
//            plantImage.setImageBitmap(bitmap);
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            plantImage.setImageBitmap(bitmap);
        }
    }

    public void addPlant(View view){
        EditText nameText = (EditText) findViewById(R.id.nameText);
        EditText locationText = (EditText) findViewById(R.id.locationText);
        EditText lightText = (EditText) findViewById(R.id.lightText);
        String name = nameText.getText().toString();
        String location = locationText.getText().toString();
        String light = lightText.getText().toString();

        if (name.length() < 1) {
            Toast.makeText(CreatePlantActivity.this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
        }
        else if (location.length() < 1) {
            Toast.makeText(CreatePlantActivity.this, "Location cannot be empty", Toast.LENGTH_SHORT).show();
        }
        else if (light.length() < 1) {
            Toast.makeText(CreatePlantActivity.this, "Light cannot be empty", Toast.LENGTH_SHORT).show();
        }
        else if (imagePath.equals("")){
            Toast.makeText(CreatePlantActivity.this, "A photo of the plant must be set", Toast.LENGTH_SHORT).show();
        }
        else {
            try {
                String sqlString = "CREATE TABLE IF NOT exists plants (pid INTEGER PRIMARY KEY, image VARCHAR, username VARCHAR, name VARCHAR, location VARCHAR, light VARCHAR, water VARCHAR)";
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
            insertValues.put("water", "1x a week");

            try {
                db.insertOrThrow("plants", null, insertValues);
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(CreatePlantActivity.this, "Error adding plant", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    public void launchSettings(MenuItem menu){
        startActivity(new Intent(CreatePlantActivity.this, MainActivity.class));
    }
    public void launchInfo(MenuItem menu){
        startActivity(new Intent(CreatePlantActivity.this, MainActivity.class));
    }
}
