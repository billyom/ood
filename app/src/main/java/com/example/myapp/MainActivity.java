package com.example.myapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;

public class MainActivity extends AppCompatActivity {

    private RecyclerView boatsRecView;
    ArrayList<Boat> boats = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }


        boatsRecView = findViewById(R.id.boatsRecView);

        BoatsRecViewAdapter adapter = new BoatsRecViewAdapter();
        adapter.setBoats(boats);
        boatsRecView.setAdapter(adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(boatsRecView);
        boatsRecView.setLayoutManager(new LinearLayoutManager(this));

    }

    public void onStartClick(View view) {

        Chronometer chron = findViewById(R.id.chronView);
        chron.setBase(SystemClock.elapsedRealtime() + 60 * 5 * 1000);
        chron.start();
        ((Button)view).setEnabled(false);
    }

    public void onFinishClick(View view) {
        Chronometer chron = findViewById(R.id.chronView);
        String timeStr = chron.getText().toString();

        // Find the Finish Time TextView associated with the Finish Button wot's been clicked.
        ViewGroup boatItem = (ViewGroup) view.getParent();
        for (int viewPos = 0; viewPos < boatItem.getChildCount(); viewPos++) {
            View siblingView = boatItem.getChildAt(viewPos);
            if (siblingView.getId() == R.id.finishTimeView) {
                ((TextView) siblingView).setText(timeStr);
                break;
            }
        }
    }

    public void onGotoMapClick(View view) {
        startActivity(new Intent(this, activity_map.class));
    }

    public void onAddClick(View view) {
        String boatName = ((EditText) findViewById(R.id.boatEdtTxt)).getText().toString();
        // TODO check boat name does not already exist or is not empty
        // TODO deactivate ADD btn if txtView is empty

        boats.add(new Boat(boatName));
        boatsRecView.getAdapter().notifyItemInserted(boats.size());

        Toast.makeText(this, boatName, Toast.LENGTH_SHORT).show();
    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END, 0) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int fromPos = viewHolder.getAdapterPosition();
            int toPos = target.getAdapterPosition();

            Collections.swap(boats, fromPos, toPos);
            recyclerView.getAdapter().notifyItemMoved(fromPos, toPos);
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }
    };
}