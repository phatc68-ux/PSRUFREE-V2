package com.example.psruu;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class RegisterActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    // ตัวแปร UI (View)
    private EditText etRegName, etRegStudentId, etRegEmail, etRegPassword;
    private EditText etRegFacebook, etRegInstagram, etRegPhone;
    private LinearLayout btnSelectProfileImage;
    private TextView tvSelectedImageStatus, tvBackToLogin;
    private Button btnRegister;

    private String profileImageUriStr = "";
    private UserRepository userRepository; // เรียกใช้ Repository (Model/Data Layer)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userRepository = new UserRepository(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etRegName = findViewById(R.id.etRegName);
        etRegStudentId = findViewById(R.id.etRegStudentId);
        etRegEmail = findViewById(R.id.etRegEmail);
        etRegPassword = findViewById(R.id.etRegPassword);

        etRegFacebook = findViewById(R.id.etRegFacebook);
        etRegInstagram = findViewById(R.id.etRegInstagram);
        etRegPhone = findViewById(R.id.etRegPhone);

        btnSelectProfileImage = findViewById(R.id.btnSelectProfileImage);
        tvSelectedImageStatus = findViewById(R.id.tvSelectedImageStatus);
        btnRegister = findViewById(R.id.btnRegister);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
    }

    private void setupListeners() {
        btnSelectProfileImage.setOnClickListener(v -> openGallery());

        btnRegister.setOnClickListener(v -> handleRegistration());

        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void handleRegistration() {
        String name = etRegName.getText().toString().trim();
        String studentId = etRegStudentId.getText().toString().trim();
        String email = etRegEmail.getText().toString().trim();
        String password = etRegPassword.getText().toString().trim();

        String facebook = etRegFacebook != null ? etRegFacebook.getText().toString().trim() : "";
        String instagram = etRegInstagram != null ? etRegInstagram.getText().toString().trim() : "";
        String phone = etRegPhone != null ? etRegPhone.getText().toString().trim() : "";

        if (name.isEmpty() || studentId.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "กรุณากรอกข้อมูลให้ครบทุกช่อง", Toast.LENGTH_SHORT).show();
            return;
        }

        if (profileImageUriStr.isEmpty()) {
            Toast.makeText(this, "กรุณาเลือกรูปโปรไฟล์", Toast.LENGTH_SHORT).show();
            return;
        }

        // ส่งข้อมูลให้ Repository เป็นผู้จัดการบันทึกข้อมูล (แยก Business Logic ออกจาก Activity)
        userRepository.registerUser(name, studentId, email, password, profileImageUriStr, facebook, instagram, phone);

        Toast.makeText(this, "สมัครสมาชิกสำเร็จ!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri sourceUri = data.getData();

            File savedFile = saveUriToInternalCache(sourceUri);
            if (savedFile != null) {
                profileImageUriStr = Uri.fromFile(savedFile).toString();
                tvSelectedImageStatus.setText("เลือกไฟล์แล้ว: " + savedFile.getName());
                tvSelectedImageStatus.setTextColor(Color.parseColor("#00794C"));
            } else {
                Toast.makeText(this, "ไม่สามารถโหลดรูปภาพนี้ได้", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File saveUriToInternalCache(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            File cacheDir = getCacheDir();
            File destinationFile = new File(cacheDir, "profile_" + System.currentTimeMillis() + ".jpg");

            FileOutputStream outputStream = new FileOutputStream(destinationFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();
            return destinationFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}