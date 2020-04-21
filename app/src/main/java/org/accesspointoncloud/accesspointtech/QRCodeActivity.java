package org.accesspointoncloud.accesspointtech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
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

public class QRCodeActivity extends AppCompatActivity {

    SurfaceView surfaceView;
    CameraSource cameraSource;
    private EditText editText;
    private TextView confCodeText;
    private TextView dbCodeText;
    private ImageView imageView;
    private Button gButton;
    private Button cButton;
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
        confCodeText = (TextView) findViewById(R.id.qrConfCodeText);
        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE).build();
        cameraSource = new CameraSource.Builder(this,barcodeDetector)
                .setRequestedPreviewSize(640,480).build();
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
                try { cameraSource.start(holder);} catch (IOException e) {
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

    public void QRCodeButton(View view) {

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = qrCodeWriter.encode(editText.getText().toString(), BarcodeFormat.QR_CODE, 120, 120);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = Bitmap.createBitmap(120,120, Bitmap.Config.RGB_565);
        for (int x=0;x<120;x++) {
            for (int y = 0; y < 120; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        imageView.setImageBitmap(bitmap);
        addConfToDb(editText.getText().toString());
    }


    @ServerTimestamp
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
                        document.getReference().update("incidentCustStartTimeField", Timestamp.now());




                    }
                }
            }
        });

    }

    public void checkDbButton(View view) {

        checkDb(editText.getText().toString());
    }

    public void checkDb(final String confCode) {
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
                        if(document.get("incidentConfCodeField").toString().isEmpty()){
                           Toast.makeText(getApplicationContext(),"no confirmation entry in DB",Toast.LENGTH_LONG).show();
                           return;
                        } else {
                            String fetchCode = document.get("incidentConfCodeField").toString();
                            if (fetchCode==confCode){
                                Toast.makeText(getApplicationContext(),"yeah we have a match",Toast.LENGTH_LONG).show();
                                document.getReference().update("incidentCustStartTimeField", Timestamp.now());
                                return;
                            } else {
                                Toast.makeText(getApplicationContext(),"wrong code baby",Toast.LENGTH_LONG).show();
                                return;
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
