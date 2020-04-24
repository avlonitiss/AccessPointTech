package org.accesspointoncloud.accesspointtech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ServerTimestamp;
import com.google.type.Date;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;



import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class QRCodeActivity extends AppCompatActivity {

    SurfaceView surfaceView;
    CameraSource cameraSource;
    private EditText editText;

    private TextView confCodeText;
    private TextView dbCodeText;
    private ImageView imageView;


    private FirebaseAuth mAuth;
    BarcodeDetector barcodeDetector;
    public static final int CAMERA_FACING_BACK=1;
    private static final int MY_CAMERA_REQUEST_CODE = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);
        mAuth = FirebaseAuth.getInstance();
        editText = findViewById(R.id.confCodeEditText);

        dbCodeText = findViewById(R.id.dbCodeText);
        imageView = findViewById(R.id.imageQRView);
        surfaceView = (SurfaceView) findViewById(R.id.camerapreview);
        confCodeText =  findViewById(R.id.qrConfCodeText);
        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE).build();
        cameraSource = new CameraSource.Builder(this,barcodeDetector)
                .setRequestedPreviewSize(1024,768).setAutoFocusEnabled(true).build();
        fetchDbData();
        int cameraFacingBack = cameraSource.CAMERA_FACING_BACK;
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
            recreate();}

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                   return;

                }
                try { cameraSource.start(holder);

                } catch (IOException e) {
                    e.printStackTrace();    }        }
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {      }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }  });

            barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {     }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrCodes = detections.getDetectedItems();
                if (qrCodes.size() !=0){
                    confCodeText.post(new Runnable() {
                        @Override
                        public void run() {
                            Vibrator vibrator = (Vibrator)getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(1000);
                            confCodeText.setText(qrCodes.valueAt(0).displayValue);
                        }
                    });
                }
            }
        });

    }
    //QR-Code Generator
    public void QRCodeButton(View view) {

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = qrCodeWriter.encode(editText.getText().toString(), BarcodeFormat.QR_CODE, 150, 150);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = Bitmap.createBitmap(150,150, Bitmap.Config.RGB_565);
        for (int x = 0; x < 150; x++) {
            for (int y = 0; y < 150; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        imageView.setImageBitmap(bitmap);
        addConfToDb(editText.getText().toString());
    }


    @ServerTimestamp /* PUBLIC IP FROM REST API AND  Update User's incident mission in Cloud Firestore database with QR- Code, StartCust Timestamp  and public IP */
    Date time;
    public void addConfToDb(final String confCode) {
        mAuth = FirebaseAuth.getInstance();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        Query myIncident = db.collection("incidentscollection").whereEqualTo("incidentTechEmailField", user.getEmail());
        myIncident.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            private static final String TAG ="tech incident docs" ;

            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, document.getId());
                        document.getReference().update("incidentConfCodeField", confCode);
                        document.getReference().update("incidentStartCustTimeField", Timestamp.now());

                        OkHttpClient client = new OkHttpClient();

//add parameters
                        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://checkip.amazonaws.com/").newBuilder();
//possible params                       urlBuilder.addQueryParameter("query", "xxxx");
                      String url = urlBuilder.build().toString();

//build the request
                        Request request = new Request.Builder().url(url).build();
//execute
                        try { /* update Cloud firestore with public ip  */
                            Response response = client.newCall(request).execute();
                            String x = response.body().string();
                            Toast.makeText(QRCodeActivity.this, "Your IP address has been recorded  "+x, Toast.LENGTH_SHORT).show();
                            document.getReference().update("incidentPublicIPField", x);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.i(TAG, e.toString());
                        }



                    }
                }
            }
        });

    }

    public void updateDbButton(View view) {

        updateDb(confCodeText.getText().toString());
        Toast.makeText(this,"update db from camera with conf code: "+confCodeText.getText().toString(),Toast.LENGTH_LONG).show();
    }

    public void updateDb(final String confCode) {
        mAuth = FirebaseAuth.getInstance();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        Query myIncident = db.collection("incidentscollection").whereEqualTo("incidentTechEmailField", user.getEmail());
        Toast.makeText(this,"update db pressed, checking if's: "+confCode,Toast.LENGTH_LONG).show();

        myIncident.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            private static final String TAG ="tech incident docs" ;

             @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, document.getId());

                        //if no conf code exists in incident database
                        if(document.get("incidentConfCodeField").toString().isEmpty()){
                           Toast.makeText(getApplicationContext(),"no confirmation entry from Cloud",Toast.LENGTH_LONG).show();

                        } else {
                            // if camera detects same qr code with db update a new Start Customer Timestamp
                            String fetchCode = document.get("incidentConfCodeField").toString();
                            if (fetchCode==confCode){
                                Toast.makeText(getApplicationContext(),"yeah we have a match",Toast.LENGTH_LONG).show();
                                document.getReference().update("incidentStartCustTimeField", Timestamp.now());

                            } else {
                                if(confCode!="") {
                                    Toast.makeText(getApplicationContext(), "changing code baby updating now", Toast.LENGTH_LONG).show();
                                    document.getReference().update("incidentStartCustTimeField", Timestamp.now());
                                    document.getReference().update("incidentConfCodeField", confCode);
                                }else {
                                    Log.d( "Scan a QRcode first", "xxxxxxxxxxxxx xxxxxxxxxxxxxx xxxxxxxxxxxxxxxxx");
                                }



                            }
                        }

                    }
                }
            }
        });

    }



    public void fetchDbData() {
        mAuth = FirebaseAuth.getInstance();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        Query myIncident = db.collection("incidentscollection").whereEqualTo("incidentTechEmailField", user.getEmail());
        myIncident.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            private static final String TAG ="tech incident docs" ;


            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, document.getId());
                            String fetchCode = document.get("incidentConfCodeField").toString();
                            if (fetchCode==""){
                                dbCodeText.setText("GENERATE-QR");
                            } else {
                                dbCodeText.setText(fetchCode);
                            }

                    }
                }
            }
        });

    }

}
