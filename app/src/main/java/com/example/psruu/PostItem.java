package com.example.psruu;

public class PostItem {
    private String title;
    private String priceOrBudget;
    private String type;
    private String imageUri;
    private String sourceBoard;
    private int originalIndex;

    public PostItem(String title, String priceOrBudget, String type, String imageUri, String sourceBoard, int originalIndex) {
        this.title = title;
        this.priceOrBudget = priceOrBudget;
        this.type = type;
        this.imageUri = imageUri;
        this.sourceBoard = sourceBoard;
        this.originalIndex = originalIndex;
    }

    public String getTitle() { return title; }
    public String getPriceOrBudget() { return priceOrBudget; }
    public String getType() { return type; }
    public String getImageUri() { return imageUri; }
    public String getSourceBoard() { return sourceBoard; }
    public int getOriginalIndex() { return originalIndex; }
}