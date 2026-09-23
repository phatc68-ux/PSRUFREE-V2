package com.example.psruu;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class WantBoardActivity extends AppCompatActivity {

    private WantBoardRepository wantBoardRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market_wanted);

        wantBoardRepository = new WantBoardRepository(this);

        // ตัวอย่างการเรียกใช้งานเมื่อผู้ใช้คลิกรายการในบอร์ดตามหาของ
        // showWantedDetailDialog("ชื่อผู้โพสต์ตัวอย่าง", "ตามหาหนังสือเรียน...", "500 บาท");
    }

    /**
     * ควบคุมการแสดง Dialog รายละเอียดของบอร์ดตามหา
     */
    public void showWantedDetailDialog(String posterName, String postTitle, String budget) {
        try {
            // ให้ Repository จัดการสร้างและผูกเหตุการณ์ของ Dialog (แยก Business Logic ออกจาก Activity)
            wantBoardRepository.createWantedDetailDialog(posterName, postTitle, budget, new WantBoardRepository.OnDialogActionListener() {
                @Override
                public void onContactClicked() {
                    // จัดการตรรกะเมื่อกดปุ่มติดต่อ (เช่น แสดง Toast หรือเปิดระบบโทร)
                    Toast.makeText(WantBoardActivity.this, "ระบบแชทถูกปิดใช้งาน", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}