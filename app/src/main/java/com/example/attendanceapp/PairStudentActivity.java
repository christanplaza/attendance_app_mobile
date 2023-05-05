package com.example.attendanceapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PairStudentActivity extends BaseActivity {
    private EditText mStudentCodeEditText;
    private Button mLinkAccountButton;

    private int parentId;

    String local_IP = Constants.LOCAL_IP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pair_student);

        mStudentCodeEditText = findViewById(R.id.studentCodeEditText);
        mLinkAccountButton = findViewById(R.id.linkAccountButton);
        SharedPreferences sharedPreferences = getSharedPreferences("attendance_app", MODE_PRIVATE);
        parentId = sharedPreferences.getInt("id", 0);

        mLinkAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linkAccountWithStudent();
            }
        });
    }

    private void linkAccountWithStudent() {
        final String studentCode = mStudentCodeEditText.getText().toString().trim();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, local_IP + "/attendance_app/api/verify_parent.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");
                            if (status.equals("success")) {
                                JSONObject student = jsonObject.getJSONObject("student");
                                String firstName = student.getString("first_name");
                                String lastName = student.getString("last_name");
                                String studentId = student.getString("id");

                                AlertDialog.Builder builder = new AlertDialog.Builder(PairStudentActivity.this);
                                builder.setMessage("Link your account to " + firstName + " " + lastName + "?")
                                        .setCancelable(false)
                                        .setPositiveButton("Link", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                LinkStudent(studentId);
                                            }
                                        })
                                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                // None
                                            }
                                        });
                                AlertDialog alert = builder.create();
                                alert.show();
                            } else {
                                String message = jsonObject.getString("message");
                                Toast.makeText(PairStudentActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error response
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("student_code", studentCode);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void LinkStudent(String studentId) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, local_IP + "/attendance_app/api/assign_parent.php",
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        if (status.equals("success")) {
                            Intent intent = new Intent(PairStudentActivity.this, ParentActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            String message = jsonObject.getString("message");
                            Toast.makeText(PairStudentActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // Handle error response
                }
            }) {
        @Override
        protected Map<String, String> getParams() {
            Map<String, String> params = new HashMap<>();
            params.put("student_id", studentId);
            params.put("parent_id", parentId+"");
            return params;
        }
    };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}