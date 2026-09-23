package com.example.psruu;

public class ReviewItem {
    private String reviewer;
    private String rating;
    private String comment;
    private String date;

    public ReviewItem(String reviewer, String rating, String comment, String date) {
        this.reviewer = reviewer;
        this.rating = rating;
        this.comment = comment;
        this.date = date;
    }

    public String getReviewer() { return reviewer; }
    public String getRating() { return rating; }
    public String getComment() { return comment; }
    public String getDate() { return date; }
}