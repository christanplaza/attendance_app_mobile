package com.example.attendanceapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExcuseLetterForm extends BaseActivity {
    private EditText noteEditText;
    private Button selectDateButton, selectImageBtn, submitButton;
    private RecyclerView classRecyclerView;
    private ClassListAdapter classListAdapter;
    private ImageView selectedImageView;
    private ProgressBar progressBar;
    private TextView selectedDateTextView;
    private Date selectedDate;
    private Bitmap selectedImageBitmap;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    String local_IP = Constants.LOCAL_IP;
    private int parent_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_excuse_letter_form);

        // Connect views to variables
        noteEditText = findViewById(R.id.noteEditText);
        selectDateButton = findViewById(R.id.selectDateButton);
        selectedDateTextView = findViewById(R.id.selectedDateTextView);
        selectImageBtn = findViewById(R.id.selectImageBtn);
        selectedImageView = findViewById(R.id.selectedImageView);
        submitButton = findViewById(R.id.submitButton);
        progressBar = findViewById(R.id.progressBar);
        classRecyclerView = findViewById(R.id.classRecyclerView);

        List<ClassItem> classItems = new ArrayList<>();

        // Set up the RecyclerView adapter
        classListAdapter = new ClassListAdapter((ArrayList<ClassItem>) classItems, new ClassListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(SparseBooleanArray itemStateArray) {
            }
        });

        classRecyclerView.setAdapter(classListAdapter);
        classRecyclerView.setLayoutManager(new LinearLayoutManager(ExcuseLetterForm.this));

        SharedPreferences sharedPreferences = getSharedPreferences("attendance_app", MODE_PRIVATE);
        parent_id = sharedPreferences.getInt("id", 0);

        // Initialize pickImageLauncher
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // Get selected image URI and display in ImageView
                        Uri selectedImageUri = result.getData().getData();
                        selectedImageView.setImageURI(selectedImageUri);

                        try {
                            selectedImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        // Display selected image in ImageView
                        selectedImageView.setImageBitmap(selectedImageBitmap);
                    }
                });

        // Attach click listener to selectImageBtn
        selectImageBtn.setOnClickListener(view -> {
            // Create intent to select image from gallery
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            // Launch activity to select image
            pickImageLauncher.launch(intent);
        });

        // Select Date Button
        selectDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get current date
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);

                // Create date picker dialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(ExcuseLetterForm.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // Display selected date
                                Calendar calendar = Calendar.getInstance();
                                calendar.set(Calendar.YEAR, year);
                                calendar.set(Calendar.MONTH, monthOfYear);
                                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                selectedDate = calendar.getTime();

                                // Format the date and display it on the text view
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                selectedDateTextView.setText(sdf.format(selectedDate));

                                // Query the classes for the selected date and populate the spinner
                                progressBar.setVisibility(View.VISIBLE);

                                // Construct the API URL
                                String url = local_IP + "/attendance_app/api/get_student_classes.php";
                                url += "?parent_id=" + parent_id;
                                url += "&excuse_date=" + sdf.format(selectedDate);

                                // Send the API request using Volley
                                StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        // Parse the JSON response
                                        try {
                                            JSONObject jsonObject = new JSONObject(response);
                                            String status = jsonObject.getString("status");
                                            if (status.equals("success")) {
                                                JSONArray classesArray = jsonObject.getJSONArray("classes");

                                                // Add each class to the list
                                                for (int i = 0; i < classesArray.length(); i++) {
                                                    JSONObject classObj = classesArray.getJSONObject(i);
                                                    String className = classObj.getString("title");
                                                    String classId = classObj.getString("id");
                                                    ClassItem classItem = new ClassItem(className, classId);
                                                    classItems.add(classItem);
                                                }

                                                progressBar.setVisibility(View.INVISIBLE);

                                                classRecyclerView.setLayoutManager(new LinearLayoutManager(ExcuseLetterForm.this));
                                                classRecyclerView.setAdapter(classListAdapter);
                                                classRecyclerView.setVisibility(View.VISIBLE);
                                                classListAdapter.notifyDataSetChanged();
                                            } else {
                                                Toast.makeText(ExcuseLetterForm.this, "Error: " + jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            // Handle JSON exception
                                        }
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        error.printStackTrace();
                                        // Handle Volley error
                                    }
                                });

                                // Add the request to the Volley queue
                                Volley.newRequestQueue(ExcuseLetterForm.this).add(request);
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String note = noteEditText.getText().toString();
                ArrayList<Integer> selectedClassIds = classListAdapter.getSelectedItems();
                String date = selectedDateTextView.getText().toString();
                String selectedImageBase64 = "";
                if (selectedImageBitmap != null) {
                    selectedImageBase64 = Utils.bitmapToBase64(selectedImageBitmap);
                }

                // Construct the API URL
                String url = local_IP + "/attendance_app/api/submit_excuse_letter.php";

                // Send the API request using Volley
                String finalSelectedImageBase6 = selectedImageBase64;
                StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Parse the JSON response
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");
                            if (status.equals("success")) {
                                // Show success message and go back to previous activity
                                Toast.makeText(ExcuseLetterForm.this, "Excuse letter submitted successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                // Show error message
                                Toast.makeText(ExcuseLetterForm.this, "Error: " + jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            // Handle JSON exception
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        // Handle Volley error
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        // Add parameters to the POST request
                        Map<String, String> params = new HashMap<>();
                        params.put("parent_id", parent_id+"");
                        params.put("note", note);
                        params.put("class_ids", TextUtils.join(",", selectedClassIds));
                        params.put("date", date);
                        params.put("image", finalSelectedImageBase6);
                        return params;
                    }
                };

                // Add the request to the Volley queue
                Volley.newRequestQueue(ExcuseLetterForm.this).add(request);
            }
        });

    }
}

