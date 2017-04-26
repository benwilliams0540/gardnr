package com.cu.gardnr;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class FullScreenActivity extends Activity {
    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        Bundle extras = getIntent().getExtras();
        String image = extras.getString("image");
        Bitmap bmp = BitmapFactory.decodeFile(image);
        //Bitmap bmp = (Bitmap) extras.getParcelable("imagebitmap");

        ImageView imgDisplay;
        Button btnClose;


        imgDisplay = (ImageView) findViewById(R.id.imgDisplay);
        btnClose = (Button) findViewById(R.id.btnClose);


        btnClose.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FullScreenActivity.this.finish();
            }
        });


        imgDisplay.setImageBitmap(bmp );

    }


}
