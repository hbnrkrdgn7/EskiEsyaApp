package com.example.eskiesyasatis;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class EditListingActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etPrice, etDescription, etItemAge;
    private AutoCompleteTextView spinnerCategory;
    private Button btnSaveListing;
    private LinearLayout layoutPhotoContainer;
    
    private Listing currentListing = null;
    
    // Mevcut fotoğrafların byte verileri (yeni eklenenler dahil)
    private List<byte[]> photoBytesList = new ArrayList<>();

    private final ActivityResultLauncher<String> getContent = registerForActivityResult(
            new ActivityResultContracts.GetMultipleContents(),
            uris -> {
                if (uris != null && !uris.isEmpty()) {
                    for (Uri uri : uris) {
                        if (photoBytesList.size() < 5) {
                            byte[] compressed = compressImageFromUri(uri);
                            if (compressed != null) {
                                photoBytesList.add(compressed);
                                addPhotoCard(compressed);
                            }
                        }
                    }
                }
            }
    );

    private byte[] compressImageFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;
            Bitmap bmp = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            if (bmp == null) return null;

            int maxDim = 500;
            float scale = Math.min((float) maxDim / bmp.getWidth(), (float) maxDim / bmp.getHeight());
            if (scale < 1.0f) {
                bmp = Bitmap.createScaledBitmap(bmp, Math.round(bmp.getWidth() * scale), Math.round(bmp.getHeight() * scale), true);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            return baos.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    /** Mevcut bir fotoğrafı (URL/base64 string) byte array'e çevirir */
    private byte[] existingImageToBytes(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) return null;
        
        if (imageUrl.startsWith("data:image")) {
            // Base64 data URI
            try {
                String base64Data = imageUrl.substring(imageUrl.indexOf(",") + 1);
                return android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT);
            } catch (Exception e) {
                return null;
            }
        }
        // Normal URL — byte olarak indirmek zorunda değiliz, doğrudan URL'yi koruyacağız
        return null;
    }

    private void addPhotoCard(byte[] imageBytes) {
        if (layoutPhotoContainer == null) return;
        float density = getResources().getDisplayMetrics().density;
        
        androidx.cardview.widget.CardView cardView = new androidx.cardview.widget.CardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                (int) (150 * density), (int) (200 * density));
        params.setMarginEnd((int) (8 * density));
        cardView.setLayoutParams(params);
        cardView.setRadius(16 * density);
        cardView.setCardElevation(2 * density);

        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        
        Bitmap bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        imageView.setImageBitmap(bmp);

        cardView.setOnLongClickListener(v -> {
            new android.app.AlertDialog.Builder(this)
                .setTitle("Fotoğrafı Sil")
                .setMessage("Bu fotoğrafı kaldırmak istiyor musunuz?")
                .setPositiveButton("Evet", (dialog, which) -> {
                    int idx = layoutPhotoContainer.indexOfChild(cardView);
                    layoutPhotoContainer.removeView(cardView);
                    if (idx >= 0 && idx < photoBytesList.size()) {
                        photoBytesList.remove(idx);
                    }
                })
                .setNegativeButton("Hayır", null)
                .show();
            return true;
        });

        cardView.addView(imageView);
        // "Fotoğraf Ekle" butonundan ÖNCE ekle
        layoutPhotoContainer.addView(cardView, layoutPhotoContainer.getChildCount() - 1);
    }

    /** Normal URL'li fotoğraf kartı ekler (Glide ile yükleme) */
    private void addPhotoCardFromUrl(String url, byte[] placeholderBytes) {
        if (layoutPhotoContainer == null) return;
        float density = getResources().getDisplayMetrics().density;
        
        androidx.cardview.widget.CardView cardView = new androidx.cardview.widget.CardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                (int) (150 * density), (int) (200 * density));
        params.setMarginEnd((int) (8 * density));
        cardView.setLayoutParams(params);
        cardView.setRadius(16 * density);
        cardView.setCardElevation(2 * density);

        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        
        com.bumptech.glide.Glide.with(this)
            .load(url)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_gallery)
            .centerCrop()
            .into(imageView);

        cardView.setOnLongClickListener(v -> {
            new android.app.AlertDialog.Builder(this)
                .setTitle("Fotoğrafı Sil")
                .setMessage("Bu fotoğrafı kaldırmak istiyor musunuz?")
                .setPositiveButton("Evet", (dialog, which) -> {
                    int idx = layoutPhotoContainer.indexOfChild(cardView);
                    layoutPhotoContainer.removeView(cardView);
                    if (idx >= 0 && idx < photoBytesList.size()) {
                        photoBytesList.remove(idx);
                    }
                })
                .setNegativeButton("Hayır", null)
                .show();
            return true;
        });

        cardView.addView(imageView);
        layoutPhotoContainer.addView(cardView, layoutPhotoContainer.getChildCount() - 1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_listing);

        ImageView btnBack = findViewById(R.id.btnBackEditListing);
        btnBack.setOnClickListener(v -> finish());

        etTitle = findViewById(R.id.etTitle);
        etPrice = findViewById(R.id.etPrice);
        etDescription = findViewById(R.id.etDescription);
        etItemAge = findViewById(R.id.etItemAge);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSaveListing = findViewById(R.id.btnSaveListing);
        layoutPhotoContainer = findViewById(R.id.layoutPhotoContainer);

        // Fotoğraf ekleme butonu
        androidx.cardview.widget.CardView cardAddPhoto = findViewById(R.id.cardAddPhoto);
        cardAddPhoto.setOnClickListener(v -> {
            if (photoBytesList.size() >= 5) {
                Toast.makeText(this, "Maksimum 5 fotoğraf ekleyebilirsiniz.", Toast.LENGTH_SHORT).show();
                return;
            }
            getContent.launch("image/*");
        });

        String listingId = getIntent().getStringExtra("LISTING_ID");
        if (listingId == null) {
            Toast.makeText(this, "İlan bulunamadı!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentListing = ListingRepository.getInstance().getListingById(listingId);
        if (currentListing == null) {
            Toast.makeText(this, "İlan bulunamadı!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Mevcut bilgileri doldur
        etTitle.setText(currentListing.getTitle());
        etPrice.setText(currentListing.getPrice());
        etDescription.setText(currentListing.getDescription());
        etItemAge.setText(currentListing.getItemAge());
        
        // Kategoriler
        String[] categories = {"Elektronik", "Ev Eşyası", "Giyim", "Kitap & Hobi", "Araç İçi", "Diğer"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, R.layout.spinner_dropdown_item, categories);
        spinnerCategory.setAdapter(categoryAdapter);
        String currentCat = currentListing.getCategory() != null ? currentListing.getCategory() : categories[0];
        spinnerCategory.setText(currentCat, false);

        // Mevcut fotoğrafları yükle
        loadExistingPhotos();

        android.widget.CheckBox cbRepublish = findViewById(R.id.cbRepublish);
        if ("sold".equals(currentListing.getStatus())) {
            cbRepublish.setVisibility(android.view.View.VISIBLE);
        }

        // Kaydet butonu
        btnSaveListing.setOnClickListener(v -> {
            String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
            String price = etPrice.getText() != null ? etPrice.getText().toString().trim() : "";
            String desc = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
            String age = etItemAge.getText() != null ? etItemAge.getText().toString().trim() : "Belirtilmemiş";

            if (title.isEmpty() || price.isEmpty() || desc.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm zorunlu bilgileri doldurun.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (photoBytesList.isEmpty()) {
                Toast.makeText(this, "Lütfen en az bir fotoğraf ekleyin.", Toast.LENGTH_SHORT).show();
                return;
            }

            String category = spinnerCategory.getText() != null ? spinnerCategory.getText().toString().trim() : "Diğer";

            btnSaveListing.setEnabled(false);
            btnSaveListing.setText("Fotoğraflar Yükleniyor...");

            // Tüm fotoğrafları sırayla yükle
            String randomId = currentListing.getId();
            com.google.firebase.storage.FirebaseStorage storage = com.google.firebase.storage.FirebaseStorage.getInstance();
            List<String> uploadedUrls = new ArrayList<>();
            
            uploadNextEditPhoto(0, randomId, storage, uploadedUrls, title, price, desc, age, category, cbRepublish.isChecked());
        });
    }

    private void loadExistingPhotos() {
        List<String> existingUrls = currentListing.getImageUrls();
        if (existingUrls == null || existingUrls.isEmpty()) return;

        for (String url : existingUrls) {
            if (url == null || url.isEmpty()) continue;

            byte[] bytes = existingImageToBytes(url);
            if (bytes != null && bytes.length > 0) {
                // Base64 data URI — byte verisi mevcut
                photoBytesList.add(bytes);
                addPhotoCard(bytes);
            } else {
                // Normal URL — Glide ile indir ve byte'a çevir (arka planda)
                photoBytesList.add(new byte[0]); // Placeholder
                int currentIndex = photoBytesList.size() - 1;
                addPhotoCardFromUrl(url, null);
                
                // URL'li fotoğrafı arka planda byte'a çevir
                new Thread(() -> {
                    try {
                        java.net.URL imageUrl = new java.net.URL(url);
                        InputStream is = imageUrl.openStream();
                        Bitmap bmp = BitmapFactory.decodeStream(is);
                        is.close();
                        
                        if (bmp != null) {
                            int maxDim = 500;
                            float scale = Math.min((float) maxDim / bmp.getWidth(), (float) maxDim / bmp.getHeight());
                            if (scale < 1.0f) {
                                bmp = Bitmap.createScaledBitmap(bmp, Math.round(bmp.getWidth() * scale), Math.round(bmp.getHeight() * scale), true);
                            }
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bmp.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                            byte[] downloaded = baos.toByteArray();
                            
                            runOnUiThread(() -> {
                                if (currentIndex < photoBytesList.size()) {
                                    photoBytesList.set(currentIndex, downloaded);
                                }
                            });
                        }
                    } catch (Exception e) {
                        android.util.Log.e("EDIT_LISTING", "Fotoğraf indirilemedi: " + e.getMessage());
                    }
                }).start();
            }
        }
    }

    private void uploadNextEditPhoto(int index, String randomId, com.google.firebase.storage.FirebaseStorage storage, 
                                      List<String> uploadedUrls, String title, String price, String desc, String age, String category, boolean republish) {
        if (index >= photoBytesList.size()) {
            // Tüm fotoğraflar yüklendi, ilanı kaydet
            saveUpdatedListing(title, price, desc, age, category, uploadedUrls, republish);
            return;
        }

        byte[] imageData = photoBytesList.get(index);
        
        // Boş byte array ise (indirilememiş URL), eski URL'yi koru
        if (imageData.length == 0) {
            List<String> oldUrls = currentListing.getImageUrls();
            if (oldUrls != null && index < oldUrls.size()) {
                uploadedUrls.add(oldUrls.get(index));
            }
            uploadNextEditPhoto(index + 1, randomId, storage, uploadedUrls, title, price, desc, age, category, republish);
            return;
        }

        com.google.firebase.storage.StorageReference storageRef = storage.getReference()
            .child("listing_images/" + randomId + "_" + index + "_" + System.currentTimeMillis() + ".jpg");

        btnSaveListing.setText("Fotoğraf " + (index + 1) + "/" + photoBytesList.size() + " Yükleniyor...");

        com.google.firebase.storage.StorageMetadata metadata = new com.google.firebase.storage.StorageMetadata.Builder()
                .setContentType("image/jpeg").build();

        storageRef.putBytes(imageData, metadata)
            .addOnSuccessListener(taskSnapshot -> {
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    storageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            uploadedUrls.add(uri.toString());
                            uploadNextEditPhoto(index + 1, randomId, storage, uploadedUrls, title, price, desc, age, category, republish);
                        })
                        .addOnFailureListener(e -> {
                            // URL alınamadı, base64 fallback
                            String dataUri = "data:image/jpeg;base64," + android.util.Base64.encodeToString(imageData, android.util.Base64.NO_WRAP);
                            uploadedUrls.add(dataUri);
                            uploadNextEditPhoto(index + 1, randomId, storage, uploadedUrls, title, price, desc, age, category, republish);
                        });
                }, 2000);
            })
            .addOnFailureListener(e -> {
                // Storage başarısız, base64 fallback
                String dataUri = "data:image/jpeg;base64," + android.util.Base64.encodeToString(imageData, android.util.Base64.NO_WRAP);
                uploadedUrls.add(dataUri);
                uploadNextEditPhoto(index + 1, randomId, storage, uploadedUrls, title, price, desc, age, category, republish);
            });
    }

    private void saveUpdatedListing(String title, String price, String desc, String age, String category, List<String> imageUrls, boolean republish) {
        String firstImage = imageUrls.isEmpty() ? "" : imageUrls.get(0);
        
        Listing updatedListing = new Listing(
            currentListing.getId(), title, price, desc, firstImage,
            age, currentListing.getDateShared(), currentListing.getLocation(), category
        );
        updatedListing.setImageUrls(imageUrls);
        updatedListing.setOwnerId(currentListing.getOwnerId());
        updatedListing.setOwnerName(currentListing.getOwnerName());
        
        if ("sold".equals(currentListing.getStatus()) && republish) {
            updatedListing.setStatus("active");
        } else {
            updatedListing.setStatus(currentListing.getStatus());
        }
        
        ListingRepository.getInstance().updateListing(updatedListing);
        Toast.makeText(this, "İlan başarıyla güncellendi!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
