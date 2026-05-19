package com.example.eskiesyasatis;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.eskiesyasatis.ChatActivity;

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

        androidx.viewpager2.widget.ViewPager2 vpListingImages = findViewById(R.id.vpListingImages);
        TextView tvDetailPrice = findViewById(R.id.tvDetailPrice);
        TextView tvDetailTitle = findViewById(R.id.tvDetailTitle);
        TextView tvDetailAge = findViewById(R.id.tvDetailAge);
        TextView tvDetailDate = findViewById(R.id.tvDetailDate);
        TextView tvDetailLocation = findViewById(R.id.tvDetailLocation);
        TextView tvDetailOwner = findViewById(R.id.tvDetailOwner);
        TextView tvDetailDesc = findViewById(R.id.tvDetailDesc);
        TextView tvDetailCategory = findViewById(R.id.tvDetailCategory);
        Button btnMessage = findViewById(R.id.btnMessage);

        tvDetailPrice.setText((listing.getPrice() != null ? listing.getPrice() : "0") + " TL");
        tvDetailTitle.setText(listing.getTitle() != null ? listing.getTitle() : "Başlık Yok");
        tvDetailAge.setText(listing.getItemAge() != null ? listing.getItemAge() : "Belirtilmedi");
        tvDetailDate.setText(listing.getDateShared() != null ? listing.getDateShared() : "Bilinmiyor");
        tvDetailLocation.setText(listing.getLocation() != null ? listing.getLocation() : "Belirtilmedi");
        tvDetailOwner.setText(listing.getOwnerName() != null ? listing.getOwnerName() : "Bilinmeyen Satıcı");
        tvDetailCategory.setText(listing.getCategory() != null ? listing.getCategory() : "Belirtilmedi");
        tvDetailDesc.setText(listing.getDescription() != null ? listing.getDescription() : "Açıklama yok.");

        // Satıcı adını tıklanabilir yap
        if (listing.getOwnerId() != null) {
            tvDetailOwner.setTextColor(getResources().getColor(R.color.primaryColor, getTheme()));
            tvDetailOwner.setOnClickListener(v -> {
                Intent profileIntent = new Intent(this, UserProfileActivity.class);
                profileIntent.putExtra("USER_ID", listing.getOwnerId());
                profileIntent.putExtra("USER_NAME", listing.getOwnerName());
                startActivity(profileIntent);
            });
        }

        java.util.List<String> imageUrls = listing.getImageUrls();
        if (imageUrls == null || imageUrls.isEmpty()) {
            imageUrls = new java.util.ArrayList<>();
            imageUrls.add(""); // Placeholder için boş string
        }
        
        ImagePagerAdapter adapter = new ImagePagerAdapter(imageUrls);
        vpListingImages.setAdapter(adapter);

        // Fotoğraf sayacı
        TextView tvPhotoCounter = findViewById(R.id.tvPhotoCounter);
        int totalPhotos = imageUrls.size();
        if (totalPhotos <= 1) {
            tvPhotoCounter.setVisibility(android.view.View.GONE);
        } else {
            tvPhotoCounter.setText("1/" + totalPhotos);
            vpListingImages.registerOnPageChangeCallback(new androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    tvPhotoCounter.setText((position + 1) + "/" + totalPhotos);
                }
            });
        }

        btnMessage.setOnClickListener(v -> {
            if (listing.getOwnerId() != null) {
                Intent chatIntent = new Intent(this, ChatActivity.class);
                chatIntent.putExtra("OTHER_USER_ID", listing.getOwnerId());
                chatIntent.putExtra("OTHER_USER_NAME", listing.getOwnerName() != null ? listing.getOwnerName() : "Satıcı");
                chatIntent.putExtra("LISTING_ID", listing.getId());
                chatIntent.putExtra("LISTING_TITLE", listing.getTitle() != null ? listing.getTitle() : "İlan");
                if (listing.getImageUriString() != null) {
                    chatIntent.putExtra("LISTING_IMAGE", listing.getImageUriString());
                }
                startActivity(chatIntent);
            } else {
                Toast.makeText(this, "Satıcı bilgisi bulunamadı.", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnBuyNow = findViewById(R.id.btnBuyNow);

        btnBuyNow.setOnClickListener(v -> {
            Intent checkoutIntent = new Intent(this, CheckoutActivity.class);
            checkoutIntent.putExtra("totalAmount", Double.parseDouble(listing.getPrice()));
            checkoutIntent.putExtra("LISTING_ID", listing.getId());
            checkoutIntent.putExtra("LISTING_TITLE", listing.getTitle());
            checkoutIntent.putExtra("LISTING_IMAGE_URL", listing.getImageUriString());
            startActivity(checkoutIntent);
        });

        if ("sold".equals(listing.getStatus())) {
            android.widget.LinearLayout buttonLayout = (android.widget.LinearLayout) btnMessage.getParent();
            buttonLayout.setVisibility(android.view.View.GONE);
            
            TextView tvSoldMsg = new TextView(this);
            tvSoldMsg.setText("BU İLAN SATILMIŞTIR");
            tvSoldMsg.setTextSize(18);
            tvSoldMsg.setTextColor(android.graphics.Color.RED);
            tvSoldMsg.setTypeface(null, android.graphics.Typeface.BOLD);
            tvSoldMsg.setGravity(android.view.Gravity.CENTER);
            tvSoldMsg.setPadding(0, 16, 0, 16);
            
            android.view.ViewGroup parentLayout = (android.view.ViewGroup) buttonLayout.getParent();
            parentLayout.addView(tvSoldMsg, parentLayout.indexOfChild(buttonLayout));
        }

        ImageView btnDeleteListing = findViewById(R.id.btnDeleteListing);
        com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        
        if (currentUser != null && listing.getOwnerId() != null && currentUser.getUid().equals(listing.getOwnerId())) {
            btnDeleteListing.setVisibility(android.view.View.VISIBLE);
            
            // Kendi ilanıysa: Sohbet/Sepet gizle, Düzenle butonu göster
            android.widget.LinearLayout buttonLayout = (android.widget.LinearLayout) btnMessage.getParent();
            buttonLayout.setVisibility(android.view.View.GONE);
            
            Button btnEditListing = findViewById(R.id.btnEditListing);
            btnEditListing.setVisibility(android.view.View.VISIBLE);
            
            btnEditListing.setOnClickListener(v -> {
                Intent editIntent = new Intent(this, EditListingActivity.class);
                editIntent.putExtra("LISTING_ID", listingId);
                startActivity(editIntent);
                finish();
            });
            
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

    private class ImagePagerAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<ImagePagerAdapter.ViewHolder> {
        private java.util.List<String> urls;

        public ImagePagerAdapter(java.util.List<String> urls) {
            this.urls = urls;
        }

        @androidx.annotation.NonNull
        @Override
        public ViewHolder onCreateViewHolder(@androidx.annotation.NonNull android.view.ViewGroup parent, int viewType) {
            ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT, 
                android.view.ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return new ViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(@androidx.annotation.NonNull ViewHolder holder, int position) {
            String url = urls.get(position);
            if (url != null && !url.isEmpty()) {
                loadImageIntoView(url, holder.imageView);
            } else {
                holder.imageView.setImageResource(android.R.drawable.ic_menu_gallery);
            }
            
            holder.imageView.setOnClickListener(v -> {
                showFullscreenGallery(position);
            });
        }

        private void loadImageIntoView(String url, ImageView imageView) {
            if (url.startsWith("data:image")) {
                try {
                    String base64Data = url.substring(url.indexOf(",") + 1);
                    byte[] decodedBytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT);
                    android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    imageView.setImageBitmap(bitmap);
                } catch (Exception e) {
                    imageView.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            } else {
                com.bumptech.glide.Glide.with(imageView.getContext())
                    .load(url)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(imageView);
            }
        }

        /** Tam ekran galeri açar — kaydırma + zoom + sayaç */
        private void showFullscreenGallery(int startPosition) {
            android.app.Dialog dialog = new android.app.Dialog(ListingDetailActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            
            // Ana layout
            android.widget.FrameLayout container = new android.widget.FrameLayout(ListingDetailActivity.this);
            container.setLayoutParams(new android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT));
            container.setBackgroundColor(0xFF000000);

            // ViewPager2 — kaydırma için
            androidx.viewpager2.widget.ViewPager2 fullscreenPager = new androidx.viewpager2.widget.ViewPager2(ListingDetailActivity.this);
            fullscreenPager.setLayoutParams(new android.widget.FrameLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT));

            // Fotoğraf sayacı — sağ üst köşe
            TextView tvCounter = new TextView(ListingDetailActivity.this);
            android.widget.FrameLayout.LayoutParams counterParams = new android.widget.FrameLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            counterParams.gravity = android.view.Gravity.TOP | android.view.Gravity.END;
            int margin = (int) (16 * getResources().getDisplayMetrics().density);
            int topMargin = (int) (48 * getResources().getDisplayMetrics().density); // Status bar altı
            counterParams.setMargins(margin, topMargin, margin, 0);
            tvCounter.setLayoutParams(counterParams);
            tvCounter.setTextColor(0xFFFFFFFF);
            tvCounter.setTextSize(16);
            tvCounter.setTypeface(null, android.graphics.Typeface.BOLD);
            tvCounter.setBackgroundColor(0x80000000);
            tvCounter.setPadding(
                (int)(14 * getResources().getDisplayMetrics().density),
                (int)(8 * getResources().getDisplayMetrics().density),
                (int)(14 * getResources().getDisplayMetrics().density),
                (int)(8 * getResources().getDisplayMetrics().density));
            tvCounter.setText((startPosition + 1) + "/" + urls.size());

            // Kapat butonu — sol üst köşe
            TextView tvClose = new TextView(ListingDetailActivity.this);
            android.widget.FrameLayout.LayoutParams closeParams = new android.widget.FrameLayout.LayoutParams(
                (int)(48 * getResources().getDisplayMetrics().density),
                (int)(48 * getResources().getDisplayMetrics().density));
            closeParams.gravity = android.view.Gravity.TOP | android.view.Gravity.START;
            closeParams.setMargins(margin, topMargin, 0, 0);
            tvClose.setLayoutParams(closeParams);
            tvClose.setText("✕");
            tvClose.setTextColor(0xFFFFFFFF);
            tvClose.setTextSize(22);
            tvClose.setGravity(android.view.Gravity.CENTER);
            tvClose.setBackgroundColor(0x80000000);
            tvClose.setOnClickListener(v -> dialog.dismiss());

            // Fullscreen PhotoView adapter
            FullscreenPhotoAdapter fullAdapter = new FullscreenPhotoAdapter(urls);
            fullscreenPager.setAdapter(fullAdapter);
            fullscreenPager.setCurrentItem(startPosition, false);

            // Sayfa değiştiğinde sayacı güncelle
            fullscreenPager.registerOnPageChangeCallback(new androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    tvCounter.setText((position + 1) + "/" + urls.size());
                }
            });

            // Tek fotoğrafsa sayacı gizle
            if (urls.size() <= 1) {
                tvCounter.setVisibility(android.view.View.GONE);
            }

            container.addView(fullscreenPager);
            container.addView(tvCounter);
            container.addView(tvClose);
            dialog.setContentView(container);
            dialog.show();
        }

        @Override
        public int getItemCount() {
            return urls.size();
        }

        class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            ImageView imageView;
            ViewHolder(ImageView itemView) {
                super(itemView);
                this.imageView = itemView;
            }
        }
    }

    /** Tam ekran galeri için PhotoView tabanlı adapter */
    private class FullscreenPhotoAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<FullscreenPhotoAdapter.PhotoViewHolder> {
        private java.util.List<String> urls;

        FullscreenPhotoAdapter(java.util.List<String> urls) {
            this.urls = urls;
        }

        @androidx.annotation.NonNull
        @Override
        public PhotoViewHolder onCreateViewHolder(@androidx.annotation.NonNull android.view.ViewGroup parent, int viewType) {
            com.github.chrisbanes.photoview.PhotoView photoView = new com.github.chrisbanes.photoview.PhotoView(parent.getContext());
            photoView.setLayoutParams(new android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT));
            return new PhotoViewHolder(photoView);
        }

        @Override
        public void onBindViewHolder(@androidx.annotation.NonNull PhotoViewHolder holder, int position) {
            String url = urls.get(position);
            if (url != null && !url.isEmpty()) {
                if (url.startsWith("data:image")) {
                    try {
                        String base64Data = url.substring(url.indexOf(",") + 1);
                        byte[] decoded = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT);
                        android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                        holder.photoView.setImageBitmap(bmp);
                    } catch (Exception e) {
                        holder.photoView.setImageResource(android.R.drawable.ic_menu_gallery);
                    }
                } else {
                    com.bumptech.glide.Glide.with(holder.itemView.getContext())
                        .load(url)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .into(holder.photoView);
                }
            } else {
                holder.photoView.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }

        @Override
        public int getItemCount() {
            return urls.size();
        }

        class PhotoViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            com.github.chrisbanes.photoview.PhotoView photoView;
            PhotoViewHolder(com.github.chrisbanes.photoview.PhotoView itemView) {
                super(itemView);
                this.photoView = itemView;
            }
        }
    }
}
