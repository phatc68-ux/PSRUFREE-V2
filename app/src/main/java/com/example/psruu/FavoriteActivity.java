package com.example.psruu;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

public class FavoriteActivity extends AppCompatActivity {

    private LinearLayout containerFavoriteProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        containerFavoriteProducts = findViewById(R.id.containerFavoriteProducts);

        // ==========================================
        // เชื่อมโยงปุ่มเมนูด้านล่าง (ตัด navChat ออกแล้ว)
        // ==========================================
        LinearLayout navMarket = findViewById(R.id.navMarket);
        LinearLayout navFavorite = findViewById(R.id.navFavorite);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        if (navMarket != null) {
            navMarket.setOnClickListener(v -> {
                // กลับไปหน้าหลัก (MainActivity)
                Intent intent = new Intent(FavoriteActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        if (navFavorite != null) {
            navFavorite.setOnClickListener(v -> {
                // อยู่หน้าโปรดอยู่แล้ว ไม่ต้องทำอะไร
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                // ไปหน้าโปรไฟล์
                Intent intent = new Intent(FavoriteActivity.this, ProfileActivity.class);
                startActivity(intent);
            });
        }

        loadFavoriteProducts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavoriteProducts(); // โหลดข้อมูลใหม่ทุกครั้งที่กลับมาหน้านี้
    }

    private void loadFavoriteProducts() {
        if (containerFavoriteProducts != null) {
            containerFavoriteProducts.removeAllViews();
        }

        SharedPreferences prefs = getSharedPreferences("PSRU_FAVORITE_PREF", MODE_PRIVATE);
        String favJson = prefs.getString("favorite_list", "[]");

        try {
            JSONArray jsonArray = new JSONArray(favJson);

            if (jsonArray.length() == 0) {
                TextView tvEmpty = new TextView(this);
                tvEmpty.setText("ยังไม่มีรายการโปรดที่บันทึกไว้ 📭");
                tvEmpty.setTextColor(Color.parseColor("#6C757D"));
                tvEmpty.setTextSize(14);
                tvEmpty.setPadding(16, 16, 16, 16);
                containerFavoriteProducts.addView(tvEmpty);
                return;
            }

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.optJSONObject(i);
                if (obj == null) continue;

                String name = obj.optString("name", "ไม่มีชื่อสินค้า");
                String price = obj.optString("price", "฿0");
                String category = obj.optString("category", "");
                String imageUriStr = obj.optString("image", "");
                String detail = obj.optString("detail", "ไม่มีรายละเอียดเพิ่มเติม");
                String location = obj.optString("location", "ม.ราชภัฏพิบูลสงคราม");

                final int index = i;

                // สร้าง Card แสดงสินค้า
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

                // เมื่อคลิกที่การ์ด จะมี Dialog ถามว่าต้องการลบออกจากรายการโปรดไหม
                cardLayout.setOnClickListener(v -> {
                    new AlertDialog.Builder(FavoriteActivity.this)
                            .setTitle("จัดการรายการโปรด")
                            .setMessage("คุณต้องการลบ \"" + name + "\" ออกจากรายการโปรดใช่หรือไม่?")
                            .setPositiveButton("ลบออก", (dialog, which) -> {
                                removeFavoriteItem(index);
                            })
                            .setNegativeButton("ยกเลิก", null)
                            .show();
                });

                // รูปภาพสินค้า
                ImageView ivProduct = new ImageView(this);
                int imgSizePx = (int) (80 * getResources().getDisplayMetrics().density);
                LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(imgSizePx, imgSizePx);
                ivProduct.setLayoutParams(imgParams);
                ivProduct.setScaleType(ImageView.ScaleType.CENTER_CROP);

                if (!imageUriStr.isEmpty()) {
                    try {
                        ivProduct.setImageURI(Uri.parse(imageUriStr));
                    } catch (Exception e) {
                        ivProduct.setBackgroundColor(Color.parseColor("#CED4DA"));
                    }
                } else {
                    ivProduct.setBackgroundColor(Color.parseColor("#CED4DA"));
                }

                // ข้อมูลสินค้า (ชื่อ, ราคา, หมวดหมู่)
                LinearLayout infoLayout = new LinearLayout(this);
                LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                infoParams.setMargins(12, 0, 0, 0);
                infoLayout.setLayoutParams(infoParams);
                infoLayout.setOrientation(LinearLayout.VERTICAL);

                TextView tvCategory = new TextView(this);
                tvCategory.setText(category + " · PSRU SWAP");
                tvCategory.setTextColor(Color.parseColor("#00794C"));
                tvCategory.setTextSize(10);

                TextView tvName = new TextView(this);
                tvName.setText(name);
                tvName.setTextColor(Color.parseColor("#212529"));
                tvName.setTextSize(14);
                tvName.setTypeface(null, android.graphics.Typeface.BOLD);

                TextView tvPrice = new TextView(this);
                tvPrice.setText(price);
                tvPrice.setTextColor(Color.parseColor("#00794C"));
                tvPrice.setTextSize(13);
                tvPrice.setTypeface(null, android.graphics.Typeface.BOLD);

                infoLayout.addView(tvCategory);
                infoLayout.addView(tvName);
                infoLayout.addView(tvPrice);

                cardLayout.addView(ivProduct);
                cardLayout.addView(infoLayout);

                containerFavoriteProducts.addView(cardLayout);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeFavoriteItem(int position) {
        SharedPreferences prefs = getSharedPreferences("PSRU_FAVORITE_PREF", MODE_PRIVATE);
        String favJson = prefs.getString("favorite_list", "[]");

        try {
            JSONArray jsonArray = new JSONArray(favJson);
            JSONArray newArray = new JSONArray();

            for (int i = 0; i < jsonArray.length(); i++) {
                if (i != position) {
                    newArray.put(jsonArray.getJSONObject(i));
                }
            }

            prefs.edit().putString("favorite_list", newArray.toString()).apply();
            loadFavoriteProducts();
            Toast.makeText(this, "ลบออกจากรายการโปรดแล้ว", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}