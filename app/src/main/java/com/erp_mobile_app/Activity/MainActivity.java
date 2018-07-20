package com.erp_mobile_app.Activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.erp_mobile_app.R;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final long INTERVAL = 5000;
    private static final long FASTEST_INTERVAL = 1000;
    private static final int PERMISSION_REQUEST_CODE = 28996 ;
    private static final float DISPLACEMENT = 10;
    private static Location CURRENT_LOCATION = null;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Context context;
    private Button button ;
    private String TAG = "MainActivity";
    private DatabaseReference ref;
    private GeoFire geoFire;
    //Todo: Replace that  EngineerID
    private String userID=Long.toHexString(Double.doubleToLongBits(Math.random()));
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_main);
        ref = FirebaseDatabase.getInstance().getReference("Location");
        geoFire = new GeoFire(ref);
        setUpLocation();
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context,MapsActivity.class));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED)
                {
                    if (isGooglePlayServicesAvailable())
                    {
                        buildGoogleApiClient();
                        createLocationRequest();
                        saveLocation();
                    }
                }
                break;
        }
    }

    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String []
                    {android.Manifest.permission.ACCESS_FINE_LOCATION ,
                            Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_REQUEST_CODE);
        }
        else {
            if (isGooglePlayServicesAvailable())
            {
                buildGoogleApiClient();
                createLocationRequest();
                saveLocation();
            }
        }
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void saveLocation() {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        CURRENT_LOCATION = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(CURRENT_LOCATION != null)
        {
            Log.e(TAG, "saveLocation: Your Loction Changed "+CURRENT_LOCATION.getLatitude() + "    "+ CURRENT_LOCATION.getLongitude());
            geoFire.setLocation(userID,new GeoLocation(CURRENT_LOCATION.getLatitude(),CURRENT_LOCATION.getLongitude()));
        }
        else
        {
            Log.e(TAG, "saveLocation: Can't et yoour location ");
        }
    }

    @Override
    public void onLocationChanged(Location location)
    {
        Log.e("location!!!!!", "Firing onLocationChanged..... "+ location.getLatitude()+location.getLongitude());
        CURRENT_LOCATION = location ;
        saveLocation();
    }


    private boolean isGooglePlayServicesAvailable()
    {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    @Override
    public void onConnected(Bundle bundle)
    {
        saveLocation();
        startLocationUpdates();
    }

    protected void startLocationUpdates()
    {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i)
    {
    mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed: " + connectionResult.toString());
    }

}

