package com.example.psruu;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private TextView btnOpenPost;
    private LinearLayout containerProducts;
    private TextView tabMarketplace, tabWantedBoard, tvSectionTitle;
    private TextView catAll, catBook, catEquipment, catIt;
    private TextView typeAll, typeSell, typeSwap, typeFree;

    private ImageView ivPreviewImage;
    private LinearLayout layoutPlaceholderImage;
    private String selectedImageUriStr = "";
    private Dialog currentDialog;
    private LinearLayout currentRowLayout = null;

    private boolean isWantedTab = false;
    private int selectedCategoryIndex = 0;
    private int selectedPostTypeFilter = 0;
    private int selectedPostTypeIndex = 0;

    private ProductRepository productRepository;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        productRepository = new ProductRepository(this);
        userRepository = new UserRepository(this);

        initViews();
        initListeners();
        loadSavedProducts();
        updatePostTypeFilterUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSavedProducts();
    }

    private void initViews() {
        btnOpenPost = findViewById(R.id.btnOpenPost);
        containerProducts = findViewById(R.id.containerProducts);
        tabMarketplace = findViewById(R.id.tabMarketplace);
        tabWantedBoard = findViewById(R.id.tabWantedBoard);
        tvSectionTitle = findViewById(R.id.tvSectionTitle);

        catAll = findViewById(R.id.catAll);
        catBook = findViewById(R.id.catBook);
        catEquipment = findViewById(R.id.catEquipment);
        catIt = findViewById(R.id.catIt);

        typeAll = findViewById(R.id.typeAll);
        typeSell = findViewById(R.id.typeSell);
        typeSwap = findViewById(R.id.typeSwap);
        typeFree = findViewById(R.id.typeFree);
    }

    private void initListeners() {
        if (tabMarketplace != null && tabWantedBoard != null) {
            tabMarketplace.setOnClickListener(v -> switchTab(false));
            tabWantedBoard.setOnClickListener(v -> switchTab(true));
        }

        if (catAll != null) catAll.setOnClickListener(v -> filterCategory(0));
        if (catBook != null) catBook.setOnClickListener(v -> filterCategory(1));
        if (catEquipment != null) catEquipment.setOnClickListener(v -> filterCategory(2));
        if (catIt != null) catIt.setOnClickListener(v -> filterCategory(3));

        if (typeAll != null) typeAll.setOnClickListener(v -> filterPostType(0));
        if (typeSell != null) typeSell.setOnClickListener(v -> filterPostType(1));
        if (typeSwap != null) typeSwap.setOnClickListener(v -> filterPostType(2));
        if (typeFree != null) typeFree.setOnClickListener(v -> filterPostType(3));

        if (btnOpenPost != null) {
            btnOpenPost.setOnClickListener(v -> showPostItemDialog(isWantedTab ? 3 : 0));
        }

        View navFavorite = findViewById(R.id.navFavorite);
        View navProfile = findViewById(R.id.navProfile);

        if (navFavorite != null) {
            navFavorite.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, FavoriteActivity.class)));
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));
        }
    }

    private void switchTab(boolean wanted) {
        isWantedTab = wanted;
        int filterVisibility = wanted ? View.GONE : View.VISIBLE;

        if (typeAll != null) typeAll.setVisibility(filterVisibility);
        if (typeSell != null) typeSell.setVisibility(filterVisibility);
        if (typeSwap != null) typeSwap.setVisibility(filterVisibility);
        if (typeFree != null) typeFree.setVisibility(filterVisibility);

        if (isWantedTab) {
            if (tabMarketplace != null) {
                tabMarketplace.setBackgroundColor(Color.TRANSPARENT);
                tabMarketplace.setTextColor(Color.parseColor("#6C757D"));
            }
            if (tabWantedBoard != null) {
                tabWantedBoard.setBackgroundColor(Color.WHITE);
                tabWantedBoard.setTextColor(Color.parseColor("#00794C"));
            }
            if (tvSectionTitle != null) tvSectionTitle.setText("รายการประกาศตามหาของ");
        } else {
            if (tabMarketplace != null) {
                tabMarketplace.setBackgroundColor(Color.WHITE);
                tabMarketplace.setTextColor(Color.parseColor("#00794C"));
            }
            if (tabWantedBoard != null) {
                tabWantedBoard.setBackgroundColor(Color.TRANSPARENT);
                tabWantedBoard.setTextColor(Color.parseColor("#6C757D"));
            }
            if (tvSectionTitle != null) tvSectionTitle.setText("รายการสินค้า (ขาย / แลก / ให้ฟรี)");
        }

        loadSavedProducts();
    }

    private void filterCategory(int index) {
        selectedCategoryIndex = index;
        updateCategoryButtonUI();
        loadSavedProducts();
    }

    private void filterPostType(int typeIndex) {
        selectedPostTypeFilter = typeIndex;
        updatePostTypeFilterUI();
        loadSavedProducts(); // โหลดข้อมูลใหม่ทันทีที่เปลี่ยนตัวกรองประเภท
    }

    private void updateCategoryButtonUI() {
        TextView[] buttons = {catAll, catBook, catEquipment, catIt};
        String[] texts = {"🔥 ทั้งหมด", "📖 หนังสือเรียน", "✏️ อุปกรณ์เรียน", "💻 ไอที/หูฟัง"};

        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i] != null) {
                buttons[i].setText(texts[i]);
                if (i == selectedCategoryIndex) {
                    buttons[i].setBackgroundColor(Color.parseColor("#00794C"));
                    buttons[i].setTextColor(Color.WHITE);
                } else {
                    buttons[i].setBackgroundColor(Color.parseColor("#FFFFFF"));
                    buttons[i].setTextColor(Color.parseColor("#495057"));
                }
            }
        }
    }

    private void updatePostTypeFilterUI() {
        if (typeAll == null || typeSell == null || typeSwap == null || typeFree == null) return;

        typeAll.setBackgroundColor(Color.parseColor(selectedPostTypeFilter == 0 ? "#212529" : "#E9ECEF"));
        typeAll.setTextColor(Color.parseColor(selectedPostTypeFilter == 0 ? "#FFFFFF" : "#495057"));

        typeSell.setBackgroundColor(Color.parseColor(selectedPostTypeFilter == 1 ? "#00794C" : "#D1E7DD"));
        typeSell.setTextColor(Color.parseColor(selectedPostTypeFilter == 1 ? "#FFFFFF" : "#00794C"));

        typeSwap.setBackgroundColor(Color.parseColor(selectedPostTypeFilter == 2 ? "#495057" : "#E2E3E5"));
        typeSwap.setTextColor(Color.parseColor(selectedPostTypeFilter == 2 ? "#FFFFFF" : "#495057"));

        typeFree.setBackgroundColor(Color.parseColor(selectedPostTypeFilter == 3 ? "#842029" : "#F8D7DA"));
        typeFree.setTextColor(Color.parseColor(selectedPostTypeFilter == 3 ? "#FFFFFF" : "#842029"));
    }

    private void showPostItemDialog(int defaultTypeIndex) {
        currentDialog = new Dialog(MainActivity.this);
        currentDialog.setContentView(R.layout.dialog_post_item);

        if (currentDialog.getWindow() != null) {
            currentDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.92);
            currentDialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        EditText etProductName = currentDialog.findViewById(R.id.etProductName);
        EditText etProductPrice = currentDialog.findViewById(R.id.etProductPrice);
        EditText etProductDetail = currentDialog.findViewById(R.id.etProductDetail);
        EditText etLocation = currentDialog.findViewById(R.id.etLocation);
        Spinner spinnerCategory = currentDialog.findViewById(R.id.spinnerCategory);
        TextView btnClose = currentDialog.findViewById(R.id.btnClose);
        Button btnSubmitPost = currentDialog.findViewById(R.id.btnSubmitPost);

        LinearLayout btnSelectImage = currentDialog.findViewById(R.id.btnSelectImage);
        ivPreviewImage = currentDialog.findViewById(R.id.ivPreviewImage);
        layoutPlaceholderImage = currentDialog.findViewById(R.id.layoutPlaceholderImage);
        selectedImageUriStr = "";

        LinearLayout btnTypeSell = currentDialog.findViewById(R.id.btnTypeSell);
        LinearLayout btnTypeSwap = currentDialog.findViewById(R.id.btnTypeSwap);
        LinearLayout btnTypeFree = currentDialog.findViewById(R.id.btnTypeFree);
        LinearLayout btnTypeWanted = currentDialog.findViewById(R.id.btnTypeWanted);

        selectedPostTypeIndex = defaultTypeIndex;
        updatePostTypeUI(btnTypeSell, btnTypeSwap, btnTypeFree, btnTypeWanted);

        if (btnTypeSell != null) btnTypeSell.setOnClickListener(v -> { selectedPostTypeIndex = 0; updatePostTypeUI(btnTypeSell, btnTypeSwap, btnTypeFree, btnTypeWanted); });
        if (btnTypeSwap != null) btnTypeSwap.setOnClickListener(v -> { selectedPostTypeIndex = 1; updatePostTypeUI(btnTypeSell, btnTypeSwap, btnTypeFree, btnTypeWanted); });
        if (btnTypeFree != null) btnTypeFree.setOnClickListener(v -> { selectedPostTypeIndex = 2; updatePostTypeUI(btnTypeSell, btnTypeSwap, btnTypeFree, btnTypeWanted); });
        if (btnTypeWanted != null) btnTypeWanted.setOnClickListener(v -> { selectedPostTypeIndex = 3; updatePostTypeUI(btnTypeSell, btnTypeSwap, btnTypeFree, btnTypeWanted); });

        String[] categories = {"📖 หนังสือเรียน", "✏️ อุปกรณ์เรียน", "💻 ไอที/หูฟัง", "👕 เสื้อผ้า/เบ็ดเตล็ด"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerCategory.setAdapter(adapter);

        if (btnClose != null) btnClose.setOnClickListener(v -> currentDialog.dismiss());
        if (btnSelectImage != null) {
            btnSelectImage.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            });
        }

        if (btnSubmitPost != null) {
            btnSubmitPost.setOnClickListener(v -> {
                String name = etProductName.getText().toString().trim();
                String price = etProductPrice.getText().toString().trim();
                String detail = etProductDetail.getText().toString().trim();
                String location = etLocation.getText().toString().trim();
                String category = spinnerCategory.getSelectedItem().toString();

                if (name.isEmpty()) {
                    etProductName.setError("กรุณากรอกชื่อสินค้า");
                    return;
                }

                if (price.isEmpty()) price = "0";
                if (detail.isEmpty()) detail = "ไม่มีรายละเอียดเพิ่มเติม";
                if (location.isEmpty()) location = "ม.ราชภัฏพิบูลสงคราม";

                boolean isWanted = (selectedPostTypeIndex == 3);

                UserProfile profile = userRepository.getUserProfile();
                String sellerName = profile != null ? profile.getName() : "ชื่อผู้ขาย";
                String sellerFacebook = profile != null ? profile.getFacebook() : "-";
                String sellerInstagram = profile != null ? profile.getInstagram() : "-";
                String sellerPhone = profile != null ? profile.getPhone() : "-";
                String sellerProfileImage = profile != null ? profile.getImageUri() : "";

                Product newProduct = new Product(
                        name, "฿" + price, category, selectedImageUriStr, detail, location, selectedPostTypeIndex,
                        sellerName, sellerFacebook, sellerInstagram, sellerPhone, sellerProfileImage
                );

                productRepository.saveProduct(newProduct, isWanted);
                loadSavedProducts();

                Toast.makeText(MainActivity.this, "โพสต์ประกาศสำเร็จ! 🎉", Toast.LENGTH_SHORT).show();
                currentDialog.dismiss();
            });
        }

        currentDialog.show();
    }

    private void updatePostTypeUI(LinearLayout s, LinearLayout sw, LinearLayout f, LinearLayout w) {
        if (s == null || sw == null || f == null || w == null) return;
        s.setBackgroundColor(Color.parseColor(selectedPostTypeIndex == 0 ? "#D1E7DD" : "#FFFFFF"));
        sw.setBackgroundColor(Color.parseColor(selectedPostTypeIndex == 1 ? "#E2E3E5" : "#FFFFFF"));
        f.setBackgroundColor(Color.parseColor(selectedPostTypeIndex == 2 ? "#F8D7DA" : "#FFFFFF"));
        w.setBackgroundColor(Color.parseColor(selectedPostTypeIndex == 3 ? "#E8DAEF" : "#FFFFFF"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri sourceUri = data.getData();
            selectedImageUriStr = convertUriToBase64(sourceUri);

            if (!selectedImageUriStr.isEmpty() && ivPreviewImage != null && layoutPlaceholderImage != null) {
                try {
                    byte[] decodedString = Base64.decode(selectedImageUriStr, Base64.DEFAULT);
                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    ivPreviewImage.setImageBitmap(decodedBitmap);
                    ivPreviewImage.setVisibility(View.VISIBLE);
                    layoutPlaceholderImage.setVisibility(View.GONE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String convertUriToBase64(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return "";

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            int targetSize = 400;
            int width = options.outWidth;
            int height = options.outHeight;
            int inSampleSize = 1;

            if (width > targetSize || height > targetSize) {
                final int halfWidth = width / 2;
                final int halfHeight = height / 2;
                while ((halfWidth / inSampleSize) >= targetSize && (halfHeight / inSampleSize) >= targetSize) {
                    inSampleSize *= 2;
                }
            }

            inputStream = getContentResolver().openInputStream(uri);
            options.inJustDecodeBounds = false;
            options.inSampleSize = inSampleSize;
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            if (bitmap != null) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream);
                byte[] byteArray = outputStream.toByteArray();
                return Base64.encodeToString(byteArray, Base64.DEFAULT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private void loadSavedProducts() {
        if (containerProducts != null) containerProducts.removeAllViews();
        currentRowLayout = null;

        List<Product> productList = productRepository.getProducts(isWantedTab, selectedCategoryIndex, selectedPostTypeFilter);
        for (Product product : productList) {
            addProductCardUI(product);
        }
    }

    private void addProductCardUI(Product product) {
        if (currentRowLayout == null || currentRowLayout.getChildCount() >= 2) {
            currentRowLayout = new LinearLayout(this);
            currentRowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            currentRowLayout.setOrientation(LinearLayout.HORIZONTAL);
            ((LinearLayout.LayoutParams) currentRowLayout.getLayoutParams()).setMargins(0, 0, 0, 12);
            containerProducts.addView(currentRowLayout, 0);
        }

        View cardView = getLayoutInflater().inflate(R.layout.item_product, currentRowLayout, false);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        int childIndex = currentRowLayout.getChildCount();
        cardParams.setMargins(childIndex == 0 ? 0 : 6, 0, childIndex == 0 ? 6 : 0, 0);
        cardView.setLayoutParams(cardParams);

        ImageView ivProduct = cardView.findViewById(R.id.ivProduct);
        TextView tvBadge = cardView.findViewById(R.id.tvBadge);
        TextView tvCategory = cardView.findViewById(R.id.tvCategory);
        TextView tvName = cardView.findViewById(R.id.tvName);
        TextView tvPrice = cardView.findViewById(R.id.tvPrice);

        if (product.getImageUri() != null && !product.getImageUri().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(product.getImageUri(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                if (bitmap != null) {
                    ivProduct.setImageBitmap(bitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String badgeText = "ขาย";
        int badgeColor = Color.parseColor("#00794C");
        if (isWantedTab) {
            badgeText = "ตามหา";
            badgeColor = Color.parseColor("#6F42C1");
        } else {
            switch (product.getPostType()) {
                case 0: badgeText = "ขาย"; badgeColor = Color.parseColor("#00794C"); break;
                case 1: badgeText = "แลก"; badgeColor = Color.parseColor("#495057"); break;
                case 2: badgeText = "ให้ฟรี"; badgeColor = Color.parseColor("#842029"); break;
                case 3: badgeText = "ตามหา"; badgeColor = Color.parseColor("#6F42C1"); break;
            }
        }
        tvBadge.setText(badgeText);
        tvBadge.setBackgroundColor(badgeColor);

        tvCategory.setText(product.getCategory());
        tvName.setText(product.getName());
        tvPrice.setText(product.getPrice());

        cardView.setOnClickListener(v -> showProductDetailDialog(product));
        currentRowLayout.addView(cardView);
    }

    private void showProductDetailDialog(Product product) {
        Dialog detailDialog = new Dialog(MainActivity.this);
        detailDialog.setContentView(R.layout.dialog_product_detail);

        if (detailDialog.getWindow() != null) {
            detailDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.92);
            detailDialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        ImageView ivDetailProduct = detailDialog.findViewById(R.id.ivDetailProduct);
        ImageView ivSellerProfile = detailDialog.findViewById(R.id.ivSellerProfile);
        TextView tvDetailCategory = detailDialog.findViewById(R.id.tvDetailCategory);
        TextView tvDetailName = detailDialog.findViewById(R.id.tvDetailName);
        TextView tvDetailPrice = detailDialog.findViewById(R.id.tvDetailPrice);
        TextView tvDetailDescription = detailDialog.findViewById(R.id.tvDetailDescription);
        TextView tvDetailLocation = detailDialog.findViewById(R.id.tvDetailLocation);
        View btnCloseDetail = detailDialog.findViewById(R.id.btnClose);
        TextView btnFavorite = detailDialog.findViewById(R.id.ivFavorite);
        TextView btnRateSeller = detailDialog.findViewById(R.id.btnRateSeller);

        TextView tvDetailSellerName = detailDialog.findViewById(R.id.tvSellerName);
        TextView tvDetailFacebook = detailDialog.findViewById(R.id.tvContactFacebook);
        TextView tvDetailInstagram = detailDialog.findViewById(R.id.tvContactInstagram);
        TextView tvDetailPhone = detailDialog.findViewById(R.id.tvContactPhone);

        if (tvDetailName != null) tvDetailName.setText(product.getName());
        if (tvDetailPrice != null) tvDetailPrice.setText(product.getPrice());
        if (tvDetailCategory != null) tvDetailCategory.setText(product.getCategory() + " · PSRU SWAP");
        if (tvDetailDescription != null) tvDetailDescription.setText(product.getDetail());
        if (tvDetailLocation != null) tvDetailLocation.setText(product.getLocation());

        if (tvDetailSellerName != null) tvDetailSellerName.setText(product.getSellerName());
        if (tvDetailFacebook != null) tvDetailFacebook.setText("Facebook: " + product.getSellerFacebook());
        if (tvDetailInstagram != null) tvDetailInstagram.setText("Instagram: " + product.getSellerInstagram());
        if (tvDetailPhone != null) tvDetailPhone.setText("โทร: " + product.getSellerPhone());

        if (ivDetailProduct != null && product.getImageUri() != null && !product.getImageUri().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(product.getImageUri(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                if (bitmap != null) {
                    ivDetailProduct.setImageBitmap(bitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (ivSellerProfile != null) {
            try {
                UserProfile currentProfile = userRepository.getUserProfile();
                String profileImg = "";

                if (currentProfile != null && currentProfile.getImageUri() != null && !currentProfile.getImageUri().isEmpty()) {
                    profileImg = currentProfile.getImageUri();
                } else if (product.getSellerProfileImage() != null && !product.getSellerProfileImage().isEmpty()) {
                    profileImg = product.getSellerProfileImage();
                }

                if (!profileImg.isEmpty()) {
                    if (!profileImg.startsWith("file://") && !profileImg.startsWith("content://") && !profileImg.startsWith("http")) {
                        byte[] decodedString = Base64.decode(profileImg, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        if (bitmap != null) {
                            ivSellerProfile.setImageBitmap(bitmap);
                        }
                    } else {
                        ivSellerProfile.setImageURI(Uri.parse(profileImg));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (btnRateSeller != null) {
            btnRateSeller.setOnClickListener(v -> showRatingDialog(product.getSellerName()));
        }

        if (btnFavorite != null) {
            btnFavorite.setOnClickListener(v -> {
                try {
                    android.content.SharedPreferences prefs = getSharedPreferences("PSRU_FAVORITE_PREF", MODE_PRIVATE);
                    String favJson = prefs.getString("favorite_list", "[]");
                    org.json.JSONArray jsonArray = new org.json.JSONArray(favJson);
                    org.json.JSONObject newObj = new org.json.JSONObject();
                    newObj.put("name", product.getName());
                    newObj.put("price", product.getPrice());
                    newObj.put("category", product.getCategory());
                    newObj.put("image", product.getImageUri());
                    newObj.put("detail", product.getDetail());
                    newObj.put("location", product.getLocation());
                    jsonArray.put(newObj);
                    prefs.edit().putString("favorite_list", jsonArray.toString()).apply();
                    Toast.makeText(MainActivity.this, "บันทึกเข้ารายการโปรดแล้ว ❤️", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        if (btnCloseDetail != null) btnCloseDetail.setOnClickListener(v -> detailDialog.dismiss());
        detailDialog.show();
    }

    private void showRatingDialog(String sellerName) {
        Dialog ratingDialog = new Dialog(MainActivity.this);
        ratingDialog.setContentView(R.layout.dialog_rating);

        if (ratingDialog.getWindow() != null) {
            ratingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85);
            ratingDialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        RatingBar ratingBar = ratingDialog.findViewById(R.id.ratingBar);
        EditText etReviewComment = ratingDialog.findViewById(R.id.etReviewComment);
        Button btnSubmitRating = ratingDialog.findViewById(R.id.btnSubmitRating);

        if (btnSubmitRating != null) {
            btnSubmitRating.setOnClickListener(v -> {
                float ratingScore = ratingBar != null ? ratingBar.getRating() : 5.0f;
                String comment = etReviewComment != null ? etReviewComment.getText().toString().trim() : "";

                ProfileRepository profileRepository = new ProfileRepository(MainActivity.this);
                profileRepository.addReview("ผู้ใช้งานทั่วไป", ratingScore, comment, "วันนี้");

                Toast.makeText(MainActivity.this,
                        "ให้คะแนน " + sellerName + " " + (int)ratingScore + " ดาวสำเร็จ! ⭐",
                        Toast.LENGTH_SHORT).show();

                ratingDialog.dismiss();
            });
        }

        ratingDialog.show();
    }
}