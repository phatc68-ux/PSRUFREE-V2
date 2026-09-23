package com.example.psruu;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class FavoriteActivity extends AppCompatActivity {

    private LinearLayout containerFavoriteProducts;
    private FavoriteRepository favoriteRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        containerFavoriteProducts = findViewById(R.id.containerFavoriteProducts);
        favoriteRepository = new FavoriteRepository(this);

        setupBottomNavigation();
        loadFavoriteProducts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavoriteProducts();
    }

    private void setupBottomNavigation() {
        LinearLayout navMarket = findViewById(R.id.navMarket);
        LinearLayout navFavorite = findViewById(R.id.navFavorite);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        if (navMarket != null) {
            navMarket.setOnClickListener(v -> {
                Intent intent = new Intent(FavoriteActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        if (navFavorite != null) {
            navFavorite.setOnClickListener(v -> {
                // อยู่หน้าโปรดอยู่แล้ว
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(FavoriteActivity.this, ProfileActivity.class);
                startActivity(intent);
            });
        }
    }

    private void loadFavoriteProducts() {
        if (containerFavoriteProducts != null) {
            containerFavoriteProducts.removeAllViews();
        }

        List<Product> productList = favoriteRepository.getFavoriteProducts();

        if (productList.isEmpty()) {
            renderEmptyView();
            return;
        }

        for (int i = 0; i < productList.size(); i++) {
            Product product = productList.get(i);
            final int index = i;
            renderProductCard(product, index);
        }
    }

    private void renderEmptyView() {
        TextView tvEmpty = new TextView(this);
        tvEmpty.setText("ยังไม่มีรายการโปรดที่บันทึกไว้ 📭");
        tvEmpty.setTextColor(Color.parseColor("#6C757D"));
        tvEmpty.setTextSize(14);
        tvEmpty.setPadding(16, 16, 16, 16);
        containerFavoriteProducts.addView(tvEmpty);
    }

    private void renderProductCard(Product product, int index) {
        LinearLayout cardLayout = createCardLayout();

        cardLayout.setOnClickListener(v -> {
            new AlertDialog.Builder(FavoriteActivity.this)
                    .setTitle("จัดการรายการโปรด")
                    .setMessage("คุณต้องการลบ \"" + product.getName() + "\" ออกจากรายการโปรดใช่หรือไม่?")
                    .setPositiveButton("ลบออก", (dialog, which) -> {
                        favoriteRepository.removeFavoriteItem(index);
                        loadFavoriteProducts();
                        Toast.makeText(this, "ลบออกจากรายการโปรดแล้ว", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("ยกเลิก", null)
                    .show();
        });

        ImageView ivProduct = createProductImageView(product.getImageUri());
        LinearLayout infoLayout = createProductInfoLayout(product);

        cardLayout.addView(ivProduct);
        cardLayout.addView(infoLayout);
        containerFavoriteProducts.addView(cardLayout);
    }

    private LinearLayout createCardLayout() {
        LinearLayout cardLayout = new LinearLayout(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 12);
        cardLayout.setLayoutParams(cardParams);
        cardLayout.setOrientation(LinearLayout.HORIZONTAL);
        cardLayout.setBackgroundColor(Color.WHITE);
        cardLayout.setPadding(12, 12, 12, 12);
        cardLayout.setElevation(2f);
        cardLayout.setClickable(true);
        cardLayout.setFocusable(true);
        return cardLayout;
    }

    private ImageView createProductImageView(String imageUriStr) {
        ImageView ivProduct = new ImageView(this);
        int imgSizePx = (int) (80 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(imgSizePx, imgSizePx);
        ivProduct.setLayoutParams(imgParams);
        ivProduct.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ivProduct.setBackgroundColor(Color.parseColor("#CED4DA"));

        // แก้ไขให้รองรับการถอดรหัส Base64 String ให้แสดงรูปภาพได้อย่างถูกต้อง
        if (imageUriStr != null && !imageUriStr.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(imageUriStr, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                if (bitmap != null) {
                    ivProduct.setImageBitmap(bitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ivProduct;
    }

    private LinearLayout createProductInfoLayout(Product product) {
        LinearLayout infoLayout = new LinearLayout(this);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        infoParams.setMargins(12, 0, 0, 0);
        infoLayout.setLayoutParams(infoParams);
        infoLayout.setOrientation(LinearLayout.VERTICAL);

        TextView tvCategory = new TextView(this);
        tvCategory.setText(product.getCategory() + " · PSRU SWAP");
        tvCategory.setTextColor(Color.parseColor("#00794C"));
        tvCategory.setTextSize(10);

        TextView tvName = new TextView(this);
        tvName.setText(product.getName());
        tvName.setTextColor(Color.parseColor("#212529"));
        tvName.setTextSize(14);
        tvName.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView tvPrice = new TextView(this);
        tvPrice.setText(product.getPrice());
        tvPrice.setTextColor(Color.parseColor("#00794C"));
        tvPrice.setTextSize(13);
        tvPrice.setTypeface(null, android.graphics.Typeface.BOLD);

        infoLayout.addView(tvCategory);
        infoLayout.addView(tvName);
        infoLayout.addView(tvPrice);

        return infoLayout;
    }
}