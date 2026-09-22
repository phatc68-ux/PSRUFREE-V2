package com.example.psruu;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private LinearLayout layoutMyPostsGridContainer;
    private TextView tvPostCount, tvReviewCount, tvReviewHistoryCount;
    private TextView tvProfileFacebook, tvProfileInstagram, tvProfilePhone;
    private LinearLayout btnReviewHistory;

    private static class PostItem {
        String title;
        String priceOrBudget;
        String type;
        String imageUri;
        String sourceBoard;
        int originalIndex;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        try {
            layoutMyPostsGridContainer = findViewById(R.id.layoutMyPostsGridContainer);
            tvPostCount = findViewById(R.id.tvPostCount);
            tvReviewCount = findViewById(R.id.tvReviewCount);
            tvReviewHistoryCount = findViewById(R.id.tvReviewHistoryCount);

            // ผูกตัวแปรช่องทางการติดต่อ
            tvProfileFacebook = findViewById(R.id.tvProfileFacebook);
            tvProfileInstagram = findViewById(R.id.tvProfileInstagram);
            tvProfilePhone = findViewById(R.id.tvProfilePhone);

            // ผูกปุ่มประวัติรีวิว
            btnReviewHistory = findViewById(R.id.btnReviewHistory);
            if (btnReviewHistory != null) {
                btnReviewHistory.setOnClickListener(v -> showReviewHistoryDialog());
            }

            loadUserProfile();
            loadRatingData();
            loadReviewCount();
            loadAndDisplayMyPostsGrid();
            setupBottomNavigation();

            View btnAddPost = findViewById(R.id.btnAddPost);
            if (btnAddPost != null) {
                btnAddPost.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            View btnLogout = findViewById(R.id.btnLogout);
            if (btnLogout != null) {
                btnLogout.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // เมธอด Static ที่ MainActivity เรียกใช้เพื่อบันทึกคะแนนรีวิว
    public static void submitNewRating(Context context, float newRating) {
        try {
            SharedPreferences prefs = context.getSharedPreferences("PSRU_USER_PREF", MODE_PRIVATE);
            prefs.edit().putFloat("USER_AVG_RATING", newRating).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadReviewCount() {
        try {
            SharedPreferences prefs = getSharedPreferences("PSRU_REVIEW_PREF", MODE_PRIVATE);
            String reviewsJson = prefs.getString("review_list", "[]");
            JSONArray jsonArray = new JSONArray(reviewsJson);

            int totalReviews = jsonArray.length();
            if (totalReviews == 0) {
                totalReviews = 1;
            }

            if (tvReviewHistoryCount != null) {
                tvReviewHistoryCount.setText(String.valueOf(totalReviews));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showReviewHistoryDialog() {
        try {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_review_history, null);
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .create();

            View btnClose = dialogView.findViewById(R.id.btnCloseDialog);
            if (btnClose != null) {
                btnClose.setOnClickListener(v -> dialog.dismiss());
            }

            SharedPreferences prefs = getSharedPreferences("PSRU_REVIEW_PREF", MODE_PRIVATE);
            String reviewsJson = prefs.getString("review_list", "[]");

            JSONArray jsonArray = new JSONArray(reviewsJson);
            ArrayList<String[]> reviewList = new ArrayList<>();

            if (jsonArray.length() == 0) {
                reviewList.add(new String[]{"ธนวัฒน์ ศึกษาดี", "5.0", "บริการดีมาก สินค้าใช้งานได้ยอดเยี่ยมครับ", "18 มิ.ย. 2569"});
            } else {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    reviewList.add(new String[]{
                            obj.optString("reviewer", "ผู้ใช้งาน"),
                            obj.optString("rating", "5.0"),
                            obj.optString("comment", "-"),
                            obj.optString("date", "")
                    });
                }
            }

            ListView lvReviewList = dialogView.findViewById(R.id.lvReviewList);
            BaseAdapter adapter = new BaseAdapter() {
                @Override
                public int getCount() { return reviewList.size(); }
                @Override
                public Object getItem(int position) { return reviewList.get(position); }
                @Override
                public long getItemId(int position) { return position; }

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    if (convertView == null) {
                        convertView = getLayoutInflater().inflate(R.layout.item_review_history, parent, false);
                    }
                    String[] data = reviewList.get(position);

                    TextView tvName = convertView.findViewById(R.id.tvReviewerName);
                    TextView tvRating = convertView.findViewById(R.id.tvReviewRating);
                    TextView tvComment = convertView.findViewById(R.id.tvReviewComment);
                    TextView tvDate = convertView.findViewById(R.id.tvReviewDate);

                    if (tvName != null) tvName.setText(data[0]);
                    if (tvRating != null) tvRating.setText("⭐ (" + data[1] + ")");
                    if (tvComment != null) tvComment.setText(data[2]);
                    if (tvDate != null) tvDate.setText(data[3]);

                    return convertView;
                }
            };

            if (lvReviewList != null) {
                lvReviewList.setAdapter(adapter);
            }

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }

            dialog.show();
        } catch (Exception e) {
            Toast.makeText(this, "เกิดข้อผิดพลาดในการโหลดประวัติรีวิว", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRatingData();
        loadReviewCount();
    }

    private void loadUserProfile() {
        try {
            SharedPreferences prefs = getSharedPreferences("PSRU_USER_PREF", MODE_PRIVATE);
            String name = prefs.getString("USER_NAME", "กัญญาณี ศรีสุข");
            String studentId = prefs.getString("USER_STUDENT_ID", "6812247005");
            String imageUriStr = prefs.getString("USER_IMAGE", "");

            String facebook = prefs.getString("USER_FACEBOOK", "Kanyanee Srisuk");
            String instagram = prefs.getString("USER_INSTAGRAM", "kan_psru");
            String phone = prefs.getString("USER_PHONE", "089-123-4567");

            TextView tvProfileName = findViewById(R.id.tvProfileName);
            if (tvProfileName != null) tvProfileName.setText(name);

            TextView tvProfileStudentId = findViewById(R.id.tvProfileStudentId);
            if (tvProfileStudentId != null) tvProfileStudentId.setText("รหัสนักศึกษา: " + studentId);

            if (tvProfileFacebook != null) tvProfileFacebook.setText(facebook);
            if (tvProfileInstagram != null) tvProfileInstagram.setText(instagram);
            if (tvProfilePhone != null) tvProfilePhone.setText(phone);

            ImageView ivProfileImage = findViewById(R.id.ivProfileImage);
            if (ivProfileImage != null && imageUriStr != null && !imageUriStr.isEmpty()) {
                safelySetImageUri(ivProfileImage, imageUriStr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadRatingData() {
        try {
            SharedPreferences prefs = getSharedPreferences("PSRU_USER_PREF", MODE_PRIVATE);
            float averageRating = prefs.getFloat("USER_AVG_RATING", 4.9f);

            if (tvReviewCount != null) {
                tvReviewCount.setText(String.format(Locale.getDefault(), "%.1f", averageRating));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadAndDisplayMyPostsGrid() {
        try {
            if (layoutMyPostsGridContainer == null) return;
            layoutMyPostsGridContainer.removeAllViews();

            List<PostItem> allPosts = new ArrayList<>();

            try {
                SharedPreferences prefsSwap = getSharedPreferences("PSRU_SWAP_PRODUCTS", MODE_PRIVATE);
                String jsonSwap = prefsSwap.getString("product_list", "[]");
                if (jsonSwap != null && jsonSwap.startsWith("[")) {
                    JSONArray arrSwap = new JSONArray(jsonSwap);
                    for (int i = 0; i < arrSwap.length(); i++) {
                        JSONObject obj = arrSwap.optJSONObject(i);
                        if (obj != null) {
                            PostItem item = new PostItem();
                            item.title = obj.optString("name", obj.optString("title", "ไม่มีชื่อสินค้า"));
                            item.priceOrBudget = "฿" + obj.optString("price", "0");
                            item.type = "ขาย";
                            item.imageUri = obj.optString("image", "");
                            item.sourceBoard = "SWAP";
                            item.originalIndex = i;
                            allPosts.add(item);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                SharedPreferences prefsWanted = getSharedPreferences("PSRU_WANTED_PRODUCTS", MODE_PRIVATE);
                String jsonWanted = prefsWanted.getString("product_list", "[]");
                if (jsonWanted != null && jsonWanted.startsWith("[")) {
                    JSONArray arrWanted = new JSONArray(jsonWanted);
                    for (int i = 0; i < arrWanted.length(); i++) {
                        JSONObject obj = arrWanted.optJSONObject(i);
                        if (obj != null) {
                            PostItem item = new PostItem();
                            item.title = obj.optString("name", obj.optString("title", "ไม่มีหัวข้อ"));
                            item.priceOrBudget = "งบไม่เกิน " + obj.optString("price", obj.optString("budget", "0")) + " บาท";
                            item.type = "ตามหา";
                            item.imageUri = obj.optString("image", "");
                            item.sourceBoard = "WANTED";
                            item.originalIndex = i;
                            allPosts.add(item);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (tvPostCount != null) {
                tvPostCount.setText(String.valueOf(allPosts.size()));
            }

            for (int i = 0; i < allPosts.size(); i += 2) {
                LinearLayout rowLayout = new LinearLayout(this);
                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                rowParams.setMargins(0, 0, 0, 8);
                rowLayout.setLayoutParams(rowParams);
                rowLayout.setOrientation(LinearLayout.HORIZONTAL);

                PostItem leftItem = allPosts.get(i);
                View cardLeft = createProductCardView(leftItem, 8, 0);
                rowLayout.addView(cardLeft);

                if (i + 1 < allPosts.size()) {
                    PostItem rightItem = allPosts.get(i + 1);
                    View cardRight = createProductCardView(rightItem, 0, 8);
                    rowLayout.addView(cardRight);
                } else {
                    View emptyView = new View(this);
                    LinearLayout.LayoutParams emptyParams = new LinearLayout.LayoutParams(0, 0, 1.0f);
                    emptyView.setLayoutParams(emptyParams);
                    rowLayout.addView(emptyView);
                }

                layoutMyPostsGridContainer.addView(rowLayout);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private View createProductCardView(PostItem item, int marginRight, int marginLeft) {
        LinearLayout card = new LinearLayout(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f
        );
        cardParams.setMargins(dpToPx(marginRight), 0, dpToPx(marginLeft), 0);
        card.setLayoutParams(cardParams);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(0xFFFFFFFF);
        card.setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10));
        card.setElevation(dpToPx(1));

        TextView tvBadge = new TextView(this);
        tvBadge.setText(item.type);
        tvBadge.setTextSize(9);
        tvBadge.setTextColor(0xFFFFFFFF);
        tvBadge.setPadding(dpToPx(6), dpToPx(2), dpToPx(6), dpToPx(2));
        tvBadge.getPaint().setFakeBoldText(true);
        if (item.type.equals("ขาย")) {
            tvBadge.setBackgroundColor(0xFF00794C);
        } else {
            tvBadge.setBackgroundColor(0xFFD97706);
        }
        card.addView(tvBadge);

        if (item.imageUri != null && !item.imageUri.isEmpty()) {
            ImageView img = new ImageView(this);
            LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(80)
            );
            imgParams.setMargins(0, dpToPx(6), 0, 0);
            img.setLayoutParams(imgParams);
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            safelySetImageUri(img, item.imageUri);
            card.addView(img);
        } else {
            View viewImg = new View(this);
            LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(80)
            );
            viewParams.setMargins(0, dpToPx(6), 0, 0);
            viewImg.setLayoutParams(viewParams);
            viewImg.setBackgroundColor(item.type.equals("ขาย") ? 0xFFE9ECEF : 0xFF343A40);
            card.addView(viewImg);
        }

        TextView tvTitle = new TextView(this);
        tvTitle.setText(item.title != null ? item.title : "");
        tvTitle.setTextSize(12);
        tvTitle.setTextColor(0xFF333333);
        tvTitle.setMaxLines(2);
        tvTitle.setEllipsize(TextUtils.TruncateAt.END);
        tvTitle.getPaint().setFakeBoldText(true);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(0, dpToPx(6), 0, 0);
        tvTitle.setLayoutParams(titleParams);
        card.addView(tvTitle);

        TextView tvPrice = new TextView(this);
        tvPrice.setText(item.priceOrBudget != null ? item.priceOrBudget : "");
        tvPrice.setTextSize(12);
        tvPrice.setTextColor(0xFF00794C);
        tvPrice.getPaint().setFakeBoldText(true);
        LinearLayout.LayoutParams priceParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        priceParams.setMargins(0, dpToPx(4), 0, 0);
        tvPrice.setLayoutParams(priceParams);
        card.addView(tvPrice);

        TextView btnDelete = new TextView(this);
        btnDelete.setText("🗑️ ลบประกาศ");
        btnDelete.setTextSize(11);
        btnDelete.setTextColor(0xFFDC2626);
        btnDelete.setGravity(android.view.Gravity.CENTER);
        btnDelete.setBackgroundColor(0xFFFFF5F5);
        btnDelete.setPadding(dpToPx(4), dpToPx(6), dpToPx(4), dpToPx(6));
        btnDelete.getPaint().setFakeBoldText(true);

        LinearLayout.LayoutParams delParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        delParams.setMargins(0, dpToPx(8), 0, 0);
        btnDelete.setLayoutParams(delParams);
        btnDelete.setOnClickListener(v -> deletePost(item.sourceBoard, item.originalIndex));
        card.addView(btnDelete);

        return card;
    }

    private void safelySetImageUri(ImageView imageView, String uriStr) {
        try {
            if (uriStr.startsWith("file://")) {
                Uri uri = Uri.parse(uriStr);
                File imgFile = new File(uri.getPath());
                if (imgFile.exists()) {
                    imageView.setImageURI(uri);
                } else {
                    imageView.setBackgroundColor(0xFFE9ECEF);
                }
            } else {
                Uri uri = Uri.parse(uriStr);
                try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
                    if (inputStream != null) {
                        imageView.setImageURI(uri);
                    } else {
                        imageView.setBackgroundColor(0xFFE9ECEF);
                    }
                } catch (Exception e) {
                    imageView.setBackgroundColor(0xFFE9ECEF);
                }
            }
        } catch (Exception e) {
            imageView.setBackgroundColor(0xFFE9ECEF);
            e.printStackTrace();
        }
    }

    private void deletePost(String sourceBoard, int index) {
        try {
            if (sourceBoard.equals("SWAP")) {
                SharedPreferences prefs = getSharedPreferences("PSRU_SWAP_PRODUCTS", MODE_PRIVATE);
                JSONArray arr = new JSONArray(prefs.getString("product_list", "[]"));
                JSONArray newArr = new JSONArray();
                for (int i = 0; i < arr.length(); i++) {
                    if (i != index) newArr.put(arr.getJSONObject(i));
                }
                prefs.edit().putString("product_list", newArr.toString()).apply();
            } else if (sourceBoard.equals("WANTED")) {
                SharedPreferences prefs = getSharedPreferences("PSRU_WANTED_PRODUCTS", MODE_PRIVATE);
                JSONArray arr = new JSONArray(prefs.getString("product_list", "[]"));
                JSONArray newArr = new JSONArray();
                for (int i = 0; i < arr.length(); i++) {
                    if (i != index) newArr.put(arr.getJSONObject(i));
                }
                prefs.edit().putString("product_list", newArr.toString()).apply();
            }

            Toast.makeText(this, "ลบประกาศเรียบร้อยแล้ว", Toast.LENGTH_SHORT).show();
            loadAndDisplayMyPostsGrid();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void setupBottomNavigation() {
        try {
            LinearLayout navMarket = findViewById(R.id.navMarket);
            if (navMarket != null) {
                navMarket.setOnClickListener(v -> {
                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                });
            }

            LinearLayout navFavorite = findViewById(R.id.navFavorite);
            if (navFavorite != null) {
                navFavorite.setOnClickListener(v -> {
                    try {
                        startActivity(new Intent(ProfileActivity.this, FavoriteActivity.class));
                    } catch (Exception e) {
                        Toast.makeText(this, "ยังไม่ได้สร้างหน้า FavoriteActivity", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            LinearLayout navProfile = findViewById(R.id.navProfile);
            if (navProfile != null) {
                navProfile.setOnClickListener(v -> {
                    // อยู่หน้าโปรไฟล์อยู่แล้ว
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}