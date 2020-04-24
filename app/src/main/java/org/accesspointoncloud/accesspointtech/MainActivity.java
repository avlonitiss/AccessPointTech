package org.accesspointoncloud.accesspointtech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView incidentCustCompanyView;
    private TextView incidentCustNameView;
    private TextView incidentDescriptionField;
    private TextView incidentCustAddress;
    private Button mButton;
    private Button qButton;
    private Button tButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButton = findViewById(R.id.mapButton);
        qButton = findViewById(R.id.qrButton);
        tButton = findViewById(R.id.timeButton);
        refineDbResults();


        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMapActivity();
            }
        });

        qButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openQRCodeActivity();
            }
        });
        //  findViewById(R.id.qrButton).setOnClickListener((View.OnClickListener) this);
    }

    /* Read from Cloud Firesotre Database  */
    public void refineDbResults() {
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        Query myIncident = db.collection("incidentscollection").whereEqualTo("incidentTechEmailField", user.getEmail());
        myIncident.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            private static final String TAG ="tech incident docs" ;

            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, document.getId());
                        String custCompany = document.get("incidentCustCompany").toString();
                        incidentCustCompanyView = findViewById(R.id.incidentCustCompanyText);
                        incidentCustCompanyView.setText(custCompany);

                        String custName = document.get("incidentCustNameField").toString();
                        incidentCustNameView = findViewById(R.id.incidentCustNameText);
                        incidentCustNameView.setText(custName);

                        String incidentDes = document.get("incidentDescriptionField").toString();
                        incidentDescriptionField = findViewById(R.id.incidentDescriptionText);
                        incidentDescriptionField.setText(incidentDes);

                        String incidentAdd = document.get("incidentCustAdField").toString();
                        incidentCustAddress = findViewById(R.id.incidentCustAdText);
                        incidentCustAddress.setText(incidentAdd);


                    }
                }
            }
        });

    }

    public void openMapActivity(){
        Intent intent = new Intent(this, AccessMapsActivity.class);
        startActivity(intent);
    }

    public void openQRCodeActivity(){
        Intent intent = new Intent(this, QRCodeActivity.class);
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();


    }
}
