package com.example.kursova;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextWatcher;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity {

    Button button1;
    CustomView autoCompleteTextView1;
    Button button2;
    ImageButton buttonLocation;
    CustomView autoCompleteTextView2;
    TextWatcher watcher;
    private LocationManager locationManager;
    private LocationListener locationListener;
    public LatLng usc;
    Geocoder geocoder;

    private static final int PERMISSION_REQUEST_CODE = 123;


    private DatabaseHelper mDBHelper;
    private SQLiteDatabase mDb;
    SupportMapFragment mapFragment;
    GoogleMap map;
    boolean fromMarker, toMarker, drawRoute = false;
    Polyline lineDirect;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        fromMarker = false;
        toMarker = false;

        mDBHelper = new DatabaseHelper(this);
        geocoder = new Geocoder(this, Locale.getDefault());

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(googleMap -> {
            map = googleMap;
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(50.450521, 30.525644), 9.85f);
            map.animateCamera(cameraUpdate);
            map.setOnMapClickListener(latLng -> {
                drawRoute = true;
                addRoute();
            });
        });

        try {
            mDBHelper.updateDataBase();
        } catch (IOException mIOException) {
            throw new Error("UnableToUpdateDatabase");
        }

        try {
            mDb = mDBHelper.getWritableDatabase();
        } catch (SQLException mSQLException) {
            throw mSQLException;
        }

        button1 = findViewById(R.id.button1);
        autoCompleteTextView1 = findViewById(R.id.search1);
        button2 = findViewById(R.id.button2);
        autoCompleteTextView2 = findViewById(R.id.search2);
        buttonLocation = findViewById(R.id.button3);

        //Пропишем обработчик клика кнопки
        button1.setOnClickListener(v -> {
            onDetailsClick(autoCompleteTextView1);
        });

        button2.setOnClickListener(v -> onDetailsClick(autoCompleteTextView2));

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                usc = new LatLng(location.getLatitude(), location.getLongitude());

                try {
                    List<Address> addresses = geocoder.getFromLocation(usc.latitude, usc.longitude, 1);
                    //sendMessage(addresses.get(0).getAddressLine(0));
                    if (!(autoCompleteTextView1.getText().toString().equals("Моє місцезнаходження")))
                        autoCompleteTextView1.setText("Моє місцезнаходження");
                    if (drawRoute) {
                        addRoute();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };

      buttonLocation.setOnClickListener(v -> {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);

            } else {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean allowed = true;

        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:

                for (int res : grantResults) {
                    // if user granted all permissions.
                    allowed = allowed && (res == PackageManager.PERMISSION_GRANTED);
                }

                break;
            default:
                // if user not granted permissions.
                allowed = false;
                break;
        }

        if (allowed) {
            //user granted all permissions we can perform our task.
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
        }else{
            sendMessage("Доступ відхилено");
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event){
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                v.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
            autoCompleteTextView2.clearFocus();
            autoCompleteTextView1.clearFocus();
        }

        return super.dispatchTouchEvent(event);
    }

    public Marker addMarker(MarkerOptions options) {
        if (null != map) {

            Marker m = map.addMarker(options);
            return m;
        }
        return null;
    }

    public void addRoute(){
        if(lineDirect != null) {
            lineDirect.remove();
        }
        if(autoCompleteTextView1.marker != null && autoCompleteTextView2.marker != null){
            //Получаем контекст для запросов, mapsApiKey хранит в себе String с ключом для карт
            GeoApiContext geoApiContext = new GeoApiContext.Builder()
                    .apiKey("AIzaSyBln5_DNu82LeAndBI2hgfDEWrqiZ69iaM")
                    .build();

//Здесь будет наш итоговый путь состоящий из набора точек
            DirectionsResult result = null;
            try {
                com.google.maps.model.LatLng origin = new com.google.maps.model.LatLng(autoCompleteTextView1.marker.getPosition().latitude,
                        autoCompleteTextView1.marker.getPosition().longitude);

                com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(autoCompleteTextView2.marker.getPosition().latitude,
                        autoCompleteTextView2.marker.getPosition().longitude);
                result = DirectionsApi.newRequest(geoApiContext)
                        //.origin(new com.google.maps.model.LatLng())
                        .origin(origin)//Место старта
                        .destination(destination)
                        .await();//Пункт назначения

            } catch (ApiException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

//Преобразование итогового пути в набор точек
            List<com.google.maps.model.LatLng> path = result.routes[0].overviewPolyline.decodePath();

//Линия которую будем рисовать
            PolylineOptions line = new PolylineOptions();

            LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();

//Проходимся по всем точкам, добавляем их в Polyline и в LanLngBounds.Builder
            for (int i = 0; i < path.size(); i++) {
                line.add(new LatLng(path.get(i).lat, path.get(i).lng));
                latLngBuilder.include(new LatLng(path.get(i).lat, path.get(i).lng));
            }

//Делаем линию более менее симпатичное
            line.width(16f).color(R.color.colorPrimary);

//Добавляем линию на карту
            lineDirect = map.addPolyline(line);

//Выставляем камеру на нужную нам позицию
            Display display = mapFragment.getActivity().getWindowManager().getDefaultDisplay();
            int stageWidth = display.getWidth();
            int stageHeight = display.getHeight();
            LatLngBounds latLngBounds = latLngBuilder.build();
            CameraUpdate track = CameraUpdateFactory.newLatLngBounds(latLngBounds, (int)(0.7*stageWidth),
                    (int)(0.7*stageHeight), 25);//width это размер нашего экрана
            map.moveCamera(track);
        }
    }

    public void stopLocationUpdate(CustomView customView) {
        if(customView == autoCompleteTextView1)
            locationManager.removeUpdates(locationListener);
    }

    public void onDetailsClick(CustomView au) {
        boolean flag = false;
        String edText = au.getText().toString();
        for (String s : au.array) {
            if (s.equals(edText)) {
                flag = true;
                break;
            }
        }
        if(edText.equals("Моє місцезнаходження"))
            flag = true;
        if (flag) {
            Intent intent = new Intent(MapActivity.this, DetailsActivity.class);
            intent.putExtra("name", edText);
            startActivity(intent);
        } else {
            sendMessage("Відсутня інформація про дану локацію");
        }
    }

    public void selectedLocationChange (){
        drawRoute = false;
        if(lineDirect != null) {
            lineDirect.remove();
        }
    }

    public void sendMessage(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }

}