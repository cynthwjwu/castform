package com.example.cynthia.weatherapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.ParseException;
import java.util.List;

public class PastActivity extends AppCompatActivity implements LocationListener {

    EditText date;
    TextView pastTemp;
    TextView pastSummary;
    TextView pastPrecip;
    TextView pastHumidity;
    TextView pastLoc;
    Button goBack;
    Button submit;
    String dateStr;
    private double lat;
    private double lon;
    private LocationManager locationManager;
    private Location loc;
    private String bestProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past);

        goBack = (Button) findViewById(R.id.psToMain);
        submit = (Button) findViewById(R.id.submit);
        pastTemp = (TextView) findViewById(R.id.pastTemp);
        pastSummary = (TextView) findViewById(R.id.pastSummary);
        pastPrecip = (TextView) findViewById(R.id.pastPrecip);
        pastHumidity = (TextView) findViewById(R.id.pastHumidity);
        pastLoc = (TextView) findViewById(R.id.pastLoc);

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
        pastLoc.setText("Location could not be fetched");

        submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                date = (EditText) findViewById(R.id.dateInput);
                dateStr = date.getText().toString();
                dateStr = dateStr + " 00:00:00";

                long epoch = 0;
                String epochStr = "";
                Date date = null;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    date = sdf.parse(dateStr);
                } catch (ParseException e) {
                    Log.e("ParseException", "Cannot parse date", e);
                }
                epoch = date.getTime() / 1000;
                epochStr = Long.toString(epoch); //the epoch time that we need to add to the end of the url

                //HTTP GET REQUEST
                String result; // Will contain JSON result
                String pastURL = "https://api.darksky.net/forecast/363920c658f7e429516bf47f6e0a9be0/" + lat + "," + lon + "," + epochStr;

                HttpGetRequest req = new HttpGetRequest();
                try {
                    result = req.execute(pastURL).get();
                    JSONObject forecastJson = new JSONObject(result);
                    JSONObject currentData = forecastJson.getJSONObject("currently");

                    double currentTemp = currentData.getDouble("temperature");
                    pastTemp.setText("Temperature: " + Double.toString(currentTemp));

                    String currentSummary = currentData.getString("summary");
                    pastSummary.setText("Summary: " + currentSummary);

                    double currentPrecip = currentData.getDouble("precipIntensity");
                    pastPrecip.setText("Precipitation: " + Double.toString(currentPrecip));

                    double currentHumidity = currentData.getDouble("humidity");
                    pastHumidity.setText("Humidity: " + Double.toString(currentHumidity));

                } catch (Exception e) {
                    pastTemp.setText("Cannot retrieve the weather data for this date");
                }
            }
        });
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
                pastLoc.setText("");
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
        pastLoc.setText("");
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
