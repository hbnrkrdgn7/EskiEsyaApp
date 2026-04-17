package com.example.eskiesyasatis;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import java.util.HashMap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class AddListingActivity extends AppCompatActivity {

    private ImageView ivListingImage;
    private TextView tvAddPhoto;
    private TextInputEditText etTitle, etPrice, etDescription, etItemAge;
    private Spinner spinnerCity, spinnerDistrict;
    private Button btnPostListing;
    
    private HashMap<String, String[]> cityDistrictsMap;
    
    private Uri selectedImageUri = null;

    // Galeri açmak için modern Android API'si
    private final ActivityResultLauncher<String> getContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivListingImage.setImageURI(uri);
                    tvAddPhoto.setVisibility(TextView.GONE); 
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_listing);

        ivListingImage = findViewById(R.id.ivListingImage);
        tvAddPhoto = findViewById(R.id.tvAddPhoto);
        etTitle = findViewById(R.id.etTitle);
        etPrice = findViewById(R.id.etPrice);
        etDescription = findViewById(R.id.etDescription);
        etItemAge = findViewById(R.id.etItemAge);
        spinnerCity = findViewById(R.id.spinnerCity);
        spinnerDistrict = findViewById(R.id.spinnerDistrict);
        btnPostListing = findViewById(R.id.btnPostListing);

        ImageView btnBackAddListing = findViewById(R.id.btnBackAddListing);
        btnBackAddListing.setOnClickListener(v -> finish());

        setupSpinners();

        // Resim/Galeri seçme işlemi
        ivListingImage.setOnClickListener(v -> {
            getContent.launch("image/*");
        });

        // İlan Paylaş Butonu
        btnPostListing.setOnClickListener(v -> {
            String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
            String price = etPrice.getText() != null ? etPrice.getText().toString().trim() : "";
            String desc = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
            String age = etItemAge.getText() != null ? etItemAge.getText().toString().trim() : "Belirtilmemiş";
            
            String selectedCity = spinnerCity.getSelectedItem() != null ? spinnerCity.getSelectedItem().toString() : "";
            String selectedDistrict = spinnerDistrict.getSelectedItem() != null ? spinnerDistrict.getSelectedItem().toString() : "";
            String location = (!selectedCity.isEmpty() && !selectedDistrict.isEmpty()) ? selectedCity + ", " + selectedDistrict : "Türkiye";

            if (selectedImageUri == null) {
                Toast.makeText(this, "Lütfen bir fotoğraf seçin.", Toast.LENGTH_SHORT).show();
            } else if (title.isEmpty() || price.isEmpty() || desc.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm zorunlu (başlık, fiyat, açıklama, konum) bilgileri doldurun.", Toast.LENGTH_SHORT).show();
            } else {
                String imageUriStr = copyUriToInternalStorage(selectedImageUri);
                if (imageUriStr == null) {
                    imageUriStr = selectedImageUri.toString();
                }
                
                String randomId = String.valueOf(System.currentTimeMillis());
                
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                String currentDate = sdf.format(new java.util.Date());
                
                Listing newListing = new Listing(randomId, title, price, desc, imageUriStr, age, currentDate, location);
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
                ListingRepository.getInstance().addListing(newListing);
                
                Toast.makeText(this, "İlan başarıyla paylaşıldı!", Toast.LENGTH_SHORT).show();
                finish(); // Anasayfaya dön
            }
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
        
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cities);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCity.setAdapter(cityAdapter);

        spinnerCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String city = cities[position];
                String[] districts = cityDistrictsMap.get(city);
                if (districts != null) {
                    ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(AddListingActivity.this, android.R.layout.simple_spinner_item, districts);
                    districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerDistrict.setAdapter(districtAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private String copyUriToInternalStorage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;
            
            File outputFile = new File(getFilesDir(), "listing_" + System.currentTimeMillis() + ".jpg");
            OutputStream outputStream = new FileOutputStream(outputFile);
            
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            
            return Uri.fromFile(outputFile).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
