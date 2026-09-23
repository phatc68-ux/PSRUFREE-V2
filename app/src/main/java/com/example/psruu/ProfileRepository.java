package com.example.psruu;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProfileRepository {

    private static final String PREF_NAME = "PSRU_USER_PREF";
    private static final String KEY_USER_PROFILE = "user_profile";
    private static final String REVIEW_PREF_NAME = "PSRU_REVIEW_PREF";
    private static final String KEY_REVIEWS = "review_list";

    private Context context;
    private SharedPreferences prefs;
    private SharedPreferences reviewPrefs;
    private UserRepository userRepository;

    public ProfileRepository(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.reviewPrefs = context.getSharedPreferences(REVIEW_PREF_NAME, Context.MODE_PRIVATE);
        this.userRepository = new UserRepository(context);
    }

    public void saveUserProfile(UserProfile profile) {
        try {
            userRepository.registerUser(
                    profile.getName(),
                    profile.getStudentId(),
                    "",
                    "",
                    profile.getImageUri(),
                    profile.getFacebook(),
                    profile.getInstagram(),
                    profile.getPhone()
            );

            JSONObject obj = new JSONObject();
            obj.put("name", profile.getName());
            obj.put("facebook", profile.getFacebook());
            obj.put("instagram", profile.getInstagram());
            obj.put("phone", profile.getPhone());
            obj.put("imageUri", profile.getImageUri());

            prefs.edit().putString(KEY_USER_PROFILE, obj.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public UserProfile getUserProfile() {
        if (userRepository != null) {
            UserProfile profile = userRepository.getUserProfile();
            if (profile != null && profile.getName() != null && !profile.getName().equals("ชื่อผู้ขาย")) {
                return profile;
            }
        }

        String jsonString = prefs.getString(KEY_USER_PROFILE, null);
        if (jsonString == null) {
            return new UserProfile("นักศึกษา พบส.", "-", "-", "-", "", "");
        }

        try {
            JSONObject obj = new JSONObject(jsonString);
            String name = obj.optString("name", "นักศึกษา พบส.");
            String facebook = obj.optString("facebook", "-");
            String instagram = obj.optString("instagram", "-");
            String phone = obj.optString("phone", "-");
            String imageUri = obj.optString("imageUri", "");

            return new UserProfile(name, facebook, instagram, phone, imageUri, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new UserProfile("นักศึกษา พบส.", "-", "-", "-", "", "");
    }

    public List<PostItem> getAllMyPosts() {
        List<PostItem> postItems = new ArrayList<>();
        ProductRepository productRepo = new ProductRepository(context);

        // 1. ดึงโพสต์จากตลาดสินค้าปกติ (isWanted = false)
        List<Product> normalProducts = productRepo.getProducts(false, 0, 0);
        for (int i = 0; i < normalProducts.size(); i++) {
            Product p = normalProducts.get(i);
            try {
                PostItem item = new PostItem(
                        p.getName(),
                        p.getPrice(),
                        p.getCategory(),
                        p.getImageUri(),
                        "KEY_PRODUCTS", // ใช้ระบุ sourceBoard
                        i             // originalIndex
                );
                postItems.add(item);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 2. ดึงโพสต์จากบอร์ดตามหาของ (isWanted = true) มารวมกัน
        List<Product> wantedProducts = productRepo.getProducts(true, 0, 0);
        for (int i = 0; i < wantedProducts.size(); i++) {
            Product p = wantedProducts.get(i);
            try {
                PostItem item = new PostItem(
                        p.getName(),
                        p.getPrice(),
                        p.getCategory(),
                        p.getImageUri(),
                        "KEY_WANTED", // ใช้ระบุ sourceBoard ว่ามาจากบอร์ดตามหา
                        i             // originalIndex
                );
                postItems.add(item);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return postItems;
    }

    public void deletePost(String sourceBoard, int index) {
        try {
            ProductRepository productRepo = new ProductRepository(context);
            boolean isWanted = "KEY_WANTED".equals(sourceBoard);
            productRepo.deleteProduct(isWanted, index);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addReview(String reviewerName, float rating, String comment, String date) {
        try {
            UserProfile currentProfile = getUserProfile();
            String actualName = (currentProfile != null && currentProfile.getName() != null && !currentProfile.getName().isEmpty())
                    ? currentProfile.getName() : "ผู้ใช้งาน";

            JSONArray jsonArray = new JSONArray(reviewPrefs.getString(KEY_REVIEWS, "[]"));
            JSONObject obj = new JSONObject();
            obj.put("reviewerName", actualName);
            obj.put("rating", rating);
            obj.put("comment", comment);
            obj.put("date", date);

            JSONArray newArray = new JSONArray();
            newArray.put(obj);
            for (int i = 0; i < jsonArray.length(); i++) {
                newArray.put(jsonArray.getJSONObject(i));
            }
            reviewPrefs.edit().putString(KEY_REVIEWS, newArray.toString()).apply();

            // คำนวณและบันทึกคะแนนเฉลี่ยใหม่ทันที
            List<ReviewItem> updatedList = getReviewList();
            if (!updatedList.isEmpty()) {
                float total = 0;
                for (ReviewItem item : updatedList) {
                    try {
                        total += Float.parseFloat(item.getRating());
                    } catch (Exception ignored) {}
                }
                float newAvg = total / updatedList.size();
                userRepository.saveAverageRating(Math.round(newAvg * 10.0f) / 10.0f);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getReviewCount() {
        return getReviewList().size();
    }

    public List<ReviewItem> getReviewList() {
        List<ReviewItem> list = new ArrayList<>();
        String jsonString = reviewPrefs.getString(KEY_REVIEWS, "[]");

        UserProfile currentProfile = getUserProfile();
        String currentRealName = (currentProfile != null && currentProfile.getName() != null && !currentProfile.getName().equals("ชื่อผู้ขาย"))
                ? currentProfile.getName() : "นักศึกษา พบส.";

        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                String name = currentRealName;
                String savedName = obj.optString("reviewerName", "");
                if (!savedName.isEmpty() && !savedName.equals("ผู้ใช้งานทั่วไป") && !savedName.equals("นักศึกษา พบส.") && !savedName.equals("ชื่อผู้ขาย")) {
                    name = savedName;
                }

                float ratingFloat = (float) obj.optDouble("rating", 5.0);
                String ratingStr = String.valueOf(ratingFloat);
                String comment = obj.optString("comment", "");
                String date = obj.optString("date", "วันนี้");

                list.add(new ReviewItem(name, ratingStr, comment, date));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}