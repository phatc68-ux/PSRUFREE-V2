package com.example.psruu;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etLoginEmailOrId, etLoginPassword;
    private Button btnLogin;
    private TextView tvGoToRegister;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // กำหนดใช้งาน Repository (Model/Data layer)
        userRepository = new UserRepository(this);

        initView();
        initListener();
    }

    private void initView() {
        etLoginEmailOrId = findViewById(R.id.etLoginEmailOrId);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);
    }

    private void initListener() {
        // กดปุ่มเข้าสู่ระบบ
        btnLogin.setOnClickListener(v -> handleLogin());

        // ไปหน้าสมัครสมาชิก
        tvGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void handleLogin() {
        String inputUser = etLoginEmailOrId.getText().toString().trim();
        String inputPassword = etLoginPassword.getText().toString().trim();

        if (inputUser.isEmpty() || inputPassword.isEmpty()) {
            Toast.makeText(LoginActivity.this, "กรุณากรอกข้อมูลให้ครบทุกช่อง", Toast.LENGTH_SHORT).show();
            return;
        }

        // ให้ Repository เป็นคนตรวจสอบข้อมูล (แยก Business Logic ออกจาก Activity)
        boolean isAuthenticated = userRepository.authenticate(inputUser, inputPassword);

        if (isAuthenticated) {
            Toast.makeText(LoginActivity.this, "เข้าสู่ระบบสำเร็จ!", Toast.LENGTH_SHORT).show();

            // พาไปหน้าแรก (MainActivity) ทันที
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();

        } else {
            Toast.makeText(LoginActivity.this, "อีเมล/รหัสนักศึกษา หรือรหัสผ่านไม่ถูกต้อง", Toast.LENGTH_SHORT).show();
        }
    }
}