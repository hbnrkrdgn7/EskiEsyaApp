package com.example.eskiesyasatis;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FavoritesRepository {
    private static FavoritesRepository instance;
    private List<Listing> favoriteListings;
    private FirebaseFirestore db;
    private OnFavoritesChangedListener listener;

    public interface OnFavoritesChangedListener {
        void onFavoritesChanged();
    }

    private FavoritesRepository() {
        favoriteListings = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        fetchFavoritesFromFirestore();
    }

    public static FavoritesRepository getInstance() {
        if (instance == null) {
            instance = new FavoritesRepository();
        }
        return instance;
    }

    public static void resetInstance() {
        instance = null;
    }

    public void setOnFavoritesChangedListener(OnFavoritesChangedListener listener) {
        this.listener = listener;
    }

    private void fetchFavoritesFromFirestore() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        db.collection("Users").document(userId).collection("Favorites")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w("Firestore", "Favorites Listen failed.", error);
                        return;
                    }

                    if (value != null) {
                        favoriteListings.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Listing listing = doc.toObject(Listing.class);
                            favoriteListings.add(listing);
                        }
                        if (listener != null) {
                            listener.onFavoritesChanged();
                        }
                    }
                });
    }

    public List<Listing> getFavoriteListings() {
        return favoriteListings;
    }
    
    public boolean isFavorite(String listingId) {
        for (Listing item : favoriteListings) {
            if (item.getId().equals(listingId)) {
                return true;
            }
        }
        return false;
    }

    public void addFavorite(Listing listing) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId != null) {
            db.collection("Users").document(userId).collection("Favorites").document(listing.getId()).set(listing);
        }
    }
    
    public void removeFavorite(String listingId) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId != null) {
            db.collection("Users").document(userId).collection("Favorites").document(listingId).delete();
        }
    }
}
