package com.example.psruu;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FavoriteRepository {
    private SharedPreferences prefs;

    public FavoriteRepository(Context context) {
        prefs = context.getSharedPreferences("PSRU_FAVORITE_PREF", Context.MODE_PRIVATE);
    }

    public List<Product> getFavoriteProducts() {
        List<Product> productList = new ArrayList<>();
        String favJson = prefs.getString("favorite_list", "[]");

        try {
            JSONArray jsonArray = new JSONArray(favJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.optJSONObject(i);
                if (obj == null) continue;

                Product product = new Product(
                        obj.optString("name"),
                        obj.optString("price"),
                        obj.optString("category"),
                        obj.optString("image"),
                        obj.optString("detail"),
                        obj.optString("location"),
                        obj.optInt("postType", 0),
                        obj.optString("sellerName", "ชื่อผู้ขาย"),
                        obj.optString("sellerFacebook", "-"),
                        obj.optString("sellerInstagram", "-"),
                        obj.optString("sellerPhone", "-"),
                        obj.optString("sellerProfileImage", "") // 👈 เพิ่มพารามิเตอร์ตัวที่ 12 ตรงนี้
                );
                productList.add(product);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return productList;
    }

    // เพิ่มเมธอดนี้เข้าไปเพื่อรองรับการลบรายการโปรดตามตำแหน่ง (index)
    public void removeFavoriteItem(int index) {
        String favJson = prefs.getString("favorite_list", "[]");
        try {
            JSONArray jsonArray = new JSONArray(favJson);
            JSONArray newArray = new JSONArray();

            for (int i = 0; i < jsonArray.length(); i++) {
                if (i != index) {
                    newArray.put(jsonArray.get(i));
                }
            }

            prefs.edit().putString("favorite_list", newArray.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}