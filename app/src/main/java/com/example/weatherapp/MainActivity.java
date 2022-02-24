package com.example.weatherapp;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.SimpleTimeZone;

public class MainActivity extends AppCompatActivity
{
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    private RelativeLayout homeRL;
    private ProgressBar loadingPB;
    private TextView cityNameTV, temperatureTV, conditionTV;
    private TextInputEditText cityEdt;
    private ImageView backIV;
    private ImageView iconIV,logoIV;
    private LinearLayout logoLL;
    private ArrayList<WeatherRVModel> weatherRVModelArrayList;
    private WeatherAdapter weatherAdapter;
    private String cityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        homeRL = findViewById(R.id.idRLHome);
        loadingPB = findViewById(R.id.idPLoading);
        cityNameTV = findViewById(R.id.idTVCityName);
        temperatureTV = findViewById(R.id.idTVTemperature);
        conditionTV = findViewById(R.id.idTVCondition);
        cityEdt = findViewById(R.id.idEditCity);
        backIV = findViewById(R.id.idIVBack);
        iconIV = findViewById(R.id.idIVIcon);
        logoLL = findViewById(R.id.idLLLogo);
        logoIV = findViewById(R.id.idIVLogo);
        ImageView searchIV = findViewById(R.id.idIVSearch);
        RecyclerView weatherRV = findViewById(R.id.idRVWeather);
        weatherRVModelArrayList = new ArrayList<>();
        weatherAdapter = new WeatherAdapter(this, weatherRVModelArrayList);
        weatherRV.setAdapter(weatherAdapter);

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
        }

        Picasso.get().load("https://ieeevit.org/images/main_logo_ieee.png").into(logoIV);
        Picasso.get().load("https://wallpaper.dog/large/17016720.png").into(backIV);

        searchIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String city = Objects.requireNonNull(cityEdt.getText()).toString();
                if (city.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter city name", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        getWeatherInfo(city, weatherRV);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted..", Toast.LENGTH_SHORT).show();
                /*loadingPB.setVisibility(View.GONE);
                homeRL.setVisibility(View.VISIBLE);*/
            } else {
                Toast.makeText(this, "Please provide the permissions", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    private void getWeatherInfo(String cityName,RecyclerView weatherRV){
        String url =  "http://api.weatherapi.com/v1/forecast.json?key=ccaba12ff11649ceb1c202611222202&q="+cityName+"&days=1&aqi=yes&alerts-yes";
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
            @Override
            public void onResponse(JSONObject response) {
                weatherRVModelArrayList.clear();

                try {
                    String temperature = response.getJSONObject("current").getString("temp_c");
                    temperatureTV.setText(temperature+"°C");

                    int isDay = response.getJSONObject("current").getInt("is_day");
                    String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    Picasso.get().load("http:".concat(conditionIcon)).into(iconIV);

                    conditionTV.setText(condition);
                    if(isDay==1){
                        //morning
                        Picasso.get().load("https://cdn.dribbble.com/users/925716/screenshots/3333720/attachments/722376/after_noon.png").into(backIV);
                    }
                    else{
                        //night
                        Picasso.get().load("https://cdn.dribbble.com/users/925716/screenshots/3333720/attachments/722375/night.png").into(backIV);
                    }

                    JSONObject forecastObj = response.getJSONObject("forecast");
                    JSONObject forecast0 = forecastObj.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourArray = forecast0.getJSONArray("hour");

                    for(int i = 0; i < hourArray.length();i++){
                        JSONObject hourObj = hourArray.getJSONObject(i);
                        String time = hourObj.getString("time");
                        String temper = hourObj.getString("temp_c");
                        String img = hourObj.getJSONObject("condition").getString("icon");
                        String wind = hourObj.getString("wind_kph");
                        weatherRVModelArrayList.add(new WeatherRVModel(time,temper,img,wind));

                    }
                    weatherAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                cityNameTV.setText(cityName.toUpperCase(Locale.ROOT));
                logoLL.setVisibility(View.GONE);
                conditionTV.setVisibility(View.VISIBLE);
                temperatureTV.setVisibility(View.VISIBLE);
                iconIV.setVisibility(View.VISIBLE);
                backIV.setVisibility(View.VISIBLE);
                weatherRV.setVisibility(View.VISIBLE);

            }
        },
        new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse (VolleyError error){
                Toast.makeText(MainActivity.this, "Please enter valid city", Toast.LENGTH_SHORT).show();
                conditionTV.setVisibility(View.GONE);
                temperatureTV.setVisibility(View.GONE);
                iconIV.setVisibility(View.GONE);
                backIV.setVisibility(View.VISIBLE);
                weatherRV.setVisibility(View.GONE);

            }
        });

        requestQueue.add(jsonObjectRequest);
    }
}
