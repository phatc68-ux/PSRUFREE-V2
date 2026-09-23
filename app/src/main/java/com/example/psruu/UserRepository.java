package com.example.psruu;

import android.content.Context;
import android.content.SharedPreferences;

public class UserRepository {
    private SharedPreferences prefs;
    private Context context;

    public UserRepository(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences("PSRU_USER_PREF", Context.MODE_PRIVATE);
    }

    public boolean authenticate(String username, String password) {
        return username != null && !username.trim().isEmpty() && password != null && !password.trim().isEmpty();
    }

    // เมธอดสำหรับบันทึกข้อมูลการสมัครสมาชิก
    public void registerUser(String name, String studentId, String email, String password,
                             String imageUri, String facebook, String instagram, String phone) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("USER_NAME", name);
        editor.putString("USER_STUDENT_ID", studentId);
        editor.putString("USER_EMAIL", email);
        editor.putString("USER_PASSWORD", password);
        editor.putString("USER_IMAGE", imageUri);
        editor.putString("USER_FACEBOOK", facebook);
        editor.putString("USER_INSTAGRAM", instagram);
        editor.putString("USER_PHONE", phone);
        editor.apply();
    }

    // เมธอดสำหรับดึงข้อมูลโปรไฟล์ (ปรับลำดับพารามิเตอร์ให้ตรงกับ UserProfile constructor ของคุณ)
    public UserProfile getUserProfile() {
        String name = prefs.getString("USER_NAME", "ชื่อผู้ขาย");
        String studentId = prefs.getString("USER_STUDENT_ID", "-");
        String imageUri = prefs.getString("USER_IMAGE", "");
        String facebook = prefs.getString("USER_FACEBOOK", "-");
        String instagram = prefs.getString("USER_INSTAGRAM", "-");
        String phone = prefs.getString("USER_PHONE", "-");

        // ส่งค่าครบทั้ง 6 ตัว (รวม studentId และ imageUri ให้ตรงกับ Constructor ของ UserProfile)
        return new UserProfile(name, studentId, imageUri, facebook, instagram, phone);
    }
    public void saveAverageRating(float rating) {
        prefs.edit().putFloat("USER_AVG_RATING", rating).apply();
    }

    // คำนวณคะแนนเฉลี่ยจากรีวิวจริง ๆ ที่ถูกบันทึกไว้
    public float getAverageRating() {
        try {
            ProfileRepository profileRepo = new ProfileRepository(context);
            java.util.List<ReviewItem> reviews = profileRepo.getReviewList();
            if (reviews == null || reviews.isEmpty()) {
                return prefs.getFloat("USER_AVG_RATING", 5.0f); // ค่าเริ่มต้นถ้ายังไม่มีรีวิว
            }

            float totalSum = 0;
            for (ReviewItem item : reviews) {
                try {
                    totalSum += Float.parseFloat(item.getRating());
                } catch (Exception ignored) {
                }
            }
            float average = totalSum / reviews.size();
            // ปัดเศษให้เหลือทศนิยม 1 ตำแหน่ง
            return Math.round(average * 10.0f) / 10.0f;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return prefs.getFloat("USER_AVG_RATING", 5.0f);
    }
}