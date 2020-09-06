package com.example.kursova;

import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.app.ActivityCompat;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class DetailsActivity extends AppCompatActivity {

    private DatabaseHelper mDBHelper;
    private SQLiteDatabase mDb;
    TextView textViewName;
    TextView textViewAddress;
    TextView textViewDescription;
    ImageView imageViewImage;
    List<Address> addresses;
    private LocationManager locationManager;
    private LocationListener locationListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        textViewName = findViewById(R.id.name);
        textViewAddress = findViewById(R.id.address);
        textViewDescription = findViewById(R.id.description);
        imageViewImage = findViewById(R.id.image);

        mDBHelper = new DatabaseHelper(this);
        mDb = mDBHelper.getWritableDatabase();
        String name = "";
        Cursor cursor;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            name = extras.getString("name");
        }
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        if (name.equals("Моє місцезнаходження")) {
            textViewName.setText("Зачекайте, визначається ваше положення на карті ...");
            textViewDescription.setVisibility(View.INVISIBLE);
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            locationListener = new LocationListener() {

                @Override
                public void onLocationChanged(Location location) {

                    try {
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        textViewAddress.setText(addresses.get(0).getAddressLine(0));
                        textViewName.setText("Ви знаходитесь за адресою");
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

                }
            };

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

        }
        else {
            // получаем элемент по id из бд
            cursor = mDb.rawQuery("select * from places where name=?", new String[]{name});
            cursor.moveToFirst();
            try {
                addresses = geocoder.getFromLocation(Double.valueOf(cursor.getString(2)), Double.valueOf(cursor.getString(3)), 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            textViewName.setText(cursor.getString(1));
            textViewAddress.setText(addresses.get(0).getAddressLine(0));
            textViewDescription.setText(cursor.getString(4));
            byte[] bbb = cursor.getBlob(5);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inDither = false;
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inTempStorage = new byte[1024 * 32];

            Bitmap bm = BitmapFactory.decodeByteArray(bbb, 0, bbb.length, options);
            imageViewImage.setImageBitmap(bm);
            cursor.close();
        }

    }

}