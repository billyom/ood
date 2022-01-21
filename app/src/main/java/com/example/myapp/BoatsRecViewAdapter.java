package com.example.myapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class BoatsRecViewAdapter extends RecyclerView.Adapter<BoatsRecViewAdapter.ViewHolder> {

    private ArrayList<Boat> boats = new ArrayList<>();

    public BoatsRecViewAdapter() {
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.boats_rec_view_item,
                parent, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    // This is how the RecyclerView asks the application to populate the ViewHolder with the
    // details of a specific item
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.boatsRecViewNameTxtView.setText(boats.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return boats.size();
    }

    public void setBoats(ArrayList<Boat> boats) {
        this.boats = boats;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView boatsRecViewNameTxtView;
        public ViewHolder(@NonNull View itemView) {

            super(itemView);

            boatsRecViewNameTxtView = itemView.findViewById(R.id.boatNameView);
        }
    }
}
