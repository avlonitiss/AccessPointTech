package org.accesspointoncloud.accesspointtech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


public class MainActivity extends AppCompatActivity {
   FirebaseFirestore db = FirebaseFirestore.getInstance();
 //   private FirebaseAuth mAuth;
    FirebaseUser user;
    private TextView incidentCustCompanyView;
    private TextView incidentCustNameView;
    private TextView incidentDescriptionField;
    private TextView incidentCustAddress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    //    mAuth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Query myIncident = db.collection("incidentscollection").whereEqualTo("incidentTechEmailField", user.getEmail());


        incidentCustCompanyView = findViewById(R.id.incidentCustCompanyText);
        incidentCustNameView = findViewById(R.id.incidentCustNameText);
        incidentDescriptionField = findViewById(R.id.incidentDescriptionText);
        incidentCustAddress = findViewById(R.id.incidentCustAdText);

       // findViewById(R.id.mapButton).setOnClickListener((View.OnClickListener) this);
        //findViewById(R.id.qrButton).setOnClickListener((View.OnClickListener) this);
    }


    @Override
    public void onStart() {
        super.onStart();


    }
}
