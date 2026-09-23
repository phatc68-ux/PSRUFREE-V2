package com.example.psruu;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private LinearLayout layoutMyPostsGridContainer;
    private TextView tvPostCount, tvReviewCount, tvReviewHistoryCount;
    private TextView tvProfileFacebook, tvProfileInstagram, tvProfilePhone;
    private LinearLayout btnReviewHistory;

    private UserRepository userRepository;
    private ProfileRepository profileRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userRepository = new UserRepository(this);
        profileRepository = new ProfileRepository(this);

        try {
            layoutMyPostsGridContainer = findViewById(R.id.layoutMyPostsGridContainer);
            tvPostCount = findViewById(R.id.tvPostCount);
            tvReviewCount = findViewById(R.id.tvReviewCount);
            tvReviewHistoryCount = findViewById(R.id.tvReviewHistoryCount);

            tvProfileFacebook = findViewById(R.id.tvProfileFacebook);
            tvProfileInstagram = findViewById(R.id.tvProfileInstagram);
            tvProfilePhone = findViewById(R.id.tvProfilePhone);

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

    public static void submitNewRating(Context context, float newRating) {
        UserRepository repo = new UserRepository(context);
        repo.saveAverageRating(newRating);
    }

    private void loadReviewCount() {
        try {
            int totalReviews = profileRepository.getReviewCount();
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

            List<ReviewItem> reviewList = profileRepository.getReviewList();

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
                    ReviewItem data = reviewList.get(position);

                    TextView tvName = convertView.findViewById(R.id.tvReviewerName);
                    TextView tvRating = convertView.findViewById(R.id.tvReviewRating);
                    TextView tvComment = convertView.findViewById(R.id.tvReviewComment);
                    TextView tvDate = convertView.findViewById(R.id.tvReviewDate);

                    if (tvName != null) tvName.setText(data.getReviewer());
                    if (tvRating != null) tvRating.setText("⭐ (" + data.getRating() + ")");
                    if (tvComment != null) tvComment.setText(data.getComment());
                    if (tvDate != null) tvDate.setText(data.getDate());

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
        loadAndDisplayMyPostsGrid(); // โหลดรายการโพสต์ใหม่ทุกครั้งที่กลับมาที่หน้านี้
    }

    private void loadUserProfile() {
        try {
            UserProfile profile = userRepository.getUserProfile();

            TextView tvProfileName = findViewById(R.id.tvProfileName);
            if (tvProfileName != null) tvProfileName.setText(profile.getName());

            TextView tvProfileStudentId = findViewById(R.id.tvProfileStudentId);
            if (tvProfileStudentId != null) tvProfileStudentId.setText("รหัสนักศึกษา: " + profile.getStudentId());

            if (tvProfileFacebook != null) tvProfileFacebook.setText(profile.getFacebook());
            if (tvProfileInstagram != null) tvProfileInstagram.setText(profile.getInstagram());
            if (tvProfilePhone != null) tvProfilePhone.setText(profile.getPhone());

            ImageView ivProfileImage = findViewById(R.id.ivProfileImage);
            if (ivProfileImage != null && profile.getImageUri() != null && !profile.getImageUri().isEmpty()) {
                safelySetImageUri(ivProfileImage, profile.getImageUri());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadRatingData() {
        try {
            float averageRating = userRepository.getAverageRating();
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

            List<PostItem> allPosts = profileRepository.getAllMyPosts();

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
        tvBadge.setText(item.getType());
        tvBadge.setTextSize(9);
        tvBadge.setTextColor(0xFFFFFFFF);
        tvBadge.setPadding(dpToPx(6), dpToPx(2), dpToPx(6), dpToPx(2));
        tvBadge.getPaint().setFakeBoldText(true);
        if (item.getType().equals("ขาย")) {
            tvBadge.setBackgroundColor(0xFF00794C);
        } else {
            tvBadge.setBackgroundColor(0xFFD97706);
        }
        card.addView(tvBadge);

        if (item.getImageUri() != null && !item.getImageUri().isEmpty()) {
            ImageView img = new ImageView(this);
            LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(80)
            );
            imgParams.setMargins(0, dpToPx(6), 0, 0);
            img.setLayoutParams(imgParams);

            img.setScaleType(ImageView.ScaleType.FIT_CENTER);
            img.setBackgroundColor(0xFFF8F9FA);

            safelySetImageUri(img, item.getImageUri());
            card.addView(img);
        } else {
            View viewImg = new View(this);
            LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(80)
            );
            viewParams.setMargins(0, dpToPx(6), 0, 0);
            viewImg.setLayoutParams(viewParams);
            viewImg.setBackgroundColor(item.getType().equals("ขาย") ? 0xFFE9ECEF : 0xFF343A40);
            card.addView(viewImg);
        }

        TextView tvTitle = new TextView(this);
        tvTitle.setText(item.getTitle() != null ? item.getTitle() : "");
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
        tvPrice.setText(item.getPriceOrBudget() != null ? item.getPriceOrBudget() : "");
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
        btnDelete.setOnClickListener(v -> deletePost(item.getSourceBoard(), item.getOriginalIndex()));
        card.addView(btnDelete);

        return card;
    }

    private void safelySetImageUri(ImageView imageView, String uriStr) {
        try {
            if (uriStr == null || uriStr.isEmpty()) {
                imageView.setBackgroundColor(0xFFE9ECEF);
                return;
            }

            if (!uriStr.startsWith("file://") && !uriStr.startsWith("content://") && !uriStr.startsWith("http")) {
                try {
                    byte[] decodedString = Base64.decode(uriStr, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

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
            profileRepository.deletePost(sourceBoard, index);
            Toast.makeText(this, "ลบประกาศเรียบร้อยแล้ว", Toast.LENGTH_SHORT).show();

            // รีเฟรชข้อมูลและอัปเดตหน้าจอทันทีหลังจากลบสำเร็จ
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