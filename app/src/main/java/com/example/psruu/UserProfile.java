package com.example.psruu;

public class UserProfile {
    private String name;
    private String studentId;
    private String imageUri;
    private String facebook;
    private String instagram;
    private String phone;

    public UserProfile(String name, String studentId, String imageUri, String facebook, String instagram, String phone) {
        this.name = name;
        this.studentId = studentId;
        this.imageUri = imageUri;
        this.facebook = facebook;
        this.instagram = instagram;
        this.phone = phone;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getImageUri() { return imageUri; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }

    public String getFacebook() { return facebook; }
    public void setFacebook(String facebook) { this.facebook = facebook; }

    public String getInstagram() { return instagram; }
    public void setInstagram(String instagram) { this.instagram = instagram; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}