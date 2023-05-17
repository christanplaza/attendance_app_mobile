package com.example.attendanceapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ParentActivity extends BaseActivity {
    String local_IP = Constants.LOCAL_IP;
    private Button logoutButton, submitExcuseLetterButton, viewAttendanceButton;
    private TextView welcomeTextView;
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent);

        logoutButton = findViewById(R.id.logoutButton);
        submitExcuseLetterButton = findViewById(R.id.submitExcuseLetter);
        webView = findViewById(R.id.webView);
        viewAttendanceButton = findViewById(R.id.viewAttendanceLogs);
        welcomeTextView = findViewById(R.id.welcomeTextView);

        SharedPreferences sharedPreferences = getSharedPreferences("attendance_app", MODE_PRIVATE);
        int parentId = sharedPreferences.getInt("id", 0);
        String name = sharedPreferences.getString("name", "");


        welcomeTextView.setText("Welcome " + name);

        webView.getSettings().setJavaScriptEnabled(true); // Enable JavaScript (if needed)
        webView.loadUrl(local_IP + "/attendance_app/parent/attendance_logs.php?id="+parentId); // Load the URL you want to display

        String apiUrl = local_IP + "/attendance_app/api/parent_login_verification.php";
        StringRequest request = new StringRequest(
                Request.Method.POST,
                apiUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");
                            if (status.equals("success")) {
                                String student_id = jsonObject.getString("student_id");
                            } else if (status.equals("null")) {
                                Intent intent = new Intent(ParentActivity.this, PairStudentActivity.class);
                                startActivity(intent);
                                Toast.makeText(ParentActivity.this, "Your account is not linked", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ParentActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ParentActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("parent_id", parentId + "");
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        submitExcuseLetterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ParentActivity.this, ExcuseLetterForm.class);
                startActivity(intent);
            }
        });

        viewAttendanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webView.setVisibility(View.VISIBLE); // Show the WebView
            }
        });
    }

    public void onBackPressed() {
        if (webView.getVisibility() == View.VISIBLE) {
            webView.setVisibility(View.GONE); // Hide the WebView
        } else {
            super.onBackPressed(); // Continue with the default back button behavior
        }
    }

    private void logout() {
        Toast.makeText(ParentActivity.this, "User Logged Out", Toast.LENGTH_SHORT).show();

        sessionManager.setLogin(false, null);
        // Perform any logout-related tasks (e.g., clear local data)

        // Redirect to LoginActivity and clear the back stack
        Intent intent = new Intent(ParentActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}