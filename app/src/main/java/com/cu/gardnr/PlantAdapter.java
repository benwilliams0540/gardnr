package com.cu.gardnr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.PlantViewHolder>{
    List<Plant> plants;
    String imagePath;

    public static class PlantViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView plantName;
        TextView plantLocation;
        ImageView plantPhoto;

        PlantViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cv);
            plantName = (TextView) itemView.findViewById(R.id.plant_name);
            plantLocation = (TextView) itemView.findViewById(R.id.plant_location);
            plantPhoto = (ImageView) itemView.findViewById(R.id.plant_photo);
        }
    }

    PlantAdapter(List<Plant> plants){
        this.plants = plants;
    }

    @Override
    public int getItemCount() {
        return plants.size();
    }

    @Override
    public PlantViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item, viewGroup, false);
        PlantViewHolder pvh = new PlantViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(PlantViewHolder plantViewHolder, int i){
        plantViewHolder.plantName.setText(plants.get(i).getName());
        plantViewHolder.plantLocation.setText(plants.get(i).getLocation());
        imagePath = plants.get(i).getImage();
        Handler customHandler = new Handler();
        Runnable scaleImage = createRunnable(plantViewHolder, imagePath);
        customHandler.postDelayed(scaleImage, 0);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    private Runnable createRunnable(final PlantViewHolder plantViewHolder, final String imagePath) {
        Runnable scaleImage = new Runnable() {
            public void run() {
                int targetW = 200;
                int targetH = 242;

                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(imagePath, bmOptions);
                int photoW = bmOptions.outWidth;
                int photoH = bmOptions.outHeight;
                int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

                bmOptions.inJustDecodeBounds = false;
                bmOptions.inSampleSize = scaleFactor;

                Bitmap bitmap = BitmapFactory.decodeFile(imagePath, bmOptions);
                plantViewHolder.plantPhoto.setImageBitmap(bitmap);
            }
        };
        return scaleImage;
    }
}
