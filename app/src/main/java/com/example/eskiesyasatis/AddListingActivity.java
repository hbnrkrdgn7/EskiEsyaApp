package com.example.eskiesyasatis;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import java.util.HashMap;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class AddListingActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etPrice, etDescription, etItemAge;
    private AutoCompleteTextView spinnerCity, spinnerDistrict, spinnerCategory;
    private Button btnPostListing;
    
    private HashMap<String, String[]> cityDistrictsMap;
    
    private java.util.List<Uri> selectedImageUris = new java.util.ArrayList<>();
    private java.util.List<byte[]> selectedImageBytes = new java.util.ArrayList<>();
    private LinearLayout layoutPhotoContainer;

    // Galeri açmak için modern Android API'si - Çoklu seçim
    private final ActivityResultLauncher<String> getContent = registerForActivityResult(
            new ActivityResultContracts.GetMultipleContents(),
            uris -> {
                if (uris != null && !uris.isEmpty()) {
                    for (Uri uri : uris) {
                        if (selectedImageUris.size() < 5) {
                            byte[] compressed = compressImageFromUri(uri);
                            if (compressed != null && compressed.length > 0) {
                                selectedImageUris.add(uri);
                                selectedImageBytes.add(compressed);
                                addImageViewToContainer(uri);
                            } else {
                                Toast.makeText(this, "Bir fotoğraf okunamadı, atlıyorum.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            }
    );

    /**
     * Fotoğrafı URI'den okur, boyutunu küçültür ve JPEG olarak sıkıştırır.
     * Maksimum 800x800 piksel, %70 kalite — hem hızlı yüklensin hem az yer kaplasın.
     */
    private byte[] compressImageFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (originalBitmap == null) return null;

            // Boyutunu küçült (max 500x500 — base64 yedek planı için boyut kontrolü)
            int maxDim = 500;
            int width = originalBitmap.getWidth();
            int height = originalBitmap.getHeight();
            float scale = Math.min((float) maxDim / width, (float) maxDim / height);
            
            if (scale < 1.0f) {
                int newWidth = Math.round(width * scale);
                int newHeight = Math.round(height * scale);
                originalBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
            }

            // JPEG olarak sıkıştır (%50 kalite — base64 yedek planında doküman boyutunu küçük tut)
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            originalBitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            return baos.toByteArray();
        } catch (Exception e) {
            android.util.Log.e("ADD_LISTING", "Fotoğraf sıkıştırma hatası: " + e.getMessage(), e);
            return null;
        }
    }

    private void addImageViewToContainer(Uri uri) {
        if (layoutPhotoContainer == null) return;
        
        androidx.cardview.widget.CardView cardView = new androidx.cardview.widget.CardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                (int) (150 * getResources().getDisplayMetrics().density),
                (int) (200 * getResources().getDisplayMetrics().density));
        params.setMarginEnd((int) (8 * getResources().getDisplayMetrics().density));
        cardView.setLayoutParams(params);
        cardView.setRadius(16 * getResources().getDisplayMetrics().density);
        cardView.setCardElevation(2 * getResources().getDisplayMetrics().density);

        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageURI(uri);

        // Fotoğrafı silmek için uzun basma
        cardView.setOnLongClickListener(v -> {
            new android.app.AlertDialog.Builder(this)
                .setTitle("Fotoğrafı Sil")
                .setMessage("Bu fotoğrafı kaldırmak istiyor musunuz?")
                .setPositiveButton("Evet", (dialog, which) -> {
                    int idx = layoutPhotoContainer.indexOfChild(cardView);
                    layoutPhotoContainer.removeView(cardView);
                    if (idx >= 0 && idx < selectedImageUris.size()) {
                        selectedImageUris.remove(idx);
                        selectedImageBytes.remove(idx);
                    }
                })
                .setNegativeButton("Hayır", null)
                .show();
            return true;
        });

        cardView.addView(imageView);
        layoutPhotoContainer.addView(cardView, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_listing);

        etTitle = findViewById(R.id.etTitle);
        etPrice = findViewById(R.id.etPrice);
        etDescription = findViewById(R.id.etDescription);
        etItemAge = findViewById(R.id.etItemAge);
        spinnerCity = findViewById(R.id.spinnerCity);
        spinnerDistrict = findViewById(R.id.spinnerDistrict);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnPostListing = findViewById(R.id.btnPostListing);
        layoutPhotoContainer = findViewById(R.id.layoutPhotoContainer);
        
        androidx.cardview.widget.CardView cardAddPhoto = findViewById(R.id.cardAddPhoto);
        cardAddPhoto.setOnClickListener(v -> {
            if (selectedImageUris.size() >= 5) {
                Toast.makeText(this, "Maksimum 5 fotoğraf ekleyebilirsiniz.", Toast.LENGTH_SHORT).show();
                return;
            }
            getContent.launch("image/*");
        });

        ImageView btnBackAddListing = findViewById(R.id.btnBackAddListing);
        btnBackAddListing.setOnClickListener(v -> finish());

        setupSpinners();

        // İlan Paylaş Butonu
        btnPostListing.setOnClickListener(v -> {
            String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
            String price = etPrice.getText() != null ? etPrice.getText().toString().trim() : "";
            String desc = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
            String age = etItemAge.getText() != null ? etItemAge.getText().toString().trim() : "Belirtilmemiş";
            
            String selectedCity = spinnerCity.getText() != null ? spinnerCity.getText().toString().trim() : "";
            String selectedDistrict = spinnerDistrict.getText() != null ? spinnerDistrict.getText().toString().trim() : "";
            String location = (!selectedCity.isEmpty() && !selectedDistrict.isEmpty()) ? selectedCity + ", " + selectedDistrict : "Türkiye";
            
            String selectedCategory = spinnerCategory.getText() != null ? spinnerCategory.getText().toString().trim() : "Diğer";

            if (selectedImageUris.isEmpty()) {
                Toast.makeText(this, "Lütfen en az bir fotoğraf seçin.", Toast.LENGTH_SHORT).show();
            } else if (title.isEmpty() || price.isEmpty() || desc.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm zorunlu (başlık, fiyat, açıklama, konum) bilgileri doldurun.", Toast.LENGTH_SHORT).show();
            } else {
                btnPostListing.setEnabled(false);
                btnPostListing.setText("Fotoğraflar Yükleniyor...");

                String randomId = String.valueOf(System.currentTimeMillis());
                com.google.firebase.storage.FirebaseStorage storage = com.google.firebase.storage.FirebaseStorage.getInstance();
                java.util.List<String> uploadedUrls = new java.util.ArrayList<>();
                
                uploadNextImage(0, randomId, storage, uploadedUrls, title, price, desc, location, age, selectedCategory);
            }
        });
    }

    private void uploadNextImage(int index, String randomId, com.google.firebase.storage.FirebaseStorage storage, java.util.List<String> uploadedUrls, String title, String price, String desc, String location, String age, String category) {
        if (index >= selectedImageBytes.size()) {
            createAndSaveListing(randomId, uploadedUrls, title, price, desc, location, age, category);
            return;
        }

        byte[] imageData = selectedImageBytes.get(index);
        com.google.firebase.storage.StorageReference storageRef = storage.getReference().child("listing_images/" + randomId + "_" + index + ".jpg");

        btnPostListing.setText("Fotoğraf " + (index + 1) + "/" + selectedImageBytes.size() + " Yükleniyor...");

        // Content type metadata ekle
        com.google.firebase.storage.StorageMetadata metadata = new com.google.firebase.storage.StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build();

        storageRef.putBytes(imageData, metadata)
            .addOnSuccessListener(taskSnapshot -> {
                btnPostListing.setText("Fotoğraf " + (index + 1) + " - URL Alınıyor...");
                fetchDownloadUrlWithRetry(storageRef, 0, downloadUrl -> {
                    uploadedUrls.add(downloadUrl);
                    uploadNextImage(index + 1, randomId, storage, uploadedUrls, title, price, desc, location, age, category);
                }, () -> {
                    String manualUrl = "https://firebasestorage.googleapis.com/v0/b/"
                        + storage.getReference().getBucket()
                        + "/o/listing_images%2F" + randomId + "_" + index + ".jpg?alt=media";
                    uploadedUrls.add(manualUrl);
                    uploadNextImage(index + 1, randomId, storage, uploadedUrls, title, price, desc, location, age, category);
                });
            })
            .addOnFailureListener(e -> {
                // Storage yüklemesi başarısız — Firestore'a base64 olarak kaydet (yedek plan)
                String errorMsg = e.getMessage() != null ? e.getMessage() : "Bilinmeyen hata";
                android.util.Log.e("UPLOAD_ERROR", "Storage hatası, Firestore yedek planına geçiliyor: " + errorMsg, e);
                
                // Fotoğrafı base64'e çevirip data URI olarak kaydet
                String base64Image = android.util.Base64.encodeToString(imageData, android.util.Base64.NO_WRAP);
                String dataUri = "data:image/jpeg;base64," + base64Image;
                uploadedUrls.add(dataUri);
                
                Toast.makeText(this, "Fotoğraf " + (index + 1) + " alternatif yöntemle kaydedildi.", Toast.LENGTH_SHORT).show();
                uploadNextImage(index + 1, randomId, storage, uploadedUrls, title, price, desc, location, age, category);
            });
    }

    private void fetchDownloadUrlWithRetry(com.google.firebase.storage.StorageReference ref, int attempt,
                                            java.util.function.Consumer<String> onSuccess, Runnable onFinalFailure) {
        int delayMs = 2000 + (attempt * 1500);

        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            ref.getDownloadUrl()
                .addOnSuccessListener(uri -> onSuccess.accept(uri.toString()))
                .addOnFailureListener(e -> {
                    if (attempt < 2) {
                        fetchDownloadUrlWithRetry(ref, attempt + 1, onSuccess, onFinalFailure);
                    } else {
                        onFinalFailure.run();
                    }
                });
        }, delayMs);
    }

    private void createAndSaveListing(String randomId, java.util.List<String> imageUrls, String title, String price, String desc, String location, String age, String category) {
        String firstImageUriStr = imageUrls.isEmpty() ? "" : imageUrls.get(0);
        
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
        String currentDate = sdf.format(new java.util.Date());
        
        Listing newListing = new Listing(randomId, title, price, desc, firstImageUriStr, age, currentDate, location, category);
        newListing.setImageUrls(imageUrls);
        
        com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            newListing.setOwnerId(currentUser.getUid());
            String dName = currentUser.getDisplayName();
            if (dName == null || dName.isEmpty()) {
                String email = currentUser.getEmail();
                if (email != null && email.contains("@")) {
                    dName = email.substring(0, email.indexOf("@"));
                } else {
                    dName = "Gizli Kullanıcı";
                }
            }
            newListing.setOwnerName(dName);
        }
        newListing.setStatus("active");
        
        btnPostListing.setText("Paylaşılıyor...");
        ListingRepository.getInstance().addListing(newListing)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "İlan başarıyla paylaşıldı!", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                btnPostListing.setEnabled(true);
                btnPostListing.setText("İLANI PAYLAŞ");
                Toast.makeText(this, "İlan veritabanına eklenirken hata oluştu: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }

    private void setupSpinners() {
        cityDistrictsMap = new HashMap<>();
        cityDistrictsMap.put("İstanbul", new String[]{"Kadıköy", "Beşiktaş", "Şişli", "Üsküdar", "Maltepe", "Sarıyer", "Fatih", "Pendik"});
        cityDistrictsMap.put("Ankara", new String[]{"Çankaya", "Keçiören", "Yenimahalle", "Mamak", "Etimesgut", "Sincan", "Gölbaşı"});
        cityDistrictsMap.put("İzmir", new String[]{"Karşıyaka", "Konak", "Bornova", "Buca", "Çiğli", "Urla", "Göztepe"});
        cityDistrictsMap.put("Bursa", new String[]{"Nilüfer", "Osmangazi", "Yıldırım", "Mudanya", "Gemlik", "İnegöl"});
        cityDistrictsMap.put("Antalya", new String[]{"Muratpaşa", "Konyaaltı", "Kepez", "Alanya", "Manavgat"});
        cityDistrictsMap.put("Adana", new String[]{"Seyhan", "Yüreğir", "Çukurova", "Sarıçam"});
        cityDistrictsMap.put("Konya", new String[]{"Selçuklu", "Meram", "Karatay", "Ereğli"});
        cityDistrictsMap.put("Gaziantep", new String[]{"Şahinbey", "Şehitkamil", "Nizip"});
        cityDistrictsMap.put("Kayseri", new String[]{"Kocasinan", "Melikgazi", "Talas"});
        cityDistrictsMap.put("Mersin", new String[]{"Yenişehir", "Mezitli", "Akdeniz", "Toroslar"});
        cityDistrictsMap.put("Eskişehir", new String[]{"Odunpazarı", "Tepebaşı"});

        String[] cities = {"İstanbul", "Ankara", "İzmir", "Bursa", "Antalya", "Adana", "Konya", "Gaziantep", "Kayseri", "Mersin", "Eskişehir"};
        
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this, R.layout.spinner_dropdown_item, cities);
        spinnerCity.setAdapter(cityAdapter);
        spinnerCity.setText(cities[0], false);

        updateDistrictDropdown(cities[0]);

        spinnerCity.setOnItemClickListener((parent, view, position, id) -> {
            String city = cities[position];
            updateDistrictDropdown(city);
        });

        String[] categories = {"Elektronik", "Ev Eşyası", "Giyim", "Kitap & Hobi", "Araç İçi", "Diğer"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, R.layout.spinner_dropdown_item, categories);
        spinnerCategory.setAdapter(categoryAdapter);
        spinnerCategory.setText(categories[0], false);
    }

    private void updateDistrictDropdown(String city) {
        String[] districts = cityDistrictsMap.get(city);
        if (districts != null) {
            ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(this, R.layout.spinner_dropdown_item, districts);
            spinnerDistrict.setAdapter(districtAdapter);
            spinnerDistrict.setText(districts[0], false);
        }
    }
}
