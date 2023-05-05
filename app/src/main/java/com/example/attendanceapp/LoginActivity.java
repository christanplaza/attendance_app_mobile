package com.example.attendanceapp;
import com.example.attendanceapp.Constants;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class LoginActivity extends AppCompatActivity {
    String local_IP = Constants.LOCAL_IP;
    private EditText usernameEditText;
    private TextView signupTextView;
    private EditText passwordEditText;
    private Button loginButton;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signupTextView = findViewById(R.id.signupTextView);

        sessionManager = new SessionManager(getApplicationContext());

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authenticateUser();
            }
        });

        signupTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void authenticateUser() {
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        SharedPreferences sharedPref = getSharedPreferences("attendance_app", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        String apiUrl = local_IP + "/attendance_app/api/login.php";

        StringRequest request = new StringRequest(
                Request.Method.POST,
                apiUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");
                            if (success) {
                                Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                                // Get user role from the response
                                String userRole = jsonObject.getString("userRole");
                                // Get user id from the response
                                int id = jsonObject.getInt("id");

                                editor.putInt("id", id);
                                editor.commit();

                                // Update login state and user role
                                sessionManager.setLogin(true, userRole);

                                // Start the appropriate activity based on  role
                                Intent intent;
                                if (userRole.equals("student")) {
                                    intent = new Intent(LoginActivity.this, MainActivity.class);
                                } else if (userRole.equals("parent")) {
                                    intent = new Intent(LoginActivity.this, ParentActivity.class);
                                } else if (userRole.equals("faculty")) {
                                    intent = new Intent(LoginActivity.this, TeacherActivity.class);
                                } else {
                                    throw new JSONException("Invalid user role");
                                }

                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(LoginActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("password", password);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}