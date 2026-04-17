package com.example.eskiesyasatis;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ListingRepository {
    private static ListingRepository instance;
    private List<Listing> listings;
    private FirebaseFirestore db;
    private OnDataChangedListener listener;

    public interface OnDataChangedListener {
        void onDataChanged();
    }

    private ListingRepository() {
        listings = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        fetchListingsFromFirestore();
    }

    public static ListingRepository getInstance() {
        if (instance == null) {
            instance = new ListingRepository();
        }
        return instance;
    }

    public void setOnDataChangedListener(OnDataChangedListener listener) {
        this.listener = listener;
    }

    private void fetchListingsFromFirestore() {
        db.collection("Listings")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w("Firestore", "Listen failed.", error);
                        return;
                    }

                    if (value != null) {
                        listings.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Listing listing = doc.toObject(Listing.class);
                            if (listing.getId() == null) {
                                listing.setId(doc.getId());
                            }
                            listings.add(listing);
                        }
                        if (listener != null) {
                            listener.onDataChanged();
                        }
                    }
                });
    }

    public List<Listing> getListings() {
        return listings;
    }
    
    public Listing getListingById(String id) {
        if (id == null) return null;
        for(Listing listing : listings) {
            if(listing.getId() != null && listing.getId().equals(id)) return listing;
        }
        return null;
    }

    public void addListing(Listing listing) {
        // Doğrudan veritabanına ekliyoruz. SnapshotListener sayesinde
        // veritabanına eklendiği an "listings" listesi otomatik güncellenecektir.
        db.collection("Listings").document(listing.getId()).set(listing);
    }
    
    public void updateListing(Listing listing) {
        db.collection("Listings").document(listing.getId()).set(listing);
    }

    public List<Listing> getListingsByOwnerId(String ownerId) {
        List<Listing> userListings = new ArrayList<>();
        if (ownerId == null) return userListings;
        for (Listing listing : listings) {
            if (ownerId.equals(listing.getOwnerId())) {
                userListings.add(listing);
            }
        }
        return userListings;
    }

    public void deleteListing(String listingId) {
        db.collection("Listings").document(listingId).delete();
    }

    public void deleteListingsByOwnerId(String ownerId) {
        db.collection("Listings").whereEqualTo("ownerId", ownerId).get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    doc.getReference().delete();
                }
            });
    }
}
