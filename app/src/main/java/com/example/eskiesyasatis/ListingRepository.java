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
    private List<OnDataChangedListener> listeners = new ArrayList<>();
    private boolean isLoaded = false;

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

    public boolean isLoaded() {
        return isLoaded;
    }


    public void setOnDataChangedListener(OnDataChangedListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    public void removeOnDataChangedListener(OnDataChangedListener listener) {
        listeners.remove(listener);
    }

    private void fetchListingsFromFirestore() {
        db.collection("Listings")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w("Firestore", "Listen failed.", error);
                        isLoaded = true;
                        for (OnDataChangedListener l : new ArrayList<>(listeners)) {
                            l.onDataChanged();
                        }
                        return;
                    }

                    if (value != null) {
                        listings.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Listing listing = doc.toObject(Listing.class);

                            // CRITICAL KONTROL: Eğer dokümanın içi tamamen boşaltılmışsa veya
                            // başlığı/durumu null geldiyse bu bozuk veriyi listeye hiç ekleme!
                            if (listing != null && listing.getTitle() != null && listing.getStatus() != null) {
                                if (listing.getId() == null) {
                                    listing.setId(doc.getId());
                                }
                                listings.add(listing);
                            }
                        }
                        Log.d("Firestore", "Toplam " + listings.size() + " ilan yüklendi");
                        isLoaded = true;
                        for (OnDataChangedListener l : new ArrayList<>(listeners)) {
                            l.onDataChanged();
                        }
                    }
                });
    }

    public List<Listing> getListings() {
        return listings;
    }

    public List<Listing> getActiveListings() {
        List<Listing> activeListings = new ArrayList<>();
        for (Listing listing : listings) {
            if ("active".equals(listing.getStatus())) {
                activeListings.add(listing);
            }
        }
        return activeListings;
    }
    
    public Listing getListingById(String id) {
        if (id == null) return null;
        for(Listing listing : listings) {
            if(listing.getId() != null && listing.getId().equals(id)) return listing;
        }
        return null;
    }

    public com.google.android.gms.tasks.Task<Void> addListing(Listing listing) {
        // Döndürülen Task sayesinde çağıran yer işlemin bitmesini bekleyebilir
        return db.collection("Listings").document(listing.getId()).set(listing);
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
