package org.accesspointoncloud.accesspointtech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {


    private GoogleMap mMap;

    LocationManager locationManager;
    LocationListener locationListener;




    public void centreMapOnLocation(Location location, String title){

        LatLng userLocation = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(userLocation).title(title));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,12));

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                centreMapOnLocation(lastKnownLocation,"Your Location");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


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
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        mMap = googleMap;
        final Intent intent = getIntent();
        Query myIncident = db.collection("incidentscollection").whereEqualTo("incidentTechEmailField", user.getEmail());

        myIncident.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            private static final String TAG ="tech incident docs" ;
            GeoPoint geoPoint;
            String yourMission;

            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, document.getId());
                        geoPoint = (GeoPoint) document.get("incidentGeoLocation");
                        yourMission = (String) document.get("incidentCustCompany");
                        Log.v("geoPoint for you mission: ",geoPoint.toString());
                        Log.v("Customer company :", yourMission);
                       // Intent intent = getIntent();
                        // Add a marker in incident location and move the camera

                                            }
                    LatLng incident1 = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(incident1).title("your mission: "+yourMission));


                }
            }
        });


       // if (intent.getIntExtra("Place Number",0) == 0 ){
   //     locationManager = (LocationManager)org.accesspointoncloud.accesspointtech.MapActivity.this.getSystemService(Context.LOCATION_SERVICE);


        // Zoom into users location


       Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
         assert lastKnownLocation != null;
           if (lastKnownLocation != null){centreMapOnLocation(lastKnownLocation,"Your Location");}

    //   if (ContextCompat.checkSelfPermission(org.accesspointoncloud.accesspointtech.MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
       //     locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
        //    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            // assert lastKnownLocation != null;
        //    if (lastKnownLocation != null){centreMapOnLocation(lastKnownLocation,"Your Location");}

       // } else {
//
  //          ActivityCompat.requestPermissions(org.accesspointoncloud.accesspointtech.MapActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
   //     }
     //   }


}
}
