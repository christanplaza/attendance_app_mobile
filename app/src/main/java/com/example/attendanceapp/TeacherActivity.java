package com.example.attendanceapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.qrcode.QRCodeWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TeacherActivity extends BaseActivity {
    String local_IP = Constants.LOCAL_IP;
    private Button logoutButton, scanButton, closeButton;
    private boolean scanInProgress;
    private int teacherId;
    private String name;
    private TextView welcomeTextView, noteTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        logoutButton = findViewById(R.id.logoutButton);
        scanButton = findViewById(R.id.scanButton);
        welcomeTextView = findViewById(R.id.welcomeTextView);
        closeButton = findViewById(R.id.closeAttendanceButton);
        noteTextView = findViewById(R.id.noteTextView);

        SharedPreferences sharedPreferences = getSharedPreferences("attendance_app", MODE_PRIVATE);
        teacherId = sharedPreferences.getInt("id", 0);
        name = sharedPreferences.getString("name", "");

        scanButton.setVisibility(View.GONE);
        closeButton.setVisibility(View.GONE);

        welcomeTextView.setText("Welcome, " + name);

        String url = local_IP + "/attendance_app/api/get_teacher_class.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if (status.equals("success")) {
                        scanButton.setVisibility(View.VISIBLE);
                        closeButton.setVisibility(View.VISIBLE);
                    } else if (status.equals("error")) {
                        String message = jsonObject.getString("message");
                        noteTextView.setText("You have scheduled classes for this timeslot.");
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Invalid QR", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("teacher_id", teacherId + "");
                return params;
            }
        };

        Volley.newRequestQueue(TeacherActivity.this).add(stringRequest);


        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initiateScan();
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = local_IP + "/attendance_app/api/mark_absent.php";
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");
                            if (status.equals("success")) {
                                String message = jsonObject.getString("message");
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                                scanButton.setVisibility(View.GONE);
                                closeButton.setVisibility(View.GONE);
                                noteTextView.setText("You have closed the attendance for this class.");
                            } else if (status.equals("error")) {
                                String message = jsonObject.getString("message");
                                // Display error message
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Invalid QR", Toast.LENGTH_SHORT).show();
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("teacher_id", teacherId + "");
                        return params;
                    }
                };

                Volley.newRequestQueue(TeacherActivity.this).add(stringRequest);
            }
        });
    }



    protected void initiateScan() {
        scanInProgress = true;
        IntentIntegrator intentIntegrator = new IntentIntegrator(TeacherActivity.this);
        intentIntegrator.setOrientationLocked(false);
        intentIntegrator.setPrompt("Scan a QR Code");
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        intentIntegrator.initiateScan();
    }

    public void onBackPressed() {
        if (scanInProgress) {
            // If scanning is in progress, stop the scanning process
            scanInProgress = false;
            // Handle any necessary cleanup or actions here
            // For example, you can show a toast message or perform any other desired operations
            Toast.makeText(this, "Scanning cancelled", Toast.LENGTH_SHORT).show();
        } else {
            // If scanning is not in progress, proceed with the default back button behavior
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {
            String contents = intentResult.getContents();
            String url = local_IP + "/attendance_app/api/verify_attendance.php";
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        if (status.equals("success")) {
                            String message = jsonObject.getString("message");
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                        } else if (status.equals("error")) {
                            String message = jsonObject.getString("message");
                            Log.d("response", message);
                            // Display error message
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                            finish();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    // Reopen the QR scanner
                    initiateScan();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), "Invalid QR", Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("unique_string", contents);
                    return params;
                }
            };

            Volley.newRequestQueue(this).add(stringRequest);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void logout() {
        Toast.makeText(TeacherActivity.this, "User Logged Out", Toast.LENGTH_SHORT).show();

        sessionManager.setLogin(false, null);
        // Perform any logout-related tasks (e.g., clear local data)

        // Redirect to LoginActivity and clear the back stack
        Intent intent = new Intent(TeacherActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}