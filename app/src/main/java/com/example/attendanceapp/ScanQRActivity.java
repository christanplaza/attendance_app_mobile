package com.example.attendanceapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ScanQRActivity extends BaseActivity {
    private SurfaceView mCameraPreview;
    private TextView mQRCodeTextView;
    private CameraSource mCameraSource;
    private BarcodeDetector mBarcodeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qractivity);

        mCameraPreview = findViewById(R.id.camera_preview);
        mQRCodeTextView = findViewById(R.id.qr_result_text_view);

        mBarcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        mCameraSource = new CameraSource.Builder(this, mBarcodeDetector)
                .setAutoFocusEnabled(true)
                .build();

        mCameraPreview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                if (isCameraPermissionGranted()) {
                    try {
                        if (ActivityCompat.checkSelfPermission(ScanQRActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            requestCameraPermission();
                            return;
                        }
                        mCameraSource.start(mCameraPreview.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    requestCameraPermission();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                mCameraSource.stop();
            }
        });

        mBarcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                SparseArray<Barcode> barcodes = detections.getDetectedItems();

                if (barcodes.size() > 0) {
                    String qrcode = barcodes.valueAt(0).displayValue;
                    mQRCodeTextView.post(() -> mQRCodeTextView.setText(qrcode));
                    if (mCameraSource != null) {
                        mCameraSource.stop();
                    }
                    Log.d("Response: ", qrcode);
                }
            }
        });
    }

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;

    private boolean isCameraPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission granted
                // Start camera preview
                try {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    mCameraSource.start(mCameraPreview.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // Camera permission denied
                // Show an explanation or request the permission again
            }
        }
    }

//                    String url = "http://192.168.1.6/attendance_app/api/verify_attendance.php";
//                    StringRequest request = new StringRequest(Request.Method.POST, url,
//                            new Response.Listener<String>() {
//                                @Override
//                                public void onResponse(String response) {
//                                    // Handle the response from the API
//                                    // ...
//                                    Log.d("Response: ", response);
//                                }
//                            },
//                            new Response.ErrorListener() {
//                                @Override
//                                public void onErrorResponse(VolleyError error) {
//                                    // Handle the error
//                                    // ...
//                                }
//                            }) {
//                        @Override
//                        protected Map<String, String> getParams() throws AuthFailureError {
//                            Map<String, String> params = new HashMap<>();
//                            params.put("unique_string", qrcode);
//                            return params;
//                        }
//                    };
//                    Volley.newRequestQueue(ScanQRActivity.this).add(request);


//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        Log.d("RequestCode", requestCode+"");
//
//        // Handle the result of the QR code scan
//        if (requestCode == REQUEST_QR_SCAN && resultCode == RESULT_OK && data != null) {
//            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
//            if (result != null && result.getContents() != null) {
//                String verify = result.getContents();
//                Log.d("RequestCode", verify);
//
//                // Make the Volley POST request with the student ID and verify string
//
//        }
//    }
}