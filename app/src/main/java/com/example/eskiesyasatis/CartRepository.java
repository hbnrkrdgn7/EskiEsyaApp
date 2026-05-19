package com.example.eskiesyasatis;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CartRepository {
    private static CartRepository instance;
    private List<Listing> cartListings;
    private FirebaseFirestore db;
    private OnCartChangedListener listener;

    public interface OnCartChangedListener {
        void onCartChanged();
    }

    private CartRepository() {
        cartListings = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        fetchCartFromFirestore();
    }

    public static CartRepository getInstance() {
        if (instance == null) {
            instance = new CartRepository();
        }
        return instance;
    }

    public static void resetInstance() {
        instance = null;
    }

    public void setOnCartChangedListener(OnCartChangedListener listener) {
        this.listener = listener;
    }

    private void fetchCartFromFirestore() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        db.collection("Users").document(userId).collection("Cart")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w("Firestore", "Cart Listen failed.", error);
                        return;
                    }

                    if (value != null) {
                        cartListings.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Listing listing = doc.toObject(Listing.class);
                            cartListings.add(listing);
                        }
                        if (listener != null) {
                            listener.onCartChanged();
                        }
                    }
                });
    }

    public List<Listing> getCartListings() {
        return cartListings;
    }

    public void addToCart(Listing listing) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId != null) {
            db.collection("Users").document(userId).collection("Cart").document(listing.getId()).set(listing);
        }
    }

    public void clearCart() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId != null) {
            db.collection("Users").document(userId).collection("Cart")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            doc.getReference().delete();
                        }
                        cartListings.clear();
                        if (listener != null) {
                            listener.onCartChanged();
                        }
                    });
        }
    }
}
