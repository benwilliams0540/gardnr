package com.cu.gardnr;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

public class PlantTouchHelper extends ItemTouchHelper.SimpleCallback {
    private PlantAdapter plantAdapter;
    private Context context;


    public PlantTouchHelper(PlantAdapter plantAdapter, Context context){
        super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.plantAdapter = plantAdapter;
        this.context = context;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return true;
    }

    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction){
        plantAdapter.onItemRemove(viewHolder);
    }
}
