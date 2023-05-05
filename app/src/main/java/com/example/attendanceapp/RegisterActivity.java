package com.example.attendanceapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    String local_IP = Constants.LOCAL_IP;
    private Button registerButton;

    private EditText firstNameEditText, lastNameEditText, usernameEditText, emailEditText, passwordEditText, confirmPasswordEditText;

    private String role = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerButton = findViewById(R.id.registerButton);
        RadioGroup roleRadioGroup = findViewById(R.id.roleRadioGroup);
        RadioButton parentRadioButton = findViewById(R.id.parentRadioButton);
        RadioButton studentRadioButton = findViewById(R.id.studentRadioButton);

        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        usernameEditText = findViewById(R.id.usernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);

        roleRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if (checkedId == R.id.parentRadioButton) {
                    // parent radio button is checked
                    role = "parent";
                } else if (checkedId == R.id.studentRadioButton) {
                    // student radio button is checked
                    role = "student";
                }
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get values from EditText fields
                String firstName = firstNameEditText.getText().toString().trim();
                String lastName = lastNameEditText.getText().toString().trim();
                String username = usernameEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String confirmPassword = confirmPasswordEditText.getText().toString().trim();

                // Validate input fields
                if (TextUtils.isEmpty(firstName)) {
                    firstNameEditText.setError("Please enter your first name.");
                    return;
                }

                if (TextUtils.isEmpty(lastName)) {
                    lastNameEditText.setError("Please enter your last name.");
                    return;
                }

                if (TextUtils.isEmpty(username)) {
                    usernameEditText.setError("Please enter a username.");
                    return;
                }

                if (TextUtils.isEmpty(email)) {
                    emailEditText.setError("Please enter your email.");
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailEditText.setError("Please enter a valid email.");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    passwordEditText.setError("Please enter a password.");
                    return;
                }

                if (TextUtils.isEmpty(confirmPassword)) {
                    confirmPasswordEditText.setError("Please confirm your password.");
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    confirmPasswordEditText.setError("Passwords do not match.");
                    return;
                }

                int selectedId = roleRadioGroup.getCheckedRadioButtonId();

                // Check if a radio button is selected
                if (selectedId == -1) {
                    // No radio button is selected, show an error message
                    Toast.makeText(getApplicationContext(), "Please select a role", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Make Volley POST request to register user
                String url = local_IP + "/attendance_app/api/register.php";
                StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Handle response from server
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");
                            String message = jsonObject.getString("message");

                            if (status.equals("success")) {
                                // Show success message and redirect to login page
                                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                finish();
                            } else {
                                // Show error message
                                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Show error message
                        Toast.makeText(RegisterActivity.this, "Registration failed. Please try again later.", Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        // Set POST parameters for Volley request
                        Map<String, String> params = new HashMap<>();
                        params.put("first_name", firstName);
                        params.put("last_name", lastName);
                        params.put("username", username);
                        params.put("email", email);
                        params.put("password", password);
                        params.put("role", role);
                        return params;
                    }
                };

                // Add Volley request to queue
                Volley.newRequestQueue(RegisterActivity.this).add(request);
            }
        });
    }
}