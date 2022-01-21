package com.example.myapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

public class activity_map extends AppCompatActivity {

    // TODO https://stackoverflow.com/questions/6178896/how-to-draw-a-line-in-imageview-on-android

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);


        DrawImageView drawView = (DrawImageView) findViewById(R.id.mapView);

        // set start coords
        drawView.left = 0;
        drawView.top = 0;
        drawView.right = 400;
        drawView.bottom = 400;

        // draw
        drawView.invalidate();
        drawView.drawRect = true;
    }

    public void onGotoBoatsClick(View view){
        finish();
    }
}