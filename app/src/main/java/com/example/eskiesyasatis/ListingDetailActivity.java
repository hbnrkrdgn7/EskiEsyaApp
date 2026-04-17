package com.example.eskiesyasatis;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ListingDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing_detail);

        ImageView btnBackDetail = findViewById(R.id.btnBackDetail);
        btnBackDetail.setOnClickListener(v -> finish());

        String listingId = getIntent().getStringExtra("LISTING_ID");
        if (listingId == null) {
            Toast.makeText(this, "İlan bulunamadı!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Listing listing = ListingRepository.getInstance().getListingById(listingId);
        if (listing == null) {
            Toast.makeText(this, "İlan bulunamadı!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ImageView ivDetailImage = findViewById(R.id.ivDetailImage);
        TextView tvDetailPrice = findViewById(R.id.tvDetailPrice);
        TextView tvDetailTitle = findViewById(R.id.tvDetailTitle);
        TextView tvDetailAge = findViewById(R.id.tvDetailAge);
        TextView tvDetailDate = findViewById(R.id.tvDetailDate);
        TextView tvDetailLocation = findViewById(R.id.tvDetailLocation);
        TextView tvDetailOwner = findViewById(R.id.tvDetailOwner);
        TextView tvDetailDesc = findViewById(R.id.tvDetailDesc);
        Button btnMessage = findViewById(R.id.btnMessage);
        Button btnAddToCart = findViewById(R.id.btnAddToCart);

        tvDetailPrice.setText((listing.getPrice() != null ? listing.getPrice() : "0") + " TL");
        tvDetailTitle.setText(listing.getTitle() != null ? listing.getTitle() : "Başlık Yok");
        tvDetailAge.setText(listing.getItemAge() != null ? listing.getItemAge() : "Belirtilmedi");
        tvDetailDate.setText(listing.getDateShared() != null ? listing.getDateShared() : "Bilinmiyor");
        tvDetailLocation.setText(listing.getLocation() != null ? listing.getLocation() : "Belirtilmedi");
        tvDetailOwner.setText(listing.getOwnerName() != null ? listing.getOwnerName() : "Bilinmeyen Satıcı");
        tvDetailDesc.setText(listing.getDescription() != null ? listing.getDescription() : "Açıklama yok.");

        if (listing.getImageUriString() != null) {
            try {
                ivDetailImage.setImageURI(Uri.parse(listing.getImageUriString()));
            } catch (Exception e) {
                ivDetailImage.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }
        
        ivDetailImage.setOnClickListener(v -> {
            android.app.Dialog dialog = new android.app.Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            android.widget.ImageView fullImage = new android.widget.ImageView(this);
            fullImage.setLayoutParams(new android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT, 
                android.view.ViewGroup.LayoutParams.MATCH_PARENT));
            fullImage.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
            if (listing.getImageUriString() != null) {
                try {
                    fullImage.setImageURI(Uri.parse(listing.getImageUriString()));
                } catch (Exception e) {
                    fullImage.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            } else {
                fullImage.setImageResource(android.R.drawable.ic_menu_gallery);
            }
            dialog.setContentView(fullImage);
            fullImage.setOnClickListener(v1 -> dialog.dismiss());
            dialog.show();
        });

        btnMessage.setOnClickListener(v -> {
            Toast.makeText(this, "Mesajlaşma özelliği henüz eklenmedi.", Toast.LENGTH_SHORT).show();
        });

        btnAddToCart.setOnClickListener(v -> {
            CartRepository.getInstance().addToCart(listing);
            Toast.makeText(this, "Ürün sepete eklendi!", Toast.LENGTH_SHORT).show();
        });

        ImageView btnDeleteListing = findViewById(R.id.btnDeleteListing);
        com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        
        if (currentUser != null && listing.getOwnerId() != null && currentUser.getUid().equals(listing.getOwnerId())) {
            btnDeleteListing.setVisibility(android.view.View.VISIBLE);
            
            // Eğer kendi ilanıysa butonları tamamen gizle
            android.widget.LinearLayout buttonLayout = (android.widget.LinearLayout) btnMessage.getParent();
            buttonLayout.setVisibility(android.view.View.GONE);
            
            btnDeleteListing.setOnClickListener(v -> {
                new android.app.AlertDialog.Builder(this)
                    .setTitle("İlanı Sil")
                    .setMessage("Bu ilanı kalıcı olarak silmek istediğinize emin misiniz?")
                    .setPositiveButton("Evet, Sil", (dialog, which) -> {
                        ListingRepository.getInstance().deleteListing(listingId);
                        Toast.makeText(this, "İlan başarıyla silindi.", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("İptal", (dialog, which) -> dialog.dismiss())
                    .show();
            });
        }
    }
}
