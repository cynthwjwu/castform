package com.example.cynthia.weatherapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;

import android.content.Intent;


public class MainActivity extends AppCompatActivity {

    Button getCurrent;
    Button getForecast;
    Button getPast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getCurrent = (Button) findViewById(R.id.getCurrent);
        getForecast = (Button) findViewById(R.id.getForecast);
        getPast = (Button) findViewById(R.id.getPast);

        //get current weather
        getCurrent.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), CurrentActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });
        //get forecast
        getForecast.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), ForecastActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });
        //get past weather
        getPast.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), PastActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });
    }
}
