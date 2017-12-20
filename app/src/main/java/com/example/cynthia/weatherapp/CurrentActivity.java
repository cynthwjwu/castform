package com.example.cynthia.weatherapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.DecimalFormat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CurrentActivity extends AppCompatActivity implements LocationListener {

    TextView temperature;
    TextView humidity;
    TextView windSpeed;
    TextView precip;
    TextView avg48Hrs;
    TextView currentLoc;
    Button goBack;
    private LocationManager locationManager;
    private Location loc;
    double lat;
    double lon;
    private String bestProvider;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current);

        temperature = (TextView) findViewById(R.id.temperature);
        humidity = (TextView) findViewById(R.id.humidity);
        windSpeed = (TextView) findViewById(R.id.windSpeed);
        precip = (TextView) findViewById(R.id.precip);
        avg48Hrs = (TextView) findViewById(R.id.avg48Hrs);
        currentLoc = (TextView) findViewById(R.id.currentLoc);
        goBack = (Button) findViewById(R.id.crToMain);

        //default coordinates
        lat = 30.2672;
        lon = -97.7431;

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
        currentLoc.setText("Location could not be fetched");

        //go back to main page
        goBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });



        String result; // Will contain JSON result
        String forecastURL = "https://api.darksky.net/forecast/363920c658f7e429516bf47f6e0a9be0/" + lat + "," + lon;

        HttpGetRequest req = new HttpGetRequest();
        try {
            result = req.execute(forecastURL).get();
            JSONObject forecastJson = new JSONObject(result);
            JSONObject currentData = forecastJson.getJSONObject("currently");

            //Get current data
            double currentTemp = currentData.getDouble("temperature");
            temperature.setText("Temperature: " + Double.toString(currentTemp));

            //Get current humidity
            double humid = currentData.getDouble("humidity");
            humidity.setText("Humidity: " + Double.toString(humid));

            //Get wind speed
            double wind = currentData.getDouble("windSpeed");
            windSpeed.setText("Wind Speed: " + Double.toString(wind));

            //Get precipitation
            double rain = currentData.getDouble("precipIntensity");
            precip.setText("Precipitation: " + Double.toString(rain));


            //Get average temperature over the next 48 hours
            double avg = 0;
            ArrayList<Double> hourlyTemps = new ArrayList<Double>();
            JSONObject hourlyData = forecastJson.getJSONObject("hourly");
            JSONArray hourlyDataArray = hourlyData.getJSONArray("data");

            for (int i = 0; i < hourlyDataArray.length(); i++) {
                JSONObject hourData = hourlyDataArray.getJSONObject(i);
                double hourTemp = hourData.getDouble("temperature");
                hourlyTemps.add(hourTemp);
            }

            for (Double temp : hourlyTemps) {
                avg += temp;
            }

            avg = avg / (double) hourlyTemps.size();
            String avgStr = new DecimalFormat("#.##").format(avg);
            avg48Hrs.setText("Average Temp over Next 48 Hours: " + avgStr);

        } catch (Exception e) {
            temperature.setText("Cannot display current weather data");
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
                currentLoc.setText("");
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
        currentLoc.setText("");
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
