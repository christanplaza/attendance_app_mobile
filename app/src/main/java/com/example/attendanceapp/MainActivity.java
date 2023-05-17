package com.example.attendanceapp;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends BaseActivity {

    private Button logoutButton, generateButton;
    private TextView welcomeText, studentCode, noteText, announcementText;
    String local_IP = Constants.LOCAL_IP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logoutButton = findViewById(R.id.logoutButton);
        generateButton = findViewById(R.id.generateButton);
        welcomeText = findViewById(R.id.welcomeTextView);
        studentCode = findViewById(R.id.student_code);
        noteText = findViewById(R.id.noteTextView);
        announcementText = findViewById(R.id.announcementTextView);

        SharedPreferences sharedPreferences = getSharedPreferences("attendance_app", MODE_PRIVATE);
        int studentId = sharedPreferences.getInt("id", 0);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, QRActivity.class);
                startActivity(intent);
            }
        });

        // Instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        String url = local_IP + "/attendance_app/api/student_info.php?id=" + studentId;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Response", response);
                        // Parse the JSON response
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");
                            if (status.equals("success")) {
                                String firstName = jsonObject.getString("first_name");
                                String lastName = jsonObject.getString("last_name");
                                String uniqueCode = jsonObject.getString("unique_code");
                                String msg = jsonObject.getString("msg");

                                announcementText.setText(msg);

                                // Do something with the retrieved data
                                welcomeText.setText("Welcome " + firstName + " " + lastName);
                                studentCode.setText("Your Code is: " + uniqueCode);
                            }

                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing JSON response", e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle error
                Log.e(TAG, "Error retrieving student info", error);
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);

        // Instantiate the RequestQueue
        RequestQueue queue1 = Volley.newRequestQueue(MainActivity.this);
        String url1 = local_IP + "/attendance_app/api/get_student_absences.php?student_id=" + studentId;

        // Request a string response from the provided URL.
        StringRequest stringRequest1 = new StringRequest(Request.Method.GET, url1,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Parse the JSON response
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Log.d("response", response);
                            String status = jsonObject.getString("status");
                            if (status.equals("success")) {
                                int remainingAbsences = jsonObject.getInt("remaining_absences");

                                if (remainingAbsences < 3) {
                                    noteText.setText("You have " + remainingAbsences + " absences left before you are marked DROPPED for this subject");
                                }
                            }

                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing JSON response", e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle error
                Log.e(TAG, "Error retrieving student info", error);
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest1);
    }

    private void logout() {
        Toast.makeText(MainActivity.this, "User Logged Out", Toast.LENGTH_SHORT).show();

        sessionManager.setLogin(false, null);
        // Perform any logout-related tasks (e.g., clear local data)

        // Redirect to LoginActivity and clear the back stack
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}