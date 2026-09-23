package com.example.psruu;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

public class WantBoardRepository {

    private Context context;

    public interface OnDialogActionListener {
        void onContactClicked();
    }

    public WantBoardRepository(Context context) {
        this.context = context;
    }

    /**
     * จัดการสร้าง Dialog รายละเอียดของประกาศตามหาของ (แยกตรรกะออกจาก Activity)
     */
    public void createWantedDetailDialog(String posterName, String postTitle, String budget, OnDialogActionListener listener) {
        try {
            Dialog dialog = new Dialog(context);

            LayoutInflater inflater = LayoutInflater.from(context);
            View dialogView = inflater.inflate(R.layout.dialog_wanted_detail, null);
            dialog.setContentView(dialogView);

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }

            // ปุ่มดูโปรไฟล์
            Button btnViewProfile = dialogView.findViewById(R.id.btnViewProfile);
            if (btnViewProfile != null) {
                btnViewProfile.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(context, ProfileActivity.class);
                        context.startActivity(intent);
                        dialog.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            // ปุ่มติดต่อผู้โพสต์
            Button btnContactWanted = dialogView.findViewById(R.id.btnContactWanted);
            if (btnContactWanted != null) {
                btnContactWanted.setOnClickListener(v -> {
                    try {
                        if (listener != null) {
                            listener.onContactClicked();
                        }
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