package com.example.psruu;

public class Product {
    private String name;
    private String price;
    private String category;
    private String imageUri;
    private String detail;
    private String location;
    private int postType;
    private String sellerName;
    private String sellerFacebook;
    private String sellerInstagram;
    private String sellerPhone;
    private String sellerProfileImage;

    public Product(String name, String price, String category, String imageUri,
                   String detail, String location, int postType,
                   String sellerName, String sellerFacebook, String sellerInstagram,
                   String sellerPhone, String sellerProfileImage) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.imageUri = imageUri;
        this.detail = detail;
        this.location = location;
        this.postType = postType;
        this.sellerName = sellerName;
        this.sellerFacebook = sellerFacebook;
        this.sellerInstagram = sellerInstagram;
        this.sellerPhone = sellerPhone;
        this.sellerProfileImage = sellerProfileImage;
    }

    public String getName() { return name; }
    public String getPrice() { return price; }
    public String getCategory() { return category; }
    public String getImageUri() { return imageUri; }
    public String getDetail() { return detail; }
    public String getLocation() { return location; }
    public int getPostType() { return postType; }
    public String getSellerName() { return sellerName; }
    public String getSellerFacebook() { return sellerFacebook; }
    public String getSellerInstagram() { return sellerInstagram; }
    public String getSellerPhone() { return sellerPhone; }
    public String getSellerProfileImage() { return sellerProfileImage; }
}