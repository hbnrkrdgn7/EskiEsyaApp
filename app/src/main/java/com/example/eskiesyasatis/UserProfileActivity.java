package com.example.eskiesyasatis;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity {

    private ImageView ivUserPhoto;
    private TextView tvUserName, tvUserEmail, tvUserJoined, tvListingCount, tvNoListings;
    private RecyclerView recyclerView;
    private ListingAdapter listingAdapter;
    private List<Listing> userListings = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        String userId = getIntent().getStringExtra("USER_ID");
        String userName = getIntent().getStringExtra("USER_NAME");

        if (userId == null) {
            Toast.makeText(this, "Kullanıcı bulunamadı.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();

        // View'ları bağla
        ImageView btnBack = findViewById(R.id.btnBack);
        ivUserPhoto = findViewById(R.id.ivUserPhoto);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvUserJoined = findViewById(R.id.tvUserJoined);
        tvListingCount = findViewById(R.id.tvListingCount);
        tvNoListings = findViewById(R.id.tvNoListings);
        recyclerView = findViewById(R.id.recyclerViewUserListings);

        btnBack.setOnClickListener(v -> finish());

        // Geçici olarak intent'ten gelen adı göster
        if (userName != null) {
            tvUserName.setText(userName);
        }

        // RecyclerView ayarla
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        listingAdapter = new ListingAdapter(userListings);
        recyclerView.setAdapter(listingAdapter);

        // Kullanıcı bilgilerini Firestore'dan çek
        loadUserProfile(userId);

        // Kullanıcının ilanlarını çek
        loadUserListings(userId);
    }

    private void loadUserProfile(String userId) {
        db.collection("Users").document(userId)
            .get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String name = doc.getString("name");
                    String email = doc.getString("email");
                    String profileImageUrl = doc.getString("profileImageUrl");

                    if (name != null && !name.isEmpty()) {
                        tvUserName.setText(name);
                    }
                    if (email != null) {
                        tvUserEmail.setText(email);
                    }

                    tvUserJoined.setText("Retrova Üyesi");

                    // Profil fotoğrafını yükle
                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        ivUserPhoto.setImageTintList(null);
                        Glide.with(this)
                            .load(profileImageUrl)
                            .circleCrop()
                            .placeholder(R.drawable.ic_person)
                            .into(ivUserPhoto);
                    }
                }
            })
            .addOnFailureListener(e -> {
                // Kullanıcı dokümanı yoksa sadece isim göster
            });
    }

    private void loadUserListings(String userId) {
        db.collection("Listings")
            .whereEqualTo("ownerId", userId)
            .whereEqualTo("status", "active")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                userListings.clear();
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    Listing listing = doc.toObject(Listing.class);
                    listing.setId(doc.getId());
                    userListings.add(listing);
                }
                listingAdapter.updateList(userListings);

                // İlan sayısını güncelle
                int count = userListings.size();
                tvListingCount.setText(count + " aktif ilan");

                if (count == 0) {
                    tvNoListings.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    tvNoListings.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "İlanlar yüklenemedi.", Toast.LENGTH_SHORT).show();
            });
    }
}
