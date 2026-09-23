package com.example.psruu;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProductRepository {

    private static final String PREF_NAME = "PSRU_PRODUCTS_PREF";
    private static final String KEY_PRODUCTS = "product_list";
    private static final String KEY_WANTED = "wanted_list";

    private SharedPreferences prefs;

    public ProductRepository(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveProduct(Product product, boolean isWanted) {
        List<Product> currentList = getProducts(isWanted, 0, 0);
        currentList.add(0, product);
        saveListToPrefs(currentList, isWanted);
    }

    private void saveListToPrefs(List<Product> list, boolean isWanted) {
        JSONArray jsonArray = new JSONArray();
        for (Product p : list) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("name", p.getName());
                obj.put("price", p.getPrice());
                obj.put("category", p.getCategory());
                obj.put("imageUri", p.getImageUri());
                obj.put("detail", p.getDetail());
                obj.put("location", p.getLocation());
                obj.put("postType", p.getPostType());

                obj.put("sellerName", p.getSellerName());
                obj.put("sellerFacebook", p.getSellerFacebook());
                obj.put("sellerInstagram", p.getSellerInstagram());
                obj.put("sellerPhone", p.getSellerPhone());
                obj.put("sellerProfileImage", p.getSellerProfileImage());

                jsonArray.put(obj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String key = isWanted ? KEY_WANTED : KEY_PRODUCTS;
        prefs.edit().putString(key, jsonArray.toString()).apply();
    }

    public List<Product> getProducts(boolean isWanted, int categoryFilter, int postTypeFilter) {
        List<Product> list = new ArrayList<>();
        String key = isWanted ? KEY_WANTED : KEY_PRODUCTS;
        String jsonString = prefs.getString(key, "[]");

        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String name = obj.optString("name", "");
                String price = obj.optString("price", "");
                String category = obj.optString("category", "");
                String imageUri = obj.optString("imageUri", "");
                String detail = obj.optString("detail", "");
                String location = obj.optString("location", "");
                int postType = obj.optInt("postType", 0);

                String sellerName = obj.optString("sellerName", "ชื่อผู้ขาย");
                String sellerFacebook = obj.optString("sellerFacebook", "-");
                String sellerInstagram = obj.optString("sellerInstagram", "-");
                String sellerPhone = obj.optString("sellerPhone", "-");
                String sellerProfileImage = obj.optString("sellerProfileImage", "");

                // 1. เงื่อนไขกรองตามหมวดหมู่ (Category Filter)
                if (categoryFilter > 0) {
                    String targetCat = "";
                    if (categoryFilter == 1) targetCat = "หนังสือเรียน";
                    else if (categoryFilter == 2) targetCat = "อุปกรณ์เรียน";
                    else if (categoryFilter == 3) targetCat = "ไอที";
                    else if (categoryFilter == 4) targetCat = "เสื้อผ้า";

                    if (!category.contains(targetCat)) {
                        continue; // ข้ามข้อมูลที่ไม่ตรงหมวดหมู่
                    }
                }

                // 2. เงื่อนไขกรองตามประเภทประกาศย่อย (ขาย, แลก, ให้ฟรี) เฉพาะในหน้าตลาดสินค้า
                if (!isWanted && postTypeFilter > 0) {
                    int targetPostType = postTypeFilter - 1; // 1->0 (ขาย), 2->1 (แลก), 3->2 (ให้ฟรี)
                    if (postType != targetPostType) {
                        continue; // ข้ามข้อมูลที่ไม่ตรงประเภท
                    }
                }

                Product p = new Product(
                        name, price, category, imageUri, detail, location, postType,
                        sellerName, sellerFacebook, sellerInstagram, sellerPhone,
                        sellerProfileImage
                );
                list.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void deleteProduct(boolean isWanted, int index) {
        try {
            // ดึงรายการปัจจุบันขึ้นมาทั้งหมดก่อน (ไม่ใช้ตัวกรอง เพื่อให้ index ตรงกับข้อมูลจริงใน SharedPreferences)
            List<Product> currentList = getProducts(isWanted, 0, 0);

            // ตรวจสอบความถูกต้องของ Index
            if (index >= 0 && index < currentList.size()) {
                currentList.remove(index); // ลบรายการออกจาก List
                saveListToPrefs(currentList, isWanted); // บันทึกรายการใหม่ลง SharedPreferences ทันที
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}