package com.example.cynthia.weatherapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ForecastActivity extends AppCompatActivity implements LocationListener {

    TextView temp1Hr;
    TextView temp2Hr;
    TextView temp3Hr;
    TextView temp4Hr;
    TextView temp5Hr;
    TextView temp1DayLow;
    TextView temp1DayHigh;
    TextView temp2DayLow;
    TextView temp2DayHigh;
    TextView temp3DayLow;
    TextView temp3DayHigh;
    TextView temp4DayLow;
    TextView temp4DayHigh;
    TextView temp5DayLow;
    TextView temp5DayHigh;
    TextView temp6DayLow;
    TextView temp6DayHigh;
    TextView temp7DayLow;
    TextView temp7DayHigh;
    TextView presentLoc;
    Button goBack;
    private double lat;
    private double lon;
    private LocationManager locationManager;
    private Location loc;
    private String bestProvider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        temp1Hr = (TextView) findViewById(R.id.temp1Hr);
        temp2Hr = (TextView) findViewById(R.id.temp2Hr);
        temp3Hr = (TextView) findViewById(R.id.temp3Hr);
        temp4Hr = (TextView) findViewById(R.id.temp4Hr);
        temp5Hr = (TextView) findViewById(R.id.temp5Hr);
        temp1DayLow = (TextView) findViewById(R.id.temp1DayLow);
        temp1DayHigh = (TextView) findViewById(R.id.temp1DayHigh);
        temp2DayLow = (TextView) findViewById(R.id.temp2DayLow);
        temp2DayHigh = (TextView) findViewById(R.id.temp2DayHigh);
        temp3DayLow = (TextView) findViewById(R.id.temp3DayLow);
        temp3DayHigh = (TextView) findViewById(R.id.temp3DayHigh);
        temp4DayLow = (TextView) findViewById(R.id.temp4DayLow);
        temp4DayHigh = (TextView) findViewById(R.id.temp4DayHigh);
        temp5DayLow = (TextView) findViewById(R.id.temp5DayLow);
        temp5DayHigh = (TextView) findViewById(R.id.temp5DayHigh);
        temp6DayLow = (TextView) findViewById(R.id.temp6DayLow);
        temp6DayHigh = (TextView) findViewById(R.id.temp6DayHigh);
        temp7DayLow = (TextView) findViewById(R.id.temp7DayLow);
        temp7DayHigh = (TextView) findViewById(R.id.temp7DayHigh);
        presentLoc = (TextView) findViewById(R.id.presentLoc);
        goBack = (Button) findViewById(R.id.fcToMain);

        //default coordinates
        lat = 30.2672;
        lon = -97.7431;

        //go back to main page
        goBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        //Get all providers
        List<String> providers = locationManager.getAllProviders();
        Criteria criteria = new Criteria();
        bestProvider = locationManager.getBestProvider(criteria, false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 10);
            loc = locationManager.getLastKnownLocation(bestProvider);
            return;
        }
        else {
            loc = locationManager.getLastKnownLocation(bestProvider);
        }
        presentLoc.setText("Location could not be fetched");

        String result; // Will contain JSON result
        String forecastURL = "https://api.darksky.net/forecast/363920c658f7e429516bf47f6e0a9be0/" + lat + "," + lon;

        //*****CURRENT WEATHER DATA*****
        HttpGetRequest req = new HttpGetRequest();
        try {
            result = req.execute(forecastURL).get();
            JSONObject forecastJson = new JSONObject(result);



            //Get average temperature over the next 48 hours
            ArrayList<Double> hourlyTemps = new ArrayList<Double>();
            JSONObject hourlyData = forecastJson.getJSONObject("hourly");
            JSONArray hourlyDataArray = hourlyData.getJSONArray("data");

            for (int i = 0; i < hourlyDataArray.length(); i++) {
                JSONObject hourData = hourlyDataArray.getJSONObject(i);
                double hourTemp = hourData.getDouble("temperature");
                hourlyTemps.add(hourTemp);
            }


            //Get temperature for the next 5 hours
            temp1Hr.setText("Temp in 1 Hr: " + hourlyTemps.get(0));
            temp2Hr.setText("Temp in 2 Hrs: " + hourlyTemps.get(1));
            temp3Hr.setText("Temp in 3 Hrs: " + hourlyTemps.get(2));
            temp4Hr.setText("Temp in 4 Hrs: " + hourlyTemps.get(3));
            temp5Hr.setText("Temp in 5 Hrs: " + hourlyTemps.get(4));

            //Get temperature for the next 7 days
            ArrayList<Double> dailyLowTemps = new ArrayList<Double>();
            ArrayList<Double> dailyHighTemps = new ArrayList<Double>();
            JSONObject dailyData = forecastJson.getJSONObject("daily");
            JSONArray dailyDataArray = dailyData.getJSONArray("data");

            for (int i = 0; i < dailyDataArray.length(); i++) {
                JSONObject dayData = dailyDataArray.getJSONObject(i);
                double lowTemp = dayData.getDouble("temperatureLow");
                double highTemp = dayData.getDouble("temperatureHigh");
                dailyLowTemps.add(lowTemp);
                dailyHighTemps.add(highTemp);
            }

            temp1DayLow.setText("Day 1 Low: " + dailyLowTemps.get(0));
            temp1DayHigh.setText("Day 1 High: " + dailyHighTemps.get(0));
            temp2DayLow.setText("Day 2 Low: " + dailyLowTemps.get(1));
            temp2DayHigh.setText("Day 2 High: " + dailyHighTemps.get(1));
            temp3DayLow.setText("Day 3 Low: " + dailyLowTemps.get(2));
            temp3DayHigh.setText("Day 3 High: " + dailyHighTemps.get(2));
            temp4DayLow.setText("Day 4 Low: " + dailyLowTemps.get(3));
            temp4DayHigh.setText("Day 4 High: " + dailyHighTemps.get(3));
            temp5DayLow.setText("Day 5 Low: " + dailyLowTemps.get(4));
            temp5DayHigh.setText("Day 5 High: " + dailyHighTemps.get(4));
            temp6DayLow.setText("Day 6 Low: " + dailyLowTemps.get(5));
            temp6DayHigh.setText("Day 6 High: " + dailyHighTemps.get(5));
            temp7DayLow.setText("Day 7 Low: " + dailyLowTemps.get(6));
            temp7DayHigh.setText("Day 7 High: " + dailyHighTemps.get(6));

        } catch (Exception e) {
            temp1Hr.setText("Cannot display current weather data");
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 10);
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            return;
        } else {
            loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (loc != null) {
                lat = loc.getLatitude();
                lon = loc.getLongitude();
            }
            else {
                presentLoc.setText("");
            }
        }
        locationManager.requestLocationUpdates(bestProvider, 20000, 1, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        lat = loc.getLatitude();
        lon = loc.getLongitude();
        presentLoc.setText("");
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }



    /**
     *  HTTP GET REQUEST
     */
    private class HttpGetRequest extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection con = null;
            BufferedReader br = null;

            String respJson = null; //will contain json response as string

            try {
                //construct url
                URL url = new URL(params[0]);

                //create request to DarkSky and open connection
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.connect();

                //read input stream into a string
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder sb = new StringBuilder();

                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }

                //stream empty. do not need to parse
                if (sb.length() == 0) {
                    return null;
                }

                respJson = sb.toString();
                return respJson;

            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error", e);
                return null;
            } finally {
                if (con != null) {
                    con.disconnect();
                }
                if (br != null) {
                    try {
                        br.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.i("json", result);
        }

    }
}
