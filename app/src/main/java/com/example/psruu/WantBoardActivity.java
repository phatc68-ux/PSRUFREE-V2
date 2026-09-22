package com.example.psruu;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class WantBoardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market_wanted);

        // ตัวอย่างการเรียกใช้งานเมื่อผู้ใช้คลิกรายการในบอร์ดตามหาของ
        // showWantedDetailDialog("ชื่อผู้โพสต์ตัวอย่าง", "ตามหาหนังสือเรียน...", "500 บาท");
    }

    public void showWantedDetailDialog(String posterName, String postTitle, String budget) {
        try {
            Dialog dialog = new Dialog(WantBoardActivity.this);

            // เรียกใช้เลย์เอาต์ของบอร์ดตามหาของ
            dialog.setContentView(R.layout.dialog_wanted_detail);

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }

            // ปุ่มดูโปรไฟล์
            Button btnViewProfile = dialog.findViewById(R.id.btnViewProfile);
            if (btnViewProfile != null) {
                btnViewProfile.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(WantBoardActivity.this, ProfileActivity.class);
                        startActivity(intent);
                        dialog.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            // ปุ่มติดต่อผู้โพสต์ (ตัดระบบแชทออก ปรับเป็นการแจ้งเตือนหรือเปลี่ยนเป็นการโทรแทนได้)
            Button btnContactWanted = dialog.findViewById(R.id.btnContactWanted);
            if (btnContactWanted != null) {
                btnContactWanted.setOnClickListener(v -> {
                    try {
                        // ปิดการใช้งานแชท และแสดงข้อความแจ้งเตือนแทน
                        Toast.makeText(this, "ระบบแชทถูกปิดใช้งาน", Toast.LENGTH_SHORT).show();

                        // หากต้องการเปลี่ยนเป็นโทรติดต่อแทน สามารถเปิดใช้โค้ดด้านล่างนี้แทนได้ครับ
                        /*
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:0891234567")); // ใส่เบอร์โทรจริงของผู้โพสต์
                        startActivity(intent);
                        */

                        dialog.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}