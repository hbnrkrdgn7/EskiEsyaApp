package com.example.eskiesyasatis;

public class Listing {
    private String id;
    private String title;
    private String price;
    private String description;
    private String imageUriString;
    private boolean isFavorite;
    private String itemAge;
    private String dateShared;
    private String location;
    private String ownerId;
    private String ownerName;
    private String status = "active";
    private String category;
    private java.util.List<String> imageUrls;

    public Listing() {
        // Firebase Firestore için boş kurucu metod zorunludur
    }

    public Listing(String id, String title, String price, String description, String imageUriString, String itemAge, String dateShared, String location, String category) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.description = description;
        this.imageUriString = imageUriString;
        this.itemAge = itemAge;
        this.dateShared = dateShared;
        this.location = location;
        this.category = category;
        this.isFavorite = false;
        this.imageUrls = new java.util.ArrayList<>();
        if (imageUriString != null && !imageUriString.isEmpty()) {
            this.imageUrls.add(imageUriString);
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUriString() { 
        if (imageUrls != null && !imageUrls.isEmpty()) return imageUrls.get(0);
        return imageUriString; 
    }
    public void setImageUriString(String imageUriString) { this.imageUriString = imageUriString; }
    
    public java.util.List<String> getImageUrls() {
        if (imageUrls == null || imageUrls.isEmpty()) {
            java.util.List<String> list = new java.util.ArrayList<>();
            if (imageUriString != null && !imageUriString.isEmpty()) {
                list.add(imageUriString);
            }
            return list;
        }
        return imageUrls;
    }
    public void setImageUrls(java.util.List<String> imageUrls) { this.imageUrls = imageUrls; }

    public String getItemAge() { return itemAge; }
    public void setItemAge(String itemAge) { this.itemAge = itemAge; }
    public String getDateShared() { return dateShared; }
    public void setDateShared(String dateShared) { this.dateShared = dateShared; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
    
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getStatus() { return status != null ? status : "active"; }
    public void setStatus(String status) { this.status = status; }

    public String getCategory() { return category != null ? category : "Diğer"; }
    public void setCategory(String category) { this.category = category; }
}
