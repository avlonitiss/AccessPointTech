package org.accesspointoncloud.accesspointtech;

import androidx.annotation.NonNull;

import androidx.fragment.app.FragmentActivity;


import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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

import java.util.Objects;

public class AccessMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);
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
        mMap = googleMap;

        createIncidentLocationFromFirestore();

    }

    public void createIncidentLocationFromFirestore(){

        GeoPoint geoPoint = new GeoPoint(-34,151);
        double initPointLat = geoPoint.getLatitude();
        double initPointLon = geoPoint.getLongitude();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        Query myIncident = db.collection("incidentscollection").whereEqualTo("incidentTechEmailField", user.getEmail());
        final Marker incidentMarker = mMap.addMarker(new MarkerOptions().title("random title").position(new LatLng(initPointLat,initPointLon)));
        myIncident.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

            private static final String TAG ="tech incident docs" ;
        GeoPoint geoPoint;
        String yourMission;

        @Override
        public void onComplete(@NonNull Task< QuerySnapshot > task) {
            if(task.isSuccessful()){
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Log.d(TAG, document.getId());
                    geoPoint = (GeoPoint) document.get("incidentGeoLocation");
                    yourMission = (String) document.get("incidentCustCompany");
                    Log.v("geoPoint for you mission: ",geoPoint.toString());
                    Log.v("Customer company :", yourMission);

                }
                LatLng incident1 = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                incidentMarker.setPosition(incident1);
                incidentMarker.setTitle(yourMission);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(incident1,12));

            }

        }
    });

    }

}
