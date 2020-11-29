package com.example.w2w_texi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class DriversMapActivity  extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener{

    private GoogleMap mMap;
    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;

    private Button LogoutDriverBtn;
    private  Button SettingsDriverbtn;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Boolean currentLogoutDriverStatus = false;
    private DatabaseReference AssignedCustomerRef,AssignedCustomerPicUpRef;
    private String DriverId;
    private String CustomerId;
    Marker PickUpMarker;
    private ValueEventListener AssignedCustomerPicUpRefListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drivers_map);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        DriverId = mAuth.getCurrentUser().getUid();

        LogoutDriverBtn =  (Button) findViewById(R.id.driver_logout_btn);
        SettingsDriverbtn = (Button) findViewById(R.id.driver_settings_btn);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        SettingsDriverbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DriversMapActivity.this,SettingsActivity2.class);
                intent.putExtra("type","Drivers");
                startActivity(intent);
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this).addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();


        LogoutDriverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                currentLogoutDriverStatus = true;
                DisconnectDriver();

                mAuth.signOut();
                LogoutDriver();
            }
        });

        GetAssignedCustomerRequest();
    }
    private void GetAssignedCustomerRequest() {

        AssignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers")
                .child(DriverId).child("CustomerRideId");
        AssignedCustomerPicUpRefListener = AssignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){

                    CustomerId = snapshot.getValue().toString();

                    getAssignedCustomerPicUpLocation();
                }
                else {
                    CustomerId = "";
                    if(PickUpMarker != null){
                        PickUpMarker.remove();
                    }
                    if(AssignedCustomerPicUpRefListener != null){
                        AssignedCustomerPicUpRef.removeEventListener(AssignedCustomerPicUpRefListener);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getAssignedCustomerPicUpLocation() {
        AssignedCustomerPicUpRef = FirebaseDatabase.getInstance().getReference().child("Customers Request").child(CustomerId).child("l");
        AssignedCustomerPicUpRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    List<Object> CustomerLocationMap = (List<Object>)snapshot.getValue(); double LocationLat = 0;
                    double LocationLng = 0;
                    if(CustomerLocationMap.get(0) != null){
                        LocationLat = Double.parseDouble(CustomerLocationMap.get(0).toString());

                    }
                    if(CustomerLocationMap.get(1) != null){
                        LocationLng = Double.parseDouble(CustomerLocationMap.get(1).toString());

                    }
                    LatLng DriverLatLng = new LatLng(LocationLat,LocationLng);
                    PickUpMarker = mMap.addMarker(new MarkerOptions().position(DriverLatLng)
                            .title("Customer PickUp Location ").icon(BitmapDescriptorFactory.fromResource(R.drawable.user)) );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Add a marker in Sydney and move the camera
        mMap = googleMap;


        buildGoogleApiClient();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mMap.setMyLocationEnabled(true);

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(DriversMapActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }


    @Override
    public void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (com.google.android.gms.location.LocationListener) this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if(getApplicationContext() != null){

            mLastLocation = location;


            //Place current location marker
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            // MarkerOptions markerOptions = new MarkerOptions();
            //markerOptions.position(latLng);
            //markerOptions.title("Current Position");
            //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
            //mCurrLocationMarker = mMap.addMarker(markerOptions);

            //move map camera
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
            String UserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DatabaseReference DriverAvailiblityRef = FirebaseDatabase.getInstance().getReference().child("Drivers available");
            GeoFire geofireAvailibility = new GeoFire(DriverAvailiblityRef);

            DatabaseReference DriverWorkingRef = FirebaseDatabase.getInstance().getReference().child("Driver Working");
            GeoFire geofireWorking = new GeoFire(DriverWorkingRef);

            switch (CustomerId){
                case "":
                    geofireWorking.removeLocation(UserID);
                    geofireAvailibility.setLocation(UserID, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {

                        }


                    });
                    break;
                default:
                    geofireAvailibility.removeLocation(UserID);
                    geofireWorking.setLocation(UserID, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {

                        }


                    });
                    break;

            }

        }

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }


    protected  void onstop(){
        super.onStop();
        if(!currentLogoutDriverStatus){
            DisconnectDriver();

        }


    }

    private void DisconnectDriver() {
        String UserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference DriverAvailiblityRef = FirebaseDatabase.getInstance().getReference().child("Drivers available");
        GeoFire geofire = new GeoFire(DriverAvailiblityRef);
        geofire.removeLocation(UserID);
    }
    private void LogoutDriver() {
        Intent WelcomeIntent = new Intent(DriversMapActivity.this,WelcomeActivity.class);
        WelcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(WelcomeIntent);
        finish();

    }

}