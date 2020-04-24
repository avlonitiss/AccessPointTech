package org.accesspointoncloud.accesspointtech;


import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
//import com.google.firebase.quickstart.auth.BuildConfig;
//import com.google.firebase.quickstart.auth.R;

import java.io.IOException;
import java.util.Collections;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.google.android.gms.vision.L.e;

/**
 * Demonstrate authentication using the FirebaseUI-Android library. This activity demonstrates
 * using FirebaseUI for basic email/password sign in.
 *
 * For more information, visit https://github.com/firebase/firebaseui-android
 */
public class FirebaseUIActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "logErrorTag";
    private FirebaseAuth mAuth;
    private Button button;
    private TextView mStatusView;

    private TextView mDetailView;

    @Override
          protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        //policy for web service acceptance
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        setContentView(R.layout.activity_firebase_ui);
        button = findViewById(R.id.incident_button);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();


        mStatusView = findViewById(R.id.status);
        mDetailView = findViewById(R.id.detail);


        findViewById(R.id.signInButton).setOnClickListener(this);
        findViewById(R.id.signOutButton).setOnClickListener(this);



        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                      openMainActivity();
            }
        });
    }

    public void openMainActivity(){

/*  update cloud firestore with incidentGoTimeStamp */

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        Query myIncident = db.collection("incidentscollection").whereEqualTo("incidentTechEmailField", user.getEmail());
        myIncident.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {


            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        if(document.get("incidentGoTimeStampField")==null)

                        {
                            document.getReference().update("incidentGoTimeStampField", Timestamp.now());
                        }
                        Log.d(TAG, document.getId());

                    }
                }
            }
        });


Intent intent =new Intent(this, MainActivity.class);
startActivity(intent);


    };

    @Override
    protected void onStart() {
        super.onStart();

        updateUI(mAuth.getCurrentUser());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // Sign in succeeded
//get an instance of the client
                OkHttpClient client = new OkHttpClient();
//add parameters
                HttpUrl.Builder urlBuilder = HttpUrl.parse("https://checkip.amazonaws.com/").newBuilder();
                String url = urlBuilder.build().toString();
//build the request
                Request request = new Request.Builder().url(url).build();
//execute
                try {
                    Response response = client.newCall(request).execute();
                    String x = response.body().string();
                    Toast.makeText(this, "Your Public IP is "+x, Toast.LENGTH_SHORT).show();
                    Log.v("response-body",x);
            } catch (IOException e) {
                    e.printStackTrace();
                    Log.i(TAG, e.toString());
                }

                updateUI(mAuth.getCurrentUser());

          } else {
                // Sign in failed
                Toast.makeText(this, "Sign In Failed", Toast.LENGTH_SHORT).show();
                updateUI(null);
            }
        }
    }

    private void startSignIn() {
        // Build FirebaseUI sign in intent. For documentation on this operation and all
        // possible customization see: https://github.com/firebase/firebaseui-android
        Intent intent = AuthUI.getInstance().createSignInIntentBuilder()
                .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                .setAvailableProviders(Collections.singletonList(
                        new AuthUI.IdpConfig.EmailBuilder().build()))
                .setLogo(R.mipmap.ic_launcher)
                .build();

        startActivityForResult(intent, RC_SIGN_IN);
    }





    private void updateUI(FirebaseUser user) {
        if (user != null) {


            mStatusView.setText(getString(R.string.firebaseui_status_fmt, user.getEmail()));
            mDetailView.setText(getString(R.string.id_fmt, user.getUid()));

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            Query myIncident = db.collection("incidentscollection").whereEqualTo("incidentTechEmailField", user.getEmail());
          myIncident.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
              private static final String TAG ="tech incident docs" ;

              @Override
              public void onComplete(@NonNull Task<QuerySnapshot> task) {
                  if(task.isSuccessful()){
                      for (QueryDocumentSnapshot document : task.getResult()) {
                          Log.d(TAG, document.getId());
                          document.getData();
                          if (document.getData().isEmpty()) {
                              findViewById(R.id.incident_button).setVisibility(View.GONE);
                          } else {
                              findViewById(R.id.incident_button).setVisibility(View.VISIBLE);

                          }
                      }
                  }
              }
          });


                 mStatusView.setText(getString(R.string.firebaseui_status_fmt, user.getEmail()));
                 mDetailView.setText(getString(R.string.id_fmt, user.getUid()));

            findViewById(R.id.signInButton).setVisibility(View.GONE);
            findViewById(R.id.signOutButton).setVisibility(View.VISIBLE);

        } else {
            // Signed out
            mStatusView.setText(R.string.signed_out);
            mDetailView.setText(null);

            findViewById(R.id.signInButton).setVisibility(View.VISIBLE);
            findViewById(R.id.signOutButton).setVisibility(View.GONE);
            findViewById(R.id.incident_button).setVisibility(View.GONE);

        }
    }

    private void signOut() {
        AuthUI.getInstance().signOut(this);
        updateUI(null);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.signInButton:
                startSignIn();
                break;
            case R.id.signOutButton:
                signOut();
                break;
        }
    }
}
