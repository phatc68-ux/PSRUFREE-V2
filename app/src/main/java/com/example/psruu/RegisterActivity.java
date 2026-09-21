package com.example.psruu;

import android.content.Intent;
import android.content.SharedPreferences;
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

    // เพิ่มตัวแปรสำหรับช่องทางการติดต่อ
    private EditText etRegName, etRegStudentId, etRegEmail, etRegPassword;
    private EditText etRegFacebook, etRegInstagram, etRegPhone;
    private LinearLayout btnSelectProfileImage;
    private TextView tvSelectedImageStatus, tvBackToLogin;
    private Button btnRegister;

    private String profileImageUriStr = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // ผูกตัวแปรกับหน้า UI (XML)
        etRegName = findViewById(R.id.etRegName);
        etRegStudentId = findViewById(R.id.etRegStudentId);
        etRegEmail = findViewById(R.id.etRegEmail);
        etRegPassword = findViewById(R.id.etRegPassword);

        // ฟิลด์ช่องทางการติดต่อ (ตรวจสอบให้แน่ใจว่าใน activity_register.xml มี id ตรงกัน)
        etRegFacebook = findViewById(R.id.etRegFacebook);
        etRegInstagram = findViewById(R.id.etRegInstagram);
        etRegPhone = findViewById(R.id.etRegPhone);

        btnSelectProfileImage = findViewById(R.id.btnSelectProfileImage);
        tvSelectedImageStatus = findViewById(R.id.tvSelectedImageStatus);
        btnRegister = findViewById(R.id.btnRegister);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        // กดเลือกรูปภาพจากเครื่อง
        btnSelectProfileImage.setOnClickListener(v -> openGallery());

        // กดปุ่มสมัครสมาชิก
        btnRegister.setOnClickListener(v -> {
            String name = etRegName.getText().toString().trim();
            String studentId = etRegStudentId.getText().toString().trim();
            String email = etRegEmail.getText().toString().trim();
            String password = etRegPassword.getText().toString().trim();

            // ดึงค่าช่องทางการติดต่อ
            String facebook = etRegFacebook != null ? etRegFacebook.getText().toString().trim() : "";
            String instagram = etRegInstagram != null ? etRegInstagram.getText().toString().trim() : "";
            String phone = etRegPhone != null ? etRegPhone.getText().toString().trim() : "";

            if (name.isEmpty() || studentId.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "กรุณากรอกข้อมูลให้ครบทุกช่อง", Toast.LENGTH_SHORT).show();
                return;
            }

            if (profileImageUriStr.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "กรุณาเลือกรูปโปรไฟล์", Toast.LENGTH_SHORT).show();
                return;
            }

            // บันทึกข้อมูลลง SharedPreferences สำหรับนำไปแสดงผลที่หน้าโปรไฟล์
            SharedPreferences prefs = getSharedPreferences("PSRU_USER_PREF", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("USER_NAME", name);
            editor.putString("USER_STUDENT_ID", studentId);
            editor.putString("USER_EMAIL", email);
            editor.putString("USER_PASSWORD", password);
            editor.putString("USER_IMAGE", profileImageUriStr);

            // บันทึกช่องทางการติดต่อเพิ่มเติม
            editor.putString("USER_FACEBOOK", facebook);
            editor.putString("USER_INSTAGRAM", instagram);
            editor.putString("USER_PHONE", phone);

            editor.apply();

            Toast.makeText(RegisterActivity.this, "สมัครสมาชิกสำเร็จ!", Toast.LENGTH_SHORT).show();

            // กลับไปหน้าก่อนหน้า (Login หรือ หน้าหลัก)
            finish();
        });

        // กลับไปหน้า Login
        tvBackToLogin.setOnClickListener(v -> finish());
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

            try {
                final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getContentResolver().takePersistableUriPermission(sourceUri, takeFlags);
            } catch (Exception ignored) {}

            // คัดลอกรูปภาพมาเก็บไว้ใน Cache ภายในแอป เพื่อให้หน้า ProfileActivity เปิดอ่านได้ตลอดเวลา
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