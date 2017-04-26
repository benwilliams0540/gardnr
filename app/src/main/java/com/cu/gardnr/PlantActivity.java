package com.cu.gardnr;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.util.DisplayMetrics;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class PlantActivity extends AppCompatActivity {
    private static SQLiteDatabase db;
    private static SharedPreferences preferences;
    private static Handler customHandler = new Handler();
    private Integer pid;
    private Plant plant;
    private ArrayList<Plant> photos;
    private String imagePath = "";
    private String username;
    private boolean timelinePhoto = false;
    private String timelineURI;
    private String timelineURL;

    private ProgressDialog progressDialog;
    private Runnable loadPlant = new Runnable() {
        public void run() {
            try {
                String sqlString = "CREATE TABLE IF NOT exists plants (pid INTEGER PRIMARY KEY, image VARCHAR, username VARCHAR, name VARCHAR, location VARCHAR, light VARCHAR, water VARCHAR, notification VARCHAR)";
                db = PlantActivity.this.openOrCreateDatabase("gardnr", MODE_PRIVATE, null);
                db.execSQL(sqlString);
            } catch (Exception e) {
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
            //setupUI();
            new GetPhotos().execute();
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                AlertDialog.Builder builder = new AlertDialog.Builder(PlantActivity.this);

                builder.setTitle("Are you sure you want to discard these changes?")
                        .setIcon(R.drawable.ic_delete)
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                return;
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
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PlantActivity.this);
        builder.setTitle("Are you sure you want to discard these changes?")
                .setIcon(R.drawable.ic_delete)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        return;
                    }
                })
                .setPositiveButton("Discard", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        PlantActivity.super.onBackPressed();
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
        setContentView(R.layout.activity_plant);

        pid = getIntent().getIntExtra("plant", 0);
        username = getIntent().getStringExtra("username");
        photos = new ArrayList<Plant>();

        progressDialog = ProgressDialog.show(this, "Loading timeline picture", "", false);
        preferences = this.getSharedPreferences("com.cu.gardnr", Context.MODE_PRIVATE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        customHandler.post(loadPlant);
    }

    private void downloadImage(int pid, int id, String image){
        PlantActivity.ImageDownloader task = new PlantActivity.ImageDownloader();
        try {
            Bitmap bitmap = task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, image).get();
            savePhoto(bitmap, pid, id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File createImageFile(int pid, int id) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        preferences.edit().putString("" + pid + id, image.getAbsolutePath()).apply();

        for (int i = 0; i < photos.size(); i++){
            if (photos.get(i).getID().equals(id)){
                photos.get(i).setImage(image.getAbsolutePath());
            }
        }

        return image;
    }

    private void savePhoto(Bitmap image, int pid, int id) {
        try {
            File photoFile = createImageFile(pid, id);
            FileOutputStream fos = new FileOutputStream(photoFile);
            image.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d("", "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d("", "Error accessing file: " + e.getMessage());
        }
    }

    private void setupUI(){
        imagePath = plant.getImage();
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

        String[] frequencies = getResources().getStringArray(R.array.frequencies);

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

        for (int i = 0; i < photos.size(); i++){
            Log.i("imagePath", photos.get(i).getImage());

            final Bitmap b = BitmapFactory.decodeFile(photos.get(i).getImage());
            final ImageView image = new ImageView(this);

            image.setImageBitmap(Bitmap.createScaledBitmap(b, dpToPx(80), dpToPx(80), false));
            image.setPadding(dpToPx(2), 0, dpToPx(2), 0);

            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(PlantActivity.this, FullScreenActivity.class);

                    Bundle extras = new Bundle();
                    extras.putString("image", plant.getImage());
                    intent.putExtras(extras);
                    startActivity(intent);
                }
            });

            LinearLayout timelineLayout = (LinearLayout) findViewById(R.id.timelineLayout);
            timelineLayout.addView(image);
        }
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getBaseContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
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

        if (timelinePhoto) {
            String filename = image.getAbsolutePath().substring(image.getAbsolutePath().lastIndexOf("/") + 1);
            timelineURL = "https://people.cs.clemson.edu/~brw2/x820/gardnr/scripts/uploads/" + filename;
            timelineURI = image.getAbsolutePath();
        } else {
            imagePath = image.getAbsolutePath();
        }
        return image;
    }

    private boolean checkStoragePermissions(){
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }
        else {
            return checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private boolean checkCameraPermissions(){
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }
        else {
            return checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void addPhoto(int source){
        File photoFile = null;

        if (source == 0){
            timelinePhoto = false;
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
            timelinePhoto = false;
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
        } else if (source == 2) {
            timelinePhoto = true;
            if (Build.VERSION.SDK_INT < 23) {
                Intent takePictureIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(takePictureIntent, 1);
            } else {
                if (checkStoragePermissions()) {
                    Intent takePictureIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(takePictureIntent, 1);
                } else {
                    requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                }
            }
        } else if (source == 3) {
            timelinePhoto = true;
            if (Build.VERSION.SDK_INT < 23) {
                saveCameraPhoto();
            } else {
                if (checkCameraPermissions()) {
                    saveCameraPhoto();
                } else {
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

        new UploadFileAsync().execute();
        new AddPhoto().execute();
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

        new UploadFileAsync().execute();
        new AddPhoto().execute();
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

    public void addTimelinePhoto(View v) {
        String[] sources = {"Gallery", "Camera"};
        AlertDialog.Builder builder = new AlertDialog.Builder(PlantActivity.this);

        builder.setTitle("Choose a source")
                .setItems(sources, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int position) {
                        addPhoto(position + 2);
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

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

    public void createPlant(MenuItem menu) {
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

    private class GetPhotos extends AsyncTask<String, String, String> {
        protected String doInBackground(String... args) {
            JSONParser jParser = new JSONParser();
            HashMap params = new HashMap<>();
            params.put("pid", Integer.toString(plant.getPID()));
            String URL = "https://people.cs.clemson.edu/~brw2/x820/gardnr/scripts/get_photos.php";
            JSONObject json = jParser.makeHttpRequest(URL, "POST", params);

            try {
                int success = json.getInt("success");

                if (success == 1) {
                    JSONArray externalProfiles = json.getJSONArray("photos");

                    for (int i = 0; i < externalProfiles.length(); i++) {
                        JSONObject c = externalProfiles.getJSONObject(i);

                        int id = c.getInt("id");
                        int pid = c.getInt("pid");
                        String image = c.getString("image");
                        photos.add(new Plant(id, pid, image));
                    }
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
            new CheckPictures().execute();
        }
    }

    private class CheckPictures extends AsyncTask<Integer, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.setMessage("" + 1 + "/" + photos.size());
            progressDialog.setProgress(0);
            progressDialog.setSecondaryProgress(0);
        }

        @Override
        protected String doInBackground(Integer... args) {
            for (int i = 0; i < photos.size(); i++) {
                int id = photos.get(i).getID();
                int pid = photos.get(i).getPID();
                String imageURL = photos.get(i).getImage();

                String imagePath = preferences.getString("" + pid + id, "false");
                if (imagePath.equalsIgnoreCase("false")) {
                    downloadImage(pid, id, imageURL);
                } else {
                    photos.get(i).setImage(imagePath);

                    //ContentValues insertValues = new ContentValues();
                    //insertValues.put("image", imagePath);

                    //String[] argument = new String[]{Integer.toString(pid)};
                    //db.update("plants", insertValues, "pid=?", argument);
                }
                publishProgress(i);
            }

            return "success";
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int offset = 100 / photos.size();
            progressDialog.setProgress((values[0] + 1) * offset);
            progressDialog.setSecondaryProgress((values[0] + 1) * offset);
            progressDialog.setMessage("" + (values[0] + 2) + "/" + photos.size());
        }

        @Override
        protected void onPostExecute(String s) {
            progressDialog.dismiss();
            setupUI();
        }
    }

    public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.connect();

                InputStream inputStream = connection.getInputStream();

                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                return bitmap;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class UploadFileAsync extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                Log.i("File to upload", timelineURI);
                String sourceFileUri = timelineURI;

                HttpURLConnection conn = null;
                DataOutputStream dos = null;
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                int maxBufferSize = 1 * 1024 * 1024;
                File sourceFile = new File(sourceFileUri);

                if (sourceFile.isFile()) {

                    try {
                        String uploadServerUri = "https://people.cs.clemson.edu/~brw2/x820/gardnr/scripts/upload_photo.php?";

                        // open a URL connection to the Servlet
                        FileInputStream fileInputStream = new FileInputStream(
                                sourceFile);
                        URL url = new URL(uploadServerUri);

                        // Open a HTTP connection to the URL
                        conn = (HttpURLConnection) url.openConnection();
                        conn.setDoInput(true); // Allow Inputs
                        conn.setDoOutput(true); // Allow Outputs
                        conn.setUseCaches(false); // Don't use a Cached Copy
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Connection", "Keep-Alive");
                        conn.setRequestProperty("ENCTYPE",
                                "multipart/form-data");
                        conn.setRequestProperty("Content-Type",
                                "multipart/form-data;boundary=" + boundary);
                        conn.setRequestProperty("bill", sourceFileUri);

                        dos = new DataOutputStream(conn.getOutputStream());

                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        dos.writeBytes("Content-Disposition: form-data; name=\"bill\";filename=\""
                                + sourceFileUri + "\"" + lineEnd);

                        dos.writeBytes(lineEnd);

                        // create a buffer of maximum size
                        bytesAvailable = fileInputStream.available();

                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        buffer = new byte[bufferSize];

                        // read file and write it into form...
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                        while (bytesRead > 0) {

                            dos.write(buffer, 0, bufferSize);
                            bytesAvailable = fileInputStream.available();
                            bufferSize = Math
                                    .min(bytesAvailable, maxBufferSize);
                            bytesRead = fileInputStream.read(buffer, 0,
                                    bufferSize);

                        }

                        // send multipart form data necesssary after file
                        // data...
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(twoHyphens + boundary + twoHyphens
                                + lineEnd);

                        // Responses from the server (code and message)
                        int serverResponseCode = conn.getResponseCode();
                        String serverResponseMessage = conn
                                .getResponseMessage();

                        if (serverResponseCode == 200) {

                            // messageText.setText(msg);
                            //Toast.makeText(ctx, "File Upload Complete.",
                            //      Toast.LENGTH_SHORT).show();

                            // recursiveDelete(mDirectory1);

                        }

                        // close the streams //
                        fileInputStream.close();
                        dos.flush();
                        dos.close();

                    } catch (Exception e) {

                        // dialog.dismiss();
                        e.printStackTrace();

                    }
                    // dialog.dismiss();

                } // End else block


            } catch (Exception ex) {
                // dialog.dismiss();

                ex.printStackTrace();
            }
            return "Executed";
        }
    }

    private class AddPhoto extends AsyncTask<String, String, String> {
        protected String doInBackground(String... args) {

            JSONParser jParser = new JSONParser();
            HashMap params = new HashMap<>();
            Log.i("pid", "" + plant.getPID());
            params.put("pid", Integer.toString(plant.getPID()));
            Log.i("image", timelineURL);
            params.put("image", timelineURL);

            String URL = "https://people.cs.clemson.edu/~brw2/x820/gardnr/scripts/add_photo.php";
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
        }
    }
}
