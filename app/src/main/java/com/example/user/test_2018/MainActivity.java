package com.example.user.test_2018;

import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.preference.PreferenceActivity;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;
import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    String LOCATION_PROVIDER = LocationManager.NETWORK_PROVIDER;
    LocationManager mLocationManager;
    LocationListener mLocationListener;
    boolean mLocationPermissionGranted;
    final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 123;
    final long MIN_TIME = 5000;
    // Distance between location updates (1000m or 1km)
    final float MIN_DISTANCE = 1000;
    double longitude;
    double latitude;
    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";
    // App ID to use OpenWeather data
    final String APP_ID = "";
    // Time between location updates (5000 milliseconds or 5 seconds)
    boolean locationCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Switch locationOnOff  = (Switch) findViewById(R.id.locationControl);


        // check current state of a Switch (true or false).
        if(locationOnOff.isChecked()==true)
        {
            //Check location Permission
            locationCheck=true;
            getLocationPermission();

        }
        else
        {
            //Enter City Name
        }
        locationOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                if(isChecked==true)
                {
                    locationCheck=true;
                    getLocationPermission();
                }
                else
                {
                    EditText city = (EditText) findViewById(R.id.editText);
                    city.setText("");
                    locationCheck=false;
                    if(mLocationManager!=null) {
                        mLocationManager.removeUpdates(mLocationListener);
                    }
                }

                }
        });


        //Get Current Location
        //getCurrentLocation();

        //Get City Name
        //getCityName();

        //Search Button
        ImageButton search = (ImageButton) findViewById(R.id.search);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Get Current Weather Report
                RequestParams rp = new RequestParams();
                rp.put("APPID", APP_ID);
                if(locationCheck==true) {
                    rp.put("lat", latitude);
                    rp.put("lon", longitude);
                }
                else
                {
                    EditText city = (EditText) findViewById(R.id.editText);
                    rp.put("q",city.getText());
                }
                getWeatherReport(rp);
            }
        });
    }

    private void getLocationPermission() {
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                longitude = location.getLongitude();
                latitude = location.getLatitude();
                //Get City Name
                getCityName();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                //Log.d("Clima", "onProviderDisabled() callback received");
            }

        };
    /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
        if (ActivityCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            mLocationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, mLocationListener);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    getLocationPermission();
                }
            }
        }
        //Get Current Location
        //getCurrentLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mLocationManager != null) mLocationManager.removeUpdates(mLocationListener);
    }

    public void getCurrentLocation() {
        if (mLocationPermissionGranted = true) {

            /* mLocationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                    longitude = location.getLongitude();
                    latitude = location.getLatitude();
                }
                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {
                    //Log.d("Clima", "onProviderDisabled() callback received");
                }

            }; */


        } else {

        }
    }

    public void getCityName() {
        EditText city = (EditText) findViewById(R.id.editText);
        try {
            Geocoder gcd = new Geocoder(MainActivity.this, Locale.getDefault());
            List<Address> addresses = gcd.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0)
                city.setText(addresses.get(0).getFeatureName() + " " + addresses.get(0).getSubLocality() + " " + addresses.get(0).getLocality());
        } catch (IOException e) {

        }

    }

    public void getWeatherReport(RequestParams rp) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(WEATHER_URL,rp,new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response){
                Log.d("Clima","Success" +response.toString());
                WeatherModelParser wmp = new WeatherModelParser();
                wmp.jsonParser(response);
                //wmp.getCity();
                //wmp.getTemp();
                updateUI(wmp);

            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e , JSONObject response){
                Log.e("Clima","Fail"+e.toString());
                Log.d("Clima","StatusCode"+ statusCode);
                Toast.makeText(MainActivity.this,"RequestFailed",Toast.LENGTH_SHORT).show();

            }
        });
    }

    public void updateUI(WeatherModelParser wmp)
    {
        String city=wmp.getCity();
        String temp=wmp.getTemp();

        TextView temper= (TextView) findViewById(R.id.temp);
        temper.setText(temp);
    }
}
