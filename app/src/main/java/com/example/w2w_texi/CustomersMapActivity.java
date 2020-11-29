package com.example.w2w_texi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
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

import java.util.HashMap;
import java.util.List;

public class CustomersMapActivity  extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener{
    private GoogleMap mMap;

    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    GoogleApiClient mGoogleApiClient;

    private Button CustomerLogoutbtn;
    private  Button CallCabCarbtn;
    private Button SettingBtn;
    private  String CustomerID;
    private LatLng CustomerPicupLocation;
    private int Radius = 1;
    private boolean DriverFound = false,RequestType = false ;
    private String DriverFoundId;



    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    Marker DriverMarker,PickUpMarker;
    GeoQuery geoQuery;
    private DatabaseReference CustomerDatabaseRef;
    private  DatabaseReference DriverAvailableRef;
    private  DatabaseReference DriversRef;
    private DatabaseReference DriverLocationRef;
    private ValueEventListener DriverLocationRefListner;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customers_map);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        Log.d("currentuser", String.valueOf(currentUser));

        CustomerDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Customers request");
        DriverAvailableRef = FirebaseDatabase.getInstance().getReference().child(" Drivers Available");
        DriverLocationRef =  FirebaseDatabase.getInstance().getReference().child("Drivers Working");

        CustomerLogoutbtn = (Button) findViewById(R.id.Customer_Logout_btn);
        SettingBtn = (Button) findViewById(R.id.Customer_Settings_btn);
        CallCabCarbtn = (Button) findViewById(R.id.Customer_CallCab_btn);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        SettingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CustomersMapActivity.this,SettingsActivity2.class);
                intent.putExtra("type","Customers");
                startActivity(intent);
            }
        });
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this).addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        CustomerLogoutbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mAuth.signOut();
                LogoutCustomer();
            }
        });

        CallCabCarbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(RequestType){
                    RequestType = false;
                    geoQuery.removeAllListeners();
                    DriverLocationRef.removeEventListener(DriverLocationRefListner);

                    if(DriverFound != false){
                        DriversRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers")
                                .child(DriverFoundId).child("CustomerRideId");
                        DriversRef.removeValue();

                        DriverFoundId = null;
                    }
                    DriverFound = false;
                    Radius = 1;

                    CustomerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    GeoFire geoFire = new GeoFire(CustomerDatabaseRef);
                    geoFire.removeLocation(CustomerID);
                    if(PickUpMarker != null){
                        PickUpMarker.remove();
                    }
                    if(DriverMarker != null){
                        DriverMarker.remove();
                    }
                    CallCabCarbtn.setText("Call a Cab");


                }
                else {
                    RequestType = true;
                    CustomerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    GeoFire geoFire = new GeoFire(CustomerDatabaseRef);
                    geoFire.setLocation(CustomerID, new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {

                        }


                    });
                    CustomerPicupLocation = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(CustomerPicupLocation).title("My Location")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.user)));

                    PickUpMarker = mMap.addMarker(new MarkerOptions().position(CustomerPicupLocation));
                    CallCabCarbtn.setText("Geeting your driver");
                    GetClosestDriverCab();

                }


            }
        });

    }
    private void GetClosestDriverCab() {
        GeoFire geofire = new GeoFire(DriverAvailableRef);
        geoQuery =  geofire.queryAtLocation(new GeoLocation(CustomerPicupLocation.latitude,CustomerPicupLocation.longitude),Radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!DriverFound && RequestType){
                    DriverFound = true;
                    DriverFoundId = key;

                    DriversRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(DriverFoundId);
                    HashMap DriverMap = new HashMap();
                    DriverMap.put("CustomerRideId",CustomerID);
                    DriversRef.updateChildren(DriverMap);

                    GettingDriverLocation();
                    CallCabCarbtn.setText("Looking for Driver Location");
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(!DriverFound){
                    Radius = Radius + 1 ;
                    GetClosestDriverCab();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {


            }
        });
    }

    private void GettingDriverLocation() {

        DriverLocationRefListner = DriverLocationRef.child(DriverFoundId).child("l").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && RequestType){
                    List<Object> DriverLocationMap = (List<Object>) snapshot.getValue();
                    double LocationLat = 0;
                    double LocationLng = 0;
                    CallCabCarbtn.setText("Driver Found");
                    if(DriverLocationMap.get(0) != null){
                        LocationLat = Double.parseDouble(DriverLocationMap.get(0).toString());

                    }
                    if(DriverLocationMap.get(1) != null){
                        LocationLng = Double.parseDouble(DriverLocationMap.get(1).toString());

                    }
                    LatLng DriverLatLng = new LatLng(LocationLat,LocationLng);
                    if(DriverMarker != null){
                        DriverMarker.remove();
                    }

                    Location location1 = new Location("");
                    location1.setLatitude(CustomerPicupLocation.latitude);
                    location1.setLongitude(CustomerPicupLocation.longitude);

                    Location location2 = new Location("");
                    location2.setLatitude(DriverLatLng.latitude);
                    location2.setLongitude(DriverLatLng.longitude);

                    float Distance = location1.distanceTo(location2);

                    if(Distance < 90){
                        CallCabCarbtn.setText("Driver's Reached");
                    }
                    else {
                        CallCabCarbtn.setText("Driver Found :" + String.valueOf(Distance));
                    }

                    DriverMarker = mMap.addMarker(new MarkerOptions().position(DriverLatLng).title("Your Driver is here").icon(BitmapDescriptorFactory.fromResource(R.drawable.car)) );

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



        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

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

    @Override
    public void onLocationChanged(Location location) {
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
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));


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
    protected void onStop() {
        super.onStop();


    }
    private void LogoutCustomer() {

        Intent WelcomeIntent = new Intent(CustomersMapActivity.this,WelcomeActivity.class);
        WelcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(WelcomeIntent);
        finish();
    }

}