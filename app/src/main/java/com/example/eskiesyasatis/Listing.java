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

    public Listing() {
        // Firebase Firestore için boş kurucu metod zorunludur
    }

    public Listing(String id, String title, String price, String description, String imageUriString, String itemAge, String dateShared, String location) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.description = description;
        this.imageUriString = imageUriString;
        this.itemAge = itemAge;
        this.dateShared = dateShared;
        this.location = location;
        this.isFavorite = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public String getPrice() { return price; }
    public String getDescription() { return description; }
    public String getImageUriString() { return imageUriString; }
    public String getItemAge() { return itemAge; }
    public String getDateShared() { return dateShared; }
    public String getLocation() { return location; }
    
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
    
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
}
