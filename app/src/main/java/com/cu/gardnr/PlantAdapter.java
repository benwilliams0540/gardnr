package com.cu.gardnr;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.PlantViewHolder>{
    List<Plant> plants;
    List<Plant> plantsToDelete = new ArrayList<Plant>();
    RecyclerView recyclerView;

    PlantAdapter(List<Plant> plants){
        this.plants = plants;
    }

    public static class PlantViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView plantName;
        TextView plantLocation;

        TextView waterTag;
        EditText plantWater;

        ImageView plantPhoto;
        private int mOriginalHeight = 0;
        private boolean mIsViewExpanded = false;

        PlantViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cv);
            plantName = (TextView) itemView.findViewById(R.id.plant_name);
            plantLocation = (TextView) itemView.findViewById(R.id.plant_location);

            waterTag = (TextView) itemView.findViewById(R.id.water_tag);
            plantWater = (EditText) itemView.findViewById(R.id.plant_water);

            plantPhoto = (ImageView) itemView.findViewById(R.id.plant_photo);
        }
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
    public void onBindViewHolder(final PlantViewHolder plantViewHolder, final int i){
        plantViewHolder.plantName.setText(plants.get(i).getName());
        plantViewHolder.plantLocation.setText("Location: " + plants.get(i).getLocation());
        plantViewHolder.plantWater.setText(plants.get(i).getWater());
        String imagePath = plants.get(i).getImage();
        Handler customHandler = new Handler();
        Runnable scaleImage = createImageRunnable(plantViewHolder, imagePath);
        customHandler.post(scaleImage);

        plantViewHolder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
//                if (plantViewHolder.expandedView.getVisibility() == View.GONE) {
//                    plantViewHolder.expandedView.setVisibility(View.VISIBLE);
//                }
//                else {
//                    plantViewHolder.expandedView.setVisibility(View.GONE);
//                }
//                Log.i("Clicked", "" + plants.get(i).getPID());
                if (plantViewHolder.mOriginalHeight == 0) {
                    plantViewHolder.mOriginalHeight = v.getHeight();
                }
                ValueAnimator valueAnimator;
                if (!plantViewHolder.mIsViewExpanded) {
                    //plantViewHolder.expandedView.setVisibility(View.VISIBLE);
                    plantViewHolder.waterTag.setVisibility(View.VISIBLE);
                    plantViewHolder.plantWater.setVisibility(View.VISIBLE);
                    plantViewHolder.mIsViewExpanded = true;
                    valueAnimator = ValueAnimator.ofInt(plantViewHolder.mOriginalHeight, plantViewHolder.mOriginalHeight + (int) (plantViewHolder.mOriginalHeight * 1.5));
                } else {
                    //plantViewHolder.expandedView.setVisibility(View.GONE);
                    plantViewHolder.waterTag.setVisibility(View.GONE);
                    plantViewHolder.plantWater.setVisibility(View.GONE);
                    plantViewHolder.mIsViewExpanded = false;
                    valueAnimator = ValueAnimator.ofInt(plantViewHolder.mOriginalHeight + (int) (plantViewHolder.mOriginalHeight * 1.5), plantViewHolder.mOriginalHeight);
                }
                valueAnimator.setDuration(300);
                valueAnimator.setInterpolator(new LinearInterpolator());
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator animation) {
                        Integer value = (Integer) animation.getAnimatedValue();
                        v.getLayoutParams().height = value.intValue();
                        v.requestLayout();
                    }
                });
                valueAnimator.start();

            }
        });
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void onItemRemove(final RecyclerView.ViewHolder viewHolder) {
        final int adapterPosition = viewHolder.getAdapterPosition();
        final Plant plant = plants.get(adapterPosition);
        Snackbar snackbar = Snackbar
                .make(recyclerView, "PLANT REMOVED", Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.i("position", "" + adapterPosition);
                        Handler customHandler = new Handler();
                        Runnable undo = createUndoRunnable(plant, adapterPosition);
                        customHandler.post(undo);
                    }
                });
        snackbar.show();
        MainActivity.removePlant(plant);
        plants.remove(adapterPosition);
        notifyItemRemoved(adapterPosition);
        plantsToDelete.add(plant);
    }

    private Runnable createImageRunnable(final PlantViewHolder plantViewHolder, final String imagePath) {
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

    private Runnable createUndoRunnable(final Plant plant, final int position){
        Runnable undo = new Runnable() {
            public void run() {
                MainActivity.cancelRemove();
                plants.add(position, plant);
                notifyItemInserted(position);
                recyclerView.scrollToPosition(position);
                plantsToDelete.remove(plant);
            }
        };
        return undo;
    }
}
