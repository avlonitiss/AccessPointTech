package org.accesspointoncloud.accesspointtech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

public class QRCodeActivity extends AppCompatActivity {

    private EditText editText;
    private ImageView imageView;
    private Button gButton;
    private Button rButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);
        mAuth = FirebaseAuth.getInstance();
        editText = findViewById(R.id.confCodeEditText);
        imageView = findViewById(R.id.imageQRView);

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


}
