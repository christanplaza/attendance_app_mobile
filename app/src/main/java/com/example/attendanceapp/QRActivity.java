package com.example.attendanceapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class QRActivity extends BaseActivity {
    private ImageView qrImageView;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qractivity);

        qrImageView = findViewById(R.id.qr_image_view);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Generating QR Code...");
        progressDialog.show();

        SharedPreferences sharedPreferences = getSharedPreferences("attendance_app", MODE_PRIVATE);
        int studentId = sharedPreferences.getInt("id", 0);

        String url = "http://192.168.54.60/attendance_app/api/verify_class.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {try {
                JSONObject jsonObject = new JSONObject(response);
                String status = jsonObject.getString("status");
                if (status.equals("success")) {
                    String verify = jsonObject.getString("verify");
                    // Generate QR code image with verify string
                    QRCodeWriter writer = new QRCodeWriter();
                    BitMatrix bitMatrix = writer.encode(verify, BarcodeFormat.QR_CODE, 512, 512);
                    int width = bitMatrix.getWidth();
                    int height = bitMatrix.getHeight();
                    Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                        }
                    }
                    // Set QR code image to ImageView
                    qrImageView.setImageBitmap(bmp);
                } else if (status.equals("error")) {
                    String message = jsonObject.getString("message");
                    // Display error message
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                    finish();
                }
                progressDialog.dismiss();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (WriterException e) {
                throw new RuntimeException(e);
            }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Error getting verification string", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("student_id", studentId + "");
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }
}