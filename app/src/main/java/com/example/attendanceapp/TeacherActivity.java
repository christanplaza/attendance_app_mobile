package com.example.attendanceapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class TeacherActivity extends BaseActivity {
    private Button logoutButton, scanButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        logoutButton = findViewById(R.id.logoutButton);
        scanButton = findViewById(R.id.scanButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(TeacherActivity.this, ScanQRActivity.class);
//                startActivity(intent);
                initiateScan();
            }
        });
    }

    protected void initiateScan() {
        IntentIntegrator intentIntegrator = new IntentIntegrator(TeacherActivity.this);
        intentIntegrator.setOrientationLocked(true);
        intentIntegrator.setPrompt("Scan a QR Code");
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        intentIntegrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {
            String contents = intentResult.getContents();
            Log.d("Content", contents);
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